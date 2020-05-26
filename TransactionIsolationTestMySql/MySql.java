import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;

public class MySql
{
	private String db;
	private Statement stmt;
	private Connection con;
	private Random rng;
	private int i = 0;
	private String tableName;
	
	public MySql(Random random, String db, String tableName)
	{
		this.tableName = tableName;
		this.db = db;
		this.rng = random;
	}
	
	public void makeConnection(String ip, String user, String pass)
	{
		System.out.println("makeConnection");
		try
		{
			//Class.forName("com.mysql.cj.jdbc.Driver");
			this.con = DriverManager.getConnection("jdbc:mysql://" + ip + "/" + db + "?&useSSL=false", user, pass);
			this.stmt = con.createStatement();  
		}
		catch(Exception e)
		{
			System.out.println(db +".makeConnection -> Error");
			e.printStackTrace();
		}
	}
	
	public void setReadCommitted()
	{
		try
		{
			System.out.println("Transaction Isolation Level: " + con.getTransactionIsolation());
			con.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
			System.out.println("Transaction Isolation Level: " + con.getTransactionIsolation());
		}
		catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void setSerializable()
	{
		try
		{
			System.out.println("Transaction Isolation Level: " + con.getTransactionIsolation());
			con.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
			System.out.println("Transaction Isolation Level: " + con.getTransactionIsolation());
		}
		catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public int checkIsolationLevel()
	{
		try
		{
			return con.getTransactionIsolation();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		return -1;
	}
	public Statement getStatment()
	{
		return stmt;
	}
	public Connection getCon()
	{
		return con;
	}
	public void performOperations(MySql otherSql, MySql other1, MySql other2)
	{	
		Statement stmt2 = otherSql.getStatment();
		Connection con2 = otherSql.getCon();
		String update1 = "UPDATE TEST SET c2 = " + 5 + ", c3 = " + 6 + ", c4 = " + 7 + ", c5 = " + 8 + ", c6 = " + 9;
		String update2 = "UPDATE TEST SET c2 = " + 10 + ", c3 = " + 10 + ", c4 = " + 10 + ", c5 = " + 10 + ", c6 = " + 10;
		String insert = "INSERT INTO " + tableName + " VALUES("+ i + ", " + rng.nextInt(100) + ", " + rng.nextInt(100) + ", " + rng.nextInt(100) + ", " + rng.nextInt(100) + ", " + rng.nextInt(100) + ")";
		String sleep = "SELECT SLEEP(1)";
		String query = "SELECT * FROM TEST";
		String delete = "DELETE FROM TEST WHERE c1 = 0";
		i++;
		try {
			System.out.println("Setting autocommit to false");
			con2.setAutoCommit(false);
			System.out.println("Connection1: Inserting first value");
			stmt.execute(insert);
			System.out.println("Connection1: Sleeping for 1 second");
			stmt.executeQuery(sleep);
			System.out.println();
			
			System.out.println("Connection2: reading insert");
			ResultSet val = stmt2.executeQuery(query);
			System.out.println("Values retrieved from read committed connection");
			printResults(val);
			//System.out.println("Connection2: Sleeping for 1 second");
			//stmt2.executeQuery(sleep);
			System.out.println();
			
			System.out.println("Connection1: Update 1 inserting values");
			stmt.executeUpdate(update1);
			System.out.println("Connection1: Sleeping for 1 second");
			stmt.executeQuery(sleep);
			System.out.println();
			
			System.out.println("Connection2: reading insert");
			val = stmt2.executeQuery(query);
			System.out.println("Values retrieved from read committed connection");
			printResults(val);
			//System.out.println("Connection2: Sleeping for 1 second");
			//stmt2.executeQuery(sleep);
			System.out.println();
			
			System.out.println("Connection1: Update 2 inserting values");
			stmt.executeUpdate(update2);
			System.out.println("Connection1: Sleeping for 1 second");
			stmt.executeQuery(sleep);
			System.out.println();
			
			System.out.println("Connection2: reading insert");
			val = stmt2.executeQuery(query);
			System.out.println();
			System.out.println("Values retrieved from read committed connection");
			printResults(val);
			//System.out.println("Connection2: Sleeping for 1 second");
			
			System.out.println("Connection2: Commiting transaction");
			con2.commit();
			con2.setAutoCommit(true);
			
			System.out.println("Connection1: Deleting row");
			stmt.executeUpdate(delete);
			
			val.close();
		    stmt.close();
		    con.close();
			con2.close();
			stmt2.close();
			
			if(other1 != null & other2 != null)
			{
				other2.performOperations(other1, null, null);
			}
			
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private void printResults(ResultSet rs)
	{
		try
		{
			while(rs.next())
			{
				 	System.out.println();
				    System.out.println("VALUES READ");
					System.out.print(rs.getInt("c1") + " ");
					System.out.print(rs.getInt("c2") + " ");
					System.out.print(rs.getInt("c3") + " ");
					System.out.print(rs.getInt("c4") + " ");
					System.out.print(rs.getInt("c5") + " ");
					System.out.println(rs.getInt("c6") + " ");
				System.out.println();
			}
		}
		catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}