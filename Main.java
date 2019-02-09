import java.util.ArrayList;
import java.util.List;
import java.awt.Color;

import javax.swing.JFrame;

public class Main {

	public static int win = 0; //win: an int specifying the winner's color ID
	public static List<Chap> board = new ArrayList<Chap>(); //all chaps on the board.
	public static Event event; //contains up to 2 involved chaps and event type.

	public static void main(String[] args) {

		initialize();

	}

	public static void initialize() {
		JFrame frame = new JFrame("Chapayev");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(new Board());
		frame.pack();
		frame.setVisible(true);
	}

	public static int gameLoop(int team) {
		double time = 0;
		boolean allStop = false;
		boolean teamSwitch = false;
		while(allStop == false) {
			findNextEvent(time);
			//we are guaranteed that nothing happens till t=nextEvent.
			//move pieces assuming nothing collides until that time.
			teamSwitch = animate(event, team);
			time = event.time;
			handleEvent(event);
			//updateBoard();
			allStop = checkStop();
		}
		//now, all the peices have stopped moving.
		//figure out whose turn it is
		if(team == 1 && teamSwitch == true){
			team = 2;
		}
		else if(team == 2 && teamSwitch == true){
			team = 1;
		}
		return team;
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


	public static void findNextEvent(double time) {
	    double smallest = -1; //-1 is the special character for "NO EVENT"

	    //iterate over all predicted stops. find soonest one.
	    event.type = 1;
	    for(Chap c : board){
	      double candidate = c.stopTime(time);
	      if(candidate > 0 && candidate < smallest) {
	        smallest = candidate;
	        event.c1 = c;
	        event.time = candidate;
	      }
	    }

	    //iterate over all possible collision times. find soonest positive time.
	    for(Chap a: board) {
	      for(Chap b: board) {
	        if(a==b) {
	          double candidate = -1;
	        }
	        else {
	          double candidate = collide(a,b,time); //collide needs to be worked on
	          if(candidate > 0 && candidate < smallest) {
	            smallest = candidate;
	            event.type = 2;
	            event.time = candidate;
	            event.c1 = a;
	            event.c2 = b;
	          }
	        }
	      }
	    }

	    //these loops have automatically populated the Event. time to animate!
	}

	public static boolean animate(Event e, int color) {
		boolean teamSwitch  = false;
		if(e.type == 3 && e.c1.getTeam() == color){
			teamSwitch = true;
			//animate
			e.c1.whereAt(e.time);
		}
		return teamSwitch;
	}


	public static void handleEvent(Event e) {
	    //resets velocities manually to [0,0] for stops,
	    //  solves for velocities in a collision.
	}



	public static double collide(Chap a, Chap b, double time) {
		//return time of collision between a and b.
	   //this method requires some serious stuff.
		//add calculated time to collision to current time t.
	   return 0;
	 }

}
