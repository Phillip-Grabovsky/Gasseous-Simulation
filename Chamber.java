import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.util.List;

import javax.swing.JPanel;

public class Chamber extends JPanel {


  public static final int WINDOW_SIZE = 800;
  public static final int RADIUS = 1;
  //private double[][][] animation;

	//The dimensions of the panel
	public Dimension getPreferredSize() {

		return new Dimension(WINDOW_SIZE, WINDOW_SIZE);

	}

	//Method for painting everything
	//Note: Method is called twice during initialization
	public void paintComponent(Graphics g) {
    //setup stuff
		super.paintComponent(g);
		this.setBackground(Color.WHITE);

    //draw the box.
    g.drawLine(0,0,800,0);
    g.drawLine(800,0,800,800);
    g.drawLine(800,800,0,800);
    g.drawLine(0,800,0,0);

    double[][] layout = Main.getCurrentLayout(); //get the frame to display

    //display it
    for(double[] position : layout){
      int p0 = (int)Math.round(position[0]+400);
      int p1 = (int)Math.round(position[1]+400);
      drawChap(g,p0,p1);
    }

  }

	//Prints the board's full data
	public static void printBoard(List<Particle> chamber){

		for(Particle c : chamber){
			c.printInfo();
	    }

	}

	//Draw circle with the x and y being the center coordinates of the circle
	public void drawChap(Graphics g, int x, int y) {

		g.fillOval(x - RADIUS, y - RADIUS, 2 * RADIUS, 2 * RADIUS);

	}

}
