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

  public static int WINDOW_SIZE;
  public static int RADIUS;
  public static boolean enable3dVisuals;

  public Chamber(int radius, int dimension, boolean enable3d){
    this.WINDOW_SIZE = 2*dimension;
    this.RADIUS = radius;
    this.enable3dVisuals = enable3d;
  }

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
    g.drawLine(0,0,WINDOW_SIZE,0);
    g.drawLine(WINDOW_SIZE,0,WINDOW_SIZE,WINDOW_SIZE);
    g.drawLine(WINDOW_SIZE, WINDOW_SIZE,0,WINDOW_SIZE);
    g.drawLine(0,WINDOW_SIZE,0,0);

    double[][] layout = Main.getCurrentLayout(); //get the frame to display

    //display it
    for(double[] position : layout){
      drawChap(g,position[0],position[1],position[2]);
    }

  }

	//Prints the board's full data
	public static void printBoard(List<Particle> chamber){

		for(Particle c : chamber){
			c.printInfo();
	    }

	}

	//Draw circle with the x and y being the center coordinates of the circle
	public void drawChap(Graphics g, int x, int y, int z) {

    double zScalar = 10 * ((double)z/800);
    int intZ = (int)(Math.round(zScalar));
    if(enable3dVisuals == false){
      intZ = 1;
    }
		g.fillOval(x - RADIUS, y - RADIUS, 2 * intZ * RADIUS, 2 * intZ * RADIUS);

	}

}
