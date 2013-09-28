package imagelabel;

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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

enum MouseState {
	NONE, MOVE, SIZENW, SIZESE;
}

public class Main {
	private final String[] PICTURE_EXTENSIONS = { "*.jpg", "*.gif", "*.bmp",
			"*.png" };
	private Shell shell = null;
	private Image image = null;
	private Canvas canvas = null;
	private Button loadButton = null;
	private Label label = null;
	private Display display = Display.getDefault();
	private int upX = -1, upY = -1, downX = -1, downY = -1;
	private MouseState mouseState = MouseState.NONE;

	class CanvasListener implements Listener, PaintListener {
		int lastX = -1, lastY = -1;

		@Override
		public void handleEvent(Event event) {
			switch (event.type) {
			case SWT.MouseMove:
				if ((event.stateMask & SWT.BUTTON1) != 0) {
					if (mouseState == MouseState.NONE)
						setRectangle(lastX, lastY, event.x, event.y);
					else {
						int dx = event.x - lastX, dy = event.y - lastY;
						int nupX = upX, nupY = upY, ndownX = downX, ndownY = downY;
						lastX = event.x;
						lastY = event.y;
						if (mouseState != MouseState.SIZESE) {
							nupX += dx;
							nupY += dy;
						}
						if (mouseState != MouseState.SIZENW) {
							ndownX += dx;
							ndownY += dy;
						}
						setRectangle(nupX, nupY, ndownX, ndownY);
					}

				} else if (upX > -1) {
					int x = event.x, y = event.y;
					System.out.println(x + " " + y);

					if (Math.abs(x - upX) <= 5 && Math.abs(y - upY) <= 5)
						setMouseState(MouseState.SIZENW);
					else if (Math.abs(x - downX) <= 5
							&& Math.abs(y - downY) <= 5)
						setMouseState(MouseState.SIZESE);
					else if (x >= upX + 5 && y >= upY + 5 && x <= downX - 5
							&& y <= downY - 5)
						setMouseState(MouseState.MOVE);
					else
						setMouseState(MouseState.NONE);
				}
				break;
			case SWT.MouseDown:
				lastX = event.x;
				lastY = event.y;
				break;
			}
		}

		@Override
		public void paintControl(PaintEvent e) {
			Image image = (Image) canvas.getData();
			if (image != null)
				e.gc.drawImage(image, 0, 0);
			if (upX > -1) {
				e.gc.setLineWidth(3);
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
			fileDialog.setFilterExtensions(PICTURE_EXTENSIONS);
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

	public void setRectangle(int upX, int upY, int downX, int downY) {
		this.upX = upX;
		this.upY = upY;
		this.downX = downX;
		this.downY = downY;
		label.setText(String
				.format("(%d, %d) (%d, %d)", upX, upY, downX, downY));
		canvas.redraw();
	}

	public void createContents() {
		shell = new Shell(display);
		image = new Image(display, "eclipse48.gif");
		canvas = new Canvas(shell, SWT.BORDER);
		label = new Label(shell, SWT.BORDER);
		loadButton = new Button(shell, SWT.PUSH);
	}

	public void setPosition() {
		shell.setSize(800, 600);
		canvas.setBounds(10, 10, 500, 500);
		loadButton.setBounds(10, 520, 50, 30);
		label.setBounds(70, 520, 200, 30);
	}

	public void addListeners() {
		CanvasListener canvasListener = new CanvasListener();
		canvas.addListener(SWT.MouseDown, canvasListener);
		canvas.addListener(SWT.MouseMove, canvasListener);
		// canvas.addListener(SWT.MouseUp, canvasListener);
		canvas.addPaintListener(canvasListener);
		loadButton.addSelectionListener(new LoadListener());
	}

	public Main() {
		createContents();
		addListeners();

		shell.open();
		loadButton.setText("Load");
		shell.setText("Labeling tool");

		setPosition();
		while (!shell.isDisposed()) {
				display.sleep();
		}
		display.dispose();
	}

	public static void main(String[] args) {
		new Main();
	}

}
