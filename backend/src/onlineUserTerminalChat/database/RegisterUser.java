package onlineUserTerminalChat.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

import org.mindrot.jbcrypt.BCrypt;
// demo code 
public class RegisterUser
{
    public static void main(String[] args)
    {
        String username = "happy";
        String password = "happy123";

        try
        {
            // Load MySQL Driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Connect Database
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/terminalchat","root","root");

            // Hash Password
            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

            System.out.println("Original Password : "+ password);

            System.out.println("Hashed Password : "+ hashedPassword);

            // Insert Query
            String query ="INSERT INTO login(username, password_hash) VALUES(?, ?)";

            PreparedStatement ps = connection.prepareStatement(query);

            ps.setString(1, username);
            ps.setString(2, hashedPassword);

            int rowsAffected = ps.executeUpdate();

            if(rowsAffected > 0)
            {
                System.out.println("User Saved Successfully");
            }

            ps.close();
            connection.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
}