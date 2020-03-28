package haven.purus;

import haven.*;

import java.util.List;

public class herbPicker implements Runnable {

	private GameUI gui;

	public herbPicker(GameUI gui) {
		this.gui = gui;
	}

	@Override
	public void run() {
		Gob targetHerb = getNearestHerb();
		if (targetHerb == null) return;

		// Right click the crop
		BotUtils.doClick(targetHerb, 3, 0);

		// Wait for harvest menu to appear
		while (gui.ui.root.findchild(FlowerMenu.class) == null) {
			BotUtils.sleep(10);
		}

		// Select pick option
		FlowerMenu menu = gui.ui.root.findchild(FlowerMenu.class);
		if (menu != null) {
			for (FlowerMenu.Petal opt : menu.opts) {
				if (opt.name.equals("Harvest")) {
					menu.choose(opt);
					menu.destroy();
				}
			}
		}

		boolean gotItem = false;
		int ticks = 0;
		while (!gotItem) {
			if (BotUtils.findObjectById(targetHerb.id) == null) gotItem = true;
			BotUtils.sleep(20);
			ticks++;
			if ( ticks > 750) break;
		}

		if (gotItem && targetHerb.getres() != null && targetHerb.getres().name.contains("cattail")) {
			dropCattailGarbage();
		}

	}

	public void dropCattailGarbage() {
		//Wait for hand empty incase there is itsy web or something
		while (BotUtils.getItemAtHand() != null) {
			BotUtils.sleep(20);
		}
		List<WItem> inventoryContents = BotUtils.getInventoryContents(BotUtils.playerInventory());
		for (WItem wi : inventoryContents) {
			String resname = wi.item.resource().name;
			if (resname.contains("cattailhead") || resname.contains("cattailroots")) {
				wi.item.wdgmsg("drop", Coord.z);
			}
		}
	}

	public Gob getNearestHerb() {
		Coord2d plc = BotUtils.player().rc;
		double nearestDistance = 999999999;
		Gob nearest = null;
		synchronized (gui.ui.sess.glob.oc) {
			for (Gob gob : gui.ui.sess.glob.oc) {
				if (gob.getres() != null && gob.getres().name.contains("/herbs/")){
					double dist = gob.rc.dist(plc);
					if (dist < 222 && (nearest == null || dist < nearestDistance)) {
						nearest = gob;
						nearestDistance = dist;
					}
				}
			}
		}
		return nearest;
	}

}
