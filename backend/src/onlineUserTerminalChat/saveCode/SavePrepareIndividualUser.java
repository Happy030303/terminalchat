package onlineUserTerminalChat.saveCode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import java.util.List;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class SavePrepareIndividualUser
{
    static PrivateKey privateKey ;
//_________________________________________________________________________________________________________________________
//_________________________________________________________________________________________________________________________

//here we will encrypt the msg
    public static List<String> encryptMessage(String msg)
    {
        SecretKey aesKey = null;
        KeyGenerator keyGenerator;
        String encryptedMessage = null;
        try 
        {
// We are usign RSA to share encrypted AES key...
// genrating AES key using keygenerator here ... 
            keyGenerator = KeyGenerator.getInstance("AES");                                 // java sa AES "keyGenerator" object mang rhae hai
            keyGenerator.init(256);                                                         // telling keyGenerator 256 bit AES key genrate karni hai
    
            aesKey = keyGenerator.generateKey();                                  // random AES key genrate ho rahi hai
            // String aesKeyString = Base64.getEncoder().encodeToString(aesKey.getEncoded());  // convert object of aesKey into string so we can print it in string form
            // System.out.println("\nAES key is : "+aesKeyString); 

// encrypt msg using AES key . . . 
            Cipher cipher = Cipher.getInstance("AES");                                     // Cipher encryption/decryption machine hai, yaha hum Cipher ka object bana rahe hai using 'getInstance()'
            cipher.init(Cipher.ENCRYPT_MODE, aesKey);                                      // putting aes key, ki use this aes key encrypt message
            byte[] encryptedBytes = cipher.doFinal(msg.getBytes());                        // converint msg into bytes & encrypting those bytes
            encryptedMessage = Base64.getEncoder().encodeToString(encryptedBytes);  // encrypted message object ko string ma convert kar rahe, taki print kar sake
            System.out.println("\nEncrypted Message is : "+encryptedMessage);

        } 
        catch (Exception e) { System.out.println("error aayi in \"IndividualUser.java\" ");e.printStackTrace();}
        
        String encryptedAESkey = rsaToEncryptAESkey(aesKey); // sends aes key to encrypt it

        return List.of(encryptedMessage, encryptedAESkey);
    }

//_________________________________________________________________________________________________________________________
//_________________________________________________________________________________________________________________________

// here we want to encrypt the aes key
    public static String rsaToEncryptAESkey(SecretKey aesKey)
    {

        String encryptedAESKeyString = null ;
        try
        {

            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");         // Java se RSA KeyPairGenerator mang rahe hain.
            keyPairGenerator.initialize(2048);                                               // 2048-bit RSA keys generate karne ke liye bol rahe hain.
            KeyPair keyPair = keyPairGenerator.generateKeyPair();                            // RSA Public Key aur RSA Private Key generate ho gayi.
            PublicKey publicKey = keyPair.getPublic();                                       // Generated RSA key pair se Public Key nikal rahe hain.
            privateKey = keyPair.getPrivate();                                    // Generated RSA key pair se Private Key nikal rahe hain.
            
            byte[] aesKeyBytes = aesKey.getEncoded();
            
            Cipher rsaCipher = Cipher.getInstance("RSA"); // give me RSA engine
            rsaCipher.init(Cipher.ENCRYPT_MODE,publicKey); // Use reciever's RSA Public Key for encryption.
            byte[] encryptedAESKey = rsaCipher.doFinal( aesKeyBytes ); // Encrypt the AES key bytes using reciever's RSA Public Key.
            encryptedAESKeyString = Base64.getEncoder().encodeToString(encryptedAESKey); // convert encrypted key into string
            System.out.println("\nencrpted AES key : "+encryptedAESKeyString);

        }
        catch(Exception e){System.out.println("error ayi in rsaToEncryptAESkey() method");e.printStackTrace();}
        
        return encryptedAESKeyString;
    }

//_________________________________________________________________________________________________________________________
//_________________________________________________________________________________________________________________________
     
   public static String decryptAESkey(String encryptedAESkey, String encryptedMessage)
{
    try
    {
        byte[] encryptedAESKeyBytes = Base64.getDecoder().decode(encryptedAESkey); // Base64 string ko wapas encrypted bytes me convert kar rahe hain.


        Cipher rsaCipher = Cipher.getInstance("RSA"); // RSA Cipher object bana rahe hain.


        rsaCipher.init(Cipher.DECRYPT_MODE,privateKey); // Receiver ki RSA Private Key use karke
                                                        // decryption mode start kar rahe hain.


        byte[] aesKeyBytes = rsaCipher.doFinal( encryptedAESKeyBytes); // Encrypted AES key ko decrypt kar rahe hain.


        SecretKey aesKey = new SecretKeySpec( aesKeyBytes,"AES"); // AES key bytes ko SecretKey object me convert kar rahe hain.


        String msg = decryptMessageUsingAESkey(aesKey, encryptedMessage);
        return msg;
    }
    catch(Exception e){System.out.println("error in decryptAESkey()");e.printStackTrace();}

    return "there is some error in decrypting the message and aes key";
}
    
//_________________________________________________________________________________________________________________________
//_________________________________________________________________________________________________________________________
     
    public static String decryptMessageUsingAESkey(SecretKey aesKey, String encryptedMessage)
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
        catch(Exception e){System.out.println("error in decryptMessageUsingAESkey()");  e.printStackTrace(); }

        return null; // Agar decryption fail ho jaye to null return kar rahe hain.
    }

//_________________________________________________________________________________________________________________________
//_________________________________________________________________________________________________________________________


    public static void main(String[] args) throws UnknownHostException, IOException 
    {
        Socket socket = new Socket("localhost", 4000); // Server se connect karna ke liey socket

        try 
        {
            // I made here all the input and output stream, here....
            BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in)); // Keyboard se input lene ke liye
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream())); // Server se messages receive karne ke liye
            PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);

            System.out.print("Enter sender name : ");
            String senderName = keyboard.readLine();
            pw.println(senderName);




// user have just two things to do, send msg and listen msg
// ________________________________________________________________________________________________________

    // Listener : this thread will always listen independently
    
            Thread recieverThread = new Thread(() -> 
            {
                while (true) {
                    try 
                    {

                        String receiverName = br.readLine();
                        String encryptedMessage = br.readLine();
                        String encryptedAESkey = br.readLine();

                        // we will send encrypted message to decrypt using aes key 
                        String msg = decryptAESkey(encryptedAESkey, encryptedMessage);
                    
                        System.out.println(receiverName+":"+msg+"\n");

                    } catch (IOException e) {
                        System.out.println("error mila bhai ");
                        e.printStackTrace();
                    }
                }
            });
            recieverThread.start();


// _____________________________________________________________________________________________________

// send msg to server and server will forward  msg to other user and store in database . . . 

            System.out.print("receiver's name : ");
            String receiver = keyboard.readLine();
            pw.println(receiver);
            // Sender : this loop is to send text to the server
            while (true) 
            {
                String msg = keyboard.readLine();
                List<String> data = encryptMessage(msg);
                String encryptedMsg = data.get(0);
                String encryptedAesKey = data.get(1);
                
                System.out.println("\n");

                pw.println(encryptedMsg);
                pw.println(encryptedAesKey);

            }
        } 
        catch (Exception e) {System.out.println("exception recieved. . .");e.printStackTrace();}
        finally {socket.close();}
    }
}
 
    

