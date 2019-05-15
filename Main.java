import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.awt.Color;
import javax.swing.JFrame;
import java.lang.Math;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

public class Main {




	//CONTROL PANEL!!! ----------------------------------------------------------
	//you can also change starting arrangement of particles in the
	//	"initialize" function on the bottom.

	//section 1: simulation---------------------
	private static int numberPoints = 100;
	//Make sure that this corresponds with the # of points you make in the
	// initialize function at the bottom.

	private static int dimension = 200;
	//distance from origin to each wall. origin is in the very center of the box.

	private static int r = 5;
	//radius of each particle.

	private static double ro = 0;
	//mass distribution inside particles. ranges 0-1, inclusive.
		//0 = all mass at centerpoint (rotations dont happen here),
		//1 = balls are hollow shells,0
		//0.2 = even distribution.

	private static double stopTime = 5;
	//how much time to run the simulation.

	private static double wallSpeed = 0;
	//speed that the top and bottom walls "move" to accurately simulate
	// viscous pipe flow. (ro must be >0, as rotation is necessary for viscosity.)

	private static boolean[] boundaries = {false, false, false, false, false, false};
	//array which determines boundary conditions on particle collision with the wall.
	//false = normal wall hit physics.
	//true = 'pipe' behavior: particles which hit wall are teleported with the
	// same speed to the opposite wall, creating the effect of a flowing pipe.
	//order of conditions to be set: [right, left, top, bottom, close, far]
	// where close and far are for 3d sims only and refer to depth.

	private static boolean simulateInOnly2d = false;
	//simulates a 2d gas as opposed to a 3d gas. After points are initialzed,
	//	3rd components of velocity and position are set to 0, and the 2nd and
	//  3rd components of angular velocity will be set to 0. The gas will be
	//  effectively 'flattened' to a single plane.
	//if this is set to true, it will override all 3d animation settings to simply
	//  display a 2d gas.





	//section 2: animation---------------------
	private static int velocityBoxes = 10;
	//a second animation is played after the first one. this one shows a limited
	// "vector field", showing the average velocity of particles within each box
	// when the full domain is cut into boxes of user-determined size above.
	// NOTE: AS OF THE CURRENT VERSION, THIS IS ONLY AVAILABLE FOR 2D SIMULATIONS. :(

	private static double interval = 0.01;
	//the time interval between additions of position and velocity data to
	// the animations. Because the simulation is EVENT BASED, it will add the
	// frame of the first event which occurs after the next interval.
  //
	// NOTE: THIS VERSION CURRENTLY CANNOT ANIMATE BETWEEN TWO EVENTS. THUS MAKE
	// 		   SURE THAT ENOUGH COLLISIONS OCCUR PER UNIT TIME SO THAT FRAMES ARE
	//			 ADDED IN A WAY THAT STICKS TO THE INTERVALS CLOSELY.

	private static boolean makeAnimation = true;
	//toggles whether or not to make an animation.

	private static boolean drawBox = true;
	//draw the borders of the box or not.

	private static boolean enable3dVisuals = true;
	//projects the 3d cube to the 2d screen during animation so that you can naturally
	// look into the box. Also makes closer particles larger (this can be turned off)

	//private static int sizeChangingFactor = 8;
	//Make closer particles larger to aid in 3d visuals by this factor. If you don't want
	//  any size changing, set it to 0.

	private static double viewerDistanceRatio = 0.75;
	//3d visualizer projection settings: the distance at which the viewer peers
	// into the simulation cube, in terms of number of sidelengths of the simulation cube.

	private static int waitTime = 10;
	//the amount of milliseconds to wait after each frame.





	//section 3: output of data---------------------
	private static boolean outputSpeedData = false;
	//this outputs the final speed distribution at the end for data collection.
	//this function is a little buggy. It is set by default to only report at the last
	//	time step/animational increment, though this sometimes fails.
	//also, because the output file is filled with numbers and delimiters, the
	//	text may be displayed automatically using some other encoding format than UTF
	// 	making your data complete garbage. The best solution I have found is to run the
	//	simulation again and hope that you're met with numbers and not chinese characters.
	//^^I have not experimented much with changing the delimiters, that may change something.

