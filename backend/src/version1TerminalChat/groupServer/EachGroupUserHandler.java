package version1TerminalChat.groupServer;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;

import java.sql.Statement;
import version1TerminalChat.database.DBConnection;

public class EachGroupUserHandler implements Runnable
{
    static Connection  connection = DBConnection.getConnection();
    String username;
    BufferedReader br ;
    PrintWriter pw;
    String userAESKeyString;
    static HashMap<String, EachGroupUserHandler> hashMap;


    EachGroupUserHandler(BufferedReader br, PrintWriter pw, String username, HashMap<String, EachGroupUserHandler> hashMap)
    {
        this.br = br;
        this.pw = pw;
        this.username = username;
        this.hashMap = hashMap;
    }

    // private String getEncryptedAESKey()
    // {
    //     Admin admin = new Admin();
    //     userAESKeyString = admin.getAESKeyWhenUserJoinsGroup(username);
    //     if(userAESKeyString == null)
    //     {
    //         return null;
    //     }
    //     return userAESKeyString;
    // }

    //======================================================================================================
    
    private boolean isUserOnline(String username)
    {
        return hashMap.containsKey(username);
    }
    
    //======================================================================================================
    
    private void getOldMsgWhenUserWasOffline(EachGroupUserHandler userHandler)
    {
        try
        {
            // Step 1: Get all pending messages for this user
            String sql = "SELECT message_id FROM group_message_status WHERE receiver_name = ? AND status = 'PENDING'";

            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            while (rs.next())
            {
                int message_id = rs.getInt("message_id"); 

                // Step 2: Get the actual message
                PreparedStatement ps2 = connection.prepareStatement( "SELECT sender_name, encrypted_msg FROM group_messages WHERE message_id = ?");
                ps2.setInt(1, message_id);
                ResultSet rs2 = ps2.executeQuery();

                if (rs2.next())
                {
                    String sender = rs2.getString("sender_name");
                    String encryptedMsg = rs2.getString("encrypted_msg");

                    // Step 3: Send message to client(user)
                    userHandler.pw.println(sender);
                    userHandler.pw.println(encryptedMsg);

                    // Step 4: Mark as delivered
                    PreparedStatement ps3 = connection.prepareStatement("UPDATE group_message_status " +
                                                                            "SET status = 'DELIVERED' " +
                                                                            "WHERE message_id = ? AND receiver_name = ?");
                    ps3.setInt(1, message_id);
                    ps3.setString(2, username);
                    ps3.executeUpdate();
                }
            }
        }
        catch (Exception e)
        {
            System.out.println("Error : getOldMsgWhenUserWasOffline()");
            e.printStackTrace();
        }
    }
        
    //======================================================================================================
    
    private void saveUserMsgStatus(int message_id)
    {
        try
        {
            // get all the users name who has already joined this group
            PreparedStatement ps = connection.prepareStatement("SELECT username FROM users_in_group");
            ResultSet rs = ps.executeQuery();

            while (rs.next())
            {
                // put status = pending for all users....d
                String receiverName = rs.getString("username"); 
                String status = "PENDING";

                // If user is online make status = "DRLIVERED"
                if (hashMap.containsKey(receiverName)) 
                {
                    status = "DELIVERED";
                }

                PreparedStatement insert = connection.prepareStatement("INSERT INTO group_message_status(message_id, receiver_name, status) VALUES(?,?,?)");

                insert.setInt(1, message_id);
                insert.setString(2, receiverName);
                insert.setString(3, status);

                insert.executeUpdate();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    //======================================================================================================
    
    private void saveMsgToGroupDB(String senderName, String encryptedMsg)
    {
        try
        {
            String sql = "INSERT INTO group_messages(group_id, sender_name, encrypted_msg) VALUES(1, ?, ?)";

            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            ps.setString(1, senderName);
            ps.setString(2, encryptedMsg);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next())
            {
                int message_id = rs.getInt(1);

                // Create status row for every group member
                saveUserMsgStatus(message_id);
            }
        }
        catch (Exception e)
        {
            System.out.println("error : saveMsgToGroupDB()");
            e.printStackTrace();
        }
    }
    
    //======================================================================================================
    

    @Override
    public void run()
    {
        try
        {
            
            // storing user in hashmap, these are online
            hashMap.put(username, this);
            System.out.println(username+" is Online");
            System.out.println("Online users : "+hashMap.keySet());
            
            getOldMsgWhenUserWasOffline(this);
            
            while(true)
                {
                    String encryptMsg = br.readLine();
                    
                    // this loop will send msg to all the online users (users in hashMap)
                    for(EachGroupUserHandler recieverUserHandler : hashMap.values())
                    {
                        System.out.println("sending msg to : "+recieverUserHandler.username);
                        
                        // no other thread can switch until this peice of code is executed by this thread
                        synchronized(recieverUserHandler)
                        {
                            recieverUserHandler.pw.println(username);
                            recieverUserHandler.pw.println(encryptMsg);
                        }
                    }
                    // store msg in DB
                    saveMsgToGroupDB(username, encryptMsg); // storing msg in DB, with msg status for each user in grp.

                }
                    
                    
        }
        catch(Exception e){System.out.println("error in : EachGroupUserHandler.java class in run() method");e.printStackTrace();}

    }
}
