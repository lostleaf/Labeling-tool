package imagelabel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

public class Main {

	private Shell shell = null;
	private Image image = null;
	private Canvas canvas = null;
	private Button loadButton = null, exportButton = null, nextButton = null,
			prevButton = null;
	private Display display = Display.getDefault();
	private List list = null;
	// private PointList pointList = null;
	private MouseState mouseState = MouseState.NONE;
	private FileManager fm = null;

	class CanvasListener implements Listener, PaintListener {
		int lastX = -1, lastY = -1, cur = -1;

		@Override
		public void handleEvent(Event event) {
			switch (event.type) {
			case SWT.MouseMove:
				if ((event.stateMask & SWT.BUTTON1) != 0) {
					if (mouseState == MouseState.NONE) {
						if (cur > -1)
							setListItem(cur, lastX, lastY, event.x, event.y);
					} else {
						int dx = event.x - lastX, dy = event.y - lastY;
						lastX = event.x;
						lastY = event.y;

						if (cur > -1)
							updateListItem(cur, dx, dy);
					}

				} else
					setMouseState(fm.calcState(event.x, event.y));
				break;
			case SWT.MouseUp:
				cur = -1;
				break;
			case SWT.MouseDown:
				lastX = event.x;
				lastY = event.y;
				if (mouseState == MouseState.NONE) {
					cur = fm.listSize();
					addListItem(lastX, lastY, lastX, lastY);
				} else
					cur = fm.findCurrent(lastX, lastY);
				break;
			}
		}

		@Override
		public void paintControl(PaintEvent e) {
			Image image = (Image) canvas.getData();
			if (image != null)
				e.gc.drawImage(image, 0, 0);
			e.gc.setLineWidth(3);
			for (int i = 0; i < fm.listSize(); i++) {
				int[] a = fm.get(i);
				int upX = a[0], upY = a[1], downX = a[2], downY = a[3];
				e.gc.drawRectangle(upX, upY, downX - upX, downY - upY);
			}
		}

	}

	class LoadListener implements SelectionListener {

		@Override
		public void widgetDefaultSelected(SelectionEvent arg0) {

		}

		@Override
		public void widgetSelected(SelectionEvent arg0) {
			DirectoryDialog fileDialog = new DirectoryDialog(shell, SWT.NONE);
			String filePath = fileDialog.open();
			if (filePath != null) {
				// canvas.redraw();
				System.out.println(filePath);
				fm = new FileManager(new File(filePath));
				if (fm.hasNext())
					nextButton.setEnabled(true);
				refresh();
			}
		}
	}

	class ExportListener implements SelectionListener {

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {

		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			FileDialog fileDialog = new FileDialog(shell, SWT.NONE);
			String filePath = fileDialog.open();
			if (filePath != null) {
				System.out.println(filePath);
				PrintWriter p = null;
				try {
					p = new PrintWriter(new File(filePath));
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
				}
				for (int i = 0; i < fm.listSize(); i++) {
					int[] a = fm.get(i);
					int upX = a[0], upY = a[1], downX = a[2], downY = a[3];
					p.println(Util.coorToString(upX, upY, downX, downY));
				}
				p.close();
			}
		}

	}

	class NextListener implements SelectionListener {

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {

		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			fm.save();
			if (fm.hasNext()) {
				fm.next();
				prevButton.setEnabled(true);
				nextButton.setEnabled(fm.hasNext());
				refresh();
			}
		}

	}

	class PreviousListener implements SelectionListener {

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {

		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			fm.save();
			if (fm.hasPrevious()) {
				fm.previous();
				nextButton.setEnabled(true);
				prevButton.setEnabled(fm.hasNext());
				refresh();
			}
		}

	}

	private void refresh() {
		setImage(fm.getImage());
		list.removeAll();
		for (int[] a : fm.getList()) {
			int upX = a[0], upY = a[1], downX = a[2], downY = a[3];
			list.add(Util.coorToString(upX, upY, downX, downY));
		}
		doRedraw();
	}

	public void doRedraw() {
		canvas.redraw();
	}

	public void setImage(File imageFile) {
		if (image != null)
			image.dispose();
		ImageData data = new ImageData(imageFile.getAbsolutePath());
		if (data.width > 500 || data.height > 500) {
			double rate = Math.min(((double) data.width) / 500,
					((double) data.height) / 500);
			data = data.scaledTo((int) (data.width / rate),
					(int) (data.height / rate));
		}
		image = new Image(display, data);
		canvas.setData(image);
		canvas.redraw();
	}

	public void setMouseState(MouseState state) {
		if (mouseState == state)
			return;
		mouseState = state;
		int cursorStyle = 0;
		if (state == MouseState.NONE)
			cursorStyle = SWT.CURSOR_ARROW;
		if (state == MouseState.MOVE)
			cursorStyle = SWT.CURSOR_SIZEALL;
		if (state == MouseState.SIZENW)
			cursorStyle = SWT.CURSOR_SIZENW;
		if (state == MouseState.SIZESE)
			cursorStyle = SWT.CURSOR_SIZESE;
		shell.setCursor(new Cursor(display, cursorStyle));
	}

	public void createContents() {
		shell = new Shell(display);
		canvas = new Canvas(shell, SWT.BORDER);
		loadButton = new Button(shell, SWT.PUSH);
		exportButton = new Button(shell, SWT.PUSH);
		prevButton = new Button(shell, SWT.PUSH);
		prevButton.setEnabled(false);
		nextButton = new Button(shell, SWT.PUSH);
		nextButton.setEnabled(false);
		list = new List(shell, SWT.SINGLE | SWT.V_SCROLL | SWT.BORDER);
	}

	public void setPosition() {
		shell.setSize(800, 600);
		canvas.setBounds(10, 10, 500, 500);
		loadButton.setBounds(10, 520, 50, 30);
		//exportButton.setBounds(70, 520, 50, 30);
		prevButton.setBounds(130, 520, 60, 30);
		nextButton.setBounds(200, 520, 60, 30);
		list.setBounds(530, 10, 200, 500);
	}

	public void addListeners() {
		CanvasListener canvasListener = new CanvasListener();
		canvas.addListener(SWT.MouseDown, canvasListener);
		canvas.addListener(SWT.MouseMove, canvasListener);
		canvas.addListener(SWT.MouseUp, canvasListener);
		canvas.addPaintListener(canvasListener);
		loadButton.addSelectionListener(new LoadListener());
		exportButton.addSelectionListener(new ExportListener());
		nextButton.addSelectionListener(new NextListener());
		prevButton.addSelectionListener(new PreviousListener());
	}

	public void setListItem(int index, int upX, int upY, int downX, int downY) {
		fm.set(index, upX, upY, downX, downY);
		list.setItem(index, Util.coorToString(upX, upY, downX, downY));
		doRedraw();
	}

	public void updateListItem(int cur, int dx, int dy) {
		int[] a = fm.get(cur);
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
		setListItem(cur, nupX, nupY, ndownX, ndownY);
	}

	public void addListItem(int upX, int upY, int downX, int downY) {
		fm.add(upX, upY, downX, downY);
		list.add(Util.coorToString(upX, upY, downX, downY));
		doRedraw();
	}

	private void setText() {
		loadButton.setText("Load");
		shell.setText("Labelling tool");
		exportButton.setText("Export");
		prevButton.setText("Previous");
		nextButton.setText("Next");
	}

	public Main() {
		fm = new FileManager();
		createContents();
		addListeners();

		shell.open();
		setText();

		setPosition();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
		fm.save();
	}

	public static void main(String[] args) {
		new Main();
	}

}
