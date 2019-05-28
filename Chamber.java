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

  private int WINDOW_SIZE;
  private int RADIUS;
  //private double sizeChanging;
  private boolean enable3dVisuals;
  private boolean drawBox;
  private boolean mode;

  //precalculated lines for 3d visulizer:
  private int[] line1 = new int[4];
  //THIS CODE IS ABSOLUTE TRASH! REALLY GOTTA FIX THIS!!!!!!!!! ARGH IT SUCKS SO MUCH

  private int[] line2 = new int[4];
  private int[] line3 = new int[4];
  private int[] line4 = new int[4];

  public Chamber(int radius, int dimension, boolean e3d, double viewerRatio, boolean drawBox, boolean vels){
    this.WINDOW_SIZE = 2*dimension;
    this.RADIUS = radius;
    //this.sizeChanging = sizeChanging;
    this.enable3dVisuals = e3d;
    this.drawBox = drawBox;
    this.mode = vels;
    if(mode == false){
      enable3dVisuals = false;
    }

    if(enable3dVisuals == true){
      double D = WINDOW_SIZE/2;
      double Z = WINDOW_SIZE;
      double V = viewerRatio * WINDOW_SIZE;
      double Shift = (D * Z)/(Z + V);
      int shift = (int)(Math.round(Shift));

      //cube borders. I know this is stupid code but I'm tired!
      line1[0] = 0;
      line1[1] = 0;
      //THIS CODE IS ABSOLUTE TRASH! REALLY GOTTA FIX THIS!!!!!!!!! ARGH IT SUCKS SO MUCH
      line1[2] = shift;
      line1[3] = shift;
      //THIS CODE IS ABSOLUTE TRASH! REALLY GOTTA FIX THIS!!!!!!!!! ARGH IT SUCKS SO MUCH


      line2[0] = WINDOW_SIZE;
      line2[1] = 0;
      line2[2] = WINDOW_SIZE - shift;
      line2[3] = shift;      //THIS CODE IS ABSOLUTE TRASH! REALLY GOTTA FIX THIS!!!!!!!!! ARGH IT SUCKS SO MUCH



      line3[0] = WINDOW_SIZE;
      line3[1] = WINDOW_SIZE;
      //THIS CODE IS ABSOLUTE TRASH! REALLY GOTTA FIX THIS!!!!!!!!! ARGH IT SUCKS SO MUCH

      line3[2] = WINDOW_SIZE - shift;
      line3[3] = WINDOW_SIZE - shift;

      line4[0] = 0;     //THIS CODE IS ABSOLUTE TRASH! REALLY GOTTA FIX THIS!!!!!!!!! ARGH IT SUCKS SO MUCH

      line4[1] =  WINDOW_SIZE;
      line4[2] = shift;
      //THIS CODE IS ABSOLUTE TRASH! REALLY GOTTA FIX THIS!!!!!!!!! ARGH IT SUCKS SO MUCH

      line4[3] =WINDOW_SIZE -shift;
    }
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
    if(drawBox == true){
      g.drawLine(0,0,WINDOW_SIZE,0);
      g.drawLine(WINDOW_SIZE,0,WINDOW_SIZE,WINDOW_SIZE);
      g.drawLine(WINDOW_SIZE, WINDOW_SIZE,0,WINDOW_SIZE);
      g.drawLine(0,WINDOW_SIZE,0,0);
    }

    if(enable3dVisuals == true && drawBox == true){
      //THIS CODE IS ABSOLUTE TRASH! REALLY GOTTA FIX THIS!!!!!!!!! ARGH IT SUCKS SO MUCH
      g.drawLine(line1[0],line1[1],line1[2],line1[3]);
      g.drawLine(line2[0],line2[1],line2[2],line2[3]);
      //THIS CODE IS ABSOLUTE TRASH! REALLY GOTTA FIX THIS!!!!!!!!! ARGH IT SUCKS SO MUCH

      g.drawLine(line3[0],line3[1],line3[2],line3[3]);
      g.drawLine(line4[0],line4[1],line4[2],line4[3]);

      //outside face
      g.drawLine(line1[2], line1[3], line2[2],line2[3]);
      g.drawLine(line2[2], line2[3], line3[2],line3[3]);
      //THIS CODE IS ABSOLUTE TRASH! REALLY GOTTA FIX THIS!!!!!!!!! ARGH IT SUCKS SO MUCH

      g.drawLine(line3[2], line3[3], line4[2],line4[3]);
      g.drawLine(line4[2], line4[3], line1[2],line1[3]);
      //THIS CODE IS ABSOLUTE TRASH! REALLY GOTTA FIX THIS!!!!!!!!! ARGH IT SUCKS SO MUCH

    }

    //display it
    if(mode == true){
      double[][] layout = Main.getCurrentLayout(); //get the frame to display
      for(double[] p : layout){
        //3d projection has already been done in the Main.addToAnimation() function.
        drawChap(g,p[0],p[1],p[2],p[3],p[4]);
      }
    }
    else{
      double[][] layout = Main.getVelocityLayout(); //get the frame to display
      drawData(g, layout);
    }
  }

	//Draw circle with the x and y being the center coordinates of the circle
	public void drawChap(Graphics g, double p0, double p1, double p2, double xr, double yr) {
    int x = (int)Math.round(p0+(int)WINDOW_SIZE/2);
    int y = (int)Math.round(p1+(int)WINDOW_SIZE/2); //TODO shitty code, do it in a loop. why am i stupid like this?
    int z = (int)Math.round(p2+(int)WINDOW_SIZE/2);
    int xrad = (int)Math.round(xr);
    int yrad = (int)Math.round(yr);

		g.fillOval(x - xrad, y - xrad, 2*xrad, 2 * xrad);
	}

  public void drawData(Graphics g, double[][] layout){
    //draw all lines
    double boxSize = (double)WINDOW_SIZE/Math.sqrt(layout.length);
    for(int i = 1; i < layout.length; i++){
      g.drawLine(0, (int)Math.round(boxSize*(double)i), WINDOW_SIZE, (int)Math.round(boxSize*(double)i));
      g.drawLine((int)Math.round(boxSize*(double)i), 0, (int)Math.round(boxSize*(double)i), WINDOW_SIZE);
    }

    //draw all vectors
    for(int i = 0; i< layout.length; i++){
      //System.out.println("i: " + i);
      int[] boxCoords = {(int)Math.round(i%Math.sqrt(layout.length)),(int)(i/Math.sqrt(layout.length))};
      //System.out.println("BoxCoords: (" + boxCoords[0] + ", " + boxCoords[1] + ")");
      int[] origin = {(int)Math.round(boxCoords[0]*boxSize + 0.5*boxSize),
       WINDOW_SIZE - (int)Math.round(boxCoords[1]*boxSize + 0.5*boxSize)};
      //System.out.println("Origin: (" + origin[0] + ", " + origin[1] + ")");
      double[] velocity = layout[i];
      double[] dataPoint = {20*velocity[1]/Math.sqrt(Main.squareMag(velocity)),20*velocity[2]/Math.sqrt(Main.squareMag(velocity))};
      int[] intPoint = {(int)Math.round(dataPoint[0]), (int)Math.round(dataPoint[1])};

      g.drawLine(origin[0] - 1, origin[1] - 1, origin[0] + intPoint[0], origin[1] + intPoint[1]);
      //g.drawLine(origin[0] - 1, origin[1] - 1, intPoint[0],  intPoint[1]);
      g.fillOval( origin[0] + intPoint[0] -2, origin[1] + intPoint[1]-2, 4,4);

    }
  }

}
