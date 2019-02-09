public class Event {
  public int type; //1=stop, 2=collide, 3=leaves board
  public double time; //time of collision.
  public Chap c1; //if it's a stop, c2 is not set and not used.
  public Chap c2;

}
