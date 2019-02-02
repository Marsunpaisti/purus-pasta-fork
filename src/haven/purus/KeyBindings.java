package haven.purus;

import haven.Utils;

import java.awt.event.KeyEvent;
import java.util.ArrayList;

public class KeyBindings {

	public static KeyBinding toggleInventory = new KeyBinding("toggleInventory", KeyEvent.VK_TAB, false, false, false, "Inventory");
	public static KeyBinding toggleEquipment = new KeyBinding("toggleEquipment", KeyEvent.VK_E, true, false, false, "Equipment");
	public static KeyBinding toggleMenuSearch = new KeyBinding("toggleMenuSearch", KeyEvent.VK_S, false, true, false, "Menu search");
	public static KeyBinding toggleKin = new KeyBinding("toggleKin", KeyEvent.VK_B, true, false, false, "Kith & Kin");
	public static KeyBinding toggleCharactersheet = new KeyBinding("toggleCharactersheet", KeyEvent.VK_C, true, false, false, "Character sheet");
	public static KeyBinding toggleOptions = new KeyBinding("toggleOptions", KeyEvent.VK_O, true, false, false, "Options");
	public static KeyBinding crawlSpeed = new KeyBinding("crawlSpeed", KeyEvent.VK_Q, false, true, false, "Set crawl speed");
	public static KeyBinding walkSpeed = new KeyBinding("walkSpeed", KeyEvent.VK_W, false, true, false, "Set walk speed");
	public static KeyBinding runSpeed = new KeyBinding("runSpeed", KeyEvent.VK_E, false, true, false, "Set run speed");
	public static KeyBinding sprintSpeed = new KeyBinding("sprintSpeed", KeyEvent.VK_R, false, true, false, "Set sprint speed");
	public static KeyBinding cycleAggro = new KeyBinding("cycleAggro", KeyEvent.VK_TAB, true, false, false, "Cycle opponent");

	public static KeyBindingWnd initWnd() {
		ArrayList<KeyBinding> kbList = new ArrayList<>();
		kbList.add(toggleInventory);
		kbList.add(toggleEquipment);
		kbList.add(toggleMenuSearch);
		kbList.add(toggleKin);
		kbList.add(toggleCharactersheet);
		kbList.add(toggleOptions);
		kbList.add(crawlSpeed);
		kbList.add(walkSpeed);
		kbList.add(runSpeed);
		kbList.add(sprintSpeed);
		kbList.add(cycleAggro);

		return new KeyBindingWnd(kbList);
	}

	public static class KeyBinding {

		private String name, desc;
		private int defaultKeyCode, curKeyCode;
		private boolean defaultCtrlMod, defaultShiftMod, defaultAltMod, curCtrlMod, curShiftMod, curAltMod;

		public KeyBinding(String name, int defaultKeyCode, boolean defaultCtrlMod, boolean defaultShiftMod, boolean defaultAltMod, String desc) {
			this.name = name;
			this.defaultKeyCode = defaultKeyCode;
			this.defaultCtrlMod = defaultCtrlMod;
			this.defaultShiftMod = defaultShiftMod;
			this.defaultAltMod = defaultAltMod;
			this.desc = desc;

			curKeyCode = Utils.getprefi(name+":keyCode", defaultKeyCode);
			curCtrlMod = Utils.getprefb(name+":ctrlMod", defaultCtrlMod);
			curShiftMod = Utils.getprefb(name+":shiftMod", defaultShiftMod);
			curAltMod = Utils.getprefb(name+":altMod", defaultAltMod);
		}

		public void resetDefaults() {
			curKeyCode = defaultKeyCode;
			curCtrlMod = defaultCtrlMod;
			curShiftMod = defaultShiftMod;
			curAltMod = defaultAltMod;
			Utils.setprefi(name+":keyCode", defaultKeyCode);
			Utils.setprefb(name+":ctrlMod", defaultCtrlMod);
			Utils.setprefb(name+":shiftMod", defaultShiftMod);
			Utils.setprefb(name+":altMod", defaultAltMod);
		}

		public boolean isThis(KeyEvent ev) {
			if(ev.getKeyCode() == curKeyCode && ev.isControlDown() == curCtrlMod && ev.isShiftDown() == curShiftMod && ev.isAltDown() == curAltMod)
				return true;
			else
				return false;
		}

		public String getDesc() {
			return this.desc;
		}

		public int getCurKeyCode() {
			return curKeyCode;
		}

		public void setCurKeyCode(int curKeyCode) {
			this.curKeyCode = curKeyCode;
			Utils.setprefi(name+":keyCode", curKeyCode);
		}

		public boolean isCurCtrlMod() {
			return curCtrlMod;
		}

		public void setCurCtrlMod(boolean curCtrlMod) {
			this.curCtrlMod = curCtrlMod;
			Utils.setprefb(name+":ctrlMod", curCtrlMod);
		}

		public boolean isCurShiftMod() {
			return curShiftMod;
		}

		public void setCurShiftMod(boolean curShiftMod) {
			this.curShiftMod = curShiftMod;
			Utils.setprefb(name+":shiftMod", curShiftMod);
		}

		public boolean isCurAltMod() {
			return curAltMod;
		}

		public void setCurAltMod(boolean curAltMod) {
			this.curAltMod = curAltMod;
			Utils.setprefb(name+":altMod", curAltMod);
		}
	}
}
