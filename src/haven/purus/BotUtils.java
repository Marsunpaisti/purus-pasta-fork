package haven.purus;

import static haven.OCache.posres;

import java.awt.Color;
import java.util.regex.Pattern;

import haven.Coord;
import haven.Coord2d;
import haven.Equipory;
import haven.FlowerMenu;
import haven.FlowerMenu.Petal;
import haven.GItem;
import haven.GameUI;
import haven.Gob;
import haven.HavenPanel;
import haven.Inventory;
import haven.ItemInfo;
import haven.Loading;
import haven.Resource;
import haven.WItem;
import haven.Widget;

public class BotUtils {

	public static GameUI gui;
	private static Pattern liquidPattern = Pattern.compile(String.format("[0-9.]+ l of (%s)",
			String.join("|", new String[] { "Water", "Piping Hot Tea", "Tea" }), Pattern.CASE_INSENSITIVE));

	// Click some object with button and a modifier
	// Button 1 = Left click and 3 = right click
	// Modifier 1 - shift; 2 - ctrl; 4 - alt;
	public static void doClick(Gob gob, int button, int mod) {
		gui.map.wdgmsg("click", Coord.z, gob.rc.floor(posres), button, 0, mod, (int) gob.id, gob.rc.floor(posres), 0,
				-1);
	}

	// Find object by ID, returns null if not found
	public static Gob findObjectById(long id) {
		return gui.ui.sess.glob.oc.getgob(id);
	}

	// true if player moving
	public static boolean isMoving() {
		if (player().getv() == 0)
			return false;
		else
			return true;
	}

	// Right clicks a gob with pathfinder (Pathfinds near, then right clicks)
	public static void pfRightClick(Gob gob, int mod) {
		gui.map.pfRightClick(gob, -1, 3, mod, null);
	}

	// Chooses option from flower menu
	public static void Choose(Petal option) {
		gui.wdgmsg("cl", option.num, gui.ui.modflags());
	}

	// Click some object with an item on hand
	// Modifier 1 - shift; 2 - ctrl; 4 alt;
	public static void itemClick(Gob gob, int mod) {
		gui.map.wdgmsg("itemact", Coord.z, gob.rc.floor(posres), mod, 0, (int) gob.id, gob.rc.floor(posres), 0, -1);
	}

	// Returns players gob
	public static Gob player() {
		return gui.map.player();
	}

	// Sends message to the user
	public static void sysMsg(String str, Color col) {
		gui.msg(str, col);
	}

	// Drinks liquids from containers in inventory
	public static void drink() {
		GameUI gui = HavenPanel.lui.root.findchild(GameUI.class);
		WItem item = findDrink(playerInventory());

		if (item != null) {
			item.item.wdgmsg("iact", Coord.z, 3);
			sleep(250);
			@SuppressWarnings("deprecation")
			FlowerMenu menu = gui.ui.root.findchild(FlowerMenu.class);
			if (menu != null) {
				for (FlowerMenu.Petal opt : menu.opts) {
					if (opt.name.equals("Drink")) {
						menu.choose(opt);
						menu.destroy();
						sleep(500);
						while (gui.prog >= 0) {
							sleep(100);
						}
					}
				}
			}
		}
	}

	// Finds an item from inventory that contains liquids that can be consumed
	public static WItem findDrink(Inventory inv) {
		for (WItem item : inv.children(WItem.class)) {
			if (canDrinkFrom(item))
				return item;
		}
		Equipory e = gui.getequipory();
		WItem l = e.quickslots[6];
		WItem r = e.quickslots[7];
		if (canDrinkFrom(l))
			return l;
		if (canDrinkFrom(r))
			return r;
		return null;
	}

	// Returns true if player can drink from the item
	public static boolean canDrinkFrom(WItem item) {
		ItemInfo.Contents contents = getContents(item);
		if (contents != null && contents.sub != null) {
			for (ItemInfo info : contents.sub) {
				if (info instanceof ItemInfo.Name) {
					ItemInfo.Name name = (ItemInfo.Name) info;
					if (name.str != null && liquidPattern.matcher(name.str.text).matches())
						return true;
				}
			}
		}
		return false;
	}

	// Returns contents of an item
	public static ItemInfo.Contents getContents(WItem item) {
		try {
			for (ItemInfo info : item.item.info())
				if (info instanceof ItemInfo.Contents)
					return (ItemInfo.Contents) info;
		} catch (Loading ignored) {
		}
		return null;
	}

	// Returns players inventory
	public static Inventory playerInventory() {
		return gui.maininv;
	}

	// Takes item in hand
	public static void takeItem(Widget item) {
		item.wdgmsg("take", Coord.z);
		while (getItemAtHand() == null) {
			sleep(10);
		}
	}

	// Returns item in hand
	public static GItem getItemAtHand() {
		for (GameUI.DraggedItem item : gui.hand)
			return item.item;
		return null;
	}

	// Waits for t milliseconds
	public static void sleep(int t) {
		try {
			Thread.sleep(t);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	// Finds the nearest crop with a name and stage
	public static Gob findNearestStageCrop(int radius, int stage, String... names) {
		Coord2d plc = player().rc;
		double min = radius;
		Gob nearest = null;
		synchronized (gui.ui.sess.glob.oc) {
			for (Gob gob : gui.ui.sess.glob.oc) {
				double dist = gob.rc.dist(plc);
				if (dist < min) {
					boolean matches = false;
					for (String name : names) {
						if (isObjectName(gob, name)) {
							if (gob.getStage() == stage) {
								matches = true;
								break;
							}
						}
					}
					if (matches) {
						min = dist;
						nearest = gob;
					}
				}
			}
		}
		return nearest;
	}

	// Checks if the object's name can be found from resources
	public static boolean isObjectName(Gob gob, String name) {
		try {
			Resource res = gob.getres();
			return (res != null) && res.name.contains(name);
		} catch (Loading e) {
			return false;
		}
	}

	// Use item in hand to ground below player, for example, plant carrot
	public static void mapInteractClick(int mod) {
		gui.map.wdgmsg("itemact", getCenterScreenCoord(), player().rc.floor(posres), 3, gui.ui.modflags());
	}

	// Destroys the given gob
	public static void destroyGob(Gob gob) {
		gui.menu.wdgmsg("act", new Object[] { "destroy" });
		doClick(gob, 1, 0);
	}

	// Drops an item from the hand
	public static void dropItem(int mod) {
		gui.map.wdgmsg("drop", Coord.z, gui.map.player().rc.floor(posres), mod);
	}

	// Returns the coordinates of the center of the screen
	public static Coord getCenterScreenCoord() {
		Coord sc, sz;
		sz = gui.map.sz;
		sc = new Coord((int) Math.round(Math.random() * 200 + sz.x / 2 - 100),
				(int) Math.round(Math.random() * 200 + sz.y / 2 - 100));
		return sc;
	}
}
