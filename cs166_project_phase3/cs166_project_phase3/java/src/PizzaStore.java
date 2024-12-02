/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.lang.Math;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */
public class PizzaStore {

   // reference to physical database connection.
   private Connection _connection = null;

   // handling the keyboard inputs through a BufferedReader
   // This variable can be global for convenience.
   static BufferedReader in = new BufferedReader(
                                new InputStreamReader(System.in));

   /**
    * Creates a new instance of PizzaStore
    *
    * @param hostname the MySQL or PostgreSQL server hostname
    * @param database the name of the database
    * @param username the user name used to login to the database
    * @param password the user login password
    * @throws java.sql.SQLException when failed to make a connection.
    */
   public PizzaStore(String dbname, String dbport, String user, String passwd) throws SQLException {

      System.out.print("Connecting to database...");
      try{
         // constructs the connection URL
         String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
         System.out.println ("Connection URL: " + url + "\n");

         // obtain a physical connection
         this._connection = DriverManager.getConnection(url, user, passwd);
         System.out.println("Done");
      }catch (Exception e){
         System.err.println("Error - Unable to Connect to Database: " + e.getMessage() );
         System.out.println("Make sure you started postgres on this machine");
         System.exit(-1);
      }//end catch
   }//end PizzaStore

   /**
    * Method to execute an update SQL statement.  Update SQL instructions
    * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
    *
    * @param sql the input SQL string
    * @throws java.sql.SQLException when update failed
    */
   public void executeUpdate (String sql) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the update instruction
      stmt.executeUpdate (sql);

