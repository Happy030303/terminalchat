package securedChat.individualserver;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import database.DBConnection;


public class IndividualUser
{
    private static PublicKey otherUserPublicRsaKey; 
    private static PrivateKey userPrivateKey;
    private static Connection connection = DBConnection.getConnection();
//========================================================================================================================================

    // creating aes here only. . .
    private static SecretKey createAESKey()
    {
        try 
        {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(256);                                                                 // telling keyGenerator to generate 256bit AES key
            
            SecretKey aesKey = keyGenerator.generateKey();   
            // String aesKeyString = Base64.getEncoder().encodeToString(aesKey.getEncoded());          // convert object of aesKey into string so we can print it in string form
            // System.out.println("\nAES key is : "+aesKeyString); 
            return aesKey;                                    // random aes key generate ho rhi h

        } 
        catch (Exception e) { System.out.println("error in createAESkey() method "); e.printStackTrace();   }

        return null;
    } 


//========================================================================================================================================

    // encrypt msg using AES key . . .
    private static String encryptMsg(SecretKey aesKey, String msg)
{
    try 
    {
        Cipher cipher = Cipher.getInstance("AES");                                     // Cipher encryption/decryption machine hai, yaha hum Cipher ka object bana rahe hai using 'getInstance()'
        cipher.init(Cipher.ENCRYPT_MODE, aesKey);                                      // putting aes key, ki use this aes key encrypt message
        
        byte[] encryptedBytes = cipher.doFinal(msg.getBytes());                        // converint msg into bytes & encrypting those bytes
        String encryptedMessage = Base64.getEncoder().encodeToString(encryptedBytes);  // encrypted message object ko string ma convert kar rahe, taki print kar sake
        // System.out.println("\nEncrypted Message is : "+encryptedMessage);
        
        return encryptedMessage;
    } 
    catch (Exception e) {System.out.println("error in : encryptMsg() method . . . "); e.printStackTrace();  }
    
    return "problem aari msg ko encrypt karne ma";
    
}

// ===================================================================================================    
 
    // encrypting AES key using ?
    private static String encryptAesKey(SecretKey aesKey, PublicKey otherUserPublicRsaKey)   // we have to encrypt AES key with other user public rsa key only
    {
        try
        {
            byte[] aesKeyBytes = aesKey.getEncoded();
            
            Cipher rsaCipher = Cipher.getInstance("RSA"); // give me RSA engine
            rsaCipher.init(Cipher.ENCRYPT_MODE,otherUserPublicRsaKey); // Use reciever's RSA Public Key for encryption.
            byte[] encryptedAESKey = rsaCipher.doFinal( aesKeyBytes ); // Encrypt the AES key bytes using reciever's RSA Public Key.
            String encryptedAESKeyString = Base64.getEncoder().encodeToString(encryptedAESKey); // convert encrypted key into string
            // System.out.println("\nencrpted AES key : "+encryptedAESKeyString);

            return encryptedAESKeyString;

        }
        catch(Exception e){System.out.println("error ayi in encryptAesKey() method");e.printStackTrace(); }

        return "Problem aari AES Key ko encrypt karna ma";
        
    }

//========================================================================================================================================

    // decrypt aes key using private key
    private  static SecretKey decryptAESKey(String encryptedAESkey, PrivateKey privateKey)
    {
        try 
        {
        
            byte[] encryptedAESKeyBytes = Base64.getDecoder().decode(encryptedAESkey); // Base64 string ko wapas encrypted bytes me convert kar rahe hain.
            Cipher rsaCipher = Cipher.getInstance("RSA"); // RSA Cipher object bana rahe hain.
            rsaCipher.init(Cipher.DECRYPT_MODE,privateKey); // Receiver ki RSA Private Key use karke
                                                            // decryption mode start kar rahe hain.
            byte[] aesKeyBytes = rsaCipher.doFinal( encryptedAESKeyBytes); // Encrypted AES key ko decrypt kar rahe hain.
            SecretKey aesKey = new SecretKeySpec( aesKeyBytes,"AES"); // AES key bytes ko SecretKey object me convert kar rahe hain.
            return aesKey;
        } 
        catch (Exception e) { System.out.println("error in decryptAESKey() method");  e.printStackTrace();    }
        
        return null;
        
    }

//========================================================================================================================================

    // decrypt msg using AES key we decrypted. . . 
    private static String decryptMsg(SecretKey aesKey, String encryptedMessage)
    {
        try
        {
            byte[] encryptedMessageBytes = Base64.getDecoder().decode(encryptedMessage); // Base64 encrypted string ko wapas encrypted bytes me convert kar rahe hain.
            Cipher cipher = Cipher.getInstance("AES"); // AES Cipher object bana rahe hain.
            cipher.init(Cipher.DECRYPT_MODE, aesKey); // AES key use karke decryption mode start kar rahe hain.
            byte[] decryptedBytes = cipher.doFinal(encryptedMessageBytes); // Encrypted bytes ko decrypt karke original bytes nikal rahe hain.
            String originalMessage = new String(decryptedBytes); // Original bytes ko String message me convert kar rahe hain.
            return originalMessage; // Original decrypted message return kar rahe hain.
        }
        catch(Exception e){System.out.println("error in decryptMsg()");  e.printStackTrace(); }

        return "problem in decrypting msg, sir";
    }

//========================================================================================================================================

