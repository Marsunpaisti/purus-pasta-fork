package haven.paisti;


import haven.*;
import haven.pathfinder.*;
import java.util.ArrayList;
import java.util.Iterator;

import static haven.OCache.posres;

//I'm taking no credit for this pf, just small modifications to amber's api
public class PaistiPathfinder implements Runnable{
    private OCache oc;
    private MCache map;
    private MapView mv;
    private Coord dest;
    public boolean terminate = false;
    private int meshid;
    private int clickb;
    private Gob gob;
    private String action;
    public Coord clickCoord;
    private int modflags;
    private int interruptedRetries = 5;
    private static final int RESPONSE_TIMEOUT = 800;
    private haven.pathfinder.Map m = null;
    private Iterable<Edge> path = null;

    public PaistiPathfinder(MapView mv, Coord dest, String action) {
        this.dest = dest;
        this.action = action;
        this.oc = mv.glob.oc;
        this.map = mv.glob.map;
        this.mv = mv;
    }

    public PaistiPathfinder(MapView mv, Coord dest, Gob gob, int meshid, int clickb, int modflags, String action) {
        this.dest = dest;
        this.meshid = meshid;
        this.clickb = clickb;
        this.gob = gob;
        this.modflags = modflags;
        this.action = action;
        this.oc = mv.glob.oc;
        this.map = mv.glob.map;
        this.mv = mv;
    }

    @Override
    public void run() {
        if (this.path == null) getPath(mv.player().rc.floor(), 1);
        if (this.path != null) {
            do {
                walkPath(mv.player().rc.floor());
            } while (!shouldTerminate());
        } else {
            this.m.dbgdump();
            System.out.println("PaistiPathFinder Unable to find a path!");
        }
    }

    public Iterable<Edge> getPath(Coord src, int retries) {
        if (retries < 0) return null;
        long starttotal = System.nanoTime();
        m = new haven.pathfinder.Map(src, dest, map, mv.gameui());
        Gob player = mv.player();

        long start = System.nanoTime();

        boolean ridingHorse = false;
        boolean carryingObject = false;
        boolean onBoat = false;
        if (mv.player() != null) {
            ArrayList<String> poses = new ArrayList<>();
            Drawable d = mv.player().getattr(Drawable.class);

            if(d instanceof Composite) {
                Composite comp = (Composite)d;
                for(ResData rd:comp.prevposes) {
                    try {
                        poses.add(rd.res.get().name);
                    } catch(Loading l) {

                    }
                }
            }

            for ( String pose : poses
            ) {
                if (pose.toLowerCase().contains("riding")) {
                    ridingHorse = true;
                }
                if (pose.toLowerCase().contains("banzai")) {
                    carryingObject = true;
                }
                if (pose.toLowerCase().contains("row")) {
                    onBoat = true;
                }
            }
        }

        synchronized (oc) {
            for (Gob gob : oc) {
                if (gob.isplayer())
                    continue;
                // need to exclude destination gob so it won't get into TO candidates list
                if (this.gob != null && this.gob.id == gob.id)
                    continue;

                if (ridingHorse) {
                    if (gob.getres() != null && gob.getres().name.contains("kritter/horse") && gob.rc.dist(mv.player().rc) < 11) {
                        continue;
                    }
                }

                if (onBoat) {
                    if (gob.getres() != null && gob.getres().name.contains("vehicle/rowboat") && gob.rc.dist(mv.player().rc) < 11) {
                        continue;
                    }
                }

                if (carryingObject) {
                    if (gob.rc.dist(mv.player().rc) < 11) {
                        continue;
                    }
                }


                GobHitbox.BBox box = GobHitbox.getBBox(gob);
                int shrink = 0;
                if (box != null && isInsideBoundBox(gob.rc.floor(), gob.a, box, player.rc.floor())) {
                    do {
                        //"Shrink" box until player is no longer considered to be inside it
                        shrink++;
                        box = GobHitbox.getBBox(gob);
                        box.a = box.a.add(shrink, shrink);
                        box.b = box.b.sub(shrink, shrink);
                        if (shrink >= 10) break; //Max shrink
                    } while (isInsideBoundBox(gob.rc.floor(), gob.a, box, player.rc.floor()));
                    m.addGobAndShrink(gob, new Coord(shrink, shrink));
                    continue;
                }
                m.addGob(gob);
            }
        }

        // if player is located at a position occupied by a gob (can happen when starting too close to gobs)
        // move it slightly away from it
        if (m.isOriginBlocked()) {
            Pair<Integer, Integer> freeloc = m.getFreeLocation();

            if (freeloc == null) return null;

            clickCoord = new Coord2d(src.x + freeloc.a - Map.origin, src.y + freeloc.b - Map.origin).floor(posres);
            mv.wdgmsg("click", Coord.z, clickCoord, 1, 0);

            // FIXME
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // need to recalculate map
            System.out.println("Path not found, retrying");
            return getPath(mv.player().rc.floor(), retries-1);
        }

        // exclude any bounding boxes overlapping the destination gob
        if (this.gob != null) m.excludeGob(this.gob);
        Iterable<Edge> path = m.main();
        Iterator<Edge> it = path.iterator();
        int len = 0;
        while (it.hasNext()) {
            len++;
            it.next();
        }
        if (len == 0) return null;
        this.path = path;
        m.dbgdump();
        return path;
    }

