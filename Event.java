public class Event implements Comparable<Event>{
  public int type; //1=wall hit, 2=collide
  public double time = 99999; //time of collision.
  public Particle p1; //if it's a wall hit, c2 is not set and not used.
  public Particle p2;
  public double[] wallNormal;

  public Event(int type, Particle p1, Particle p2){
    this.type = type;
    this.p1 = p1;
    this.p2 = p2;
    this.time = 99999;
  }

  public void printInfo(){
    System.out.println("----event info-----");
    System.out.println("type: " + type);
    System.out.println("time: " + time);
    System.out.println("------end info-----");
  }

  public Event(int type, Particle p1){
    this.type = type;
    this.p1 = p1;
    this.time = 99999;
  }

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
