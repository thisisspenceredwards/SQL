import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class finalrunner
{
	public static final String IP = "localhost:3306";
	public static final String USER= "user2";
	public static final String PASS = "Thisisabadpassword12-";
	public static final String DB = "BANK";
	public static final String TABLENAME = "ACCOUNT";
	
	
	public static void main(String args[])
	{
		Connection con;
		try
		{
			con = DriverManager.getConnection("jdbc:mysql://" + IP + "/" + DB + 
					"?allowPublicKeyRetrieval=true&useSSL=false", USER, PASS);
			Statement stmt = con.createStatement();
			System.out.println("Setting Transaction Isolation to Serializable");
			con.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
			String transaction = "UPDATE ACCOUNT SET Balance = Balance - 40 WHERE ID = \"013472914\""; 
			con.setAutoCommit(false); //START OF TRANSACTION
			stmt.executeUpdate(transaction);
			con.commit(); //END OF TRANSACTION
			con.setAutoCommit(true);
			System.out.println("Transaction Ended");
			ResultSet set = stmt.executeQuery("Select BALANCE FROM ACCOUNT WHERE ID = \"013472914\"");
			set.next();
			System.out.println("After subtracting 40 from Account, Balance is now: " + set.getInt(1));	
		} 
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		
	}

}
