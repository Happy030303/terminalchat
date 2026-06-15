package individualMsg;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

// this class will just make UserHandler obj , the object will store socket, listener(), writer(pw), sender's name(senderName).
public class delete implements Runnable {
    Socket socket;
    BufferedReader br;
    PrintWriter pw;
    String senderName;

    UserHandler(Socket socket, String senderName, BufferedReader br)  // constructor
    {
        try 
        {
            this.socket = socket; // Constructor me received socket store kar rahe hain
            this.senderName = senderName;
            this.br = br ;  // Client se messages read karne ke liye
            pw = new PrintWriter(socket.getOutputStream(), true);
        } 
        catch (Exception e) { System.out.println("exception aayi hai");}
    }

    public void sendMsgToReciever(String recieverName, String msg) // sending msg to reciver 
    {
        if(MyServer.hashmap.get(recieverName) == null)
        {
            System.out.println("user not found in hashmap");
            return;
        }
        
        MyServer.hashmap.get(recieverName).pw.println(recieverName);
        MyServer.hashmap.get(recieverName).pw.println(msg);
    }

    // Database se username ke basis par user_id fetch karne ka helper method
    private int getUserId(Connection connection, String username) {
        String query = "SELECT user_id FROM users WHERE username = ? LIMIT 1";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("user_id");
                }
            }
        } catch (Exception e) {
            System.out.println("Error fetching user_id for: " + username);
            e.printStackTrace();
        }
        return -1;
    }

    // Connects to the database and inserts a message record
    public void saveMsgToDataBase(Connection connection, int senderId, int receiverId, String msg, int delivered) {
        String query = "INSERT INTO messages (sender_id, receiver_id, message, delivered) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, senderId);
            ps.setInt(2, receiverId);
            ps.setString(3, msg);
            ps.setInt(4, delivered);
            ps.executeUpdate();
            System.out.println("Message saved to database successfully.");
        } catch (Exception e) {
            System.out.println("Error saving message to database:");
            e.printStackTrace();
        }
    }

    @Override
    public void run() 
    {
        Connection connection = null;
        try 
        {
            // Database Connection Credentials
            String sqlURL = "jdbc:mysql://localhost:3306/terminalchat";
            String sqlUsername = "root";
            String sqlPassword = "mysql";
            connection = DriverManager.getConnection(sqlURL, sqlUsername, sqlPassword);

            String recieverName = br.readLine();
            System.out.println("sender's Name : " + senderName);
            System.out.println("reciever Name : " + recieverName);

            // Chat start hone par hi user_id query kar ke store kar lenge (highly efficient)
            int senderId = getUserId(connection, senderName);
            int receiverId = getUserId(connection, recieverName);

            while(true)
            {
                String msg = br.readLine();
                if (msg == null) {
                    break; // Client disconnected (null control check to prevent infinite loop)
                }
                
                sendMsgToReciever(recieverName, msg);

                // Check if receiver is online to set delivered state
                int delivered = (MyServer.hashmap.containsKey(recieverName)) ? 1 : 0;

                // Dono user_id database me hone par message save hoga
                if (senderId != -1 && receiverId != -1) {
                    saveMsgToDataBase(connection, senderId, receiverId, msg, delivered);
                } else {
                    System.out.println("Failed to save message: Sender or Receiver ID not found in database.");
                }
            }

        } 
        catch (Exception e) 
        {
            System.out.println("exception recieved. . .");
            e.printStackTrace();
        }
        finally
        {
            // Thread khatam hone par connection clean/close ho jayega
            try {
                if (connection != null && !connection.isClosed()) {
                    connection.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