    private static PublicKey recievingOtherUserPublicKey(String userName)
    {
        try
        {
            PreparedStatement ps = connection.prepareStatement("select public_rsa_key from login where username = ?");
            ps.setString(1, userName);
            ResultSet rs = ps.executeQuery();
            System.out.println("we got the public rsa key for "+userName);
            
            if(rs.next())
            {
                // System.out.println("get the other user RSA key string");  ///////////////////////////////////////////////////////////
                String otherUserRsaPublicKeyString = rs.getString("public_rsa_key");
                byte[] keyBytes = Base64.getDecoder().decode(otherUserRsaPublicKeyString);
                X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                otherUserPublicRsaKey = keyFactory.generatePublic(keySpec);
                // System.out.println("other username rsa key is : " +otherUserRsaPublicKeyString); ////////////////////////////////////////////////////
            }
            else
            { System.out.println(userName+" not found");}
            
        }
        catch(Exception e){System.out.println("error in recievingOtherUserPublicKey() method in 'IndividualUser.java' class"); e.printStackTrace();}
        
        return otherUserPublicRsaKey;
        
    }

//==========================================================================================================

    private static PrivateKey getUserPrivateKeyFromTextFile(String username)
    {
        try
        {
            // this buffer reader is only for private key reading from text file...
            BufferedReader br = new BufferedReader(new FileReader("C:\\Users\\shubh\\OneDrive\\Desktop\\terminalChat\\backend\\src\\offlineUserTerminalChat\\keys\\private_keys.txt"));
            String line;
            while ((line=br.readLine()) != null) // reads a line at a time till encounter with end line(null)
            {
                String[] parts = line.split("=", 2);
                String storeUsername = parts[0];
                String privateKeyString = parts[1];

                if(storeUsername.equals(username))
                {
                    br.close();
                
                    // convert String private key into PrivateKey...
                    byte[] keyBytes = Base64.getDecoder().decode(privateKeyString);
                    PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
                    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                    PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

                    return privateKey;
                }   
            }
            br.close();
        }
        catch(Exception e){System.out.println("error : \"getUserPrivateKey()\" method, in \"IndividualUser.java\" class... ");e.printStackTrace();}

        return null;
    }

//==========================================================================================================

    // public static void individualUserMainMethod() throws Exception 
    public static void main(String args[]) throws Exception 
    {
        
        Socket socket = new Socket("localhost", 4000); // Server se connect karna ke liye socket
        try 
        {
            
            // input and output stream connect with server on port 4000
            BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in)); // Keyboard se input lene ke liye
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream())); // Server se messages receive karne ke liye
            PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);

            //=================================================
            // Login Page
            // ================================================
                // String senderName = TerminalLogin.homePage();

            //===============================================================================
            //First, we will get both user each other's public key...
            //===============================================================================
                        // send sender name to server . . .
                // System.out.println("sent sender name ");
                System.out.println("sender's name ");
                String senderName = keyboard.readLine(); 
                //=====================           
                pw.println(senderName);
                //=====================           
                
                // sending receiver's name to server . . .
                System.out.print("receiver's name : ");      String receiverName = keyboard.readLine();       
                //=====================           
                pw.println(receiverName);
                //=====================           

                            //recieving the other user RSA key string . . . 
                otherUserPublicRsaKey = recievingOtherUserPublicKey(receiverName); // recieving other user public key

                userPrivateKey = getUserPrivateKeyFromTextFile(senderName); // sender's(User) private key

                System.out.println("code will not move until both the user will get each other's RSA public key...");


        //============================================================================================================
            

        // below we will have two independent thread : listener thread, writer thread
        
        //=============================================================================================================
            // Listener Thread : this thread will always listen independently
            Thread recieverThread = new Thread(() -> 
            {      
                // keep listening to the other user...              
                while (true) 
                {
                    try 
                    {
                        String encryptedMsg = br.readLine();
                        String encryptedAESkey = br.readLine();

                        SecretKey decyrptAesKey = decryptAESKey(encryptedAESkey, userPrivateKey);
                        String msg = decryptMsg(decyrptAesKey, encryptedMsg);

                        System.out.println(receiverName+" : "+msg+"\n");
                    }
                    catch (Exception e){System.out.println("error in listener thread... "); e.printStackTrace();}
                }
            });
            recieverThread.start();


        //==========================================================================================================

            // Main Thread will be used to send messages to other user...
            while (true) 
            {
                String msg = keyboard.readLine();

                SecretKey aesKey = createAESKey();
                String encryptedMsg = encryptMsg(aesKey, msg);
                String encryptedAesKey = encryptAesKey(aesKey, otherUserPublicRsaKey); // we have to encrypt AES key with other user public rsa key only
                
                //=============================
                pw.println(encryptedMsg);
                pw.println(encryptedAesKey);
                //=============================
            }
        } 
        catch (Exception e) {System.out.println("exception recieved. . .");e.printStackTrace();}
        finally {socket.close();}
    }
}