	private static String path = "Macintosh HD 2/Phillip/Desktop/speeds.txt";
	//the path for the output file of the speed data from above.
	// this file contains the speeds of all particles regardless of direction, at
	// every time the function addSpeeds() is called. By default this is done
	// at the very beginning and very end of the simulation.

	private static String XVelPath = "Macintosh HD 2/Phillip/Desktop/XVels.txt";
	//the path for the output file of the speed data from above.
	// this file contains the speeds of all particles IN THE X DIRECTION, at
	// every time the function addSpeeds() is called. By default this is done
	// at the very beginning and very end of the simulation.

	private static String speedDelimiter = ",";
	//separator between each speed value in the file above.

	private static String frameDelimiter = "||";
	//if multiple frames do end up being printed in the file above, this string will
	//separate them so you can choose between the two data sets (as opposed to accidentally
	//graphing one giant combined data set from multiple instances in time)

	//</control panel>------------------------------------------------------------





	//other variables. NOT for users to change unless you know what you're doing!!!!
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
	private static double[][] currentLayout = new double[numberPoints][5]; //a single frame of the animation.
	private static List<Event> recompute = new ArrayList<Event>(); //events to be recomputed. part of the time-saving structure.
	private static ArrayList<double[]> speeds = new ArrayList<double[]>();
	private static double[] right = {1,0,0}; //wall identifiers in 3d
	private static double[] left = {-1,0,0}; // ^
	private static double[] top = {0,1,0}; // ^
	private static double[] bottom = {0,-1,0}; //^
	private static double[] close = {0,0,1}; // ^
	private static double[] far = {0,0,-1}; // ^
	private static List<double[][]> velocityAnimation = new ArrayList<double[][]>();
	private static double[][] velocityLayout;
	private static double laminarStops;
	private static boolean flowGoing = true;
	private static double averageXVel;
	private static List<Double> XVelsFile = new ArrayList<Double>();



