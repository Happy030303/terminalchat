package onlineUserTerminalChat.loginpage;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Scanner;
import org.mindrot.jbcrypt.BCrypt;
import onlineUserTerminalChat.groupMsgServer.GroupUser;


// this class is responsible for login or create account for our platform : "Varta" aka "Vartagram"
public class TerminalLogin 
{
    private static Connection connection;
    private static final Scanner scan = new Scanner(System.in);

// _______________________________________________________________________________________________________________________________________________________

    public static void loginLogic()
    {
        System.out.println("\n\n                      Logging Page : \n\n");
        
        // enter these credenticals in terminal 
        System.out.print("enter username : ");                                                           // username
        String terminalUsername = scan.nextLine();
        System.out.print("enter password : ");                                                           // password
        String terminalPassword = scan.nextLine();


        
        try
        {
            //   search only by username
            String query ="SELECT username, hash_password FROM login WHERE username = ? LIMIT 1";
            PreparedStatement ps = connection.prepareStatement(query);                                    // create object of 'PreparedStatement'
            ps.setString(1, terminalUsername);                                                            // set the entered username in terminal into sql query                                                          // set the entered username in terminal into sql query
            ResultSet rs = ps.executeQuery();                                                             // it executes query and retuns the data from database acc. to the terminalUsername
            
            
            // checking the username and password are correct or not
            if(rs.next())
            {
                String storedHash = rs.getString("hash_password");
                // compare entered password with stored hash
                boolean validPassword = BCrypt.checkpw( terminalPassword, storedHash);

                if(validPassword)
                {   
                    System.out.println("login successful");
                    GroupUser.groupUser(terminalUsername);
                }
                else{System.out.println("invalid user or password");}
            }
            else{ System.out.println("\n                User Not Found..OR Wrong password");}
        }
        catch(Exception e) { System.out.println("exception in TerminalLogin.java"); e.printStackTrace();}              
    }
                
//___________________________________________________________________________________________________________________________________________________


    public static void createAccount() // now work here...
    {
        System.out.println("Creating New Account : \n\n");

        System.out.print("enter username : ");
        String username = scan.nextLine();
        System.out.print("enter password : ");
        String password = scan.nextLine();
        // convert password into  hashpassword
        String hashPassword = BCrypt.hashpw(password, BCrypt.gensalt());  


        try 
        {
            // this logic will check the database that this username exist or not...
            String query = "SELECT username FROM login WHERE username = ? LIMIT 1";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, username);                                                            // set the entered username in terminal into sql query
            ResultSet rs = ps.executeQuery();
        
            if(rs.next()==false) // username is not there, then insert the user
            {
                String query2 = "INSERT INTO login VALUES(DEFAULT, ?, ?, DEFAULT)";
                PreparedStatement ps1 = connection.prepareStatement(query2);
                ps1.setString(1, username);
                ps1.setString(2, hashPassword);
                int rows = ps1.executeUpdate();
                System.out.println("account is created successfully");
                System.out.println("number of rows changed : "+rows);
                System.out.println("Now verify your");
                loginLogic();
            }
            else
            {
                System.out.println("username already exist");
            }
        } 
        catch (Exception e) {System.out.println("i am ironman think there is some exception we faced");  e.printStackTrace();}
    }


// ____________________________________________________________________________________________________


    public static void main(String[] args) 
    {
// try block for connection only here...
    //__________________________________________________________________________________________________________________________________
        try 
        {
            // credential to connect with mysql
            String sqlURL = "jdbc:mysql://localhost:3306/terminalchat";
            String sqlUsername ="root";
            String sqlPassword = "mysql";
            // using JDBC to connect java code with database.
                connection = DriverManager.getConnection(sqlURL, sqlUsername, sqlPassword);
        
    // ________________________________________________________________________________________________________________________________________

            System.out.println("""
                                =========================
                                    TERMINAL CHAT
                                =========================
                                1. Login
                                2. Create Account
                                3. Exit
                                =========================
                                Enter Choice:
                            """);

            int choice = scan.nextInt();
            scan.nextLine();
            while(true) 
            {
                switch(choice)
                {
                    case 1: loginLogic();
                            break;
                    case 2: createAccount();
                            break;
                    case 3: System.exit(0);
                            break;
                    default: System.out.println("Invalid Choice");
                }
            }
        } 
        catch (Exception e){ System.out.println("i am ironman think there is some exception we faced"); e.printStackTrace();}
    }
//__________________________________________________________________________________________________________
}
