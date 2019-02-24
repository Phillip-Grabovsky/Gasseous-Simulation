import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.awt.Color;
import javax.swing.JFrame;
import java.lang.Math;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class Main {




	//CONTROL PANEL!!! ----------------------------------------------------------
	//you can also change starting arrangement of particles in the
	//	"initialize" function on the bottom.

	private static int numberPoints = 20;
	//Make sure that this corresponds with the # of points you make in the
	// initialize function at the bottom.

	private static int dimension = 400;
	//distance from origin to each wall. origin is in the very center of the box.

	private static int r = 2;
	//radius of each particle.

	private static double ro = 0;
	//mass distribution inside particles. ranges 0-1, inclusive.
		//0 = all mass at centerpoint (rotations dont happen here),
		//1 = balls are hollow shells,
		//0.2 = even distribution.

	private static double stopTime = 20;
	//how much time to run the simulation.

	private static double increment = 0.01;
	//increment is ONLY used for the animation (the simulation's time between each frame)
	//make sure it is finer than the time between collisions, or the animation will be shit.

	private static boolean enable3dVisuals = true;
	//3d mode makes closer particles seem bigger to get a sense of depth

	public static boolean simulateInOnly2d = false;
	//simulate a 2d gas as opposed to a 3d gas.

	private static boolean outputFinalSpeeds = false;
	//this outputs the final speed distribution at the end for data collection.
	//this function is a little buggy. It is set by default to only report at the last
	//	time step/animational increment, though this sometimes fails.
	//also, because the output file is filled with numbers and delimiters, the
	//	text may be displayed automatically using some other encoding format than UTF,
	// 	making your data complete garbage. The best solution I have found is to run the
	//	simulation again and hope that you're met with numbers and not chinese characters.
	//^^I have not experimented much with changing the delimiters, that may change something.

	private static String path = "/Users/phillip/Documents/Projects/Gasseous-Simulation/speeds.txt";
	//the path for the output file of the speed data from above.

	private static String speedDelimiter = ",";
	//separator between each speed value in the file above.

	private static String frameDelimiter = "||";
	//if multiple frames do end up being printed in the file above, this string will
	//separate them so you can choose between the two data sets (as opposed to accidentally
	//graphing one giant combined data set from multiple instances in time)

	//</control panel>------------------------------------------------------------






	private static double time = 0;
	private static double[] Z = {0,0,0}; //just a zero vector for convenience
	private static List<Particle> space = new ArrayList<Particle>(); //all chaps on the board.
	private static Particle[] spaceArray;
	private static Event event; //contains up to 2 involved chaps and event type.
	private static Event[][] collideMatrix = new Event[numberPoints][numberPoints];
		//keeps track of collision times between every pair of points. By storing these values,
		//	we can lower the running time from O(n^2) to O(n).
	private static Event[] wallList = new Event[numberPoints];
	private static List<double[][]> animation = new ArrayList<double[][]>(); //entire animation. composed of frames,
	//which encode positions of all pieces at each time increment.
	private static double[][] currentLayout = new double[numberPoints][3]; //a single frame of the animation.
	private static List<Event> recompute = new ArrayList<Event>(); //events to be recomputed. part of the time-saving structure.
	private static ArrayList<double[]> speeds = new ArrayList<double[]>();

	public static void main(String[] args) {
		System.out.println("initialization");
		initialize(); //initializes both event storers and all positions & vels.
		System.out.println("init done.");

		double numberIncrements = 0;
		while(time < stopTime) {
			System.out.println("time: " + time);
			findNextEvent(); //finds time and nature of the next event.
			addToAnimation(event);		//create a smooth series of frames leading up to this event, and add it to the animation.
			handleEvent(); //goes to that time. resets positions and velocities.
			time = event.time; //update time
		}

		animate(); //display an animation of our simulation!

	}

	public static void findNextEvent() {
		//who needs updates? Come get your updates!
		Particle[] needsUpdates; //this is for walls only.
		recompute = new ArrayList<Event>();

		if(time == 0) {
			needsUpdates = spaceArray; //wall updates: beginning

			//collision updates: beginning
			for(int i = 0; i<numberPoints; i++){
				for(int j=0; j<i; j++){
					recompute.add(collideMatrix[i][j]);
				}
			}
		}

		else if(event.type == 1) {
			needsUpdates = new Particle[]{event.p1}; //wall updates: if last was type 1

			//collision updates: if last was type 1
			for(int i = 0; i<numberPoints; i++){
				for(int j = 0; j<i; j++){
					if(collideMatrix[i][j].p1 == event.p1 || collideMatrix[i][j].p2 == event.p1) {
						recompute.add(collideMatrix[i][j]);
					}
				}
			}
		}
		else{
			needsUpdates = new Particle[]{event.p1, event.p2}; //wall updates: if last was type 2.

			//collisions updates: if last event was type 2
			for(Event e : event.p1.type2){
				if(e.p1 != event.p2 && e.p2 != event.p2){
					recompute.add(e);
				}
			}
			for(Event e : event.p2.type2){
				recompute.add(e);
			}
		}

		//update the wallList & put the new guys in their places.
		for(Particle p : needsUpdates){
			double[] update = nextWall(time,p);
			p.type1.wallNormal = new double[]{update[0], update[1], update[2]};
			p.type1.time = update[3];

		}
		Arrays.sort(wallList);

		//now, after updating the walls, update the collisions!
		for(Event e : recompute){
			e.time = collide(e.p1,e.p2);
		}


		//pick smallest collidetime
		Event smallestCollide = collideMatrix[1][0];
    for(int i = 0; i < numberPoints; i++) {
      for(int j = 0; j < i; j++) {
        if(collideMatrix[i][j].time < smallestCollide.time) {
					smallestCollide = collideMatrix[i][j];
        }
      }
    }
		//smallest walltime
		Event smallestWall = wallList[numberPoints-1];

		//pick sooner one of wall or interparticle!
		if(smallestCollide.time < smallestWall.time) {
			event = smallestCollide;
		}
		else{
			event = smallestWall;
		}
	}

	public static double[] nextWall(double time, Particle p) { //return the wallNormal and the time of collision.
		//if it's not moving
		double a = dimension;
		double[] c = p.getPosition();
		double[] v = p.getVelocity();
		double[][] nList = {{1,0,0},{-1,0,0},{0,1,0},{0,-1,0},{0,0,1},{0,0,-1}};
		double[][] candidates = {{1,0,0,0},{-1,0,0,0},{0,1,0,0},{0,-1,0,0},{0,0,1,0},{0,0,-1,0}};
		if(v[0] == 0 && v[1] == 0 && v[2] == 0) {
			return new double[]{1,0,0,99999+time};
		}

		double minTime = 99999+time;
		int minCandidate = 0;
		for(int i = 0; i<6 ; i++) {
			double[] n = nList[i];
			double Dvn = DP(v,n);

			if(Dvn <= 0.000000000001){ //shut up!
				candidates[i][3] = 99999+time;

			}
			else {
				//compute time
				double Dcn = DP(c,n);
				candidates[i][3] = (a-r-Dcn)/(Dvn);
			}

			//take minimum wall collisions
			if(candidates[i][3] < minTime) {
				minTime = candidates[i][3];
				minCandidate = i;
			}
		}
		candidates[minCandidate][3] += time;
		return candidates[minCandidate];
	}

	public static double collide(Particle a, Particle b) {
		double[] c1 = a.getPosition();
		double[] c2 = b.getPosition();
		double[] v1 = a.getVelocity();
		double[] v2 = b.getVelocity();
		double d0 = (c2[0] - c1[0]);
		double d1 = (c2[1] - c1[1]);
		double d2 = (c2[2] - c1[2]);
		double[] d = {d0,d1,d2};
		double jv0 = (v2[0] - v1[0]);
		double jv1 = (v2[1] - v1[1]);
		double jv2 = (v2[2] - v1[2]);
		double[] jv = {jv0,jv1,jv2};

		double Ddjv = DP(d,jv);
		double delta = Ddjv*Ddjv + (squareMag(jv)*(4*r*r - squareMag(d)));

		if(Ddjv >= 0|| delta <= 0) {
			return 99999+time;
		}

		double timeToCollide = (-1)*((Ddjv+Math.sqrt(delta))/squareMag(jv));
		return timeToCollide + time;
	}


	public static void handleEvent() {
	    //updates all positions, then updates all velocities.
			//POSITIONS
			for(Particle p : space){
				p.setPosition(p.whereAt(event.time - time));
			}

			//VELOCITIES
			if(event.type == 1) { //updates linear and angular vels for wall hits.
				double[] n = event.wallNormal;
				double[] p = event.p1.getPosition();
				double[] v = event.p1.getVelocity();
				double[] omg = event.p1.getAngularV();

				//compute some values
				double Dvn = DP(v,n);
				double[] Cnomg = CP(n,omg);
				double[] Cnv = CP(n,v);
				double Dnomg = DP(n,omg);

				//sets new linear velocity
				event.p1.setVelocity( LC(v,n,Cnomg,Z,   (1-ro)/(1+ro), (-2*Dvn)/(ro+1), (2*ro*r)/(ro+1), 0) );

				//sets new angular velocity
				event.p1.setAngularV( LC(omg,n,Cnv,Z,   (ro-1)/(ro+1), (2*Dnomg)/(ro+1), (-2)/(r*(ro+1)), 0) );

			}
			else{
				Particle p1 = event.p1;
				Particle p2 = event.p2;

				//get some numbers
				double[] c1 = event.p1.getPosition();
				double[] v1 = event.p1.getVelocity();
				double[] omg1 = event.p1.getAngularV();
				//......
				double[] c2 = event.p2.getPosition();
				double[] v2 = event.p2.getVelocity();
				double[] omg2 = event.p2.getAngularV();

				//compute some more numbers
				double e0 = (c2[0] - c1[0])/(2*r);
				double e1 = (c2[1] - c1[1])/(2*r);
				double e2 = (c2[2] - c1[2])/(2*r);
				double[] e = {e0,e1,e2};

				double jv0 = (v2[0] - v1[0]);
				double jv1 = (v2[1] - v1[1]);
				double jv2 = (v2[2] - v1[2]);
				double[] jv = {jv0,jv1,jv2};

				double lomg0 = (omg2[0] + omg1[0])/(2);
				double lomg1 = (omg2[1] + omg1[1])/(2);
				double lomg2 = (omg2[2] + omg1[2])/(2);
				double[] lomg = {lomg0,lomg1,lomg2};

				double Djve = DP(jv, e);
				double Delomg = DP(e, lomg);
				double[] Celomg = CP(e, lomg);
				double[] Cejv = CP(e, jv);

				p1.setVelocity( LC(v1,v2,e,Celomg,  1/(ro+1), ro/(ro+1), Djve/(ro+1), (2*ro*r)/(ro+1)) );
				p2.setVelocity( LC(v2,v1,e,Celomg,  1/(ro+1), ro/(ro+1), -Djve/(ro+1), -(2*ro*r)/(ro+1)) );

				p1.setAngularV( LC(omg1,lomg,e,Cejv,  1, -2/(ro+1), 2*Delomg/(ro+1), 1/(r*(ro+1)) ) );
				p2.setAngularV( LC(omg2,lomg,e,Cejv,  1, -2/(ro+1), 2*Delomg/(ro+1), 1/(r*(ro+1)) ) );
			}

	}

	public static void addToAnimation(Event e) {
		double totalTime = e.time - time;
		boolean reportSpeeds;
		if(outputFinalSpeeds == false){
			reportSpeeds = false;
		}
		else if(e.time > time - 2*increment){
			reportSpeeds = true;
		}
		else{
			reportSpeeds = false;
		}
		int numberLayouts = (int)Math.round(totalTime / increment);
		double[][] newLayout;
		double[] newSpeed = new double[numberPoints];

		for(int i = 0; i<numberLayouts; i++){
			newLayout = new double[numberPoints][3];
			for(int j=0; j<spaceArray.length; j++) {
				newLayout[j] = spaceArray[j].whereAt(i*increment);
				newSpeed[j] = Math.sqrt(squareMag(spaceArray[j].getVelocity())); //shshhh don't tell!
			}
			animation.add(newLayout);
			if(reportSpeeds == true){
				speeds.add(newSpeed);
			}
		}
 	}

		public static double[][] getCurrentLayout(){
			return currentLayout; //returns the current frame of animation to display
		}

		public static void animate(){
			currentLayout = animation.get(0);
			//setup animation stuff
			JFrame frame = new JFrame("Simulation");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.add(new Chamber(r, dimension, enable3dVisuals));
			frame.pack();
			frame.setVisible(true);

			//for every frame, repaint the screen and wait time.
			for(double[][] layout : animation){
				currentLayout = layout; //update which frame of animation to show

				frame.repaint();

				try{ Thread.sleep(10); }
				catch (Exception exc){}

			}

		if(outputFinalSpeeds == true){
			try {
				FileOutputStream fos = new FileOutputStream(path);
				DataOutputStream dos = new DataOutputStream(fos);
				for(double[] f : speeds){
					for(double speed : f){
						dos.writeUTF(Double.toString(speed));
						dos.writeUTF(speedDelimiter);
					}
					dos.writeUTF(frameDelimiter);
				}
		  	dos.close();
			}
			catch (IOException e) {
				System.out.println("IOException : " + e);
			}

		}

	}


		public static double squareMag(double[] v){
			return (v[0]*v[0]) + (v[1]*v[1]) + (v[2]*v[2]);
		}

		public static double DP(double[] v1, double[] v2) {
			return v1[0]*v2[0] + v1[1]*v2[1] + v1[2]*v2[2];
		}

		public static double[] CP(double[] v1, double[] v2) {
			double[] answer = {v1[1]*v2[2] - v2[1]*v1[2], v2[0]*v1[2] - v1[0]*v2[2], v1[0]*v2[1] - v2[0]*v1[1]};
			return answer;
		}

		public static double[] LC(double[] v1, double[] v2, double[] v3, double[] v4,
															double s1, double s2, double s3, double s4) {
			double zero = s1*v1[0] + s2*v2[0] + s3*v3[0] + s4*v4[0];
			double one = s1*v1[1] + s2*v2[1] + s3*v3[1] + s4*v4[1];
			double two = s1*v1[2] + s2*v2[2] + s3*v3[2] + s4*v4[2];
			double[] answer = {zero, one, two};
			return answer;
		}

		public static int getDimension(){
			return dimension;
		}

	 public static void initialize(){
		 spaceArray = new Particle[numberPoints];

		 for(int i = 0; i<numberPoints; i++) {
			 space.add(new Particle());
			 //no constructor: simply creates random particles.
		 }

		 for(int i=0; i<numberPoints; i++){
 		  	for(int j=0; j<i; j++){
 			  	collideMatrix[i][j] = new Event(2, space.get(i), space.get(j) );
					collideMatrix[i][j].p1.addType2(collideMatrix[i][j]);
					collideMatrix[i][j].p2.addType2(collideMatrix[i][j]);
 			  }
			}

		 for(int i=0; i<numberPoints; i++){
 			wallList[i] = new Event(1, space.get(i));
			space.get(i).type1 = wallList[i];
			space.get(i).solidify();
			spaceArray[i] = space.get(i);
			spaceArray[i].FinishStructure();
 		 }

		 //dont touch this portion
		 if(simulateInOnly2d == true){
			 for(Particle p : spaceArray) {
				 double[] v = p.getVelocity();
				 double[] P = p.getPosition();
				 double[] av = p.getAngularV();

				 double[] newV = {v[0], v[1], 0};
				 double[] newP = {P[1], P[1], 0};
				 double[] newAV = {av[0], 0, 0};

				 p.setVelocity(newV);
				 p.setPosition(newV);
				 p.setAngularV(newAV);
			 }
		 }
	 }

}
