package version1TerminalChat.groupServer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Base64;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import version1TerminalChat.database.DBConnection;

// pending to create a new terminal which will act like group....

public class NewGroupMaker 
{
    private static Connection connection = DBConnection.getConnection();


//================================================================================================

    private static String createNewAesKeyString()
    {
       try 
        {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(256);                                                                 // telling keyGenerator to generate 256bit AES key
            
            SecretKey aesKey = keyGenerator.generateKey();   
            String aesKeyString = Base64.getEncoder().encodeToString(aesKey.getEncoded());          // convert object of aesKey into string so we can print it in string form
            // System.out.println("\nAES key is : "+aesKeyString); 
            return aesKeyString;                                    // random aes key generate ho rhi h

        } 
        catch (Exception e) { System.out.println("error in createAESkey() method "); e.printStackTrace();   }

        return null;
    }

//================================================================================================

    private static boolean groupAlreadyExists(String groupName) 
{
    try
    {
        String folderPath = "C:\\Users\\shubh\\OneDrive\\Desktop\\terminalChat\\backend\\src\\version1TerminalChat\\keys";
        File file = new File(folderPath, "group_aes_keys.txt");

        // If the file does not exist yet, then no groups exist.
        if (!file.exists())
        {
            return false;
        }

        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;

        while ((line = reader.readLine()) != null)
        {
            String[] parts = line.split("=", 2);
            if (parts.length == 2)
            {
                String existingGroupName = parts[0];
                if (existingGroupName.equals(groupName))
                {
                    reader.close();
                    return true;
                }
            }
        }
        reader.close();
    }
    catch (Exception e){System.out.println("Error in groupAlreadyExists()");e.printStackTrace();}

    return false;
}

//================================================================================================

    private static void storeAesKeyInTextFile(String aesKeyString, String groupName)
    {
        try
        {
            // groupName = Aes key
            String folderPath = "C:\\Users\\shubh\\OneDrive\\Desktop\\terminalChat\\backend\\src\\version1TerminalChat\\keys";
            File folder = new File(folderPath);
            if(!folder.exists())//if folder doesnot exist...
            {
                folder.mkdir(); 
            }
            
            File file = new File(folder, "group_aes_keys.txt");
            FileWriter writer = new FileWriter(file, true); // append mode
            writer.write(groupName+"="+aesKeyString + System.lineSeparator());
            writer.close();
            System.out.println("group_name and aes key is stored in text file, successfully");
        }
        catch(Exception e){System.out.println("error : storeAesKeyInTextFile() method in NewGroupMaker.java "); e.printStackTrace();}
    }

//================================================================================================

    private static void storeTheGroupInDB(String group_name)
    {
        try 
        {
            PreparedStatement ps = connection.prepareStatement("INSERT INTO group_list (group_name) VALUES (?)");
            ps.setString(1, group_name);
            int rows = ps.executeUpdate();
            if(rows>0 && rows<2)
            {
                System.out.println(rows+" rows affected");
                System.out.println("group details is filled in DB");
            }
            else{System.out.println("failed to insert group details in db");}

        } 
        catch (Exception e)
        {
            System.out.println("");
            e.printStackTrace();
        }
    }

//================================================================================================
    
    public static void createNewGroup()
    {
        String groupName = "Anonymous";
        try
        {
            String aesKeyString = createNewAesKeyString();
            if(groupAlreadyExists(groupName))
            {
               System.out.println("Group already exist");
               return; 
            }
            storeAesKeyInTextFile(aesKeyString, groupName); // store the aes key in text file with group_name
            storeTheGroupInDB(groupName);
        }
        catch(Exception e){System.out.println("error : createNewGroup() method in classs, NewGroupMaker.java ");e.printStackTrace();}
    }
    public static void main(String[] args) 
    {
        createNewGroup();
    }
}
