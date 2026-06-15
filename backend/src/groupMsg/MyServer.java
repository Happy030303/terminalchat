package groupMsg;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class MyServer {
    static HashMap<String, UserHandler> hashmap = new HashMap<String, UserHandler>();

    static ServerSocket serverSocket;
    static Socket socket;

    static void printHashMap()
    {
        System.out.println("HashMap is : "+hashmap);
    }
    public static void main(String[] args) {
        try {
                serverSocket = new ServerSocket(4000);
                System.out.println("discussion group : \n\n");

                while (true) // always listen the request from clients
                {
                    socket = serverSocket.accept(); // wait till any connection is recived
                    
                    BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String senderName = br.readLine();
                    
                    UserHandler userHandler = new UserHandler(socket, senderName, br); // Here it will create UserHandler object for each User
                    Thread thread = new Thread(userHandler); // we are making thread for every client/user like kalam thread, happy thread...
                    thread.start();
                    
// store in hashmap
                    hashmap.put(senderName, userHandler); // it will keep storing the sender : sender's handler obj in Hashmap
                    // printHashMap();
                }

            } catch (Exception e) {System.out.println("error mili bhai ..."); e.printStackTrace();}

    }
}
