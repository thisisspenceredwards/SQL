package cs157bhw2;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class QuadTreeManagerObject
{
	Statement stmt;
	Connection con;
	ArrayList<QuadRectangle> keepTrack = new ArrayList<QuadRectangle>();
	public QuadTreeManagerObject(String databaseName)
	{
		try
		{
			
			Class.forName("org.sqlite.JDBC");
			File file = new File(databaseName);
			file.delete();
			this.con = DriverManager.getConnection("jdbc:sqlite:" + databaseName);
			this.stmt = con.createStatement();
			String table1 = "CREATE TABLE QUAD_TREE(FILE_NAME VARCHAR(64), ROOT_RECT_ID INTEGER)";
			String table2 = "CREATE TABLE QUAD_TREE_RECT(ID INTEGER PRIMARY KEY AUTOINCREMENT, PARENT_ID INTEGER, X_LOW REAL, Y_LOW REAL, X_HIGH REAL, Y_HIGH REAL)";
			String table3 = "CREATE TABLE QUAD_TREE_POINT(QUAD_RECT_ID INTEGER, X REAL, Y REAL, LABEL CHAR(16))";
			this.stmt.execute(table1);
			this.stmt.execute(table2);
			this.stmt.execute(table3);
			String createIndex = "CREATE INDEX CHILD_INDEX ON QUAD_TREE_RECT(PARENT_ID)";
			String createIndex2 = "CREATE INDEX POINT_INDEX ON QUAD_TREE_POINT(QUAD_RECT_ID)";
			this.stmt.execute(createIndex);
			this.stmt.execute(createIndex2);
		
		} catch (SQLException | ClassNotFoundException e) {
			System.out.println("QuadTreeManager -> Error");
			System.out.println( e) ;
		}
	}
	public void cleanArrayList()
	{
		keepTrack = new ArrayList<QuadRectangle>();
	}
	public boolean createQuadTree(String fileName, float lowX, float lowY, float highX, float highY)
	{
		if(highY - lowY < 0 || highX - lowX < 0)
		{
			System.out.println("Low Y and Low X must be less than high Y and high X");
			return false;
		}
		try
		{  //insert into quad tree rect a new rectangle
			String newEntryRect = "INSERT INTO QUAD_TREE_RECT(PARENT_ID, X_LOW, Y_LOW, X_HIGH, Y_HIGH) VALUES(?,?,?,?,?)";
			PreparedStatement preState = this.con.prepareStatement(newEntryRect);
			preState.setInt(1,  -1); preState.setFloat(2, lowX); preState.setFloat(3, lowY); preState.setFloat(4, highX); preState.setFloat(5, highY);
			preState.executeUpdate();
			//retrieve auto increment ID
			Integer id = getMaxId();
			//insert file and ID of rectangle into QUAD_TREE 
			String newEntryTree = "INSERT INTO QUAD_TREE(FILE_NAME, ROOT_RECT_ID) VALUES(?, ?)";
			preState = this.con.prepareStatement(newEntryTree);
			preState.setString(1, fileName); preState.setInt(2, id);
			boolean success = preState.execute();
			return success;
		}
		catch(SQLException e)
		{
			System.out.println("createQuadTree ->" + e);
			return false;
		}
	}
	Integer getMaxId()
	{
		String getId = "SELECT ID FROM QUAD_TREE_RECT WHERE ID = (SELECT MAX(ID) FROM QUAD_TREE_RECT)";
		ResultSet idSet;
		try {
			idSet = this.stmt.executeQuery(getId);
			return idSet.getInt("ID");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
	}
	ResultSet getPoints(Integer currentId)
	{
		String getPoints = "SELECT * FROM QUAD_TREE_POINT WHERE QUAD_RECT_ID = ?";
		PreparedStatement prepQuery;
		try {
			prepQuery = this.con.prepareStatement(getPoints);
			prepQuery.setInt(1, currentId);
			return prepQuery.executeQuery();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		
	}
	boolean insertPoint(int currentId, float record_x, float record_y, String label)
	{
		String insert = "INSERT INTO QUAD_TREE_POINT(QUAD_RECT_ID, X, Y, LABEL) VALUES(?,?,?,?)";
		PreparedStatement prepQuery;
		try {
			prepQuery = this.con.prepareStatement(insert);
			prepQuery.setInt(1, currentId); 
			prepQuery.setFloat(2, record_x);
			prepQuery.setFloat(3, record_y);
			prepQuery.setString(4, label);
			return prepQuery.execute();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
				
		
	}
	private void divideRectangle(Integer currentId, Float x_high, Float x_low, Float y_high, Float y_low, Float record_x, Float record_y, String record_label, double EPSILON)
	{
		try
		{
		//find medians//boundaries
		//
		Float x_median = (float) ((x_high + x_low)/2.0);
		Float y_median = (float) ((y_high + y_low)/2.0);
		//
		ArrayList<QuadRectangle> list = new ArrayList<QuadRectangle>();
		
		QuadRectangle quad1 = new QuadRectangle(-1, new QuadPoint(x_low, y_low), new QuadPoint(x_median, y_median));
		QuadRectangle quad2 = new QuadRectangle(-1, new QuadPoint(x_median, y_low), new QuadPoint(x_high, y_median));
		QuadRectangle quad3 = new QuadRectangle(-1, new QuadPoint(x_low, y_median), new QuadPoint(x_median, y_high));
		QuadRectangle quad4 = new QuadRectangle(-1, new QuadPoint(x_median, y_median), new QuadPoint(x_high, y_high));
		list.add(quad1);
		list.add(quad2);
		list.add(quad3);
		list.add(quad4);
			
		ResultSet savedPoints = getPoints(currentId);
		
		ArrayList<Object[]> pointList = new ArrayList<Object[]>();
		while(savedPoints.next())
		{
			Object[] points = {savedPoints.getFloat("X"), savedPoints.getFloat("Y"), savedPoints.getString("LABEL")};
			pointList.add(points);
		}
		Object[] point = {record_x, record_y, record_label};
		pointList.add(point);
		for(int i = 0; i < list.size(); i++) //"CREATE TABLE QUAD_TREE_RECT(ID INTEGER PRIMARY KEY AUTOINCREMENT, PARENT_ID INTEGER, X_LOW REAL, Y_LOW REAL, X_HIGH REAL, Y_HIGH REAL)";
		{
			float temp_top_left_x = list.get(i).top_left.x;
			float temp_top_left_y = list.get(i).top_left.y;
			float temp_bottom_right_x = list.get(i).bottom_right.x;
			float temp_bottom_right_y = list.get(i).bottom_right.y;
			String newEntryRect = "INSERT INTO QUAD_TREE_RECT(PARENT_ID, X_LOW, Y_LOW, X_HIGH, Y_HIGH) VALUES(?,?,?,?,?)";
			PreparedStatement preState = this.con.prepareStatement(newEntryRect);
			preState.setInt(1,  currentId); preState.setFloat(2, temp_top_left_x); preState.setFloat(3, temp_top_left_y); preState.setFloat(4, temp_bottom_right_x); preState.setFloat(5, temp_bottom_right_y);
			preState.executeUpdate();
			Integer maxId = getMaxId();
			int count1 = 0;
			for(int j = 0; j < pointList.size(); j++)
			{
				float temp_record_x = (float) pointList.get(j)[0];
				float temp_record_y = (float) pointList.get(j)[1];
				String temp_label = (String) pointList.get(j)[2];
				if(temp_record_x >= temp_top_left_x - EPSILON && temp_record_x <= temp_bottom_right_x + EPSILON && temp_record_y >= temp_top_left_y - EPSILON && temp_record_y <= temp_bottom_right_y + EPSILON )
				{
					count1++;
					if(count1 > 4)
					{
						divideRectangle(maxId, temp_top_left_x, temp_bottom_right_x, temp_top_left_y, temp_bottom_right_y, temp_record_x, temp_record_y, temp_label, EPSILON);
					}
					pointList.remove(j);
					j--;
					String querty = "INSERT INTO QUAD_TREE_POINT(QUAD_RECT_ID, X, Y, LABEL) VALUES(?,?,?,?)";
					preState = this.con.prepareStatement(querty);
					preState.setInt(1, maxId); preState.setFloat(2, temp_record_x); preState.setFloat(3, temp_record_y); preState.setString(4, temp_label);
					preState.executeUpdate();
				}
			}
		}
		String delete = "DELETE FROM QUAD_TREE_POINT WHERE QUAD_RECT_ID = ?";
		PreparedStatement del = this.con.prepareStatement(delete);
		del.setInt(1, currentId);
		del.executeUpdate();
	}	
	catch(SQLException e)
	{
		System.out.println("Split exception");
		System.out.println(e);
	}
	}

	boolean add(String fileName, QuadRecord record)
	{
		String query = "SELECT * FROM QUAD_TREE WHERE FILE_NAME = ?";
		try
		{
			//1st: Look up to see if quad tree exists
			PreparedStatement prepQuery = this.con.prepareStatement(query);
			prepQuery.setString(1, fileName);
			ResultSet set = prepQuery.executeQuery();
			if(!set.next())
			{
				System.out.println("File name is not in database");
				return false;
			}
			//Get the quad rect row corresponding to the ID
			String db = set.getString(1);
			Integer id = set.getInt(2);
			if(!db.equals(fileName))
			{
				System.out.println("File name is not in database");
				return false;
			}
			//check if point to insert is valid for this rectangle
			query = "SELECT * FROM QUAD_TREE_RECT WHERE ID = ?";
			prepQuery = this.con.prepareStatement(query);
			prepQuery.setInt(1, id);
			set = prepQuery.executeQuery();
			if(!set.next())
			{
				System.out.println("Point not valid for given rectangle");
				return false;
			}
			Integer currentId = set.getInt("ID");
			Float x_low = set.getFloat("X_LOW");
			Float y_low = set.getFloat("Y_LOW");
			Float x_high = set.getFloat("X_HIGH");
			Float y_high = set.getFloat("Y_HIGH");
			Float record_x = record.point.x;
			Float record_y = record.point.y;
			
			if(!(record_x >= x_low) || !(record_x <= x_high) || !(record_y >= y_low) || !(record_y <= y_high))
			{
				System.out.println("Point is not valid");
				return false;
			}
			//find node where insert is possible search for CHILD ID
			ResultSet result;
			Boolean found;
			double EPSILON = .01;  //FOR COMPARING FLOAT VALUES AND THE INNATE INACCURACY
			do
			{
				found = false;
				result = set;
				query = "SELECT * FROM QUAD_TREE_RECT WHERE PARENT_ID = ?";
				prepQuery = this.con.prepareStatement(query);
				prepQuery.setInt(1, currentId);
				set = prepQuery.executeQuery();
				//iterate through rectangles to find smallest rect that could contain point
				while(set.next() && !found)
				{
					Integer maybe_id = set.getInt("ID");
					x_low = set.getFloat("X_LOW");
					y_low = set.getFloat("Y_LOW");
					x_high = set.getFloat("X_HIGH");
					y_high = set.getFloat("Y_HIGH");
					
					if(record_x >= (x_low - EPSILON) && record_x <= (x_high + EPSILON) && record_y >= (y_low - EPSILON) && record_y < (y_high + EPSILON))
					{
						currentId = maybe_id;
						found = true;
					}	
				}	
			}
			while(found);
			//get rows in point associated with rectangle node
			set = getPoints(currentId);
			int count = 0;
			while(set.next())
			{
				float existing_point_x = set.getFloat("X");
				float existing_point_y = set.getFloat("Y");
				
				if(record_x == existing_point_x && record_y == existing_point_y)
				{
					System.out.println("Point already exists");
					return false;
				}
				count++;
			}
			if(count < 4) //should be a constant
			{
				insertPoint(currentId, record_x, record_y, record.label);
			}
			else //split!
			{	
				
				divideRectangle(currentId, x_high, x_low, y_high, y_low, record_x, record_y, record.label, EPSILON);
			}}
		
		catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return false;
	}
	public QuadRectangle[] lookupRectangle(String fileName, QuadRectangle r)
	{ 
		Integer rootId = 0;
		String getRoot = "SELECT ROOT_RECT_ID FROM QUAD_TREE WHERE FILE_NAME = ?";
		try
		{
			PreparedStatement state = this.con.prepareStatement(getRoot);
			state.setString(1, fileName);
			ResultSet set = state.executeQuery();
			if(set.next())
			{
				rootId = set.getInt("ROOT_RECT_ID");
			}
			else
			{
				System.out.println("File name is invalid");
				return null;
			}
			
			String getRectangle = "SELECT * FROM QUAD_TREE_RECT WHERE ID = ?";
			state = this.con.prepareStatement(getRectangle);
			state.setInt(1, rootId);
			set = state.executeQuery();
			Integer id;
			float x_low;
			float y_low;
			float x_high;
			float y_high;
			if(set.next())
			{
				id = set.getInt("ID");
				x_low = set.getFloat("X_LOW");
				y_low = set.getFloat("Y_LOW");
				x_high = set.getFloat("X_HIGH");
				y_high = set.getFloat("Y_HIGH");
			}
			else
			{
				System.out.println("Something went wrong");
				return null;
			}
			//get children ids
			ArrayList<QuadRectangle> list = checkForIntersection(id, x_low, y_low, x_high, y_high, r);
			if(list == null)
			{
				return null;
			}
			QuadRectangle[] array = new QuadRectangle[list.size()];
			for(int i = 0; i < list.size(); i++)
			{
				array[i] = list.get(i);
			}
			keepTrack = list;
			return array;	
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	private boolean testForIntersection(Integer id, Float x_low, Float y_low, Float x_high, Float y_high, QuadRectangle r)
	{
		Float r_x_low = r.bottom_right.x;
		Float r_y_low = r.bottom_right.y;
		Float r_x_high = r.top_left.x;
		Float r_y_high = r.top_left.y;
	
		
		if(((r_y_high >= y_high && r_y_low<= y_high) || (r_y_low <= y_low && r_y_high >= y_low)) &&
				((r_x_high >= x_low && r_x_high <= x_high) || (r_x_low >= x_low && r_x_low <= x_high)))
		{
			return true;
		}
		return false;
		
	}
	@SuppressWarnings("unchecked")
	private ArrayList<QuadRectangle> checkForIntersection(Integer id, Float x_low, Float y_low, Float x_high, Float y_high, QuadRectangle r)
	{
		ArrayList<QuadRectangle> array = new ArrayList<QuadRectangle>();
		
		try
		{
		String chillins = "SELECT * FROM QUAD_TREE_RECT WHERE PARENT_ID = ?";
		PreparedStatement state = this.con.prepareStatement(chillins);
		state.setInt(1, id);
		ResultSet set = state.executeQuery();
			while(set.next())
			{
				ArrayList<QuadRectangle> tempArray = (checkForIntersection(set.getInt("ID"), set.getFloat("X_LOW"), set.getFloat("Y_LOW"), set.getFloat("X_HIGH"), set.getFloat("Y_HIGH"), r));
				if(tempArray != null)
				{
					array.addAll(tempArray);
				}
			}
			if(array.size() == 0)
			{
				Boolean result = testForIntersection(id, x_low, y_low, x_high, y_high, r);
				if(result)
				{
					array.add(new QuadRectangle(id, new QuadPoint(x_low, y_low), new QuadPoint(x_high, y_high)));
					return array;
				}
				return array;
			}
			
			return array;
		}
		catch(SQLException e)
		{
			System.out.println("ERROR -> checkForIntersection " + e);
			return null;
		}
	}
	public QuadRecord[] lookupPoint(String fileName, QuadPoint pt1, QuadPoint pt2, int limit_offset, int limit_count)
	{
		Float lowX = pt1.x;
		Float lowY = pt1.y;
		Float highX = pt2.x;
		Float highY = pt2.y;
		ArrayList<QuadRecord> list = new ArrayList<QuadRecord>();
		String getPoints = "Select * FROM QUAD_TREE_POINT WHERE X >= ? AND X <= ? AND Y >= ? AND Y <= ? LIMIT ? OFFSET ?";
		PreparedStatement prep;
		try {
			prep = con.prepareStatement(getPoints);
			prep.setFloat(1, lowX);
			prep.setFloat(2, highX);
			prep.setFloat(3, lowY);
			prep.setFloat(4, highY);
			prep.setInt(5, limit_count);
			prep.setInt(6, limit_offset);
			ResultSet points = prep.executeQuery();
			while(points.next())
			{
				Float x = points.getFloat("X");
				Float y = points.getFloat("Y");
				String lab = points.getString("LABEL");
				list.add(new QuadRecord(lab, new QuadPoint(x, y)));
			}
			QuadRecord[] rec = new QuadRecord[list.size()];
			for(int i = 0; i < list.size(); i++)
			{
				rec[i] = list.get(i);
			}
			return rec;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(-1);
		}
		return null;
		
	}
}
