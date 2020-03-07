import haven.*;

public class Pointer extends Widget {
	public static final States.ColState col = new States.ColState(241, 227, 157, 255);

	public Indir<Resource> icon;

	private Tex licon;

	public Coord2d tc;

	public Coord lc;

	public long gobid = -1L;

	public Pointer(Indir<Resource> paramIndir) {
		super(Coord.z);
		this.icon = paramIndir;
	}

	public static Widget mkwidget(UI paramUI, Object... paramVarArgs) {
		int i = ((Integer)paramVarArgs[0]).intValue();
		Indir<Resource> indir = (i < 0) ? null : paramUI.sess.getres(i);
		return new Pointer(indir);
	}

	public void presize() {
		resize(this.parent.sz);
	}

	protected void added() {
		presize();
		super.added();
	}

	private int signum(int paramInt) {
		if (paramInt < 0)
			return -1;
		if (paramInt > 0)
			return 1;
		return 0;
	}

	private void drawarrow(GOut paramGOut, Coord paramCoord) {
		Coord coord1 = this.sz.div(2);
		paramCoord = paramCoord.sub(coord1);
		if (paramCoord.equals(Coord.z))
			paramCoord = new Coord(1, 1);
		double d = Coord.z.dist(paramCoord);
		Coord coord2 = paramCoord.mul((d - 25.0D) / d);
		float f = coord1.y / coord1.x;
		if (Math.abs(coord2.x) > coord1.x || Math.abs(coord2.y) > coord1.y)
			if (Math.abs(coord2.x) * f < Math.abs(coord2.y)) {
				coord2 = (new Coord(coord2.x * coord1.y / coord2.y, coord1.y)).mul(signum(coord2.y));
			} else {
				coord2 = (new Coord(coord1.x, coord2.y * coord1.x / coord2.x)).mul(signum(coord2.x));
			}
		Coord coord3 = coord2.sub(paramCoord).norm(30.0D);
		coord2 = coord2.add(coord1);
		BGL bGL = paramGOut.gl;
		paramGOut.state2d();
		paramGOut.state((GLState)col);
		paramGOut.apply();
		bGL.glEnable(2881);
		bGL.glBegin(4);
		paramGOut.vertex(coord2);
		paramGOut.vertex(coord2.add(coord3).add(-coord3.y / 3, coord3.x / 3));
		paramGOut.vertex(coord2.add(coord3).add(coord3.y / 3, -coord3.x / 3));
		bGL.glEnd();
		bGL.glDisable(2881);
		if (this.icon != null)
			try {
				if (this.licon == null)
					this.licon = ((Resource.Image)((Resource)this.icon.get()).layer(Resource.imgc)).tex();
				paramGOut.aimage(this.licon, coord2.add(coord3), 0.5D, 0.5D);
			} catch (Loading loading) {}
		this.lc = coord2.add(coord3);
	}

	public void draw(GOut paramGOut) {
		Coord3f coord3f;
		this.lc = null;
		if (this.tc == null)
			return;
		Gob gob = (this.gobid < 0L) ? null : this.ui.sess.glob.oc.getgob(this.gobid);
		if (gob != null) {
			try {
				coord3f = ((GameUI)getparent(GameUI.class)).map.screenxf(gob.getc());
			} catch (Loading loading) {
				return;
			}
		} else {
			coord3f = ((GameUI)getparent(GameUI.class)).map.screenxf(this.tc);
		}
		if (coord3f != null)
			drawarrow(paramGOut, new Coord(coord3f));
	}

	public void udpate(Coord2d paramCoord2d, long paramLong) {
		this.tc = paramCoord2d;
		this.gobid = paramLong;
	}

	public void uimsg(String paramString, Object... paramVarArgs) {
		if (paramString == "upd") {
			if (paramVarArgs[0] == null) {
				this.tc = null;
			} else {
				this.tc = ((Coord)paramVarArgs[0]).mul(OCache.posres);
			}
			if (paramVarArgs[1] == null) {
				this.gobid = -1L;
			} else {
				this.gobid = ((Integer)paramVarArgs[1]).intValue() & 0xFFFFFFFFL;
			}
		} else if (paramString == "icon") {
			int i = ((Integer)paramVarArgs[0]).intValue();
			Indir<Resource> indir = (i < 0) ? null : this.ui.sess.getres(i);
			this.icon = indir;
			this.licon = null;
		} else {
			super.uimsg(paramString, paramVarArgs);
		}
	}

	public Object tooltip(Coord paramCoord, Widget paramWidget) {
		if (this.lc != null && this.lc.dist(paramCoord) < 20.0D) {
			if(this.tooltip instanceof Text) {
				int dist = (int)this.tc.dist(this.ui.gui.map.player().rc)/11;
				return Text.render(((Text)this.tooltip).text + ", Distance: " + (dist < 990 ? dist : "> 990") + " tiles");
			} else {
				return this.tooltip;
			}
		}
		return null;
	}
}
