package imagelabel;

public class Util {
	public static String coorToString(int upX, int upY, int downX, int downY) {
		return String.format("(%d, %d) (%d, %d)", upX, upY, downX, downY);
	}

	public static final String[] PICTURE_EXTENSIONS = { "*.jpg", "*.gif", "*.bmp",
			"*.png" };
}
