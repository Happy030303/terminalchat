package individualMsg;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class User {

    public static void main(String[] args) throws UnknownHostException, IOException {
        Socket socket = new Socket("localhost", 4000); // Server se connect karne ka liye socket object hai, it stores
                                                       // all the informatin ki kis endpoint sa connection ban raha hai,
                                                       // even jis terminal sa execute karoge usska address bhi
        try {

            BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in)); // Keyboard se input lene ke
                                                                                            // liye
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream())); // Server se
                                                                                                    // messages receive
                                                                                                    // karne ke liye
            PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);

            System.out.print("Enter sender name : ");
            String senderName = keyboard.readLine();
            pw.println(senderName);

            // user have just two things to do, send msg and listen msg

            // Listener : this thread will always listen independently
            Thread recieverThread = new Thread(() -> {
                while (true) {
                    try {

                        String receiverName = br.readLine();
                        String msg = br.readLine();
                        System.out.println(receiverName+":"+msg+"\n");

                    } catch (IOException e) {
                        System.out.println("error mila bhai ");
                        e.printStackTrace();
                    }
                }
            });
            recieverThread.start();

            System.out.print("receiver's name : ");
            String receiver = keyboard.readLine();
            pw.println(receiver);
            // Sender : this loop is to send text to the server
            while (true) {


                System.out.print("                     msg : ");
                String msg = keyboard.readLine();
                System.out.println("\n");

                pw.println(msg);

            }
        } catch (Exception e) {
            System.out.println("exception recieved. . .");
            e.printStackTrace();
        }

    }
}
