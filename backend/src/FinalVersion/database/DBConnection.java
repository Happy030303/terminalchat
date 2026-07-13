package database;

import java.sql.Connection;
import java.sql.DriverManager;

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
        catch(Exception e) {

            e.printStackTrace();
            return null;

        }
    }
}