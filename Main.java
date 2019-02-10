import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.awt.Color;
import javax.swing.JFrame;

public class Main {

	private static List<Particle> space = new ArrayList<Particle>(); //all chaps on the board.
	private static Event event; //contains up to 2 involved chaps and event type.
	private static double time = 0;
	private static boolean allStop = false;
	private static int numberPoints = 3; //update this if changes are made to initialize().
	private static Event[][] collideMatrix = new double[numberPoints][numberPoints];
		//keeps track of collision times between every pair of points. By storing these values,
		//	we can lower the running time from O(n^2) to O(n).
	private static Event[] wallList = new double[numberPoints];

	public static void main(String[] args) {

		initialize(); //initializes both event storers and all positions & vels.

		while(allStop == false) {
			findNextEvent(time); //finds time and nature of the next event.
			//animate(event, team);
			handleEvent(event); //goes to that time. resets positions and velocities.
			time = event.time;
			//showAnimation();
			evolveIndependentEvents();
			allStop = checkStop();
		}

	}

	public static void initialize() {
		JFrame frame = new JFrame("Chapayev");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(new Board());
		frame.pack();
		frame.setVisible(true);
	}


	public static void findNextEvent(double time) {
		//who needs updates? Come get your updates!
		Particle[] needsUpdates;

		if(time == 0) {
			needsUpdates = Space;
		}
		else if(event.type == 1) {
			needsUpdates = {event.p1};
		}
		else{
			needsUpdates = {event.p1, event.p2}
		}

		//update the wallList & put the new guys in their places.
		for(Particle p : needsUpdates){
			p.type1.time = p.nextWall(time);
		}
		Arrays.sort(wallList);

		//now marking collisions for re-calculation, updating them.
		Event[] recompute = (Event[])ArrayUtils.addAll(event.p1.type2),
			event.p2.type2);
		for(event e : recompute){
			e.time = collide(e.p1,e.p2);
		}

		//pick smallest collide time
		Event smallestCollide = collideMatrix[0][0]
    for(int i = 0; i < particleNumber; i++) {
      for(int j = 0; j < i; j++) {
        if(collideMatrix[i][j].time < smallestCollide.time) {
					smallestCollide = e;
        }
      }
    }
		Event smallestWall = wallList[0];

		//pick sooner one of wall or interparticle!
		if(smallestCollide.time < smallestWall.time) {
			event = smallestCollide
		}
		else{
			event = smallestWall
		}
	}


	public static double collide(Chap a, Chap b, double time) {
		//return time of collision between a and b.
	   //this method requires some serious stuff.
		//add calculated time to collision to current time t.
	   return 9999+time; //if no collide, return 9999+time.
	 }



	public static void handleEvent(Event e) {
	    //resets velocities manually to [0,0] for stops,
	    //  solves for velocities in a collision.
	}


	 	public static void animate(Event e) {
	 		//uh...
	 	}

	 public static boolean checkStop() { //returns false if any chap is still moving.
			 for (Chap c : board) {
				 double[] currentVel = c.getVelocity();
				 if (currentVel[0] != 0 || currentVel[1] != 0) {
					 return false;
				 }
			 }
			 return true;
	 }

	 public static void initialize(){
		 for(i=0; i<numberPoints; i++) {
			 Space[i] = new Particle();
		 }

		 for(i=0; i<numberPoints; i++){
 			wallList[i].type = 1;
			wallList[i].p1 = Space[i]
 		 }
 		 for(i=0; i<numberPoints.length; i++){
 		  	for(j=0; j<i.length; j++){
 			  	collideMatrix[i][j].type = 2;
					collideMatrix[i][j].p1 = Space[i];
					collideMatrix[i][j].p2 = Space[j];
					//TODO: speedy structures here!
					//each particle must get its entire i row, plus the all the events with
					//	j values which are the same as its current i row #. maybe
					//  knowing the order in which we add Particles helps? i.e. add the i row
					//  immediately and then individually add events with the right j values
					//   knowing the correct index within Space.
 			  }
 		 }

	 }

}
