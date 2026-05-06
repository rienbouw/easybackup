package easybackup;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author Rien
 */
public class DNGBackup {

    private Utils utils;
    
    DNGBackup(Utils utils) {
        this.utils = utils;
    }
    /**
     * Find for each JPG file the corresponding DNG file and copy it to
     * the NAS when it does not exist there yet.
     */
    public int dngBackup(String dngSourceDir, String dngTargetDir, String jpgDir, long since) throws Exception {
        int result = 0;
        Map<String, File> dngSourceFiles = new ConcurrentHashMap<String, File>();
        Map<String, File> jpgFiles = new ConcurrentHashMap<String, File>();

        //System.out.println("Create a list of all DNG files..");
        utils.listImageFiles(dngSourceDir, dngSourceFiles, since);

        //System.out.println("Create a list of all JPG files..");
        utils.listImageFiles(jpgDir, jpgFiles, since);

        Path dngSourceDirPath = Paths.get(dngSourceDir);
        Path dngTargetDirPath = Paths.get(dngTargetDir);
        for (String key : jpgFiles.keySet()) {
            File dngSourceFile = dngSourceFiles.get(key);
            if (dngSourceFile != null) {
                Path dngSourceFilePath = dngSourceFile.toPath();
                Path relativePath = dngSourceDirPath.relativize(dngSourceFilePath);
                Path target = dngTargetDirPath.resolve(relativePath);
                if (!Files.exists(target)) {
                    System.out.println("Backup DNG file: " + relativePath);
                    Path targetDir = target.getParent();
                    Files.createDirectories(targetDir);
                    Files.copy(dngSourceFilePath, target);
                    result++;
                } else {
                    //System.out.println("Backup present: " + target.getFileName());
                }
            } else {
                File f = jpgFiles.get(key);
                //System.out.println("Not found: " + f.getAbsolutePath());
            }
        }

        return result;
    }
}
