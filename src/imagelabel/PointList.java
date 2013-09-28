package imagelabel;

import java.util.ArrayList;

class PointList {
	ArrayList<int[]> list = null;
	Main main = null;

	public PointList(Main main) {
		list = new ArrayList<int[]>();
		this.main = main;
	}

	public int[] get(int index) {
		return list.get(index);
	}

	public void set(int index, int upX, int upY, int downX, int downY) {
		list.set(index, new int[] { upX, upY, downX, downY });
		main.setListItem(index, Util.coorToString(upX, upY, downX, downY));
		main.doRedraw();
	}

	public void add(int upX, int upY, int downX, int downY) {
		list.add(new int[] { upX, upY, downX, downY });
		main.addListItem(Util.coorToString(upX, upY, downX, downY));
		main.doRedraw();
	}

	public int size() {
		return list.size();
	}

	public MouseState calcState(int x, int y) {
		for (int[] a : list) {
			int upX = a[0], upY = a[1], downX = a[2], downY = a[3];
			if (Math.abs(x - upX) <= 5 && Math.abs(y - upY) <= 5)
				return MouseState.SIZENW;
			else if (Math.abs(x - downX) <= 5 && Math.abs(y - downY) <= 5)
				return MouseState.SIZESE;
			else if (x >= upX + 5 && y >= upY + 5 && x <= downX - 5
					&& y <= downY - 5)
				return MouseState.MOVE;
		}
		return MouseState.NONE;
	}

	public int findCurrent(int x, int y) {
		for (int i = 0; i < list.size(); i++) {
			int[] a = list.get(i);
			int upX = a[0], upY = a[1], downX = a[2], downY = a[3];
			if ((Math.abs(x - upX) <= 5 && Math.abs(y - upY) <= 5)
					|| (Math.abs(x - downX) <= 5 && Math.abs(y - downY) <= 5)
					|| (x >= upX + 5 && y >= upY + 5 && x <= downX - 5 && y <= downY - 5))
				return i;
		}
		return -1;
	}

	public void update(int cur, int dx, int dy, MouseState mouseState) {
		int[] a = list.get(cur);
		int upX = a[0], upY = a[1], downX = a[2], downY = a[3];
		int nupX = upX, nupY = upY, ndownX = downX, ndownY = downY;

		if (mouseState != MouseState.SIZESE) {
			nupX += dx;
			nupY += dy;
		}
		if (mouseState != MouseState.SIZENW) {
			ndownX += dx;
			ndownY += dy;
		}
		set(cur, nupX, nupY, ndownX, ndownY);
	}
}