      // close the instruction
      stmt.close ();
   }//end executeUpdate

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and outputs the results to
    * standard out.
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQueryAndPrintResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and output them to standard out.
      boolean outputHeader = true;
      while (rs.next()){
		 if(outputHeader){
			for(int i = 1; i <= numCol; i++){
			System.out.print(rsmd.getColumnName(i) + "\t");
			}
			System.out.println();
			outputHeader = false;
		 }
         for (int i=1; i<=numCol; ++i)
            System.out.print (rs.getString (i) + "\t");
         System.out.println ();
         ++rowCount;
      }//end while
      stmt.close();
      return rowCount;
   }//end executeQuery

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the results as
    * a list of records. Each record in turn is a list of attribute values
    *
    * @param query the input query string
    * @return the query result as a list of records
    * @throws java.sql.SQLException when failed to execute the query
    */
   public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and saves the data returned by the query.
      boolean outputHeader = false;
      List<List<String>> result  = new ArrayList<List<String>>();
      while (rs.next()){
        List<String> record = new ArrayList<String>();
		for (int i=1; i<=numCol; ++i)
			record.add(rs.getString (i));
        result.add(record);
      }//end while
      stmt.close ();
      return result;
   }//end executeQueryAndReturnResult

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the number of results
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQuery (String query) throws SQLException {
       // creates a statement object
       Statement stmt = this._connection.createStatement ();

       // issues the query instruction
       ResultSet rs = stmt.executeQuery (query);

       int rowCount = 0;

       // iterates through the result set and count nuber of results.
       while (rs.next()){
          rowCount++;
       }//end while
       stmt.close ();
       return rowCount;
   }

   /**
    * Method to fetch the last value from sequence. This
    * method issues the query to the DBMS and returns the current
    * value of sequence used for autogenerated keys
    *
    * @param sequence name of the DB sequence
    * @return current value of a sequence
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int getCurrSeqVal(String sequence) throws SQLException {
	Statement stmt = this._connection.createStatement ();

	ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
	if (rs.next())
		return rs.getInt(1);
	return -1;
   }

   /**
    * Method to close the physical connection if it is open.
    */
   public void cleanup(){
      try{
         if (this._connection != null){
            this._connection.close ();
         }//end if
      }catch (SQLException e){
         // ignored.
      }//end try
   }//end cleanup

   /**
    * The main execution method
    *
    * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
    */
   public static void main (String[] args) {
      if (args.length != 3) {
         System.err.println (
            "Usage: " +
            "java [-classpath <classpath>] " +
            PizzaStore.class.getName () +
            " <dbname> <port> <user>");
         return;
      }//end if

      Greeting();
      PizzaStore esql = null;
      try{
         // use postgres JDBC driver.
         Class.forName ("org.postgresql.Driver").newInstance ();
         // instantiate the PizzaStore object and creates a physical
         // connection.
         String dbname = args[0];
         String dbport = args[1];
         String user = args[2];
         esql = new PizzaStore (dbname, dbport, user, "");

         boolean keepon = true;
         while(keepon) {
            // These are sample SQL statements
            System.out.println("MAIN MENU");
            System.out.println("---------");
            System.out.println("1. Create user");
            System.out.println("2. Log in");
            System.out.println("9. < EXIT");
            String authorisedUser = null;
            switch (readChoice()){
               case 1: CreateUser(esql); break;
               case 2: authorisedUser = LogIn(esql); break;
               case 9: keepon = false; break;
               default : System.out.println("Unrecognized choice!"); break;
            }//end switch
            if (authorisedUser != null) {
              boolean usermenu = true;
              while(usermenu) {
                System.out.println("MAIN MENU");
                System.out.println("---------");
                System.out.println("1. View Profile");
                System.out.println("2. Update Profile");
                System.out.println("3. View Menu");
                System.out.println("4. Place Order"); //make sure user specifies which store
                System.out.println("5. View Full Order ID History");
                System.out.println("6. View Past 5 Order IDs");
                System.out.println("7. View Order Information"); //user should specify orderID and then be able to see detailed information about the order
                System.out.println("8. View Stores"); 

                //**the following functionalities should only be able to be used by drivers & managers**
                System.out.println("9. Update Order Status");

                //**the following functionalities should ony be able to be used by managers**
                System.out.println("10. Update Menu");
                System.out.println("11. Update User");

                System.out.println(".........................");
                System.out.println("20. Log out");
                switch (readChoice()){
                   case 1: viewProfile(esql); break;
                   case 2: updateProfile(esql); break;
                   case 3: viewMenu(esql); break;
                   case 4: placeOrder(esql); break;
                   case 5: viewAllOrders(esql); break;
                   case 6: viewRecentOrders(esql); break;
                   case 7: viewOrderInfo(esql); break;
                   case 8: viewStores(esql); break;
                   case 9: updateOrderStatus(esql); break;
                   case 10: updateMenu(esql); break;
                   case 11: updateUser(esql); break;



                   case 20: usermenu = false; break;
                   default : System.out.println("Unrecognized choice!"); break;
                }
              }
            }
         }//end while
      }catch(Exception e) {
         System.err.println (e.getMessage ());
      }finally{
         // make sure to cleanup the created table and close the connection.
         try{
            if(esql != null) {
               System.out.print("Disconnecting from database...");
               esql.cleanup ();
               System.out.println("Done\n\nBye !");
            }//end if
         }catch (Exception e) {
            // ignored.
         }//end try
      }//end try
   }//end main

   public static void Greeting(){
      System.out.println(
         "\n\n*******************************************************\n" +
         "              User Interface      	               \n" +
         "*******************************************************\n");
   }//end Greeting

   /*
    * Reads the users choice given from the keyboard
    * @int
    **/
   public static int readChoice() {
      int input;
      // returns only if a correct value is given.
      do {
         System.out.print("Please make your choice: ");
         try { // read the integer, parse it and break.
            input = Integer.parseInt(in.readLine());
            break;
         }catch (Exception e) {
            System.out.println("Your input is invalid!");
            continue;
         }//end try
      }while (true);
      return input;
   }//end readChoice

   /*
    * Creates a new user
    **/
   public static void CreateUser(PizzaStore esql){

      //A user needs the following:
      // * login
      // * password
      // * role
      // * favorite items (should be null at start?)
      // * phone number
      boolean valid = false;
      String newLogin = "";
      String password = "";
      String role = "customer";
      String phoneNum = "";
      BufferedReader consoleInput = new BufferedReader(new InputStreamReader(System.in));

      while(!valid){
         System.out.print("Please enter your new login or \'q\' to cancel: ");
         try{
            newLogin = consoleInput.readLine();
            if(newLogin.equals("q")) return;

            valid = esql.executeQueryAndPrintResult("SELECT F.login FROM Users F WHERE F.login = " + "\'" +newLogin + "\'") == 0;
            if(!valid){
               System.out.println("\n User " + newLogin+" already exists. Please use a different login. \n");
            }
         }catch(Exception e){
            System.out.println("Error reading input, please try again." + e.getMessage());
            System.exit(-1);
         }
         
      }
      // if user exists already (i.e. login) display so. Execute a query with supposed login
      System.out.println("-----------------------------------------");
      System.out.println("User available.");

      valid = false;
      while(!valid){
         System.out.print("Please enter your new password: ");
         try{
            password = consoleInput.readLine();
            

            valid = !password.equals("");
            if(!valid){
               System.out.println("\n Password cannot be empty. Please try again. \n");
            }
         }catch(Exception e){
            System.out.println("Error reading input, please try again later." + e.getMessage());
            System.exit(-1);
         }
         
      }
      // if user exists already (i.e. login) display so. Execute a query with supposed login
      System.out.println("-----------------------------------------");

      valid = false;
      while(!valid){
         System.out.print("Lastly, please enter your phone number: ");
         try{
            phoneNum = consoleInput.readLine();
            

            valid = !phoneNum.equals("");
            if(!valid){
               System.out.println("\n Phone number cannot be empty. Please try again. \n");
            }
         }catch(Exception e){
            System.out.println("Error reading input, please try again later." + e.getMessage());
            System.exit(-1);
         }
         
      }
      //otherwise put in necessary information and add to user table
      try{
         esql.executeUpdate("INSERT INTO Users VALUES ("+ "\'" + newLogin + "\'" +", "+ "\'"+ password + "\'"+", 'customer', null, "+ "\'" + phoneNum + "\'" + ");");
         boolean successfullyInserted = esql.executeQuery("SELECT * FROM Users F WHERE F.login = \'" + newLogin + "\'") != 0;
         System.out.println("-----------------------------------------");

         if(successfullyInserted){
            System.out.println("Successfully created user. Returning to main menu...");
         }else{
            System.out.println("Error inserting. Please try again later.");
         }
         System.out.println("-----------------------------------------");
      }catch(Exception e){
         System.out.println(e.getMessage());
      }


   }//end CreateUser


   /*
    * Check log in credentials for an existing user
    * @return User login or null is the user does not exist
    **/
   public static String LogIn(PizzaStore esql){
      List<List<String>> queryResults = new ArrayList<>();
      BufferedReader consoleInput = new BufferedReader(new InputStreamReader(System.in));

      try{
         System.out.print("Please enter login: ");
         String login = consoleInput.readLine();
         queryResults = esql.executeQueryAndReturnResult("SELECT * FROM Users F WHERE F.login = \'" + login + "\'");
         System.out.println("-----------------------------------------");

         if(queryResults.size() == 0){
            System.out.println("No user, returning to main menu.");
            return null;
         }
         boolean valid = false;

         while(!valid){
            System.out.print("Please enter password: ");
            String password = consoleInput.readLine();
            System.out.println("-----------------------------------------");
            valid = password.equals(queryResults.get(0).get(1));
            if(valid){
               System.out.println("Login success!");
            }else{
               System.out.println("Incorrect Username or Password! Please try again.");
               
            }
            System.out.println("-----------------------------------------");
         }
      }catch(Exception e){
         System.out.println(e.getMessage());
         System.exit(-1);
      }

      //System.out.println(queryResults);
      
      return queryResults.get(0).get(0);
   }//end
   

