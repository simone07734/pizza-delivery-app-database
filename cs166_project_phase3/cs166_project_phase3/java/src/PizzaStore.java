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
import java.text.DecimalFormat;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
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
            System.out.println("");
            System.out.println("MAIN MENU");
            System.out.println("---------");
            System.out.println("1. Create user");
            System.out.println("2. Log in");
            System.out.println("9. < EXIT");
            String authorisedUser = null;
            String userRole = null;
            List<String> userInfo = new ArrayList<>();
            switch (readChoice()){
               case 1: CreateUser(esql); break;
               case 2: 
                  userInfo = LogIn(esql);
                  authorisedUser = userInfo.get(0); 
                  userRole = userInfo.get(1);
                  break;
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
                if (userRole.contains("manager") || userRole.contains("driver")) {
                  System.out.println("9. Update Order Status");
                }

                //**the following functionalities should ony be able to be used by managers**
                if (userRole.contains("manager")) {
                  System.out.println("10. Update Menu");
                  System.out.println("11. Update User");
                }

                System.out.println(".........................");
                System.out.println("20. Log out");

                //System.out.println("User info" + userInfo);
                switch (readChoice()){
                   case 1: viewProfile(esql, authorisedUser); break;
                   case 2: updateProfile(esql, authorisedUser); break;
                   case 3: viewMenu(esql); break;
                   case 4: placeOrder(esql, authorisedUser); break;
                   case 5: viewAllOrders(esql, authorisedUser, userRole); break;
                   case 6: viewRecentOrders(esql, authorisedUser, userRole); break;
                   case 7: viewOrderInfo(esql, authorisedUser, userRole); break;
                   case 8: viewStores(esql); break;
                   case 9: 
                     if(userRole.contains("manager") || (userRole.contains("driver"))){
                        updateOrderStatus(esql);
                     }
                     else System.out.println("Unrecognized choice!");
                     break;
                   case 10: 
                     if(userRole.contains("manager")) updateMenu(esql); 
                     else System.out.println("Unrecognized choice!");
                     break;

                   case 11:
                     if(userRole.contains("manager")) updateUser(esql);
                     else System.out.println("Unrecognized choice!");
                     break;

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
   public static List<String> LogIn(PizzaStore esql){
      List<List<String>> queryResults = new ArrayList<>();
      BufferedReader consoleInput = new BufferedReader(new InputStreamReader(System.in));

      try{
         String login = "";
         boolean valid = false;

         while(!valid){
            System.out.print("Please enter login: ");
            login = consoleInput.readLine();
            queryResults = esql.executeQueryAndReturnResult("SELECT * FROM Users F WHERE F.login = \'" + login + "\'");
            System.out.println("-----------------------------------------");
            valid = queryResults.size() > 0;
            if(!valid){
               System.out.println("No user, please try again.");
               continue;
               //return null;
            }

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
      List<String> output = new ArrayList<>();
      output.add(queryResults.get(0).get(0));
      output.add(queryResults.get(0).get(2));
      return output;
   }//end
   

// Rest of the functions definition go in here

   public static void viewProfile(PizzaStore esql, String _login) {
      BufferedReader consoleInput = new BufferedReader(new InputStreamReader(System.in));
      String login = "";
      String choice = "";

      String desiredField = "";
      String newValue = "";

      boolean valid = false;
      List<List<String>> result = new ArrayList<>();

      try{

      result = esql.executeQueryAndReturnResult("SELECT * FROM Users F WHERE F.login = \'" + _login + "\'");
      //display user information
      
      System.out.println("-----------------------------------------");
      System.out.println("Current user fields");
      System.out.println("");

      System.out.println("Login: "+ result.get(0).get(0));
      System.out.println("Password: "+ "********");
      System.out.println("Role: "+ result.get(0).get(2));

      System.out.println("Favorite Item: "+ result.get(0).get(3));
      System.out.println("Phone Number: "+ result.get(0).get(4));

      System.out.println("-----------------------------------------");
      System.out.println("Options");
      System.out.println("1. Return to main menu");
      System.out.println("");
      while(!valid){
         System.out.println("-----------------------------------------");
         System.out.print("Desired field: ");
         choice = consoleInput.readLine();

         switch(choice){
            case "1":
               valid = true;
               break;
            default:
               valid = false;
               System.out.println("Please enter a valid choice.");
               break;
         }
         //ask which field to change
      }
      System.out.println("-----------------------------------------");
      //update accordingly
      }catch(Exception e){System.out.println(e.getMessage());}
   }

   public static void updateProfile(PizzaStore esql, String _login) {
      BufferedReader consoleInput = new BufferedReader(new InputStreamReader(System.in));
      String login = "";
      String choice = "";

      String desiredField = "";
      String newValue = "";

      boolean valid = false;
      List<List<String>> result = new ArrayList<>();

      try{

      result = esql.executeQueryAndReturnResult("SELECT * FROM Users F WHERE F.login = \'" + _login + "\'");
      //display user information
      
      System.out.println("-----------------------------------------");
      System.out.println("Current user fields");
      System.out.println("");

      System.out.println("Login: "+ result.get(0).get(0));
      System.out.println("Password: "+ "********");
      System.out.println("Role: "+ result.get(0).get(2));

      System.out.println("Favorite Item: "+ result.get(0).get(3));
      System.out.println("Phone Number: "+ result.get(0).get(4));

      System.out.println("-----------------------------------------");
      System.out.println("Which field would you like to change?");
      System.out.println("1. Password");
      System.out.println("2. Favorite Item");
      System.out.println("3. Phone Number");
      System.out.println("4. Return to Main Menu");
      System.out.println("");
      while(!valid){
         System.out.print("Desired field: ");
         choice = consoleInput.readLine();

         valid = true;
         switch(choice){
            case "1":
               desiredField = "password";
               break;
            case "2":
               desiredField = "favoriteItems";
               break;
            case "3":
               desiredField = "phoneNum";
               break;
            case "4":
               return;
            default:
               valid = false;
               System.out.println("Please enter a valid choice.");
               break;
         }
         //ask which field to change
      }
      System.out.println("-----------------------------------------");
      System.out.println("What would you like to change this field to?");
      System.out.println("");
      System.out.print("Desired value: ");
      newValue = consoleInput.readLine();

      esql.executeUpdate("UPDATE Users SET " + desiredField + " = \'" + newValue + "\' WHERE login = " + "\'"+ _login+"\'");
      System.out.println("-----------------------------------------");
      System.out.println("Profile successfully updated. Returning to main menu...");
      //update accordingly
      }catch(Exception e){System.out.println(e.getMessage());}
   }

   public static void viewMenu(PizzaStore esql) {
      boolean exit = false;
      boolean filterByType = false;
      int maxPrice = Integer.MAX_VALUE;
      String type = "";
      int sort = -1; //-1 for no sort, 0 for low to high, 1 for high to low
      BufferedReader consoleInput = new BufferedReader(new InputStreamReader(System.in));

      while (!exit){
         //Basic menu feedback UI
         System.out.println("Pizza Menu");
         System.out.println("-----------------------------------------");

         try{
            List<List<String>> items = new ArrayList<>();
            String query = "SELECT * FROM Items F WHERE F.price < " + maxPrice;
            if(filterByType) query += " AND F.typeOfItem LIKE \'%" + type + "\'"; // account for a possible leading space

            if(sort != -1) query += " ORDER BY F.price";
            if(sort == 1) query += " DESC";

            //System.out.println(query);
            items = esql.executeQueryAndReturnResult(query);

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
         System.out.println("5. Clear Filters");
         System.out.println("6. Exit");
         System.out.println(" ");
         System.out.print("Please enter option: ");

         String choice = "";
         try{
            choice = consoleInput.readLine();
         }catch(Exception e){ System.out.println(e.getMessage()); }

         switch(choice){
            case "1":

               try{
                  String input = "";
                  boolean valid = false;

                  //while(!valid){
                     System.out.println("-----------------------------------------");
                     // System.out.print("Please enter an item type. Valid selections are 'entree' 'drinks' or 'sides': ");
                     System.out.print("Please enter an item type. ('entree,' 'drinks,' 'sides,' etc): ");
                     input = consoleInput.readLine();
                     // valid = input.equals("entree") || input.equals("drinks") || input.equals("sides");
                     // if(!valid) System.out.println("Please enter a valid item type.");
                  //}
                  filterByType = true;
                  type = input;
               }catch(Exception e){}


               break;
            case "2":

               try{
                  String input = "";
                  boolean valid = false;
                  while(!valid){
                     System.out.println("-----------------------------------------");
                     System.out.print("Please enter a maximum price: ");
                     input = consoleInput.readLine();
                     valid = isNumeric(input);
                     if(!valid) System.out.println("Please enter a valid price.");
                  }
                  maxPrice = Integer.parseInt(input);
               }catch(Exception e){}

               break;
            case "3":
               sort = 0;
               break;
            case "4":
               sort = 1;
               break;
            case "5":
               filterByType = false;
               type = "";
               sort = -1;
               maxPrice = Integer.MAX_VALUE;
               break;
            case "6":
               exit = true;
               break;
            default:
               break;
         }
      }

   }

   public static void placeOrder(PizzaStore esql, String _login) {
      /* An order needs the following:
         * orderID
         * login
         * storeID
         * totalPrice
         * timestamp
         * status
         * itemName of each item
         * quantity of each item
      */
     DecimalFormat df = new DecimalFormat("##.##");
      df.setRoundingMode(RoundingMode.DOWN); 

      boolean valid = false;
      boolean distinctItem = true;
      boolean doneOrdering = false;
      int orderID = 0;
      String storeID = "";
      float totalPrice = 0.00f;
      String status = "incomplete";
      ArrayList<String> itemNames = new ArrayList<String>();
      ArrayList<Integer> itemQuantities = new ArrayList<Integer>();
      String newItem = "";
      int newQuantity = 0;
      int itemCount = 0;
      
   
      // get the storeID and check for validity
      System.out.println("-----------------------------------------");
      System.out.print("Enter the storeID of the store you want to order from: ");
      try{
         storeID = in.readLine();
         valid = esql.executeQuery("SELECT S.storeID FROM Store S WHERE S.storeID  = \'" + storeID + "\'") > 0;
         if(!valid) {
            System.out.println("That store does not exist or is not available. Returning to main menu.");
            System.out.println("-----------------------------------------\n");
            return;
         }
      }catch(Exception e){ System.out.println(e.getMessage()); }

      while(!doneOrdering) {
         // display current order and price
         
         System.out.println("Current order: ");
         for (int i = 0; i < itemCount; i++) {
            System.out.println(itemQuantities.get(i) + " " + itemNames.get(i));
         }
         System.out.println("");
         System.out.println("Order total: $" + df.format(totalPrice));
         System.out.println("-----------------------------------------");
         
         // add item, place order, or cancel order
         System.out.println("What would you like to do?");
         System.out.println("1. Add item(s) to order");
         System.out.println("2. Send order");
         System.out.println("3. Cancel order");
         System.out.println("");
         
         try{
            switch(readChoice()) {
               case 1:
                  // get the itemName and check that it is offered
                  valid = false;
                  System.out.print("Item name: ");
                  newItem = in.readLine();
                  valid =  esql.executeQuery("SELECT T.itemName FROM Items T WHERE T.itemName  = \'" + newItem + "\'") > 0;
                  if(!valid) {
                     System.out.println("That item does not exist or is not available.");
                     System.out.println("-----------------------------------------");
                     break;
                  }
                  
                  // get item quantity
                  System.out.print("Quantity of " + newItem + ": ");
                  newQuantity = Integer.parseInt(in.readLine());
                  if(newQuantity < 1) {
                     System.out.println("Invalid amount. You must order at least 1 of this to add it to your order.");
                     break;
                  }

                  // if item of that name already is in the order, just add to it's quantity
                  distinctItem = true;
                  for (int i = 0; i < itemCount; i++) {
                     if(itemNames.get(i).equals(newItem)) {
                        itemQuantities.set(i, itemQuantities.get(i) + newQuantity);
                        distinctItem = false;
                     }
                  }

                  // add item and quantity to order, and update total price
                  if (distinctItem) {
                     itemNames.add(newItem);
                     itemQuantities.add(newQuantity);
                     itemCount++;
                  }

                  totalPrice += newQuantity * Float.parseFloat(esql.executeQueryAndReturnResult("SELECT T.price "
                                 + "FROM Items T WHERE T.itemName  = \'" + newItem + "\'").get(0).get(0));
                  break;
               case 2:
                  doneOrdering = true;
                  break;
               case 3:
                  System.out.println("Are you sure you want to cancel your order? (y/n)");
                  if(in.readLine().equals("y")) { return; }
                  break;
               default:
                  System.out.println("Not an option. Please try again.");
                  break;
            }
         }catch(Exception e){ System.out.println(e.getMessage()); }
      }

      try{
         // generate a unique order id
         Random rand = new Random();
         valid = false;
         while(!valid) {
            orderID = rand.nextInt(2000000000);
            valid = esql.executeQuery("SELECT R.orderID FROM FoodOrder R WHERE R.orderID  = \'" + orderID + "\'") == 0;
         }

         // generate timestamp
         long now = System.currentTimeMillis();
         Timestamp orderTimestamp = new Timestamp(now);

         // insert order into FoodOrder
         esql.executeUpdate("INSERT INTO FoodOrder VALUES (" + "\'" + orderID + "\', \'" + _login + "\', \'" + storeID
            + "\', \'" + totalPrice + "\', \'" + orderTimestamp + "\', \'incomplete')");

         // insert into ItemsIn Order each item with correct order id
         for (int i = 0; i < itemCount; i++) {
            esql.executeUpdate("INSERT INTO ItemsInOrder VALUES (" + "\'" + orderID + "\', \'" + itemNames.get(i) + "\', \'" + itemQuantities.get(i)+ "\')");
         }  
      }catch(Exception e){ System.out.println(e.getMessage()); }

      // helpful message about the order being placed
      System.out.println("\nWe received your order!\n");

   }

   public static void viewAllOrders(PizzaStore esql, String _login, String _role) {
      String orderQuery = "SELECT R.orderID FROM FoodOrder R ";
      
      System.out.println("-----------------------------------------");
      if(_role.contains("manager") || _role.contains("driver")) {
         orderQuery += " ORDER BY R.orderTimestamp DESC";
         System.out.println("All orders from most recent to least recent");
         try{
         esql.executeQueryAndPrintResult(orderQuery);
         }catch(Exception e){System.out.println(e.getMessage());}
      }
      else {
         orderQuery += "WHERE R.login = \'" + _login + "\' ORDER BY R.orderTimestamp DESC";
         System.out.println("Your order history from most recent to least recent");
         try{
         esql.executeQueryAndPrintResult(orderQuery);
         }catch(Exception e){System.out.println(e.getMessage());}
      }
      System.out.println("-----------------------------------------");
   }

   public static void viewRecentOrders(PizzaStore esql, String _login, String _role) {
      String orderQuery = "SELECT R.orderID FROM FoodOrder R ";
      
      System.out.println("-----------------------------------------");
      if(_role.contains("manager") || _role.contains("driver")) {
         orderQuery += " ORDER BY R.orderTimestamp DESC LIMIT 5";
         System.out.println("Five most recent orders");
         try{
         esql.executeQueryAndPrintResult(orderQuery);
         }catch(Exception e){System.out.println(e.getMessage());}
      }
      else {
         orderQuery += "WHERE R.login = \'" + _login + "\' ORDER BY R.orderTimestamp DESC LIMIT 5";
         System.out.println("Your five most recent orders");
         try{
         esql.executeQueryAndPrintResult(orderQuery);
         }catch(Exception e){System.out.println(e.getMessage());}
      }
      System.out.println("-----------------------------------------");
   }

   public static void viewOrderInfo(PizzaStore esql, String _login, String _role) {
      /* They should be able to see their orderTimestamp, totalPrice, orderStatus, and list of
      items in that order (along with the quantity). */
      String orderID = "";
      int valid = 0;
      String orderQuery = "SELECT R.orderTimestamp, R.totalPrice, R.orderstatus FROM FoodOrder R WHERE R.orderID = \'";
      String orderItemsQuery = "SELECT N.itemName, N.quantity FROM ItemsInOrder N WHERE N.orderID = \'";
      
      // get orderID
      System.out.println("-----------------------------------------");
      System.out.println("Enter orderID: ");
      try{ orderID = in.readLine(); }
      catch(Exception e){ System.out.println(e.getMessage()); }
      orderQuery += orderID;
      orderQuery += "\'";
      
      // customers can only see their own orders
      if(!_role.contains("manager") && !_role.contains("driver")) {
         orderQuery += " AND login = \'" + _login + "\'";
      }

      // print order
      System.out.println("");
      try{ valid = esql.executeQueryAndPrintResult(orderQuery); }
      catch(Exception e){ System.out.println(e.getMessage()); }
      if(valid <= 0) {
         System.out.println("no orders with that ID available");
         System.out.println("-----------------------------------------");
         return;
      }
      System.out.println("");

      // print the items in the order
      orderItemsQuery += orderID;
      orderItemsQuery += "\'";
      try { esql.executeQueryAndPrintResult(orderItemsQuery); }
      catch(Exception e){ System.out.println(e.getMessage()); }
      System.out.println("-----------------------------------------");
   }

   public static void viewStores(PizzaStore esql) {
      BufferedReader consoleInput = new BufferedReader(new InputStreamReader(System.in));
      List<List<String>> results = new ArrayList<>();
      String input = "";
      boolean exit = false;
      while(!exit){
         System.out.println("All Stores");
         System.out.println("-----------------------------------------");

         try{
            String query = "SELECT * FROM Store F";
            results = esql.executeQueryAndReturnResult(query);

            for(List<String> result : results){
               System.out.print(result.get(0) + " ");
               System.out.print(result.get(1) + ", ");
               System.out.print(result.get(2) + ", ");
               System.out.print(result.get(3) + " ");
               System.out.print(result.get(4).equals("yes") ? "Open" : "Closed");
               System.out.print("\n");
            }

            System.out.println("-----------------------------------------");
            System.out.println("Return to Main Menu? (y): ");
            input = consoleInput.readLine();
            exit = input.equals("y");

         }catch(Exception e){System.out.println(e.getMessage());}
         
      }
   }

   public static void updateOrderStatus(PizzaStore esql) {
      BufferedReader consoleInput = new BufferedReader(new InputStreamReader(System.in));
      List<List<String>> order = new ArrayList<>();
      String query = "SELECT * FROM FoodOrder F WHERE F.orderID = ";
      String orderID = "";


      //enter orderID
      System.out.println("-----------------------------------------");
      System.out.println("Please enter the order ID that you wish to change the status of.");
      System.out.print("Order ID: ");
      try{
         orderID = consoleInput.readLine();
         query += orderID;
         order = esql.executeQueryAndReturnResult(query);
         System.out.println("-----------------------------------------");
         System.out.println("Current order status");
         System.out.println("");
         for(String item : order.get(0)){
            System.out.print(item + " ");
         }

         System.out.println("");
         boolean valid = false;
         String newStatus = "";

         while(!valid){
            System.out.println("-----------------------------------------");
            System.out.print("Enter new status, incomplete (i) or complete (c): ");

            String choice = consoleInput.readLine();
            switch(choice){
               case "i":
                  newStatus = "incomplete";
                  valid = true;
                  break;
               case "c":
                  newStatus = "complete";
                  valid = true;
                  break;
               default:
                  System.out.println("Unrecognized choice, please try again.");
                  valid = false;
                  break;
            }
         }

         //Update
         esql.executeUpdate("UPDATE FoodOrder SET orderStatus = \'" + newStatus + "\' WHERE orderID = " + order.get(0).get(0));
         System.out.println("-----------------------------------------");
         System.out.println("Order status updated. Returning to main menu...");
         

      }catch(Exception e){System.out.println(e.getMessage());}
   }

   public static void updateMenu(PizzaStore esql) {
      System.out.println("\n-----------------------------------------");
      System.out.println("What would you like to do?");
      System.out.println("1. Update item information");
      System.out.println("2. Add new item");
      System.out.println("3. Go back to main menu");
      System.out.println("");

      try{
         switch(readChoice()) {
            case 1:
               updateItem(esql);
               break;
            case 2:
               addItem(esql);
               break;
            case 3: return;
            default:
               System.out.println("Not an option. Returning to main menu.");
               return;
         }
      }catch(Exception e){System.out.println(e.getMessage());}
   }

   public static void updateItem(PizzaStore esql) {
      String itemName = "";
      String fieldName = "";
      String fieldContent = "";
      List<List<String>> queryResults = new ArrayList<>();
      String matchNameQuery = "SELECT T.itemName FROM Items T WHERE T.itemName = \'";
      String updateStatement = "UPDATE Items SET ";

      // find the item
      System.out.print("Name of item to update: ");
      try { 
         itemName = in.readLine();
         matchNameQuery += itemName;
         matchNameQuery += "\'";
         queryResults = esql.executeQueryAndReturnResult(matchNameQuery);
      }catch(Exception e){System.out.println(e.getMessage());}
      if (queryResults.size() == 0) { System.out.println("Sorry, that item does not exist."); return; }

      // pick the field
      System.out.println("Pick a field to edit:");
      System.out.println("1. Ingredients");
      System.out.println("2. Type");
      System.out.println("3. Price");
      System.out.println("4. Description");
      System.out.println("5. Exit");
      System.out.println("");

      switch(readChoice()) {
         case 1:
            updateStatement += "ingredients ";
            fieldName = "ingredients";
            break;
         case 2:
            updateStatement += "typeOfItem ";
            fieldName ="typeOfItem";
            break;
         case 3:
            updateStatement += "price ";
            fieldName = "price";
            break;
         case 4:
            updateStatement += "description ";
            fieldName = "description";
            break;
         case 5: return;
         default:
            System.out.println("Not an option. Returning to main menu.");
            return;
      }

      // enter the updated information
      updateStatement += " = \'";
      System.out.print("New value for " + fieldName + ": ");
      try { fieldContent = in.readLine(); }
      catch(Exception e){System.out.println(e.getMessage());}
      updateStatement += (fieldContent + "\' WHERE itemName = \'" + itemName + "\'");

      // update database
      try { esql.executeUpdate(updateStatement); }
      catch(Exception e){System.out.println(e.getMessage());}
   }

   public static void addItem(PizzaStore esql) {
      String itemName = "";
      String fieldContent = "";
      List<List<String>> queryResults = new ArrayList<>();
      String matchNameQuery = "SELECT T.itemName FROM Items T WHERE T.itemName = \'";
      String insertQuery = "INSERT Into Items VALUES (\'";

      // find the item
      System.out.print("What is the name of the new item?: ");
      try { 
         itemName = in.readLine();
         matchNameQuery += itemName;
         matchNameQuery += "\'";
         queryResults = esql.executeQueryAndReturnResult(matchNameQuery);
      }catch(Exception e){System.out.println(e.getMessage());}
      if (queryResults.size() > 0) { System.out.println("An item with that name already exists."); return; }

      // get attributes of item
      insertQuery += (itemName + "\', \'");
      System.out.println("Enter the attributes of " + itemName);
      try{
      System.out.print("Ingredients: ");
      insertQuery += (in.readLine() + "\', \'");
      System.out.print("Type: ");
      insertQuery += (in.readLine() + "\', \'");
      System.out.print("Price in dollars: $");
      insertQuery += (in.readLine() + "\', \'");
      System.out.print("Description: ");
      insertQuery += (in.readLine() + "\')");
      System.out.println("");
      }catch(Exception e){System.out.println(e.getMessage());}

      System.out.println(insertQuery);

      // insert item into database
      try { esql.executeUpdate(insertQuery); }
      catch(Exception e){System.out.println(e.getMessage());}
   }

   public static void updateUser(PizzaStore esql) {
      
      String login = "";
      String choice = "";

      String desiredField = "";
      String newValue = "";

      boolean valid = false;
      List<List<String>> result = new ArrayList<>();

      try{
      //enter login for a user
      System.out.println("-----------------------------------------");
      System.out.print("Please enter the user's login: ");
      login = in.readLine();

      result = esql.executeQueryAndReturnResult("SELECT * FROM Users F WHERE F.login = \'" + login + "\'");
      //display user information
      while(!valid){
         System.out.println("-----------------------------------------");
         System.out.println("Current user fields");
         System.out.println("");

         System.out.println("Login: "+ result.get(0).get(0));
         System.out.println("Password: "+ result.get(0).get(1));
         System.out.println("Role: "+ result.get(0).get(2));

         System.out.println("Favorite Item: "+ result.get(0).get(3));
         System.out.println("Phone Number: "+ result.get(0).get(4));

         System.out.println("-----------------------------------------");
         System.out.println("Which field would you like to change?");
         System.out.println("1. Login");
         System.out.println("2. Password");
         System.out.println("3. Role");
         System.out.println("4. Favorite Item");
         System.out.println("5. Phone Number");
         System.out.println("6. Return to Main Menu");
         System.out.println("");
         System.out.println("Desired field: ");
         choice = in.readLine();

         valid = true;
         switch(choice){
            case "1":
               desiredField = "login";
               break;
            case "2":
               desiredField = "password";
               break;
            case "3":
               desiredField = "role";
               break;
            case "4":
               desiredField = "favoriteItems";
               break;
            case "5":
               desiredField = "phoneNum";
               break;
            case "6":
               valid = true;
               return;
            default:
               valid = false;
               System.out.println("Please enter a valid choice.");
               break;
         }
         //ask which field to change
      }
      System.out.println("-----------------------------------------");
      System.out.println("What would you like to change this field to?");
      System.out.println("");
      System.out.print("Desired value: ");
      newValue = in.readLine();

      esql.executeUpdate("UPDATE Users SET " + desiredField + " = \'" + newValue + "\' WHERE login = " + "\'" + login + "\'");
      System.out.println("-----------------------------------------");
      System.out.println("Order status updated. Returning to main menu...");
      //update accordingly
      }catch(Exception e){System.out.println(e.getMessage());}
   }

   public static boolean isNumeric(String str) { //Helper function. Matches regex
      return str.matches("-?\\d+(\\.\\d+)?");
   }
}//end PizzaStore

