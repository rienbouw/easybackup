/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package easybackup;

import org.apache.commons.net.ftp.FTPClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author Rien
 */
public class FTPTransfer {

    private Utils utils;

    FTPTransfer(Utils utils) {
        this.utils = utils;
    }
   
    /**
     * Backup each JPG file that does not have a related DNG file
     * when it does not exist in the backup already.
     */
    public int ftpTransfer(String localDir, String remoteDir, String server, String username, String password, long since) throws Exception {

        FTPClient ftpClient = new FTPClient();
        ftpClient.connect(server);
        ftpClient.login(username, password);



        Map<String, File> files = new ConcurrentHashMap<String, File>();
        if (!this.utils.listFiles(Paths.get(localDir), files, since)) {
             throw new Exception("Aborted: duplicate file names found");
        }

        for (Map.Entry<String, File> entry : files.entrySet()) {
            File file = entry.getValue();
                 InputStream inputStream = new FileInputStream(file);
                 String remoteFile = remoteDir + file.getAbsolutePath();
                 remoteFile = remoteFile.replace(localDir,"");
               boolean uploaded = ftpClient.storeFile(remoteFile, inputStream);
            if (!uploaded) {
                System.out.println("not uploaded: " + remoteFile);
            } else {
                System.out.println("uploaded: " + remoteFile);

            }

            inputStream.close();
        }

        ftpClient.logout();
        ftpClient.disconnect();

        return files.entrySet().size();

    }
}
