package onlineUserTerminalChat.individualMsgServer;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import onlineUserTerminalChat.database.DBConnection;

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
    private Connection connection;

//____________________________________________________________________________________________________
//____________________________________________________________________________________________________
    
    IndividualUserHandler(Socket socket, String senderName, String otherUserPublicRsaKeyString, BufferedReader br)  // constructor
    {
        try 
        {
            this.connection = DBConnection.getConnection();
            this.socket = socket; // Constructor me received socket store kar rahe hain
            this.senderName = senderName;
            this.other_user_public_rsa_key_String = otherUserPublicRsaKeyString;
            this.br = br ;  // Client se messages read karne ke
                                                                                     // liye
            pw = new PrintWriter(socket.getOutputStream(), true);

        } 
        catch (Exception e) { System.out.println("exception aayi hai");}
    }

// ____________________________________________________________________________________________________________________________
// ____________________________________________________________________________________________________________________________

   // checks the username in Db
   public boolean checkUserIsAvilableInDb(String senderName)
    {
        try
        {
            PreparedStatement ps = connection.prepareStatement("SELECT 1 FROM users_in_group WHERE username = ? LIMIT 1 ");
            ps.setString(1, senderName);
            ResultSet rs = ps.executeQuery();
            return rs.next(); // true if user exists , false otherwise
        }
        catch(Exception e){System.out.println("error ayi hai");e.printStackTrace();}
        return false;
    }


//____________________________________________________________________________________________________________________________
//____________________________________________________________________________________________________________________________
    
    static boolean checkUserIsOnlineOrNot(String senderName)
    {
        boolean result =  hashMap.containsKey(senderName) ;
         return result;
    }

//____________________________________________________________________________________________________________________________
//____________________________________________________________________________________________________________________________

    // saving msg to DB (mymessages)
    public void saveMsgToDataBase(String senderName, String recieverName, String encryptedMessage, String encryptedAESkey)
    {
        try
        {
            // Connection connection = DBConnection.getConnection();
            PreparedStatement ps = connection.prepareStatement( "INSERT INTO individual_server_messages(sender_name, receiver_name, encrypted_message, encrypt_AES_key, other_user_public_rsa_key ) VALUES (?, ?, ?, ?, ?)" );

            ps.setString(1, senderName);
            ps.setString(2, recieverName);
            ps.setString(3, encryptedMessage);
            ps.setString(4, encryptedAESkey);
            ps.setString(5, other_user_public_rsa_key_String);

            int rows = ps.executeUpdate();

            System.out.println(rows + " row inserted");

        }
        catch(Exception e) { System.out.println("Exception while saving message"); e.printStackTrace();}
    }

// ____________________________________________________________________________________________________________________________
// ____________________________________________________________________________________________________________________________
    
    //  sending msg to reciever
    public void sendMsgToReciever(String recieverName, String encryptedMsg, String encryptedAESkey, IndividualUserHandler recieverHandler) // sending encryptedMessage to reciver if he is online on other terminal 
    {
        
        if(!checkUserIsOnlineOrNot(recieverName))
        {
            System.out.println("user is offline, we will store encrypted msg in DB...");
            return;
        } 
        recieverHandler = hashMap.get(recieverName);
        // sending encrypted msg and encrypted AES key to the other user, other user(reciever) printWriter(pw)
        recieverHandler.pw.println(encryptedAESkey);
        recieverHandler.pw.println(encryptedMsg);

        
    }
    
// ____________________________________________________________________________________________________________________________
// ____________________________________________________________________________________________________________________________

@Override
    public void run() 
    {
        String recieverName = null;
         IndividualUserHandler recieverHandler = null;
        try 
        {
            
            System.out.println("sender's Name : " + senderName);
 
            if(checkUserIsAvilableInDb(senderName))
            {
                System.out.println("yes users are valid, we checked on DB\n");
                // store in hashmap
                hashMap.put(senderName, this); // it will keep storing the sender : sender's handler obj(the user who called the server) in Hashmap
                
                if(checkUserIsOnlineOrNot(senderName)) // checks user(sender ) is online or not, checks in hashMap
                {
                    System.out.println("online users are : "+hashMap.keySet());

                    System.out.println("yes "+senderName+" is online");

                    // sending rsa public key to reciever logic. . .
                    recieverName = br.readLine(); 
                    if(checkUserIsOnlineOrNot(recieverName)) //checks reciver user is online or not, check in hashMap
                    {
                        System.out.println(recieverName+" is also online");

                        recieverHandler = hashMap.get(recieverName);
                        recieverHandler.pw.println(other_user_public_rsa_key_String);

                        while(true)
                        {
                            
                            String encryptedMsg = br.readLine(); 
                            String encryptedAESKey = br.readLine();
                                
                            //     saveMsgToDataBase(senderName, reciverName, encryptedMessage, encryptedAESkey);
                            sendMsgToReciever(recieverName, encryptedMsg, encryptedAESKey, recieverHandler);
                            saveMsgToDataBase(senderName, recieverName, encryptedMsg, encryptedAESKey);
                        }
                    }
                    else{ System.out.println("reciever is not online, I checked online users in  hashmap"); }     
                } 

            }
            else{System.out.println("user not found in db"); return ;}
        } 
        catch (Exception e) {System.out.println("exception recieved. . .");e.printStackTrace();}

    }

}

