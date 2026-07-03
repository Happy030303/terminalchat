package onlineUserTerminalChat.groupMsgServer;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import onlineUserTerminalChat.database.DBConnection;

// this class will just make UserHandler obj , the object will store socket, listener(), writer(pw), sender's name(senderName).
public class GroupUserHandler implements Runnable 
{
    Socket socket;
    BufferedReader br;
    PrintWriter pw;
    String senderName;
    private Connection connection;



//_________________________________________________________________________________________________________
//_________________________________________________________________________________________________________


    GroupUserHandler(Socket socket, String senderName, BufferedReader br)  // constructor
    {
        try 
        {
            this.connection = DBConnection.getConnection(); // will connect with DB
            this.socket = socket; // Constructor me received socket store kar rahe hain
            this.senderName = senderName;
            this.br = br ;  // Client se messages read karne ke
                                                                                     // liye
            pw = new PrintWriter(socket.getOutputStream(), true);
        } 
        catch (Exception e) { System.out.println("exception aayi hai");}
    }


//________________________________________________________________________________________________________________
//________________________________________________________________________________________________________________
   

    private boolean checkUserInGroupFromDB(String username)
    {
        try
        {
            PreparedStatement ps = connection.prepareStatement( "SELECT * FROM users_in_group WHERE username = ?");
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            if(rs.next())
            {
                return true;    // User mil gaya
            }
            return false;       // User nahi mila
        }
        catch(SQLException e){System.out.println("Error ayi");e.printStackTrace();return false;}
    }


//___________________________________________________________________________________________________________________________________________
//___________________________________________________________________________________________________________________________________________

    // save chats on database
    // save chats on database
    private void savingGroupMsgInDB(String senderName, String msg)
    {
    try
    {
        // find userId from username
        PreparedStatement ps1 = connection.prepareStatement("SELECT userId FROM users_in_group WHERE username = ?"
                                                                );
        ps1.setString(1, senderName);
        ResultSet rs = ps1.executeQuery();

        if(rs.next())
        {
            int userId = rs.getInt("userId");
            // insert message
            PreparedStatement ps2 = connection.prepareStatement("INSERT INTO group_server_messages (sender_id, senderName, msg) VALUES (?, ?, ?)"
                                                                );
            ps2.setInt(1, userId);
            ps2.setString(2, senderName);
            ps2.setString(3, msg);

            int rowsAffected = ps2.executeUpdate();
            // System.out.println("Rows inserted : " + rowsAffected); // it is written just to check ki rows effect hoyi ya nhi
        }
        else
        {
            System.out.println("User not found in users_in_group : " + senderName);
        }
    }
    catch(SQLException e)
    {
        System.out.println("Error while saving message");
        e.printStackTrace();
    }
}


//____________________________________________________________________________________________________________________________________________
//____________________________________________________________________________________________________________________________________________

// Run() method : it is the    '''''''         STARTING POINT      ''''''''''    of this class when we run class
    @Override
    public void run() 
    {
        try 
        {
                if(checkUserInGroupFromDB(senderName))
                {
                    pw.println("GROUP_ACCESS_GRANTED");
                    System.out.println("yes \" "+senderName+"\" is already added in the group");
                    while(true)
                    {
                        String msg = br.readLine();
                        // System.out.println(" msg -> "+ recieverName+"(reciever name) : " + msg);
                        System.out.println(senderName+" : "+msg);
                        savingGroupMsgInDB(senderName, msg);
                    }
                }
                else
                {
                    pw.println("GROUP_ACCESS_DENIED");
                    System.out.println("this "+ senderName +" is not added in this group");
                }
        } 
        catch (Exception e) {System.out.println("exception recieved. . .");e.printStackTrace();}

    }

//____________________________________________________________________________________________________________________________________________________


}
