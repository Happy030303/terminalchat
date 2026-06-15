package loginpage;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Scanner;

public class TerminalLogin 
{
    private static Connection connection;
    private static final Scanner scan = new Scanner(System.in);

    private static void loginLogic()
    {
        System.out.println("Logging In : ");


        // enter these credenticals in terminal 
        System.out.print("enter username : ");                                                           // username
        String terminalUsername = scan.nextLine();
        System.out.print("enter password : ");                                                           // password
        String terminalPassword = scan.nextLine();

        try
        {
            String query = "SELECT username, password FROM login WHERE username = ? LIMIT 1";
            PreparedStatement ps = connection.prepareStatement(query);                                    // create object of 'PreparedStatement'
            ps.setString(1, terminalUsername);                                                            // set the entered username in terminal into sql query
            ResultSet rs = ps.executeQuery();                                                             // it executes query and retuns the data from database acc. to the terminalUsername


// checking the username and password are correct or not
            if(rs.next())
            {
                if(terminalUsername.equals(rs.getString("username")) && terminalPassword.equals(rs.getString("password")))
                {   
                        System.out.println("username found");
                        System.out.println("password found");
                        System.out.print("enter Room-Name you want to enter : ");
                        String roomName = scan.nextLine();
                        accessToRooms(roomName);

                }
            }
            else
            {
                System.out.println("User Not Found...");
            }
        }
        catch(Exception e)
        {
            System.out.println("i am ironman think there is some exception we faced");
            e.printStackTrace();
        }    
        
    }


    private static void accessToRooms(String roomName)
    {
        System.out.println("room name iis : "+roomName);
    }
    

    private static void createAccount() // now work here...
    {

        System.out.println("Creating New Account : \n\n");

        System.out.print("enter username : ");
        String username = scan.nextLine();
        System.out.print("enter password : ");
        String password = scan.nextLine();

        try 
        {
            String query = "SELECT username FROM login WHERE username = ? LIMIT 1";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, username);                                                            // set the entered username in terminal into sql query
            ResultSet rs = ps.executeQuery();

                
            if(rs.next()==false) // username is not there, then insert the user
            {
                String query2 = "INSERT INTO login VALUES(DEFAULT, ?, ?, DEFAULT, DEFAULT)";
                PreparedStatement ps1 = connection.prepareStatement(query2);
                ps1.setString(1, username);
                ps1.setString(2, password);
                int rows = ps1.executeUpdate();
                System.out.println("account is created");
                System.out.println("number of rows changed : "+rows);
            }
            else
            {
                System.out.println("username already exist");
            }


        } 
        catch (Exception e) 
        {
            System.out.println("i am ironman think there is some exception we faced");
            e.printStackTrace();
        }
        
    }
    public static void main(String[] args) 
    {
        // try block for connection only here...
// ...............................................................................................

        try 
        {
            // credential to connect with mysql
            String sqlURL = "jdbc:mysql://localhost:3306/terminalchat";
            String sqlUsername ="root";
            String sqlPassword = "mysql";
            // using JDBC to connect java code with database.
                connection = DriverManager.getConnection(sqlURL, sqlUsername, sqlPassword);
        } 
        catch (Exception e) 
        {
            System.out.println("i am ironman think there is some exception we faced");
            e.printStackTrace();
        }
// ...............................................................................................

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
        switch(choice)
        {
            case 1: loginLogic();
                    break;
            case 2: createAccount();
                    break;
            case 3: System.exit(0);
                    break;
            default:
                    System.out.println("Invalid Choice");
        }

    }
}
