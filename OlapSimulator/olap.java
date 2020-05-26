

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class olap 
{
	public static final String IP = "localhost:3306";
	public static final String USER= "user2";
	public static final String PASS = "Thisisabadpassword12-";
	public static final String DB = "hw5Olap";
	public static final String TABLENAME = "olapDelivery";
	static Connection getConnection()
	{
		try
		{
			Class.forName("com.mysql.jdbc.Driver");
			Connection con = DriverManager.getConnection("jdbc:mysql://" + IP + "/" + DB + "?allowPublicKeyRetrieval=true&useSSL=false", USER, PASS);
			return con;
		}
		catch(Exception e)
		{
			//if fails there exists an extra entry in the sqlite time table "Upload"
			e.printStackTrace();
		}
		return null;
	}
	static void checkLatLngFormat(String latlng)
	{
		if(!(latlng.matches("\\d+-\\d+:\\d+-\\d+")))
		{
			System.out.println(" must be xx-xx:xx-xx");
			System.exit(-1);
		}
	}
	static boolean checkDate(String date)
	{
		String format = "YYYY";
		SimpleDateFormat sdf = new SimpleDateFormat("YYYY");
		SimpleDateFormat sdf2 = new SimpleDateFormat("YYYY-MM");
		SimpleDateFormat sdf3= new SimpleDateFormat("YYYY-MM-dd");
		try
		{
			sdf.parse(date);
			return true;
		}
		catch(ParseException e)
		{
			try
			{
				sdf2.parse(date);
			} catch (ParseException e1)
			{
				// TODO Auto-generated catch block
				try {
					sdf3.parse(date);
				} catch (ParseException e2) {
					// TODO Auto-generated catch block
					System.out.println("Date must be either YYYY, YYYY-MM, or YYYY-MM-dd");
					System.exit(-1);
				}
			}
			
		}
		return false;
	}
	
	static ResultSet rollup(Connection con, int[] v, String[] dates, String name)
	{
		int topLeftLat = v[0];
		int topLeftLon = v[1];     //int topRightLat = v[2];   //int topRightLon = v[3]; //int bottomLeftLat = v[6]; 	//int bottomLeftLon = v[7];
		int bottomRightLat = v[4];
		int bottomRightLon = v[5];
		String query;
		if(dates.length == 1)
		{
			query = "Select store, pickup_time_year, AVG(amount) as avg  from olapDelivery where locationX >= " + topLeftLat + " AND locationX <= " + bottomRightLat + " AND locationY <= " + topLeftLon + " AND locationY >= " + bottomRightLon + " GROUP BY store, pickup_time_year with rollup";	
		}
		else if(dates.length == 2)
		{
			query = "Select store, pickup_time_year, pickup_time_month, AVG(amount) as avg from olapDelivery where locationX >= " + topLeftLat + " AND locationX <= " + bottomRightLat + " AND locationY <= " + topLeftLon + " AND locationY >= " + bottomRightLon + " GROUP BY store, pickup_time_year, pickup_time_month with rollup";	
		}
		else
		{
			query = "Select store, pickup_time_year, pickup_time_month, pickup_time_day, AVG(amount) as avg from olapDelivery where locationX >= " + topLeftLat + " AND locationX <= " + bottomRightLat + " AND locationY <= " + topLeftLon + " AND locationY >= " + bottomRightLon + " GROUP BY store, pickup_time_year, pickup_time_month, pickup_time_day with rollup";
		}
		System.out.println(query);
		Statement stmt;
		try
		{
			stmt = con.createStatement();
			return stmt.executeQuery(query);
		} catch (SQLException e)
		{
			e.printStackTrace();
			System.out.println("Could not perform query");
			System.exit(-1);
		}
		return null;
	}
	static int[] splitLatLng(String latlng)
	{
		String v[] = latlng.split(":");
		String one[] = v[0].split("-");
		String two[] = v[1].split("-");
		int pointOneX = Integer.parseInt(one[0]);
		int pointOneY = Integer.parseInt(one[1]);
		int pointTwoX = Integer.parseInt(two[0]);
		int pointTwoY = Integer.parseInt(two[1]);
		int topLeftLat;// = one[0];
	    int topLeftLon;// = one[1];
	    int topRightLat;// = two[0];
	    int topRightLon;// = one[1]; 
	    int bottomRightLat;// = two[0];
	    int bottomRightLon;// = two[1];
	    int bottomLeftLat;// = 
	    int bottomLeftLon;// =
		if(pointOneX < pointTwoX)
		{ // a lot of these values are extra, but i didnt know it at the time, also it helped
			// me keep everything straight, and in fear of breaking stuff I'm leavingi t
			if(pointOneY > pointTwoY)
			{
				topLeftLat = pointOneX;
				topLeftLon = pointOneY;
				topRightLat = pointTwoX;
				topRightLon = pointOneY;
				bottomRightLat = pointTwoX;
				bottomRightLon = pointTwoY;
				bottomLeftLat = pointOneX;
				bottomLeftLon = pointTwoY;
			}
			else
			{
				topLeftLat= pointOneX;
				topLeftLon= pointTwoY;
				topRightLat= pointTwoX;
				topRightLon= pointTwoY;
				bottomRightLat= pointTwoX;
				bottomRightLon= pointOneY;
				bottomLeftLat= pointOneX;
				bottomLeftLon= pointOneY;
			}
		}
		else //pointOneX > pointTwoX
		{
			if(pointOneY > pointTwoY)
			{
				topLeftLat= pointTwoX;
				topLeftLon= pointOneY;
				topRightLat= pointOneX;
				topRightLon= pointOneY;
				bottomRightLat= pointOneX;
				bottomRightLon= pointTwoY;
				bottomLeftLat= pointTwoX;
				bottomLeftLon= pointTwoY;
			}
			else
			{
				topLeftLat = pointTwoX;
				topLeftLon = pointTwoY;
				topRightLat = pointOneX;
				topRightLon = pointTwoY;
				bottomRightLat = pointOneX;
				bottomRightLon = pointOneY;
				bottomLeftLat = pointTwoX;
				bottomLeftLon = pointOneY;
			}
		}
		int[] points = new int[8];
		points[0] = topLeftLat;
		points[1] = topLeftLon;
		points[2] = topRightLat;
		points[3] = topRightLon;
		points[4] = bottomRightLat;
		points[5] = bottomRightLon;
		points[6] = bottomLeftLat;
		points[7] = bottomLeftLon;
		return points;	
	}
	static void printOutQuery(ResultSet set, int flag, String name, String[] date)
	{
		try
		{
			if(flag == 1)
			{
				System.out.println("Store" + " " + "year" + " " + "avg");
			}
			else if(flag == 2)
			{
				System.out.println("Store" + " " + "year" + " " + "month" + " " + "avg");
			}
			else
			{
				System.out.println("Store" + " " + "year" + " " + "month" + " " + "day "+ " " + "avg");
			}
			
			while(set.next())
			{
				if(flag == 1)
				{
					String dName = set.getString(1);
					String year = set.getString(2);
					String avg = set.getString(3);
					if(name.equals(dName) && date[0].equals(year))
					{
						System.out.println(dName + " " + year + " " + " " + avg);
					}
					else if(name.equals(dName) && year == null)
					{
						System.out.println(dName + " " + year + " " + " " + avg);
					}
					else if(dName == null && year == null)
					{
						System.out.println(dName + " " + year + " " + " " + avg);
					}
				
				}
				else if(flag == 2)
				{
					String dName = set.getString(1);
					String year = set.getString(2);
					String month = set.getString(3); //.substring(0, set.getString(2).length()-3);
					String avg = set.getString(4);
					if(name.equals(dName) && date[0].equals(year) && date[1].equals(month))
					{
						System.out.println(dName + " " + year + " " + month + " " + avg);
					}
					else if(name.equals(dName) && date[0].equals(year) && month == null)
					{
						System.out.println(dName + " " + year + " " + month + " " + avg);
					}
					else if(name.equals(dName) && year == null && month == null)
					{
						System.out.println(dName + " " + year + " " + month + " " + avg);
					}
					else if(dName == null && year == null && month == null)
					{
						System.out.println(dName + " " + year + " " + month + " " + avg);
					}
				}
				else
				{
					String dName = set.getString(1);
					String year = set.getString(2);
					String month= set.getString(3); //.substring(0, set.getString(2).length()-3);
					String day = set.getString(4);
					String avg = set.getString(5);
					if(name.equals(dName) && date[0].equals(year) && date[1].equals(month) && date[2].equals(day))
					{
						System.out.println(dName + " " + year + " " + month + " " + day + " " + avg);	
					}
					else if(name.equals(dName) && date[0].equals(year) && date[1].equals(month) && day == null)
					{
						System.out.println(dName + " " + year + " " + month + " " + day + " " + avg);
					}
					else if(name.equals(dName) && date[0].equals(year) && month == null && day == null)
					{
						System.out.println(dName + " " + year + " " + month + " " + day + " " + avg);
					}
					else if(name.equals(dName) && year == null && month == null && day == null)
					{
						System.out.println(dName + " " + year + " " + month + " " + day + " " + avg);
					}
					else if(dName == null && year == null && month == null && day == null)
					{
						System.out.println(dName + " " + year + " " + month + " " + day + " " + avg);
					}
				
					
				}
				
			}
		}
		catch (NumberFormatException | SQLException e) 
		{
			e.printStackTrace();
		}
	}
	static String[] parseDates(String date)
	{
		String[] v = date.split("-");
	    String[] q = v[v.length-1].split(":");
		v[v.length-1] = q[0];
		return v;
	}
	public static void main(String[] args)
	{
		if(args.length != 3)
		{
			System.out.println("Arguments must be lat1-long1:lat2-long2, name, YYYY-(MM-(dd))");
		}
		String latlng = args[0];
		String name = args[1];
		String date = args[2];
		String d[] = parseDates(date);
		int flag = d.length;
		checkLatLngFormat(latlng);
		int v[] = splitLatLng(latlng);
		
		checkDate(date);
		Connection con = getConnection();
		ResultSet set = rollup(con, v, d, name);
		printOutQuery(set, flag, name, d);
	}

}
