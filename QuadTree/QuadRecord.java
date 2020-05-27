package cs157bhw2;

public class QuadRecord
{
	 public String label;
	    public QuadPoint point;
	 
	    public QuadRecord(String label, QuadPoint point)
	    {
	        this.label = label;
	        this.point = point;
	    }
	    public QuadRecord(String label, float x, float y)
	    {
	        this.label = label;
	        this.point = new QuadPoint(x, y);
	    }
	    public String toString()
	    {
	        return label + point.toString();
	    }
}
