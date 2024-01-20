/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package easybackup;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Rien
 */
public class JPGBackup {

    private Utils utils;
    private Logger log = LogManager.getLogger(JPGBackup.class);

    JPGBackup(Utils utils) {
        this.utils = utils;
    }

    /**
     * Backup each JPG file that does not have a related DNG file
     * when it does not exist in the backup already.
     */
    public int jpgBackup(String jpgDir, String backupDir, long since) throws Exception {


        int result = 0;
        String MODEL = "Canon EOS|iPhone 11";
        Map<String, File> jpgFiles = new ConcurrentHashMap<String, File>();
        if (!this.utils.listImageFiles(jpgDir, jpgFiles, "!" + MODEL, since)) {
            throw new Exception("Aborted: duplicate file names found");
        }
        SortedSet<String> sortedJPGset = new TreeSet<String>(jpgFiles.keySet());
        Iterator<String> it = sortedJPGset.iterator();
        while (it.hasNext()) {
            String key = it.next();
            Path sourceFilePath = Paths.get(jpgFiles.get(key).getAbsolutePath());
            Path sourceDirPath = Paths.get(jpgDir);
            Path targetDirPath = Paths.get(backupDir);
            Path relativePath = sourceDirPath.relativize(sourceFilePath);
            Path target = Paths.get(targetDirPath.toString() + File.separator + relativePath.toString());
            if (!target.toFile().exists()) {
                System.out.println("Backup: " + relativePath);
                Path targetDir = target.getParent();
                new File(targetDir.toString()).mkdirs();
                log.debug("copy(" + sourceFilePath + ", " + target + ")");
                //Files.copy(sourceFilePath, target);
                result++;
            } else {
                log.trace("already backuped(" + sourceFilePath + ", " + target + ")");
            }
        }
        return result;
    }
}
