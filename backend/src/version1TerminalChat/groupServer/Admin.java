package version1TerminalChat.groupServer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import version1TerminalChat.database.DBConnection;

// admin is used to create aes key and send it to all users who joins the group....
public class Admin 
{
    static Connection connection = DBConnection.getConnection();

//=====================================================================================================================

    private static PublicKey getUserPublicKeyFromDB(String username)
    {
        try 
        {
            PreparedStatement ps = connection.prepareStatement("select public_rsa_key from login where username = ?");
            ps.setString(1, username);

            ResultSet rs = ps.executeQuery();

            if(rs.next())
            {
                String userPublicKeyString = rs.getString("public_rsa_key");
                byte[] keyBytes = Base64.getDecoder().decode(userPublicKeyString);
                X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                PublicKey userPublicKey = keyFactory.generatePublic(keySpec);

                return userPublicKey;
            }
            System.out.println("user not found");
        } 
        catch(Exception e){System.out.println("error in getUserPublicKeyFromDB() method in 'Admin.java' class"); e.printStackTrace();}
        
        return null;
    }

//=====================================================================================================================

    private static String encryptAESKey(SecretKey aesKey, PublicKey userPublicKey) 
    {
        try
        {
            byte[] aesKeyBytes = aesKey.getEncoded();
            
            Cipher rsaCipher = Cipher.getInstance("RSA"); // give me RSA engine
            rsaCipher.init(Cipher.ENCRYPT_MODE, userPublicKey); // Use reciever's RSA Public Key for encryption.
            byte[] encryptedAESKey = rsaCipher.doFinal( aesKeyBytes ); // Encrypt the AES key bytes using reciever's RSA Public Key.
            String encryptedAESKeyString = Base64.getEncoder().encodeToString(encryptedAESKey); // convert encrypted key into string
            // System.out.println("\nencrpted AES key : "+encryptedAESKeyString);

            return encryptedAESKeyString;

        }
        catch(Exception e){System.out.println("error ayi in encryptAesKey() method");e.printStackTrace(); }

        return "Problem aari AES Key ko encrypt karna ma";
    }

//=====================================================================================================================

    private static SecretKey getAesKeyFromTextFile(String groupName)
    {
        try
        {
            // this buffer reader is only for private key reading from text file...
            BufferedReader br = new BufferedReader(new FileReader("C:\\Users\\shubh\\OneDrive\\Desktop\\terminalChat\\backend\\src\\offlineUserTerminalChat\\keys\\group_aes_keys).txt"));
            String line;
            while ((line=br.readLine()) != null) // reads a line at a time till encounter with end line(null)
            {
                String[] parts = line.split("=", 2);
                String storeGroupName = parts[0];
                String storeAesKey = parts[1];

                if(storeGroupName.equals(groupName))
                {
                    br.close();
                
                    // convert String private key into PrivateKey...
                    byte[] keyBytes = Base64.getDecoder().decode(storeAesKey);

                    SecretKey aesKey = new SecretKeySpec(keyBytes, "AES");
                    return aesKey;

                }   
            }
            br.close();
        }
        catch(Exception e){System.out.println("error : \"getUserPrivateKey()\" method, in \"IndividualUser.java\" class... ");e.printStackTrace();}
        return null;
    }

//=====================================================================================================================

    public static String getEncryptAesKeyWhenUserJoinedGroup(String username, String groupName) 
    // public static String getAESKeyWhenUserJoinsGroup(String username) 
    {
        Socket socket;
        try
        {   
            socket = new Socket("localhost", 4001);
            BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream())); // listener at socket 4001 port
            PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);  

            if(getAesKeyFromTextFile(groupName) == null)
            {
                System.out.println("unable to get aes key for group, sir from text file");
            }
            SecretKey aesKey = getAesKeyFromTextFile(groupName);
            PublicKey userPublicKey = getUserPublicKeyFromDB(username);
            String encryptedAESKeyString = encryptAESKey(aesKey, userPublicKey);
            //send this encryptedAesKeyString to the users when they join any group...

            socket.close();
            return encryptedAESKeyString;
            

        }
        catch(Exception e ){System.out.println("Error in : Admin.java class");  e.printStackTrace();}
        return null;
    }

//=====================================================================================================================
    public static void main(String[] args) 
    {
        // String groupName = "thehackerNews";
        // String username = "";
        // getEncryptAesKeyWhenUserJoinGroup(username, groupName);
    }

}
