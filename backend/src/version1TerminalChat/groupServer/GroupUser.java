package version1TerminalChat.groupServer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.Key;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;


public class GroupUser 
{

    private static Key getAesKeyFromTextFile()
    {   
        try
        {
            BufferedReader br = new BufferedReader( new FileReader("C:\\Users\\shubh\\OneDrive\\Desktop\\terminalChat\\backend\\src\\version1TerminalChat\\keys\\group_aes_keys.txt"));
            String base64Key = br.readLine();
            br.close();
            byte[] keyBytes = Base64.getDecoder().decode(base64Key);
            return new SecretKeySpec(keyBytes, "AES");
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        return null;

    }


    private static String decryptMsg(String encryptMsg)
    {
        try
        {
            Key key = getAesKeyFromTextFile();
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] encryptedBytes = Base64.getDecoder().decode(encryptMsg);
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            return new String(decryptedBytes);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        return null;
    }


    private static String encryptMsg(String msg)
    {
        try
        {
            Key key = getAesKeyFromTextFile();
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] encryptedBytes = cipher.doFinal(msg.getBytes());
            return Base64.getEncoder().encodeToString(encryptedBytes);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        return null;
    }

    public static void main(String[] args) 
    {
        
           
        try(                                                // try with resorces...
            Socket socket = new Socket("localhost", 4001);
            // input and output stream via socket at port 4001
            BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream())); // listener at socket 4001 port
            PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);
            )
        {  
            
            System.out.print("username : ");
            String username = keyboard.readLine();
            System.out.println();

            pw.println(username);
                    System.out.println("I am "+username);


            //================================================================================================================
            
            // listener thread
            Thread recieverThread = new Thread(() ->   // inside this lambda expression we are writing the code for run() method which start() method will automatically execute...
            {      
                // keep listening to the other user...              
                while (true) 
                {
                    try 
                    {
                        String name = br.readLine();
                        String encryptMsg = br.readLine();

                        String msg = decryptMsg(encryptMsg);

                        System.out.println("                "+name+" : "+msg);
                        System.out.println();
                    }
                    catch (Exception e){System.out.println("error in listener thread... "); e.printStackTrace();}
                }
            });
            recieverThread.start(); // this is using 
                
                
            //================================================================================================================
            
            // main() thread for writing only 
            while (true) 
            {
                // System.out.println();
                // System.out.print(username +" : " );
                String msg = keyboard.readLine();
                // System.out.println();

                
                // encrypting msg;
                String encryptedMsg = encryptMsg(msg);
                
                pw.println(encryptedMsg);
            }


            
        } 
        catch (Exception e) { System.out.println("error : \"main() \" method in \"GroupUser.java\" class ");  e.printStackTrace();}

    }
}
