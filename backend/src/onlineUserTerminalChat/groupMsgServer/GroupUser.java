package onlineUserTerminalChat.groupMsgServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class GroupUser 
{

    public static void groupUser(String userName) throws UnknownHostException, IOException 
    {
        Socket socket = new Socket("localhost", 4001); // Server se connect karne ka liye socket object hai, it stores
                                                       // all the informatin ki kis endpoint sa connection ban raha hai,
                                                       // even jis terminal sa execute karoge usska address bhi
        try 
        {

            BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in)); // Keyboard se input lene ke
                                                                                            // liye
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream())); // Server se
                                                                                                    // messages receive
                                                                                                    // karne ke liye
            PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);

            System.out.print("sender name is  : "+userName+"\n");

            String senderName = userName;
            pw.println(senderName);

            String groupStatus = br.readLine();
            if(groupStatus.equals("GROUP_ACCESS_DENIED"))
            {
                System.out.println(senderName+" is not added in this group");
                socket.close();
                return;
            }

  // we will use GroupUser just to send text into group and this class will not listen as all text will be sent to user only...
            // Sender : this loop is to send text to the server
            while (true) 
            {
                System.out.print("msg : ");
                String msg = keyboard.readLine();
                pw.println(msg);
            }
        } 
        catch (Exception e) {System.out.println("exception recieved. . .");e.printStackTrace();}
        finally{socket.close();}
    }
}
