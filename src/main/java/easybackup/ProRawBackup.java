package easybackup;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Rien
 */
public class ProRawBackup {

    private Utils utils;
    private Logger log = LogManager.getLogger(ProRawBackup.class);

    ProRawBackup(Utils utils) {

        this.utils = utils;
    }

    /**
     * Find for each JPG file the corresponding ProRaw file and copy it to
     * the NAS when it does not exist there yet.
     */
    public int proRawBackup(String ProRawSourceDir, String ProRawTargetDir, String jpgDir, long since, boolean verbose) throws Exception {
        int result = 0;
        Map<String, File> ProRawSourceFilesMap = new ConcurrentHashMap<String, File>();
        Map<String, File> jpgFilesMap = new ConcurrentHashMap<String, File>();

        log.trace("Create a list of all ProRaw source files..");
        utils.listImageFiles(ProRawSourceDir, ProRawSourceFilesMap, "DNG", since);
        log.debug("Number of source ProRaw files: " + ProRawSourceFilesMap.size());

        log.trace("Remove from list of ProRaw source files that have a backup file..");
        ProRawSourceFilesMap = utils.removeFilesFromMapThatAreInDirectory(ProRawSourceFilesMap, new File(ProRawTargetDir), "DNG");
        log.trace("Number of source ProRaw files that do have a backup: " + ProRawSourceFilesMap.size());

        log.trace("Remove from list of ProRaw source files that do not have a corresponding JPG file..");
        ProRawSourceFilesMap = utils.removeFilesFromMapThatAreNotInDirectory(ProRawSourceFilesMap, new File(jpgDir), "jpg");
        result = ProRawSourceFilesMap.size();
        //log.debug("Number of source ProRaw files that have a corresponding JPG file and do not have a backup: " + result);

        for (Map.Entry<String, File> entry : ProRawSourceFilesMap.entrySet()) {
            Path ProRawSourceFilePath = Paths.get(entry.getValue().getAbsolutePath());
            Path ProRawSourceDirPath = Paths.get(ProRawSourceDir);
            Path ProRawTargetDirPath = Paths.get(ProRawTargetDir);
            Path relativePath = ProRawSourceDirPath.relativize(ProRawSourceFilePath);
            Path target = Paths.get(ProRawTargetDirPath.toString() + File.separator + relativePath.toString());

            Path targetDir = target.getParent();
            new File(targetDir.toString()).mkdirs();
            if (verbose) {
                log.debug("Backup " + ProRawSourceFilePath + " to " + target);
            } else {
                log.info("Backup ProRaw file: " + relativePath);
                Files.copy(ProRawSourceFilePath, target);
            }

        }
        //utils.saveFileList(ProRawSourceFilesMap, "");
//        log.debug("Create a list of all JPG files..");
//        utils.listImageFiles(jpgDir, jpgFiles, "iPhone 11", since);
//
//        SortedSet<String> sortedJPGset = new TreeSet<String>(jpgFiles.keySet());
//        Iterator<String> it = sortedJPGset.iterator();
//        while (it.hasNext()) {
//            String key = it.next();
//            if (ProRawSourceFiles.containsKey(key)) {
//                Path ProRawSourceFilePath = Paths.get(ProRawSourceFiles.get(key).getAbsolutePath());
//                Path ProRawSourceDirPath = Paths.get(ProRawSourceDir);
//                Path ProRawTargetDirPath = Paths.get(ProRawTargetDir);
//                Path relativePath = ProRawSourceDirPath.relativize(ProRawSourceFilePath);
//                Path target = Paths.get(ProRawTargetDirPath.toString() + File.separator + relativePath.toString());
//                if (!target.toFile().exists()) {
//                    log.info("Backup ProRaw file: " + relativePath);
//                    Path targetDir = target.getParent();
//                    new File(targetDir.toString()).mkdirs();
//                    Files.copy(ProRawSourceFilePath, target);
//                    log.debug("Backup " + ProRawSourceFilePath + " to " + target);
//                    result++;
//                } else {
//                    //log.trace("Exists in backup: " + ProRawSourceFilePath);
//                }
//            } else {
//                File f = jpgFiles.get(key);
//                //log.trace("Not found in ProRaw directory (key=" + key + ") : " + f.getAbsolutePath());
//            }
//        }

        return result;
    }
}
