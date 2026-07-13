package version1TerminalChat.groupServer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.security.Key;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Base64;
import java.util.HashMap;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import version1TerminalChat.database.DBConnection;

public class Test 
{
    static Connection connection = DBConnection.getConnection();


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
        try
        {
            
            System.out.print("type msg : ");
            BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in)); // keyboard input...
            String msg = keyboard.readLine();
            System.out.println();
            System.out.println("read msg : "+msg);

            String encryptMsg = encryptMsg(msg);
            System.out.println("encrypt msg : "+encryptMsg);

            String decryptMsg = decryptMsg(encryptMsg);
            System.out.println("decrypt msg : "+ decryptMsg);



        }
        catch(Exception e){System.out.println(""); e.printStackTrace();}




    }    
}
