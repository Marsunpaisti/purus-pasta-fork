package haven.purus.pbot;

import haven.*;
import haven.Window;
import haven.automation.GobSelectCallback;
import haven.purus.BotUtils;
import haven.purus.DrinkWater;
import haven.purus.pbot.gui.PBotWindow;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;

import static haven.OCache.posres;

public class PBotUtils {

	private static Coord selectedAreaA, selectedAreaB;
	private static GameUI gui = PBotAPI.gui;

	/**
	 * Sleep for t milliseconds
	 * @param t Time to wait in milliseconds
	 */
	public static void sleep(int t) {
		try {
			Thread.sleep(t);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Right click a gob with pathfinder, wait until pathfinder is finished
	 * @param gob Gob to right click
	 * @param mod 1 = shift, 2 = ctrl, 4 = alt
	 */
	public static void pfRightClick(PBotGob gob, int mod) {
		gui.map.purusPfRightClick(gob.gob, -1, 3, mod, null);
		try {
			gui.map.pastaPathfinder.join();
		} catch(InterruptedException e) {
			e.printStackTrace();
		}
	}


	/**
	 * Chooses a petal with given label from a flower menu that is currently open
	 * @param name Name of petal to open
	 */
	public static void choosePetal(String name) {
		FlowerMenu menu = gui.ui.root.findchild(FlowerMenu.class);
		if(menu != null) {
			for(FlowerMenu.Petal opt : menu.opts) {
				if(opt.name.equals(name)) {
					menu.choose(opt);
					menu.destroy();
				}
			}
		}
	}

	/**
	 * Click some place on map
	 * @param x x-coordinate
	 * @param y y-coordinate
	 * @param btn 1 = left click, 3 = right click
	 * @param mod 1 = shift, 2 = ctrl, 4 = alt
	 */
	public static void mapClick(int x, int y, int btn, int mod) {
		gui.map.wdgmsg("click", getCenterScreenCoord(), new Coord2d(x, y).floor(posres), btn, mod);
	}

	/**
	 * Use item in hand to ground below player, for example, to plant carrot
	 */
	public static void mapInteractClick() {
		gui.map.wdgmsg("itemact", PBotUtils.getCenterScreenCoord(), PBotGobAPI.player().getRcCoords().floor(posres), 3, gui.ui.modflags());
	}


	/**
	 * Coordinates of the center of the screen
	 * @return Coordinates of the center of the screen
	 */
	public static Coord getCenterScreenCoord() {
		Coord sc, sz;
		sz = gui.map.sz;
		sc = new Coord((int) Math.round(Math.random() * 200 + sz.x / 2 - 100),
				(int) Math.round(Math.random() * 200 + sz.y / 2 - 100));
		return sc;
	}


	/**
	 * Left click to somewhere with pathfinder, wait until pathfinder is finished
	 * @param x X-Coordinate
	 * @param y Y-Coordinate
	 */
	public static void pfLeftClick(int x, int y) {
		gui.map.purusPfLeftClick(new Coord(x, y), null);
		try {
			gui.map.pastaPathfinder.join();
		} catch(InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Starts crafting item with the given name
	 * @param name Name of the item ie. "clogs"
	 * @param makeAll 0 To craft once, 1 to craft all
	 */
	public static void craftItem(String name, int makeAll) {
		openCraftingWnd(name);
		loop:
		while(true) {
			for(Widget w : gui.ui.widgets.values()) {
				if (w instanceof Makewindow) {
					gui.wdgmsg(w, "make", makeAll);
					break loop;
				}
			}
			sleep(25);
		}
	}

	/**
	 * Waits for flower menu to appear
	 */
	public static void waitForFlowerMenu() {
		while(gui.ui.root.findchild(FlowerMenu.class) == null) {
			BotUtils.sleep(15);
		}
	}

	/**
	 * Waits for the hourglass timer when crafting or drinking for example
	 * Also waits until the hourglass has been seen to change at least once
	 */
	public static void waitForHourglass() {
		double prog = gui.prog;
		while (prog == gui.prog) {
			prog = gui.prog;
			sleep(5);
		}
		while (gui.prog >= 0) {
			sleep(50);
		}
	}

	/**
	 * Returns value of hourglass, -1 = no hourglass, else the value between 0.0 and 1.0
	 * @return value of hourglass
	 */
	public static double getHourglass() {
		return gui.prog;
	}

	// TODO: Return false if drinking was not successful (no water found for example)
	/**
	 * Attempts to drink water by using the same water drinking script as in extensions
	 * @param wait Wait for the drinking to finish
	 */
	public static boolean drink(boolean wait) {
		if(!gui.drinkingWater) {
			Thread t = new Thread(new DrinkWater(gui));
			t.start();
			if(wait) {
				try {
					t.join();
					if(!gui.lastDrinkingSucessful) {
						sysMsg("PBotUtils Warning: Couldn't drink, didn't find anything to drink!", Color.ORANGE);
						return false;
					}
				} catch(InterruptedException e) {
					e.printStackTrace();
				}
				waitForHourglass();
			}
		}
		return true;
	}

	/**
	 * Opens the crafting window for given item
	 * @param name Name of craft for wdgmsg
	 */
	public static void openCraftingWnd(String name) {
		// Close current window and wait for it to close
		Window wnd = PBotWindowAPI.getWindow("Crafting");
		if(wnd != null)
			PBotWindowAPI.closeWindow(wnd);
		PBotWindowAPI.waitForWindowClose("Crafting");
		gui.wdgmsg("act", "craft", name);
		PBotWindowAPI.waitForWindow("Crafting");
	}

	/**
	 * Send a system message to the user
	 * @param str Message to send
	 */
	public static void sysMsg(String str) {
		gui.msg(str, Color.WHITE);
	}

	/**
	 * Send a system message to the user
	 * @param str Message to send
	 * @param col Color of the text
	 */
	public static void sysMsg(String str, Color col) {
		gui.msg(str, col);
	}

	/**
	 * Send a system message to the user
	 * @param str Message to send
	 * @param r Amount of red colour in the text
	 * @param g Amount of green colour in the text
	 * @param b Amount of blue colour in the text
	 */
	public static void sysMsg(String str, int r, int g, int b) {
		gui.msg(str, new Color(r, g, b));
	}

	/**
	 * Returns the players inventory
	 * @return Inventory of the player
	 */
	public static PBotInventory playerInventory() {
		return new PBotInventory(gui.maininv);
	}

	/**
	 * Create a PBotWindow object, See PBotWindow for usage
	 * @param title Title of the window
	 * @param height Height of the window
	 * @param width Width of the window
	 * @param id scriptID variable of the script
	 * @return PBotWindow object
	 */
	public static PBotWindow PBotWindow(String title, int height, int width, String id) {
		PBotWindow window = new PBotWindow(new Coord(width, height), title, id);
		gui.add(window, 300, 300);
		return window;
	}

	/**
	 * Returns the item currently in the hand
	 * @return Item at hand
	 */
	public static PBotItem getItemAtHand() {
		if(gui.vhand == null)
			return null;
		else
			return new PBotItem(gui.vhand);
	}


	/**
	 * Drops an item from the hand and waits until it has been dropped
	 * @param mod 1 = shift, 2 = ctrl, 4 = alt
	 */
	public static void dropItemFromHand(int mod) {
		gui.map.wdgmsg("drop", Coord.z, gui.map.player().rc.floor(posres), mod);
		while(getItemAtHand() != null)
			sleep(25);
	}

	/**
	 * Activate area selection by dragging.
	 * To get coordinates of the area selected, use getSelectedAreaA and getSelectedAreaB
	 * User can select an area by dragging
	 */
	public static void selectArea() {
		sysMsg("Please select an area by dragging!", Color.ORANGE);
		gui.map.PBotAPISelect = true;
		while(gui.map.PBotAPISelect)
			sleep(25);
	}

	/**
	 * Get A point of the rectangle selected with selectArea()
	 * @return A-Point of the rectangle
	 */
	public static Coord getSelectedAreaA() {
		return selectedAreaA;
	}

	/**
	 * Get B point of the rectangle selected with selectArea()
	 * @return B-Point of the rectangle
	 */
	public static Coord getSelectedAreaB() {
		return selectedAreaB;
	}

	/**
	 * Callback for area select
	 */
	public static void areaSelect(Coord a, Coord b) {
		selectedAreaA = a.mul(MCache.tilesz2);
		selectedAreaB = b.mul(MCache.tilesz2).add(11, 11);
		sysMsg("Area selected!", Color.ORANGE);
	}

	/**
	 * Returns a list of gobs in the rectangle between A and B points
	 * @param a A-point of the rectangle
	 * @param b B-point of the rectangle
	 * @return List of gobs in the area, sorted to zig-zag pattern
	 */
	public static ArrayList<PBotGob> gobsInArea(Coord a, Coord b) {
		// Initializes list of crops to harvest between the selected coordinates
		ArrayList<PBotGob> gobs = new ArrayList<PBotGob>();
		double bigX = a.x > b.x ? a.x : b.x;
		double smallX = a.x < b.x ? a.x : b.x;
		double bigY = a.y > b.y ? a.y : b.y;
		double smallY = a.y < b.y ? a.y : b.y;
		synchronized(gui.ui.sess.glob.oc) {
			for(Gob gob : gui.ui.sess.glob.oc) {
				if(gob.rc.x <= bigX && gob.rc.x >= smallX && gob.getres() != null && gob.rc.y <= bigY
						&& gob.rc.y >= smallY) {
					gobs.add(new PBotGob(gob));
				}
			}
		}
		gobs.sort(new CoordSort());
		return gobs;
	}

	/**
	 * Resource name of the tile in the given location
	 * @param x X-Coord of the location (rc coord)
	 * @param y Y-Coord of the location (rc coord)
	 * @return
	 */
	public static String tileResnameAt(int x, int y) {
		try {
			Coord loc = new Coord(x, y);
			int t = gui.map.glob.map.gettile(loc.div(11));
			Resource res = gui.map.glob.map.tilesetr(t);
			if(res != null)
				return res.name;
			else
				return null;
		} catch(Loading l) {

		}
		return null;
	}

	// Sorts coordinate array to efficient zig-zag-like sequence for farming etc.
	private static class CoordSort implements Comparator<PBotGob> {
		public int compare(PBotGob a, PBotGob b) {
			if (a.gob.rc.floor().x == b.gob.rc.floor().x) {
				if (a.gob.rc.floor().x % 2 == 0)
					return (a.gob.rc.floor().y <b.gob.rc.floor().y) ? 1 : (a.gob.rc.floor().y >b.gob.rc.floor().y) ? -1 : 0;
				else
					return (a.gob.rc.floor().y <b.gob.rc.floor().y) ? -1 : (a.gob.rc.floor().y >b.gob.rc.floor().y) ? 1 : 0;
			} else
				return (a.gob.rc.floor().x <b.gob.rc.floor().x) ? -1 : (a.gob.rc.floor().x > b.gob.rc.floor().x) ? 1 : 0;
		}
	}
}
