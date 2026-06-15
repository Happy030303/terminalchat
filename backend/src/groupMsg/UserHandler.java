package groupMsg;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

// this class will just make UserHandler obj , the object will store socket, listener(), writer(pw), sender's name(senderName).
public class UserHandler implements Runnable {
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
            this.br = br ;  // Client se messages read karne ke
                                                                                     // liye
            pw = new PrintWriter(socket.getOutputStream(), true);
        } 
        catch (Exception e) { System.out.println("exception aayi hai");}
    }

    @Override
    public void run() 
    {
        try 
        {

            // String recieverName = br.readLine();
            // System.out.println("sender's Name : " + senderName);
            // System.out.println("reciever Name : " + recieverName);

            while(true)
            {

                String msg = br.readLine();
                
                // System.out.println(" msg -> "+ recieverName+"(reciever name) : " + msg);
                System.out.println(senderName+" : "+msg);
            }



        } catch (Exception e) {System.out.println("exception recieved. . .");e.printStackTrace();}

    }

}
