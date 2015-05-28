package LiveMicroscopy;

public class Particle {
	private int id;
	private double x;
	private double y;
	private double area;
	private double distance;
	
	
	public Particle(int id, double area, double x, double y) {
		this.id = id;
		this.x = x;
		this.y = y;
		this.area = area;
	}
	
	public double CompareTo(Particle otherParticle)
	{
		distance = Math.pow(this.getX()-otherParticle.getX(), 2) + Math.pow(this.getY()-otherParticle.getY(), 2);
		return Math.sqrt(distance);
	}

	public double getArea() {
		return area;
	}
	
	public int getId() {
		return id;
	}
	
	public double getX() {
		return x;
	}
	
	public double getY() {
		return y;
	}
	

	
	public void setArea(double area) {
		this.area = area;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setX(double x) {
		this.x = x;
	}

	public void setY(double y) {
		this.y = y;
	}
}
