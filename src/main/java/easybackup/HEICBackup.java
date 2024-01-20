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
public class HEICBackup {

    private Utils utils;
    private Logger log = LogManager.getLogger(HEICBackup.class);

    HEICBackup(Utils utils) {

        this.utils = utils;
    }

    /**
     * Find for each JPG file the corresponding HEIC file and copy it to
     * the NAS when it does not exist there yet.
     */
    public int heicBackup(String heicSourceDir, String heicTargetDir, String jpgDir, long since) throws Exception {
        int result = 0;
        Map<String, File> heicSourceFilesMap = new ConcurrentHashMap<String, File>();
        Map<String, File> jpgFilesMap = new ConcurrentHashMap<String, File>();

        log.trace("Create a list of all HEIC source files..");
        utils.listImageFiles(heicSourceDir, heicSourceFilesMap, "HEIC", since);
        log.debug("Number of source HEIC files: " + heicSourceFilesMap.size());

        log.trace("Remove from list of HEIC source files that have a backup file..");
        heicSourceFilesMap = utils.removeFilesFromMapThatAreInDirectory(heicSourceFilesMap, new File(heicTargetDir), "heic");
        log.trace("Number of source HEIC files that do have a backup: " + heicSourceFilesMap.size());

        log.trace("Remove from list of HEIC source files that do not have a corresponding JPG file..");
        heicSourceFilesMap = utils.removeFilesFromMapThatAreNotInDirectory(heicSourceFilesMap, new File(jpgDir), "jpg");
        result = heicSourceFilesMap.size();
        //log.debug("Number of source HEIC files that have a corresponding JPG file and do not have a backup: " + result);

        for (Map.Entry<String, File> entry : heicSourceFilesMap.entrySet()) {
            Path heicSourceFilePath = Paths.get(entry.getValue().getAbsolutePath());
            Path heicSourceDirPath = Paths.get(heicSourceDir);
            Path heicTargetDirPath = Paths.get(heicTargetDir);
            Path relativePath = heicSourceDirPath.relativize(heicSourceFilePath);
            Path target = Paths.get(heicTargetDirPath.toString() + File.separator + relativePath.toString());
            log.info("Backup HEIC file: " + relativePath);
            Path targetDir = target.getParent();
            new File(targetDir.toString()).mkdirs();
            Files.copy(heicSourceFilePath, target);
            //log.debug("Backup " + heicSourceFilePath + " to " + target);
        }
        //utils.saveFileList(heicSourceFilesMap, "");
//        log.debug("Create a list of all JPG files..");
//        utils.listImageFiles(jpgDir, jpgFiles, "iPhone 11", since);
//
//        SortedSet<String> sortedJPGset = new TreeSet<String>(jpgFiles.keySet());
//        Iterator<String> it = sortedJPGset.iterator();
//        while (it.hasNext()) {
//            String key = it.next();
//            if (heicSourceFiles.containsKey(key)) {
//                Path heicSourceFilePath = Paths.get(heicSourceFiles.get(key).getAbsolutePath());
//                Path heicSourceDirPath = Paths.get(heicSourceDir);
//                Path heicTargetDirPath = Paths.get(heicTargetDir);
//                Path relativePath = heicSourceDirPath.relativize(heicSourceFilePath);
//                Path target = Paths.get(heicTargetDirPath.toString() + File.separator + relativePath.toString());
//                if (!target.toFile().exists()) {
//                    log.info("Backup HEIC file: " + relativePath);
//                    Path targetDir = target.getParent();
//                    new File(targetDir.toString()).mkdirs();
//                    Files.copy(heicSourceFilePath, target);
//                    log.debug("Backup " + heicSourceFilePath + " to " + target);
//                    result++;
//                } else {
//                    //log.trace("Exists in backup: " + heicSourceFilePath);
//                }
//            } else {
//                File f = jpgFiles.get(key);
//                //log.trace("Not found in HEIC directory (key=" + key + ") : " + f.getAbsolutePath());
//            }
//        }

        return result;
    }
}
