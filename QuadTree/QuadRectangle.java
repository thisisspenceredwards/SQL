package cs157bhw2;
	public class QuadRectangle
	{
	    public int id;
	    public QuadPoint top_left;
	    public QuadPoint bottom_right;
	    public QuadRectangle(int id, QuadPoint top_left, QuadPoint bottom_right)
	    {
	        this.id = id;
	        this.top_left = top_left;
	        this.bottom_right = bottom_right;
	    }
	    public String toString()
	    {
	        return top_left.toString() + ", " + bottom_right.toString();
	    }
	}