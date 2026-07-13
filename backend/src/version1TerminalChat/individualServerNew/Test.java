package version1TerminalChat.individualServerNew;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import version1TerminalChat.database.DBConnection;

public class Test 
{
    private static Connection connection = DBConnection.getConnection();

    private static boolean isGroupMember(String username)
    {
        try
        {
            PreparedStatement ps = connection.prepareStatement("select 1 from users_in_group where username = ? limit 1");
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if(rs.next())
            {
                System.out.println(username+ " is member of this group");
                return true;
            }
            System.out.println(username+" is not the member of this group");
        }
        catch(Exception e){System.out.println("error : method : \"isGroupMember()\" , class : \"GroupServer.java\" ");}
        return false;
    }
    public static void main(String[] args) 
    {
        String username = "nikola";
            isGroupMember(username);
    }
}
