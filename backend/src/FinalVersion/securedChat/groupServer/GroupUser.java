package securedChat.groupServer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;


public class GroupUser 
{

    public static void main(String[] args) 
    {
        
        try 
        {
            Socket socket = new Socket("localhost", 4001);

            // input and output stream via socket at port 4001
            BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream())); // listener at socket 4001 port
            PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);  


            String username = keyboard.readLine();
            pw.println(username);




            //================================================================================================================
            
            // listener thread
            // Thread recieverThread = new Thread(() ->   // inside this lambda expression we are writing the code for run() method which start() method will automatically execute...
            // {      
            //     // keep listening to the other user...              
            //     while (true) 
            //         {
            //             try 
            //             {
                            
            //             }
            //             catch (Exception e){System.out.println("error in listener thread... "); e.printStackTrace();}
            //         }
            // });
            // recieverThread.start(); // this is using 
                
                
            //================================================================================================================
            
            // main() thread for writing only 
            // while (true) 
            // {
                
            // }


            
        } 
        catch (Exception e) { System.out.println("error : \"main() \" method in \"GroupUser.java\" class ");  e.printStackTrace();}

    }
}
