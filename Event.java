public class Event implements Comparable<Event>{
  public int type; //1=wall hit, 2=collide
  public double time; //time of collision.
  public Particle p1; //if it's a wall hit, c2 is not set and not used.
  public Particle p2;

  @Override
  public int compareTo(Event e){
    if(e.time>this.time){
      return 1;
    }
    else if(e.time<this.time){
      return -1;
    }
    return 0;
  }
}
