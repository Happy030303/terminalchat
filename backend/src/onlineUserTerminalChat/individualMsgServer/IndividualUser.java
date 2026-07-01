package onlineUserTerminalChat.individualMsgServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.mindrot.jbcrypt.BCrypt;

public class IndividualUser
{
    private static PublicKey otherUserPublicRsaKey; 
//__________________________________________________________________________________________________________________________________________
//__________________________________________________________________________________________________________________________________________
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


//__________________________________________________________________________________________________________________________________________
//__________________________________________________________________________________________________________________________________________

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
//__________________________________________________________________________________________________________________________________________
//__________________________________________________________________________________________________________________________________________

    // generating RSA key (public RSA key & private RSA key)
    private static ArrayList<Key> rsaKeyPair()
    {
        try
        {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");         // Java se RSA KeyPairGenerator mang rahe hain.
            keyPairGenerator.initialize(2048);                                               // 2048-bit RSA keys generate karne ke liye bol rahe hain.
            KeyPair keyPair = keyPairGenerator.generateKeyPair();                            // RSA Public Key aur RSA Private Key generate ho gayi.
            PublicKey publicKey = keyPair.getPublic();                                       // Generated RSA key pair se Public Key nikal rahe hain.
            PrivateKey privateKey = keyPair.getPrivate();                                    // Generated RSA key pair se Private Key nikal rahe hain.
            
            ArrayList<Key> keys = new ArrayList<>();
            keys.add(keyPair.getPublic());
            keys.add(keyPair.getPrivate());

            return keys;
        }
        catch(Exception e){System.out.println("error in rsaKeyPain() method"); e.printStackTrace();}
        
        return null;
    }


//__________________________________________________________________________________________________________________________________________
//__________________________________________________________________________________________________________________________________________
    
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
        catch(Exception e){System.out.println("error ayi in rsaToEncryptAESkey() method");e.printStackTrace(); }

        return "Problem aari AES Key ko encrypt karna ma";
        
    }
//__________________________________________________________________________________________________________________________________________
//__________________________________________________________________________________________________________________________________________

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

//__________________________________________________________________________________________________________________________________________
//__________________________________________________________________________________________________________________________________________

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


//__________________________________________________________________________________________________________________________________________
//__________________________________________________________________________________________________________________________________________


    public static void main(String[] args) throws UnknownHostException, IOException 
    {
        Socket socket = new Socket("localhost", 4000); // Server se connect karna ke liey socket

        try 
        {
            // I made here all the input and output stream, here....
            BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in)); // Keyboard se input lene ke liye
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream())); // Server se messages receive karne ke liye
            PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);

            //____________________________________________________________________________________________________
            
            // check both user are in Platform database or not
            // check both user are online or not ? ? ? 
            System.out.print("sender's name : ");
            String senderName = keyboard.readLine();
            pw.println(senderName);


            // seding public rsa key to other user . . .
            ArrayList<Key> rsaKeys = rsaKeyPair(); 
            PublicKey publicKey = (PublicKey) rsaKeys.get(0);
            PrivateKey privateKey = (PrivateKey) rsaKeys.get(1);
            String publicKeyString = Base64.getEncoder().encodeToString(publicKey.getEncoded());
            pw.println(publicKeyString);

            
            System.out.print("receiver's name : ");
            String receiverName = keyboard.readLine();
            pw.println(receiverName);

            // recieving the other user RSA key string . . . 
            // System.out.println("get the other user RSA key string");  ///////////////////////////////////////////////////////////
            String otherUserRsaPublicKeyString = br.readLine();
            byte[] keyBytes = Base64.getDecoder().decode(otherUserRsaPublicKeyString);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            otherUserPublicRsaKey = keyFactory.generatePublic(keySpec);
            // System.out.println("other username rsa key is : " +otherUserRsaPublicKeyString); ////////////////////////////////////////////////////


            System.out.println("All things are done correctly");
            



    // user have just two things to do, send msg and listen msg
    //________________________________________________________________________________________________________

        // Listener : this thread will always listen independently
            Thread recieverThread = new Thread(() -> 
            {
                while (true) {
                    try 
                    {
                        String encryptedAESkey = br.readLine();
                        String encryptedMessage = br.readLine();

                        SecretKey decyrptAesKey = decryptAESKey(encryptedAESkey, privateKey);
                        String msg = decryptMsg(decyrptAesKey, encryptedMessage);
                    
                        System.out.println(receiverName+":"+msg+"\n");

                    } catch (IOException e) {  System.out.println("error mila bhai ");  e.printStackTrace();}
                }
            });
            recieverThread.start();


    // _____________________________________________________________________________________________________

    // send msg to server and server will forward  msg to other user and store in database . . . 

            
            // Sender : this loop is to send text to the server
            while (true) 
            {
                String msg = keyboard.readLine();

                SecretKey aesKey = createAESKey();
                String encryptedMsg = encryptMsg(aesKey, msg);
                String encryptedAesKey = encryptAesKey(aesKey, otherUserPublicRsaKey); // we have to encrypt AES key with other user public rsa key only
                pw.println(encryptedMsg);
                pw.println(encryptedAesKey);

            }
        } 
        catch (Exception e) {System.out.println("exception recieved. . .");e.printStackTrace();}
        finally {socket.close();}
    }
}
