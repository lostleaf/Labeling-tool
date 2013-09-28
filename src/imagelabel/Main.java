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
	private Button loadButton = null, exportButton = null;
	// private Label label = null;
	private Display display = Display.getDefault();
	private List list = null;
	private PointList pointList = null;
	private MouseState mouseState = MouseState.NONE;

	class CanvasListener implements Listener, PaintListener {
		int lastX = -1, lastY = -1, cur = -1;

		@Override
		public void handleEvent(Event event) {
			switch (event.type) {
			case SWT.MouseMove:
				if ((event.stateMask & SWT.BUTTON1) != 0) {
					if (mouseState == MouseState.NONE) {
						if (cur > -1)
							pointList.set(cur, lastX, lastY, event.x, event.y);
					} else {
						int dx = event.x - lastX, dy = event.y - lastY;
						lastX = event.x;
						lastY = event.y;
						if (cur > -1)
							pointList.update(cur, dx, dy, mouseState);
					}

				} else
					setMouseState(pointList.calcState(event.x, event.y));
				break;
			case SWT.MouseUp:
				cur = -1;
				break;
			case SWT.MouseDown:
				lastX = event.x;
				lastY = event.y;
				if (mouseState == MouseState.NONE) {
					cur = pointList.size();
					pointList.add(lastX, lastY, lastX, lastY);
				} else
					cur = pointList.findCurrent(lastX, lastY);
				break;
			}
		}

		@Override
		public void paintControl(PaintEvent e) {
			Image image = (Image) canvas.getData();
			if (image != null)
				e.gc.drawImage(image, 0, 0);
			e.gc.setLineWidth(3);
			for (int i = 0; i < pointList.size(); i++) {
				int[] a = pointList.get(i);
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
			FileDialog fileDialog = new FileDialog(shell, SWT.NONE);
			fileDialog.setFilterExtensions(Util.PICTURE_EXTENSIONS);
			String filePath = fileDialog.open();
			if (filePath != null) {
				// canvas.redraw();
				System.out.println(filePath);
				if (image != null)
					image.dispose();
				ImageData data = new ImageData(filePath);
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
				for (int i = 0; i < pointList.size(); i++) {
					int[] a = pointList.get(i);
					int upX = a[0], upY = a[1], downX = a[2], downY = a[3];
					p.println(Util.coorToString(upX, upY, downX, downY));
				}
				p.close();
			}
		}

	}

	public void doRedraw() {
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
		pointList = new PointList(this);
		// image = new Image(display, "eclipse48.gif");
		canvas = new Canvas(shell, SWT.BORDER);
		// label = new Label(shell, SWT.BORDER);
		loadButton = new Button(shell, SWT.PUSH);
		exportButton = new Button(shell, SWT.PUSH);
		list = new List(shell, SWT.SINGLE | SWT.V_SCROLL | SWT.BORDER);
	}

	public void setPosition() {
		shell.setSize(800, 600);
		canvas.setBounds(10, 10, 500, 500);
		loadButton.setBounds(10, 520, 50, 30);
		exportButton.setBounds(70, 520, 50, 30);
		// label.setBounds(70, 520, 200, 30);
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

	}

	public void setListItem(int index, String str) {
		list.setItem(index, str);
	}

	public void setListItems(String[] s) {
		list.setItems(s);
	}

	public void addListItem(String s) {
		list.add(s);
	}

	public Main() {
		createContents();
		addListeners();

		shell.open();
		loadButton.setText("Load");
		shell.setText("Labelling tool");
		exportButton.setText("Export");

		setPosition();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}

	public static void main(String[] args) {
		new Main();
	}

}
