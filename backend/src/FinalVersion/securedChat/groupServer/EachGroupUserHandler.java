package securedChat.groupServer;

import java.io.BufferedReader;
import java.io.PrintWriter;


public class EachGroupUserHandler implements Runnable
{
    String username;
    BufferedReader br ;
    PrintWriter pw;
    String userAESKeyString;

    EachGroupUserHandler(BufferedReader br, PrintWriter pw, String username)
    {
        this.br = br;
        this.pw = pw;
        this.username = username;
    }

    // private String getEncryptedAESKey()
    // {
    //    return null;
    // }

    @Override
    public void run()
    {

        // getEncryptedAESKey();

        //======================================================================================================================

        // we need to structure of UserHandler so that each UserHandler will recieve and listen to there msg in the server or DB...
        


        
        
    }
}