// Rest of the functions definition go in here

   public static void viewProfile(PizzaStore esql) {}
   public static void updateProfile(PizzaStore esql) {}

   public static void viewMenu(PizzaStore esql) {
      boolean exit = false;
      BufferedReader consoleInput = new BufferedReader(new InputStreamReader(System.in));

      while (!exit){
         //Basic menu feedback UI
         System.out.println("Pizza Menu");
         System.out.println("-----------------------------------------");

         try{
            List<List<String>> items = new ArrayList<>();
            items = esql.executeQueryAndReturnResult("SELECT * FROM Items");

            //print all items that match query
            for(List<String> item : items){
               System.out.println(item.get(0) + " " + item.get(2) + " " + item.get(3));
         }

         }catch(Exception e){
            System.out.println(e.getMessage());
         }
         
         //list for filter options
         System.out.println("-----------------------------------------");
         System.out.println("Options");
         System.out.println(" ");
         System.out.println("1. Filter by Type");
         System.out.println("2. Filter by Max Price");
         System.out.println("3. Sort Price Low to High");
         System.out.println("4. Sort Price High to Low");
         System.out.println("5. Exit");
         System.out.println(" ");
         System.out.print("Please enter option: ");

         String choice = "";
         try{
            choice = consoleInput.readLine();
         }catch(Exception e){ System.out.println(e.getMessage()); }

         switch(choice){
            case "1":
               break;
            case "2":
               break;
            case "3":
               break;
            case "4":
               break;
            case "5":
               exit = true;
               break;
            default:
               break;
         }
      }

   }

   public static void placeOrder(PizzaStore esql) {}
   public static void viewAllOrders(PizzaStore esql) {}
   public static void viewRecentOrders(PizzaStore esql) {}
   public static void viewOrderInfo(PizzaStore esql) {}
   public static void viewStores(PizzaStore esql) {}
   public static void updateOrderStatus(PizzaStore esql) {}
   public static void updateMenu(PizzaStore esql) {}
   public static void updateUser(PizzaStore esql) {}


}//end PizzaStore

