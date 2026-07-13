package version1TerminalChat.individualServerNew;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import version1TerminalChat.database.DBConnection;


// this class is server side which handles the users, 
// this class will just make UserHandler obj , the object will store socket, listener(), writer(pw), sender's name(senderName).
public class IndividualUserHandler implements Runnable 
{
    static HashMap<String, IndividualUserHandler> hashMap = new HashMap<String, IndividualUserHandler>();

    Socket socket;
    BufferedReader br;
    PrintWriter pw;

    String senderName;
    private String other_user_public_rsa_key_String;
    private Connection connection = DBConnection.getConnection();

//==========================================================================================================================

    // constructor
    IndividualUserHandler(Socket socket, String senderName, BufferedReader br)  
    {
        try 
        {
            this.connection = DBConnection.getConnection();
            this.socket = socket; // Constructor me received socket store kar rahe hain
            this.senderName = senderName;
            this.br = br ;  // Client se messages read karne ke

            pw = new PrintWriter(socket.getOutputStream(), true);
        } 
        catch(Exception e) { System.out.println("exception aayi hai");}
    }

//==================================================================================================================

   // checks the username in platform db or not
   private boolean checkUserIsAvilableInDb(String senderName)
    {
        try
        {
            PreparedStatement ps = connection.prepareStatement("SELECT 1 FROM login WHERE username = ? LIMIT 1 ");
            ps.setString(1, senderName);
            ResultSet rs = ps.executeQuery();
            return rs.next();           // true if user exists , false otherwise
        }
        catch(Exception e){System.out.println("error ayi hai");e.printStackTrace();}
        return false;
    }

//==================================================================================================================
    
    private static boolean checkUserIsOnlineOrNot(String senderName)
    {
        boolean result =  hashMap.containsKey(senderName) ;
         return result;
    }
    
//==================================================================================================================

    // saving msg to DB (mymessages)
    private void saveMsgToDataBase(String senderName, String recieverName, String encryptedMessage, String encryptedAESkey, boolean isOnline)
    {
        try
        {
            System.out.println("saveMSgToDB...");
            // Connection connection = DBConnection.getConnection();
                
            PreparedStatement ps = connection.prepareStatement( "INSERT INTO individual_server_messages(sender_name, receiver_name, encrypted_message, encrypt_AES_key, other_user_public_rsa_key, is_delivered ) VALUES (?, ?, ?, ?, ?, ?)" );

            ps.setString(1, senderName);
            ps.setString(2, recieverName);
            ps.setString(3, encryptedMessage);
            ps.setString(4, encryptedAESkey);
            ps.setString(5, other_user_public_rsa_key_String);
            ps.setBoolean(6, isOnline);

            int rows = ps.executeUpdate();

            System.out.println(rows + " row inserted");

        }
        catch(Exception e) { System.out.println("Exception while saving message"); e.printStackTrace();}
    }

//==========================================================================================================================

    private void getOldMsgWhenUserWasOffline(String receiverName, IndividualUserHandler receiverHandlerObj)
    {
        String sql ="SELECT chat_id, encrypted_message, encrypt_AES_key FROM individual_server_messages " +
                        "WHERE receiver_name = ? AND is_delivered = false " +
                        "ORDER BY chat_id ASC";

        try
        {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, receiverName);
            ResultSet rs = ps.executeQuery();
            while (rs.next())
            {
                int chatId = rs.getInt("chat_id");
                String encryptedMsg = rs.getString("encrypted_message");
                String encryptedAESKey = rs.getString("encrypt_aes_key");
                receiverHandlerObj.pw.println(encryptedMsg);
                receiverHandlerObj.pw.println(encryptedAESKey);

                // Mark this message as delivered
                markMessageDelivered(chatId);
            }
        }
        catch (SQLException e){System.out.println("Error in getOldMsgWhenUserWasOffline()");e.printStackTrace();}
    }

//==========================================================================================================================

    private void markMessageDelivered(int chatId)
    {
        String sql = "UPDATE individual_server_messages " +
                        "SET is_delivered = true " +
                        "WHERE chat_id = ?";

        try
        {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, chatId);
            ps.executeUpdate();
        }
        catch (SQLException e){System.out.println("Error aayi : markMessageDelivered() method ");e.printStackTrace();}
    }

//==========================================================================================================================
    
   @Override
    public void run()
    {
        try
        {
            System.out.println("Sender : " + senderName);

            if(!checkUserIsAvilableInDb(senderName))
            {
                System.out.println("User not found.");
                return;
            }
            // put sender name in hashmap (hashmap will show us the online users...)
            hashMap.put(senderName, this);

            System.out.println(senderName + " is online");
            System.out.println("Online users : " + hashMap.keySet());

            // get all the old messages from DB
            getOldMsgWhenUserWasOffline(senderName, this);

            // Receiver's username (chosen once)
            String receiverName = br.readLine();

            while(true)
            {
                // Read one encrypted message
                String encryptedMsg = br.readLine();
                String encryptedAESKey = br.readLine();

                if(checkUserIsOnlineOrNot(receiverName))
                {
                    // getting reciever Handler object from hashmap, so that we can send data to reciever
                    IndividualUserHandler receiverHandler = hashMap.get(receiverName);
                    
                    // this is sending data to reciever...
                    receiverHandler.pw.println(encryptedMsg);
                    receiverHandler.pw.println(encryptedAESKey);

                    saveMsgToDataBase(senderName,receiverName,encryptedMsg,encryptedAESKey,true);
                }
                else
                {
                    // Receiver is offline
                    saveMsgToDataBase(senderName,receiverName,encryptedMsg,encryptedAESKey,false);
                }
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            hashMap.remove(senderName);

            try
            {
                socket.close();
            }
            catch(Exception e){}
        }
    }

}

