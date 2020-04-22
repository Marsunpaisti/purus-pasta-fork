package haven.purus;

import haven.*;

import java.util.List;

public class testExtension implements Runnable {

	private GameUI gui;

	public testExtension(GameUI gui) {
		this.gui = gui;
	}

	@Override
	public void run() {
		System.out.println("Running testextension");
		for (Widget w = gui.lchild; w != null; w = w.prev) {
			if (w instanceof Window) {
				Window wnd = (Window) w;
			}
		}
	}
}
