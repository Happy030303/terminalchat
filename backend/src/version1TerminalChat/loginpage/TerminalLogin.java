package version1TerminalChat.loginpage;

import java.io.FileWriter;
import java.io.IOException;
import java.io.File;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Base64;
import java.util.Scanner;
import org.mindrot.jbcrypt.BCrypt;

import version1TerminalChat.database.DBConnection;


// this class is responsible for login or create account for our platform : "Varta" aka "Vartagram"
public class TerminalLogin 
{
    private static Connection connection = DBConnection.getConnection();
    private static final Scanner scan = new Scanner(System.in);
    private static String privateRsaKeyString;

// ===================================================================================================

    private static String loginLogic()
    {
        System.out.println("\n\n                      Logging Page : \n\n");
        
        // enter these credenticals in terminal 
        System.out.print("enter username : ");                                                           // username
        String terminalUsername = scan.nextLine();

        System.out.print("enter password : ");                                                           // password
        String terminalPassword = scan.nextLine();
        
        try
        {
            //   search only by username
            String query ="SELECT username, hash_password FROM login WHERE username = ? LIMIT 1";
            PreparedStatement ps = connection.prepareStatement(query);                                    // create object of 'PreparedStatement'
            ps.setString(1, terminalUsername);                                                            // set the entered username in terminal into sql query                                                          // set the entered username in terminal into sql query
            ResultSet rs = ps.executeQuery();                                                             // it executes query and retuns the data from database acc. to the terminalUsername
            
            
            // checking the username and password are correct or not
            if(rs.next())
            {
                String storedHash = rs.getString("hash_password");
                // compare entered password with stored hash
                boolean validPassword = BCrypt.checkpw( terminalPassword, storedHash);

                if(validPassword)
                {   
                    System.out.println("login successful");
                    return terminalUsername;

                }
                else{System.out.println("invalid user or password");}
            }
            else{ System.out.println("\n                User Not Found..OR Wrong password");}
        }
        catch(Exception e) { System.out.println("exception in loginLogic() method, in \"TerminalLogin.java\" "); e.printStackTrace();}  
        
        return terminalUsername;
    }
                
// ===================================================================================================

    public static void savePrivateKeyinTextFile(String username, String privateKeyString)
    {
        try
        {
            // String folderPath = "C:\\Users\\shubh\\OneDrive\\Desktop\\terminalChat\\keys";
            String folderPath = "C:\\Users\\shubh\\OneDrive\\Desktop\\terminalChat\\backend\\src\\offlineUserTerminalChat\\keys";
            File folder = new File(folderPath);
            if(!folder.exists())
            {
                folder.mkdirs();
            }
            File file = new File(folder, "private_keys.txt");
            FileWriter writer = new FileWriter(file, true); // append mode
            writer.write(username + "=" + privateKeyString + System.lineSeparator());
            writer.close();
            System.out.println("Private key saved successfully in text file...");

            
        }
        catch(IOException e){System.out.println("Error in : savePrivateKeyinTextFile() method, in \"TerminalLogic.java\" class");e.printStackTrace();}
    }

// ===================================================================================================

    private static String createPublicRsaKeyForThisUser(String username)
    {   
        KeyPairGenerator keyPairGenerator;
        String publicRsaKeyString = null;
        try 
        {
            keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);                                               // 2048-bit RSA keys generate karne ke liye bol rahe hain.
            KeyPair keyPair = keyPairGenerator.generateKeyPair();                            // RSA Public Key aur RSA Private Key generate ho gayi.
            PublicKey publicKey = keyPair.getPublic();                                       // Generated RSA key pair se Public Key nikal rahe hain.
            publicRsaKeyString = Base64.getEncoder().encodeToString(publicKey.getEncoded());
            PrivateKey privateKey = keyPair.getPrivate();                                    // Generated RSA key pair se Private Key nikal rahe hain.
            privateRsaKeyString = Base64.getEncoder().encodeToString(privateKey.getEncoded());
            
            // saving private RSA into text file  in device only
            savePrivateKeyinTextFile(username, privateRsaKeyString);
        }  
        catch (NoSuchAlgorithmException e) {  System.out.println("error bro : \"createPublicRsaKeyForThisUser()\" method in \"TerminalLogin\" class ... ");          e.printStackTrace();}         // Java se RSA KeyPairGenerator mang rahe hain.
        
        return publicRsaKeyString;
        
    }

// ===================================================================================================
   
    private static void createAccount() // now work here...
    {
        System.out.println("Creating New Account : \n\n");

        System.out.print("enter username : ");                    String username = scan.nextLine();
        
        System.out.print("enter password : ");                    String password = scan.nextLine();
        
        // convert password into  hashpassword
        String hashPassword = BCrypt.hashpw(password, BCrypt.gensalt());  

        try 
        {
            // check the database, is this username exist already or not...
            String query = "SELECT username FROM login WHERE username = ? LIMIT 1";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, username); // set the entered username in terminal into sql query
            ResultSet rs = ps.executeQuery();
        
            if(rs.next()==false) // username is not there, then insert the user in DB...
            {
                // creating public key(session key) for user
                String publicRsaKeyString = createPublicRsaKeyForThisUser(username);

                String query2 = "INSERT INTO login (username, hash_password, public_rsa_key) VALUES (?, ?, ?)";
                PreparedStatement ps1 = connection.prepareStatement(query2);

                ps1.setString(1, username);          ps1.setString(2, hashPassword);               ps1.setString(3, publicRsaKeyString);

                ps1.executeUpdate();

                System.out.println("______account is created successfully______");

                homePage();
            }
            else{System.out.println("account aready exist...");}
        } 
        catch (Exception e) {System.out.println("error in : \"createAccount()\" method in \"TerminalLogin.java\" class ");  e.printStackTrace();}
    }


// ===================================================================================================

    // public static void main(String[] args)
    public static String homePage() 
    {
        String username = null;
        try 
        {
            
                System.out.println("""
                                    =========================
                                        TERMINAL CHAT
                                    =========================
                                    1. Login
                                    2. Create Account
                                    3. Exit
                                    =========================
                                    Enter Choice:
                                """);

                int choice = scan.nextInt();
                scan.nextLine(); // it will take the next line after taking any interger value...
                            
                switch(choice)
                {
                    case 1: username = loginLogic();
                            return username;
                                
                    case 2: createAccount();
                                break;
                    case 3: System.exit(0);
                                break;
                    default: System.out.println("Invalid Choice");
                }
            
        } 
        catch (Exception e){ System.out.println("error in main() method of TerminalLogin.java class"); e.printStackTrace();}

        return username;
    }
//__________________________________________________________________________________________________________
}
