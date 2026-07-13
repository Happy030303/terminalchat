package version1TerminalChat.database;

import java.sql.Connection;
import java.sql.DriverManager;

// this class only has one task : to create and return "Connection" object...
public class DBConnection 
{

    private static final String URL = "jdbc:mysql://localhost:3306/terminalchat";
    private static final String USER = "root";
    private static final String PASSWORD = "mysql";

    public static Connection getConnection()  
    {
        try 
        {
            Connection con = DriverManager.getConnection( URL, USER, PASSWORD );
            return con;
        }
        catch(Exception e) {System.out.println("error in \"DBConnection\" class");e.printStackTrace(); return null;}
    }
}