	//MAIN METHOD. DOES EVERYTHING. THE REST OF THE FILE IS HELPER FUNCTIONS!
	public static void main(String[] args) {

		//CONFIG STUFF + INITIALIZE POINTS, SPACE, AND EVENTS.
		if(simulateInOnly2d == true){
			enable3dVisuals = false;
		}
		else{
			velocityBoxes = 0;
		}
		System.out.println("initialization");
		initialize(); //initializes both event storers and all positions & vels.
		int numberEvents = 0;
		addSpeeds();
		double nextTime = 0;
		System.out.println("init done.");

		//MAIN SIMULATION LOOP. ADDS FRAMES TO ANIMATION, CALCULATES EVENTS.
		while(time < stopTime) {
			findNextEvent(); //finds time and nature of the next event.
			if(time > nextTime){
				nextTime+=interval;
				System.out.println(time);
				addOneEvent();
				addVelocityMap();
			}
			handleEvent(); //goes to that time. resets positions and velocities.
			time = event.time; //update time
			numberEvents++;
		}

		//SIMULATION DONE, ANIMATION MADE. DISPLAY/OUTPUT OUR DATA
		animate(); //display an animation of our simulation! And output all speed data!
		animateVelocities(); //display the animation of the velocity vector field!
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
		int smallWall = numberPoints - 1;
		Arrays.sort(wallList);
		while(wallList[smallWall].time < 0){
			smallWall--;
		}

		//now, after updating the walls, update the collisions!
		for(Event e : recompute){
			e.time = collide(e.p1,e.p2);
		}


		//pick smallest collidetime
		Event smallestCollide = collideMatrix[1][0];
    for(int i = 0; i < numberPoints; i++) {
      for(int j = 0; j < i; j++) {
        if(collideMatrix[i][j].time < smallestCollide.time && collideMatrix[i][j].time - time > 0) {
					smallestCollide = collideMatrix[i][j];
        }
      }
    }
		//smallest walltime
		Event smallestWall = wallList[smallWall];

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
				//Deal with boundary conditions here.
				//TODO: this looks ridiculous. can it be optimized?
				if(n[0] == right[0] && boundaries[0] == true){
					double[] p = event.p1.getPosition();
					event.p1.setPosition(new double[]{-1*dimension,p[1],p[2]});
				}

				else if(n[0] == left[0] && boundaries[1] == true){
					double[] p = event.p1.getPosition();
					event.p1.setPosition(new double[]{dimension,p[1],p[2]});
				}
				else if(n[1] == top[1] && boundaries[2] == true){
					double[] p = event.p1.getPosition();
					event.p1.setPosition(new double[]{p[0],-1*dimension,p[2]});
				}
				else if(n[1] == bottom[1] && boundaries[3] == true){
					double[] p = event.p1.getPosition();
					event.p1.setPosition(new double[]{p[0],dimension,p[2]});
				}
				else if(n[2] == close[2] && boundaries[4] == true){
					double[] p = event.p1.getPosition();
					event.p1.setPosition(new double[]{p[0],p[1],dimension});
				}
				else if(n[2] == bottom[2] && boundaries[5] == true){
					double[] p = event.p1.getPosition();
					event.p1.setPosition(new double[]{p[0],p[1],-1*dimension});
				}

				else{

					double[] p = event.p1.getPosition();
					double[] vel = event.p1.getVelocity();
					double[] v = {vel[0] - wallSpeed, vel[1], 0};
					double[] omg = event.p1.getAngularV();

					//compute some values
					double Dvn = DP(v,n);
					double[] Cnomg = CP(n,omg);
					double[] Cnv = CP(n,v);
					double Dnomg = DP(n,omg);

					//sets new linear velocity
					double[] vNoShift = LC(v,n,Cnomg,Z,   (1-ro)/(1+ro), (-2*Dvn)/(ro+1), (2*ro*r)/(ro+1), 0);
					double[] vShift = {vNoShift[0] + wallSpeed, vNoShift[1], 0};
					event.p1.setVelocity( vShift );
					//sets new angular velocity
					event.p1.setAngularV( LC(omg,n,Cnv,Z,   (ro-1)/(ro+1), (2*Dnomg)/(ro+1), (-2)/(r*(ro+1)), 0) );

				}

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

	public static void addOneEvent(){
		double[][] newLayout;
		double[] newSpeed = new double[numberPoints];
		newLayout = new double[numberPoints][5];
		for(int j=0; j<spaceArray.length; j++) {
			///3D Visuals!! Woohoo!
			double newX;
			double newY;
			if(enable3dVisuals == true){
				double[] XYZ =  spaceArray[j].getPosition();
				double X = XYZ[0];
				double Y = XYZ[1];
				double noShiftZ = XYZ[2];
				double Z = dimension - noShiftZ;

				//we first project the X.
				double D = -1 * X;
				double V = dimension * 2 * viewerDistanceRatio;
				double shiftX = (D * Z)/(Z + V);
				newX = X + shiftX;

				D +=r;
				double edgeshiftX = (D * Z)/(Z+V);

				//now we do same for Y.
				D = -1 * Y;
				double shiftY = (D * Z)/(Z + V);
				newY = Y + shiftY;
				D += r;
				double edgeshiftY = (D*Z)/(Z+V);

				double newxrad = shiftX + r - edgeshiftX;
				double newyrad = shiftY + r - edgeshiftY;


				newLayout[j] = new double[]{newX, newY, noShiftZ, newxrad, newyrad};

			}
			else{
				double [] pos = spaceArray[j].getPosition();
				newLayout[j] = new double[]{pos[0],pos[1],pos[2],r,r};
			}
		}
		animation.add(newLayout);
	}



	public static void addVelocityMap(){
		if(velocityBoxes == 0){
			return;
		}
		int size = (dimension*2)/velocityBoxes;
		double[][] newVelocityMap = new double[velocityBoxes * velocityBoxes][3];
		//go through each point, updating average velocity in whatever box it's in!
		double totalXVel = 0;
		for(Particle p : spaceArray){
			double[] pos = p.getPosition();
			int[] Box = {-1,-1};

			//find which box it's in!
			for(int i = 0; i<2; i++){
				boolean boxFound = false;
				while(boxFound == false){
					Box[i]++;
					/*System.out.println("Box num: " + Box[i]);
					System.out.println("Range (" + ((Box[i]*size) - dimension) + ", " + (((Box[i]+1)*size)-dimension) +")");
					System.out.println("Pos: " + pos[i]);*/
					if(pos[i] >= (Box[i]*size) - dimension && pos[i] <= ((Box[i]+1)*size)-dimension ){
						//System.out.println("BOXFOUND! " + i);
						boxFound = true;
					}
					if(pos[i] < -1 * dimension){
						boxFound = true;
					}
				}
			}

			int index = velocityBoxes*Box[1] + Box[0];
			while(index >= newVelocityMap.length){
				index -= velocityBoxes;
			}
			double[] vel = p.getVelocity();
			double pointsAveraged = newVelocityMap[index][0];
			newVelocityMap[index][1] = (newVelocityMap[index][1] * pointsAveraged + vel[0])/(pointsAveraged+1);
			newVelocityMap[index][2] = (newVelocityMap[index][2] * pointsAveraged + vel[1])/(pointsAveraged+1);

			newVelocityMap[index][0]++;
			totalXVel += vel[0];
		}
		averageXVel = totalXVel / numberPoints;
		XVelsFile.add(averageXVel);
		velocityAnimation.add(newVelocityMap);

	}

	public static void addSpeeds() {
		double[] newSpeed = new double[numberPoints];
		for(int j=0; j<spaceArray.length; j++) {
			newSpeed[j] = Math.sqrt(squareMag(spaceArray[j].getVelocity())); //shshhh don't tell!
		}
		speeds.add(newSpeed);
	}


		public static double[][] getCurrentLayout(){
			return currentLayout; //returns the current frame of animation to display
		}
		public static double[][] getVelocityLayout(){
			return velocityLayout;
		}



		public static void animate(){
			if(outputSpeedData == true){
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

				try {
					FileOutputStream fos = new FileOutputStream(XVelPath);
					DataOutputStream dos = new DataOutputStream(fos);
					for(Double f : XVelsFile){
						dos.writeUTF(Double.toString(f));
						dos.writeUTF(speedDelimiter);
					}
					dos.close();
				}
				catch (IOException e) {
					System.out.println("IOException : " + e);
				}

			}

			if(makeAnimation == false){
				return;
			}
			currentLayout = animation.get(0);
			//setup animation stuff
			JFrame frame = new JFrame("Simulation");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.add(new Chamber(r, dimension, enable3dVisuals, viewerDistanceRatio, drawBox, false));
			frame.pack();
			frame.setVisible(true);

			//for every frame, repaint the screen and wait time.
			for(double[][] layout : animation){
				currentLayout = layout; //update which frame of animation to show

				frame.repaint();

				try{ Thread.sleep(waitTime); }
				catch (Exception exc){}

			}
	}

	public static void animateVelocities(){
		if(velocityBoxes == 0){
			return;
		}
		velocityLayout = velocityAnimation.get(0);
		//setup animation stuff
		JFrame newFrame = new JFrame("Velocities");
		newFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		newFrame.add(new Chamber(r, dimension, enable3dVisuals, viewerDistanceRatio, drawBox, false));
		newFrame.pack();
		newFrame.setVisible(true);

		//for every frame, repaint the screen and wait time.
		for(double[][] layout : velocityAnimation){
			velocityLayout = layout; //update which frame of animation to show

			newFrame.repaint();

			try{ Thread.sleep(waitTime); }
			catch (Exception exc){}

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
		 Random random = new Random();

		 for(int i = 0; i<numberPoints; i++) {
			 space.add(new Particle());
			 //no args in constructor ==> randomly determine all pos and v.
			 //to use Particle constructor:
			 // space.add(new Particle(PositionVector, velocityVector, angularVelocityVector))
			 // usually, angularVelocity is set to {0,0,0} whether you want rotation or not.
			 // with a ro=0, no rotation. Particles will rotate when ro > 0. (see top)
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

				 double[] newV = {v[0], v[1], 0};
				 double[] newP = {P[0], P[1], 0};

				 p.setVelocity(newV);
				 p.setPosition(newP);
				 p.setAngularV(Z);
			 }
		 }
	 }

}
