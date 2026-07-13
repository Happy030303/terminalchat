package securedChat.individualserver;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

class IndividualMsgServer 
{
    static ServerSocket serverSocket;
    static Socket socket;

    public static void main(String[] args) 
    {
        try 
        {
                serverSocket = new ServerSocket(4000);
                System.out.println("server started : ");

                while (true) // always listen the request from clients
                {
                    socket = serverSocket.accept(); // wait till any connection is recived
                    BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    
                    String senderName = br.readLine(); 

                    // creating thread for each user connect with server...
                    IndividualUserHandler userHandler = new IndividualUserHandler(socket, senderName, br); // Here it will create UserHandler object for each User
                    Thread thread = new Thread(userHandler); // we are making thread for every client/user like kalam thread, happy thread... and run() method independtely bhi run() hota rahe
                    thread.start();                
                }
        } catch (Exception e) {System.out.println("error mili : main() method of PrepareIndividualMsgServer class"); e.printStackTrace();}

    }
}
