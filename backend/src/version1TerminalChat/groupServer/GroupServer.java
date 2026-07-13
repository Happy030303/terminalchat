package version1TerminalChat.groupServer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;

import version1TerminalChat.database.DBConnection;
import version1TerminalChat.loginpage.TerminalLogin;

public class GroupServer 
{
    static ServerSocket serverSocket;
    static Socket socket;
    private static Connection connection = DBConnection.getConnection();
    static HashMap<String, EachGroupUserHandler> hashMap = new HashMap<String, EachGroupUserHandler>() ;

//==================================================================================================

    private static boolean userVerification(String username)
    {
        try
        {
            PreparedStatement ps = connection.prepareStatement("SELECT 1 from login where username = ? limit 1"); // it will return 1 if the condition is verifies true that is username = "elon"
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            return rs.next(); // rs.next() checks is there next() row or not, if yes returns true, else false.
        }
        catch(Exception e){System.out.println("error : \"userVerification()\" method in calss \"GroupServer.java\" class ");e.printStackTrace();}

        return false;
    }
    
//==================================================================================================

    private static boolean isGroupMember(String username)
    {
        try
        {
            PreparedStatement ps = connection.prepareStatement("select 1 from users_in_group where username = ? limit 1");
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if(rs.next())
            {
                System.out.println(username+ "is member of this group");
                return true;
            }
            System.out.println(username+" is not the member of this group");
        }
        catch(Exception e){System.out.println("error : method : \"isGroupMember()\" , class : \"GroupServer.java\" ");}
        return false;
    }

//==================================================================================================

    public static void main(String[] args) 
    {
            try 
            {
                serverSocket = new ServerSocket(4001);
                System.out.println("server started...");
                
                while (true) // server thread will keep listening 
                {
                    socket = serverSocket.accept();// wait till any connection is recieved
                    BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);
                    
                   //---------------------------------
                   String username = br.readLine();
                   //---------------------------------

                    // Verification
                   //=================================================================================================================
                   // verifing the user is a user of our platform or not.
                   if(!userVerification(username))
                    {
                        System.out.println(username+" need to create his account, ");
                        System.out.println(username+" user not found in our platform database");
                        
                        TerminalLogin.homePage();
                        return;
                    }

                    System.out.println(username+" user is memeber of our platform");
                    
                    if(!isGroupMember(username))
                    {
                        System.out.println(username+" user is not the member of this group");
                        return;
                    }
                    
                    System.out.println(username+" user is member of this group");
                            
                    //=================================================================================================================
                    // UserHandler object in server to handler each user, sir.
                    EachGroupUserHandler userHandler = new EachGroupUserHandler(br, pw, username, hashMap); // sending the printwriter and bufferedReader of the userHandler like "elonUserHandler's" pw and br, "happyUserHandler's" pw and br.
                    Thread thread = new Thread(userHandler); // making userHandler thread for each user who sends request to server like kalam userHandler Thread, Happy userHandler Thread...
                    thread.start(); 

                
                }
            } 
            catch (Exception e) { System.out.println("error in : \"main() \" method in,  \" GroupServer.java \"class "); e.printStackTrace();   }
    }
}
