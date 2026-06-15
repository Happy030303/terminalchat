package individualMsg;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import database.DBConnection;

// this class will just make UserHandler obj , the object will store socket, listener(), writer(pw), sender's name(senderName).
public class UserHandler implements Runnable {
    Socket socket;
    BufferedReader br;
    PrintWriter pw;
    String senderName;
    private Connection connection;
    UserHandler(Socket socket, String senderName, BufferedReader br)  // constructor
    {
        try 
        {
            this.connection = DBConnection.getConnection();
            this.socket = socket; // Constructor me received socket store kar rahe hain
            this.senderName = senderName;
            this.br = br ;  // Client se messages read karne ke
                                                                                     // liye
            pw = new PrintWriter(socket.getOutputStream(), true);
        } 
        catch (Exception e) { System.out.println("exception aayi hai");}
    }


    // public void sendMsgToReciever(String recieverName, String msg) // sending msg to reciver 
    // {
    //     if(MyServer.hashmap.get(recieverName) == null)
    //     {
    //         System.out.println("user not found in hashmap");
    //         return;
    //     }
        
    //         MyServer.hashmap.get(recieverName).pw.println(recieverName);
    //         MyServer.hashmap.get(recieverName).pw.println(msg);
        
    // }




   public boolean checkUserIsAvilableInDb(String senderName, String recieverName)
    {
        try
        {
            

            PreparedStatement ps = connection.prepareStatement("SELECT COUNT(*) FROM users WHERE username = ? OR username = ?"
                                                                );

            ps.setString(1, senderName);
            ps.setString(2, recieverName);

            ResultSet rs = ps.executeQuery();

            if(rs.next())
            {
                return rs.getInt(1) == 2;
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        return false;
    }





    public void saveMsgToDataBase(String senderName, String recieverName, String msg)
    {
        try
        {
            // Connection connection = DBConnection.getConnection();

            PreparedStatement ps = connection.prepareStatement(
                            "INSERT INTO mymessage(sender_name, receiver_name, message) VALUES (?, ?, ?)"
                                                    );

            ps.setString(1, senderName);
            ps.setString(2, recieverName);
            ps.setString(3, msg);

            int rows = ps.executeUpdate();

            System.out.println(rows + " row inserted");

        }
        catch(Exception e) { System.out.println("Exception while saving message"); e.printStackTrace();}
    }

    @Override
    public void run() 
    {
        try 
        {

            String recieverName = br.readLine();
            System.out.println("sender's Name : " + senderName);
            System.out.println("reciever Name : " + recieverName);

            while(true)
            {

                String msg = br.readLine();
                // sendMsgToReciever(recieverName, msg);
                // System.out.println(" msg -> "+ recieverName+"(reciever name) : " + msg);
                if(checkUserIsAvilableInDb(senderName, recieverName))
                {
                    System.out.println("yes we found the users");
                    saveMsgToDataBase(senderName, recieverName, msg);
                }
                else{System.out.println("user not found in db");}
            }



        } catch (Exception e) {System.out.println("exception recieved. . .");e.printStackTrace();}

    }

}
