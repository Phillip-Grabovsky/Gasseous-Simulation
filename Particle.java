import java.util.Random;
import java.util.ArrayList;
import java.util.List;

public class Particle {

  private double[] position = new double[3]; //position vector
  private double[] velocity = new double[3]; //velocity vector
  private double[] angularV = new double[3]; //angular velocity vector.
  public Event type1;
  public Event[] type2;
  private List<Event> type2liquid= new ArrayList<Event>();

  public Particle(){
    int size = Main.getDimension();
    Random random = new Random();
    double randPos1 = (random.nextDouble()*2*size)-size;
    double randPos2 = (random.nextDouble()*2*size)-size; //shitty. gosh darn. code. yuck
    double randPos3 = (random.nextDouble()*2*size)-size;

    double randVel1 = (random.nextDouble()*8*size)-4*size;
    double randVel2 = (random.nextDouble()*8*size)-4*size;
    double randVel3 = (random.nextDouble()*8*size)-4*size;


    double randAngVel1 = (random.nextDouble()*2*20)-20;
    double randAngVel2 = (random.nextDouble()*2*20)-20;
    double randAngVel3 = (random.nextDouble()*2*20)-20;

    this.position = new double[]{randPos1,randPos2, randPos3};
    this.velocity = new double[]{randVel1,randVel2,randVel3};
    this.angularV = new double[]{0,0,0};
  }

  public void FinishStructure(){
    this.type1.type = 1;
    this.type1.p1 = this;
  }

  public Particle(double[] pos, double[] vel, double[] angvel) {
    this.position = pos;
    this.velocity = vel;
    this.angularV = angvel;

  }

  public void printInfo() {
    System.out.println("---------------------------");
    System.out.println("pos: (" + position[0] + "," + position[1] + ")");
    System.out.println("vel: (" + velocity[0] + "," + velocity[1] + ")");
    System.out.println("---------------------------");
  }

  public double[] whereAt(double t){
    //returns where it will be in t time units.
    double[] p = position;
    double[] v = velocity;

    return new double[]{p[0]+v[0]*t, p[1]+v[1]*t, p[2]+v[2]*t};
  }

  public void addType2(Event e){
    //add type 2 event to mutable array list
    type2liquid.add(e);
  }

  public void solidify(){
    //convert type2liquid arryalist to type2 Array. no longer mutable so structure
    // is solid and cannot change!
    type2 = new Event[type2liquid.size()];
    for(int i = 0; i<type2.length; i++){
      type2[i] = type2liquid.get(i);
    }
  }

  public double[] getAngularV(){
    return this.angularV;
  }

  public void setAngularV(double[] angularV){
    this.angularV = angularV;
  }

  public double[] getPosition() {
    return this.position;
  }

  public void setPosition(double[] pos) {
    this.position = pos;
  }

  public double[] getVelocity() {
    return this.velocity;
  }

  public void setVelocity(double[] velocity) {
    this.velocity = velocity;
  }

}