    private boolean shouldTerminate() {
        synchronized (this){
            return this.terminate;
        }
    }

    private void walkPath(Coord src) {
        Iterator<Edge> it = path.iterator();
        Coord2d currentTarget = null;
        while (it.hasNext() && !shouldTerminate()) {
            Edge e = it.next();
            currentTarget = new Coord2d(src.x + e.dest.x - Map.origin, src.y + e.dest.y - Map.origin);
            clickCoord = currentTarget.floor(posres);

            if (action != null && !it.hasNext())
                mv.gameui().act(action);

            if (gob != null && !it.hasNext()) {
                mv.wdgmsg("click", gob.sc, clickCoord, clickb, modflags, 0, (int) gob.id, gob.rc.floor(posres), 0, meshid);
                MapView.pllastcc = new Coord2d(src.x + e.dest.x - Map.origin, src.y + e.dest.y - Map.origin);
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e1) {
                    return;
                }
            } else {
                mv.wdgmsg("click", Coord.z, clickCoord, 1, 0);
                MapView.pllastcc = new Coord2d(src.x + e.dest.x - Map.origin, src.y + e.dest.y - Map.origin);
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e1) {
                    return;
                }
            }

            // wait for gob to start moving
            long moveWaitStart = System.currentTimeMillis();
            while (!mv.player().isMoving() && !shouldTerminate()) {
                try {
                    Thread.sleep(25);
                } catch (InterruptedException e1) {
                    return;
                }
                if (System.currentTimeMillis() - moveWaitStart > RESPONSE_TIMEOUT)
                    break;
            }

            // wait for it to finish
            long iterTime = System.currentTimeMillis();
            long notMovingDuration = 0;
            while (!shouldTerminate()) {
                long currentTime = System.currentTimeMillis();

                //Counter to check how long we are stopped
                if (!mv.player().isMoving()) {
                    notMovingDuration += (currentTime - iterTime);
                } else {
                    notMovingDuration = 0;
                }
                iterTime = currentTime;

                if (notMovingDuration >= 75) {
                    break;
                }

                if (currentTarget != null && currentTarget.dist(mv.player().rc) <= 0.7) {
                    break;
                }

                try {
                    Thread.sleep(10);
                } catch (InterruptedException e1) {
                    return;
                }

                /*
                // FIXME
                // when right clicking gobs, char will try to navigate towards gob's rc
                // however he will be blocked by gob's bounding box.
                // therefore we just wait for a bit
                LinMove lm = mv.player().getLinMove();
                if (gob != null && !it.hasNext() && lm != null && now - lm.lastupd > 500)
                    break;

                 */
            }
        }

        synchronized (this){
            terminate = true;
        }
    }

    static public boolean isInsideBoundBox(Coord gobRc, double gobA, GobHitbox.BBox gobBBox, Coord point) {
        final Coordf relative = new Coordf(point.sub(gobRc)).rotate(-gobA);
        return relative.x >= gobBBox.a.x && relative.x <= gobBBox.b.x &&
                relative.y >= gobBBox.a.y && relative.y <= gobBBox.b.y;
    }
}
