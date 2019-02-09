import java.awt.Color;
import java.awt.event.MouseEvent;

public class Constants {

	public static final int WINDOW_SIZE = 800;

	public static final int ONE_START_Y = 50;
	public static final int TWO_START_Y = 700;
	public static final Color ONE_COLOR = Color.BLUE;
	public static final Color TWO_COLOR = Color.RED;

	public static final int RADIUS = 1;
	public static final double FRICTION_COEFFICIENT = 0.75;

	public static double distance(MouseEvent e, Chap chap) {

		double[] position = chap.getPosition();

		return (Math.sqrt((Math.abs(e.getX() - position[0])) + (Math.abs(e.getY() - position[1]))));

	}

}
