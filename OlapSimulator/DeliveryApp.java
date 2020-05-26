import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DeliveryApp 
{
///////MYSQL VARIABLES////////
	//Spencer Edwards 013472914
	//Nicholas Dowell  013472134
	
public static final String IP = "localhost:3306";
public static final String USER= "user2";
public static final String PASS = "Thisisabadpassword12-";
public static final String DB = "hw5Olap";
public static final String TABLENAME = "olapDelivery";
	
/////////////////////////////
////////////////////////////
	
	Statement stmt;
	Connection con;
	
	static void createDatabase(String id)
	{
		File tmp = new File(id + ".sqlite");
		if(!tmp.exists())
		{
			tmp.delete();
		}
	}
	static void createTable(Statement stmt, String id)
	{
		try 
		{
			String table = "CREATE TABLE IF NOT EXISTS DELIVERY (location VARCHAR(20), store VARCHAR(9), pickup_time VARCHAR(30), time_to_deliver VARCHAR (15), amount VARCHAR(15))";
			stmt.execute(table);
			table = "CREATE TABLE IF NOT EXISTS UPLOAD(ID INTEGER PRIMARY KEY AUTOINCREMENT, lastUploadTime VARCHAR(20))";
			stmt.execute(table);
		}
		catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	static Boolean insertData(Connection con, String location, String store, String pickup_time, String time_to_deliver, String amount) 
	{
		System.out.println("Data inserted");
		String insert = "INSERT INTO DELIVERY (location, store, pickup_time, time_to_deliver, amount) VALUES(?, ?, ?, ?, ?)";
		try
		{
			PreparedStatement pStmt = con.prepareStatement(insert);
			pStmt.setString(1, location); pStmt.setString(2, store);
			pStmt.setString(3, pickup_time); pStmt.setString(4, time_to_deliver);
			pStmt.setString(5, amount);
			pStmt.executeUpdate();
			return true;
		}
		catch(Exception e){System.out.println(e); System.out.println("Insertion failed"); System.exit(-1);}
		return false;
	}
	static Boolean checkFormat(String date, String format)
	{
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		sdf.setLenient(false);
		try
		{
			sdf.parse(date);
			return true;
		}
		catch(ParseException e)
		{
			System.out.println("Date " + date + " must be in the format " + format);
			System.exit(-1);
		}
		return false;
	}
	static Boolean checkLocation(String location)
	{
		if(location.split("-").length != 2)
		{
			System.out.println("Location must be int-int");
			System.exit(-1);
			return false; //never get here
		}
		else
		{
			return true;
		}
	}
	static Boolean checkAmount(String amount)
	{
		try
		{
			Float.parseFloat(amount);
			return true;
		}
		catch(Exception e)
		{
			System.out.println("Amount must be a float");
			System.exit(-1);
		}
		return false;
	}
	static Boolean checkId(String id)
	{
		
		try
		{
			if(id.length() == 9)
			{
				Integer.parseInt(id);
				return true;
			}
		}
		catch(Exception e){}
		System.out.println("ID must be a 9 digit Integer");
		System.exit(-1);
		return false;
	}
	static String checkCommand(String command)
	{
		command = command.toLowerCase();
		if(command.equals("time_to_deliver"))
		{
			return command;
		}
		else if(command.equals("location"))
		{
			return command;
		}
		else
		{
			System.out.println("Final argument for show must be either \"time_to_deliver\" or \"location\"");
			System.exit(-1);
			return "";
		}
	}
	static void printResultSet(ResultSet set)
	{
		try
		{
			while(set.next())
			{
				String var = set.getString(1);
				String time = set.getString(2).substring(0, set.getString(2).length()-3);
				String amount = set.getString(3);
				Double iAmount = Double.parseDouble(amount);
				System.out.println(var + " " + time + " " + "$" + String.format("%.2f", iAmount));
			}
		}
		catch (NumberFormatException | SQLException e) 
		{
			e.printStackTrace();
		}
	}
	static ResultSet queryDatabase(Connection con, String date, String format)
	{
		String strStmt = "";
		if(format.equals("location"))
		{
			strStmt = "SELECT location, time(sum(strftime('%s', time_to_deliver) - strftime('%s', '00:00:00')),'unixepoch'), SUM(amount) FROM DELIVERY WHERE pickup_time LIKE \"" + date + "%\" Group by location";
		}
		else if(format.equals("time_to_deliver"))
		{
			strStmt = "SELECT store, time(sum(strftime('%s', time_to_deliver) - strftime('%s', '00:00:00')),'unixepoch'), SUM(amount) FROM DELIVERY WHERE pickup_time LIKE \"" + date + "%\" Group by store";
		}
		else if(format.equals("send"))
		{
			strStmt = "SELECT * from DELIVERY WHERE pickup_time >= " + "\"" + date + "\"" ;
		}
		try 
		{
			Statement stmt = con.createStatement();
			ResultSet set = stmt.executeQuery(strStmt);
				return set;
		}
		catch (SQLException e) 
		{
			e.printStackTrace();
			System.exit(-1);
		}
		return null;
	}
	static String[] parseCoordinates(String coor)
	{
		return coor.split("-");
	}
	static String[] parseDates(String date)
	{
		String[] v = date.split("-");
	    String[] q = v[v.length-1].split(":");
		v[v.length-1] = q[0];
		return v;
	}
	static void insertIntoMysql(ResultSet set, Connection con)
	{
		
		try
		{
			String insert = "INSERT INTO " + TABLENAME + " VALUES(?, ?, ?, ?, ?, ?, ?, ?)";
			/*if(!set.next())
			{
				System.out.println("No values to insert into MySql");
			}
			else
			{
				System.out.println("Inserted Into MySql");
			}
			*/
			while(set.next())
			{
				PreparedStatement stmt = con.prepareStatement(insert);
				String coordinates[] = parseCoordinates(set.getString(1));
				String parseDate[] = parseDates(set.getString(3));
				stmt.setString(1, coordinates[0]);
				stmt.setString(2, coordinates[1]);
				stmt.setString(3, set.getString(2));
				stmt.setString(4, parseDate[0]);
				stmt.setString(5, parseDate[1]);
				stmt.setString(6, parseDate[2]);
				stmt.setString(7, set.getString(4));
				stmt.setString(8, set.getString(5));
				stmt.executeUpdate();
			}
		}
		catch (SQLException e)
		{
			
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	static String insertFetchTime(Connection con, String insert)
	{
		String retrieve = "SELECT lastUploadTime FROM upload order by id desc limit 1";
		String query = "INSERT INTO UPLOAD(lastUploadTime) VALUES(?)";
		PreparedStatement pStmt;
		try 
		{
			Statement stmt = con.createStatement();
			ResultSet setty = stmt.executeQuery(retrieve);
			pStmt = con.prepareStatement(query);
			pStmt.setString(1, insert);
			pStmt.executeUpdate();
			if(setty.next())
			{
				return setty.getString(1);
			}
			else
			{
				return "0000-00-00:00:00:00"; //get all to upload
			}
			
			
			
		} catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(-1);;
		}
		return  "0000-00-00:00:00:00";	
	}
	static Connection connectToMySql()
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
	public static void main(String[] args) throws ClassNotFoundException
	{
		Class.forName("org.sqlite.JDBC");
		if(!(args.length == 2 || args.length == 4 || args.length == 7))
		{
			System.out.println("Need either 2, 4 or 7 arguments");
		}
		String id = args[1];
		checkId(id);	
		createDatabase(id);
		Connection con = null;
		Statement stmt = null;
		try
		{
			con = DriverManager.getConnection("jdbc:sqlite:" + id + ".sqlite");
			stmt = con.createStatement();
		}
		catch(Exception e)
		{
			System.out.println("Connecting to database failed");
			System.exit(-1);
		}
		String command = args[0].toLowerCase();
		if(command.equals("deliver") && args.length > 6)
		{				
			String location = args[2];
			String store = args[3];
			String pickup_time = args[4];
			String time_to_deliver = args[5];
			String amount = args[6];
			checkFormat(pickup_time, "YYYY-MM-dd:HH:mm:ss");
			checkFormat(time_to_deliver, "HH:mm:ss");
			checkLocation(location);
			checkAmount(amount);
			createTable(stmt, id);
			insertData(con, location, store, pickup_time, time_to_deliver, amount);
		}
		else if(command.equals("show") && args.length > 2)
		{
			String date = args[2];
			checkFormat(date, "YYYY-MM-dd");
			String format = checkCommand(args[3]);
			ResultSet set = queryDatabase(con, date, format);
			printResultSet(set);
			
		}
		else if(command.equals("send") && args.length > 1)
		{
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("YYYY-MM-dd:HH:mm:ss");  
			String date = simpleDateFormat.format(new Date());
		    String time = insertFetchTime(con, date);
		    ResultSet set = queryDatabase(con, time, "send");
		    Connection mysqlCon = connectToMySql();
		    insertIntoMysql(set, mysqlCon);
		}
		else
		{
			System.out.println("Commands are:");
			System.out.println("1) deliver id location store pickup_time time_to_deliver amount");
			System.out.println("2) show id date format");
			System.out.println("3) send id");
		}
	}
}