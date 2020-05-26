import java.util.Random;
	
public class Runner 
{
		public static final String IP = "localhost:3306";
		public static final String USER= "hw1";
		public static final String PASS = "password";
		public static final String DB = "IsolationTest";
		public static final String TABLENAME = "test";
		public static Random rng = new Random();
		public static void main(String args[])
		{
			try
			{
				MySql sql1 = new MySql(rng, DB, TABLENAME);
				MySql sql2 = new MySql(rng, DB, TABLENAME);
				sql1.makeConnection(IP, USER, PASS);
				sql2.makeConnection(IP, USER, PASS);
				sql1.setReadCommitted();
				
				if(sql1.checkIsolationLevel() != 2)
				{
					System.out.println("Isolation level not succesfully set");
					System.exit(-1);
				}
				if(sql2.checkIsolationLevel() != 4)
				{
					System.out.println("Isolation level not succesfully set");
					System.exit(-1);
				}
				
				MySql sql3 = new MySql(rng, DB, TABLENAME);
				MySql sql4 = new MySql(rng, DB, TABLENAME);
			
				sql3.makeConnection(IP, USER, PASS);
				sql4.makeConnection(IP, USER, PASS);
				sql3.setSerializable();
			
				if(sql3.checkIsolationLevel() != 8)
				{
					System.out.println("Isolation level not succesfully set");
					System.exit(-1);
				}
				if(sql4.checkIsolationLevel() != 4)
				{
					System.out.println("Isolation level not succesfully set");
					System.exit(-1);
				}
				sql2.performOperations(sql1, sql3, sql4);
			}
				
			catch(Exception e)
			{
				System.out.println(e.getStackTrace());
			}		
		}
	
	public static void callback()
	{
		
	}
}

