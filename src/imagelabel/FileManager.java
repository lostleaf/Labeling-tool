package imagelabel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class FileManager {
	final private String[] EXTENSION = { "jpg", "gif", "png", "bmp", "jpeg" };
	final private Set<String> EXTENSION_SET = new HashSet<String>(
			Arrays.asList(EXTENSION));
	private File[] files = null;
	int ord;
	ArrayList<int[]> list = null;

	public int[] get(int index) {
		return list.get(index);
	}

	public void set(int index, int upX, int upY, int downX, int downY) {
		list.set(index, new int[] { upX, upY, downX, downY });
	}

	public void add(int upX, int upY, int downX, int downY) {
		list.add(new int[] { upX, upY, downX, downY });
	}

	public int listSize() {
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

	public FileManager(File path) {
		if (path.isDirectory()) {
			files = path.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return EXTENSION_SET.contains(name.split("\\.")[1]);
				}
			});

		} else
			files = new File[] { path };
		ord = 0;
		list = new ArrayList<int[]>();
		load();
	}

	public FileManager() {
		files = new File[0];
		list = new ArrayList<int[]>();
	}

	public boolean hasNext() {
		return ord < files.length - 1;
	}

	public boolean hasPrevious() {
		return ord > 0;
	}

	public File getImage() {
		return files[ord];
	}

	public int getOrd() {
		return ord;
	}

	public ArrayList<int[]> getList() {
		load();
		return list;
	}

	public int fileSize() {
		return files.length;
	}

	private File getLabellingFile() {
		String name = files[ord].getAbsolutePath() + ".labelling";
		File file = new File(name);
		if (!file.exists())
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		return file;
	}

	public void next() {
		switchTo(ord + 1);
	}

	public void previous() {
		switchTo(ord - 1);
	}

	public void save() {
		PrintWriter p = null;
		try {
			p = new PrintWriter(getLabellingFile());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		for (int[] a : list)
			p.printf("%d %d %d %d\n", a[0], a[1], a[2], a[3]);
		p.close();
	}

	private void load() {
		Scanner cin = null;
		try {
			cin = new Scanner(getLabellingFile());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		list.clear();
		while (cin.hasNextInt()) {
			int upX = cin.nextInt(), upY = cin.nextInt();
			int downX = cin.nextInt(), downY = cin.nextInt();
			list.add(new int[] { upX, upY, downX, downY });
		}
		cin.close();
	}

	public void switchTo(int num) {
		// System.out.println(num + " " + listSize());
		if (num >= 0 && num < fileSize()) {
			// System.out.println("switch to " + num);
			ord = num;
			load();
		}
	}

	// public static void main(String args[]) {
	// FileManager fm = new FileManager(new File("/home/jack/test"));
	// System.out.println(fm.getLabelling());
	// }
}
