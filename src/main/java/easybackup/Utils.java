/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package easybackup;

import com.drew.imaging.jpeg.JpegMetadataReader;
import com.drew.imaging.jpeg.JpegProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.xmp.XmpDirectory;
import org.apache.commons.configuration.AbstractFileConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.sanselan.ImageReadException;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;


/**
 * @author Rien Bouw
 */
public class Utils {
    private Logger log = LogManager.getLogger(Utils.class);

    private String MODEL = "ILCE-7M2";
    private FileFilter imagesFilter = new FileFilter() {
        @Override
        public boolean accept(File f) {

            if (f.getAbsolutePath().contains("Originals")
                    || f.getAbsolutePath().contains("iPhone iPad")
                    || f.getAbsolutePath().contains("1920x1080")
                    || f.getAbsolutePath().contains(".picasaoriginals")
                    || f.getAbsolutePath().contains(".DS_Store")) {
                return false;
            }

            if (f.isDirectory()) {
                return true;
            }

            String n = f.getName();
            return n.endsWith(".jpg") || n.endsWith(".JPG")
                    || n.endsWith(".dng") || n.endsWith(".DNG");
        }
    };

    public boolean listImageFiles(String dir, Map<String, File> map, long since) throws Exception {
        Path imagesPath = Paths.get(dir);
        return listImageFiles(imagesPath, map, MODEL, since);
    }

    /**
     * List all image files (not) made with camera of the given model.
     *
     * @param dir
     * @param map
     * @param model list for this model, or prefix with ! to list not made with
     *              this model
     * @throws Exception
     */
    public boolean listImageFiles(String dir, Map<String, File> map, String model, long since) throws Exception {
        Path imagesPath = Paths.get(dir);
        return listImageFiles(imagesPath, map, model, since);
    }

    private boolean listImageFiles(Path imagesPath, Map<String, File> map, String model, long since) throws IOException {
        boolean successfull = true;

        log.debug("listImagesFiles from " + imagesPath);
        Files.walk(imagesPath)
                .parallel()
                .filter(Files::isDirectory)
                .forEach(path -> {
                            try {
                                long modmillis = Files.getLastModifiedTime(path).toMillis();
                                if (modmillis > since) {
                                    log.debug("Scan " + path.getFileName() + (since > 0 ? (" " + humanDateTime(modmillis) + " > " + humanDateTime(since)) : ""));
                                    listImageFilesOneDirectory(path, map, model);
                                } else {
                                    log.trace("Skipped " + path.getFileName() + " not modified " + humanDateTime(modmillis) + " > since " + humanDateTime(since));
                                }

                            } catch (IOException ex) {
                                log.error("Exception " + ex);

                            }
                        }
                );

        return successfull;
    }

    public void saveFileList(Map<String, File> map, String name) {
        for (Map.Entry<String, File> entry : map.entrySet()) {
            log.debug(entry.getKey() + "/" + entry.getValue());
        }
    }

    private boolean listImageFilesOneDirectory(Path imagesPath, Map<String, File> map, String model) throws IOException {
        AtomicBoolean successFull = new AtomicBoolean(true);

        Files.walk(imagesPath)
                .filter(path -> filterTypeAndCamera(path, model))
                .parallel()
                .forEach(path -> {
                            File fileToMap = path.toFile();
                            String id = fileNameWithoutExtension(fileToMap);
                            File f = map.get(id);
                            if (f != null) {
                                if (!haveSameOriginalDate(f, fileToMap)) {
                                    // Two files have the same name but different orginal date, so must be different pics;
                                    log.warn("Duplicate name for different images:" + f.getAbsolutePath() + " == " + fileToMap.getAbsolutePath());
                                    successFull.set(false);
                                }
                            }
                            //log.trace("Add to map (key=" + id + "): " + fileToMap);
                            map.put(id, fileToMap);
                        }
                );

        return successFull.get();
    }


    public boolean listFiles(Path listPath, Map<String, File> map, long since) throws IOException {
        boolean successfull = true;

        Files.walk(listPath)
                .parallel()
                .filter(Files::isDirectory)
                .forEach(path -> {
                            try {
                                listFilesOneDirectory(path, map, since);

                            } catch (IOException ex) {

                            }
                        }
                );

        return successfull;
    }

    private boolean listFilesOneDirectory(Path imagesPath, Map<String, File> map, long since) throws IOException {
        AtomicBoolean successFull = new AtomicBoolean(true);

        Files.walk(imagesPath)
                .collect(Collectors.toList())
                .parallelStream()

                //   Files.walk(imagesPath)
                //        .parallel()
                .filter(Files::isRegularFile)
                .forEach(path -> {
                    File fileToMap = path.toFile();
                    try {
                        long modmillis = Files.getLastModifiedTime(path).toMillis();
                        if (modmillis > since) {
                            //log.trace(path.getFileName() + " " + humanDateTime(modmillis) + " > " + humanDateTime(since));
                            String id = fileNameWithoutExtension(fileToMap);
                            File f = map.get(id);
                            map.put(fileToMap.toString(), fileToMap);
                        } else {
                            //System.out.println("Skip " + path.getFileName() + " " + humanDateTime(modmillis) + " > " + humanDateTime(since));
                        }
                    } catch (IOException ex) {

                    }
                });

        return successFull.get();
    }

    private boolean isIgnorablePhotoFile(File file) {
        return file.getAbsolutePath().contains("Originals")
                || file.getAbsolutePath().contains("iPhone iPad")
                || file.getAbsolutePath().contains("1920x1080")
                || file.getAbsolutePath().contains(".picasaoriginals");
    }

    boolean filterTypeAndCamera(Path path, String model) {
        File file = path.toFile();
        boolean passThisFile = true;

        if (isIgnorablePhotoFile(file)) {
            passThisFile = false;
        }

        if (passThisFile) {
            if (file.isDirectory()) {
                passThisFile = false;
            }
        }

        if (passThisFile && model != null && model.equals("HEIC")) {
            String n = file.getName();
            if (!n.endsWith(".HEIC")) {
                //log.trace("not HEIC " + n);
                passThisFile = false;
            }
        }

        if (passThisFile && model != null && !model.equals("HEIC")) {
            String n = file.getName();
            if (!n.endsWith(".jpg") && !n.endsWith(".JPG")
                    && !n.endsWith(".dng") && !n.endsWith(".DNG")) {
                passThisFile = false;
            }
        }

        if (passThisFile && model != null && !model.equals("HEIC") && fileExtension(file).equals("jpg")) {
            Metadata metadata = null;
            try {
                try {
                    metadata = JpegMetadataReader.readMetadata(file);

                } catch (IOException ex) {
                    log.error(ex);
                }

            } catch (JpegProcessingException ex) {
                log.error(ex);
            }
            String modelExif = null;

            if (metadata == null) {
                //log.debug("Skip, no metadata found for " + path);
                passThisFile = false;
            }
            if (passThisFile) {
                Directory directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
                if (directory == null) {
                    directory = metadata.getFirstDirectoryOfType(XmpDirectory.class);
                }
                if (directory != null) {
                    String tag_model = directory.getDescription(ExifIFD0Directory.TAG_MODEL);
                    if (tag_model != null) {
                        modelExif = tag_model;
                    }
                }
                if (modelExif != null) {
                    if (model.startsWith("!")) {
                        // Ignore file when created with given model
                        //if (modelExif.startsWith(model.substring(1))) {
                        if (model.contains(modelExif)) {
                            log.trace("Ignore  " + modelExif + " for " + path);
                            passThisFile = false;
                        }
                    } else {
                        // Skip file when not created with given model
                        if (!modelExif.startsWith(model)) {
                            //log.trace("Skip " + modelExif + " for " + path);
                            passThisFile = false;
                        }
                    }
                } else {
                    //log.trace("Skip, no exif found for " + path);
                    passThisFile = false;
                }
            }
        }
        return passThisFile;
    }

    // @return true when successful
    private boolean listImageFilesSlow(File dir, Map<String, File> map, String model, long since) throws Exception {
        boolean hasError = false;
        File[] files = dir.listFiles(imagesFilter);
        if (files == null) {
            throw new Exception(dir.getAbsolutePath() + " is an invalid path");
        }
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            Path path = Paths.get(file.getAbsolutePath());
            if (file.isDirectory()) {
                listImageFiles(path, map, model, since);
            } else {

                if (fileExtension(file).equals("jpg")) {
                    String modelInFile = model(file);
                    if (model.startsWith("!")) {
                        // Ignore file when created with given model
                        if (modelInFile.startsWith(model.substring(1))) {
                            continue;
                        }
                    } else {
                        // Skip file when not created with given model
                        if (!modelInFile.startsWith(model)) {
                            //System.out.println("Skip " + file.getAbsolutePath() + " " + model + "<>" + MODEL);
                            continue;
                        }
                    }
                }
                String id = fileNameWithoutExtension(file);

                File f = map.get(id);
                if (f != null) {
                    if (!haveSameOriginalDate(f, files[i])) {
                        // Two files have the same name but different orginal date, so must be different pics;
                        log.warn("Duplicate name for different images:" + f.getAbsolutePath() + " == " + files[i].getAbsolutePath());
                        hasError = true;
                    }
                }
                map.put(id, files[i]);
            }
        }
        return !hasError;
    }

    // Duplicate is allowed when both files have the same exif original date
    private boolean haveSameOriginalDate(File original, File duplicate) {
        boolean haveSameOriginalDate = true;
        try {
            String o = originalDate(original);
            String d = originalDate(duplicate);
            if (o == null && d == null) {
                haveSameOriginalDate = true;
            } else if (o == null || d == null) {
                haveSameOriginalDate = false;
            } else {
                haveSameOriginalDate = o.equals(d);
            }
        } catch (ImageReadException ex) {
            log.error(ex);
        } catch (IOException ex) {
            log.error(ex);
        }
        return haveSameOriginalDate;
    }

    private String fileNameWithoutExtension(File file) {
        String name;
        name = file.getName();
        int idx = name.lastIndexOf(".");
        if (idx > -1) {
            name = name.substring(0, idx);
        }
        return name;
    }

    private String fileExtension(File file) {
        String ext = "", name;
        name = file.getName();
        int idx = name.lastIndexOf(".");
        if (idx > -1) {
            ext = name.substring(idx + 1).toLowerCase();
        }
        return ext;
    }

    private String model(File file) throws ImageReadException, IOException {
        String model = "";
        Metadata metadata = null;
        try {
            metadata = JpegMetadataReader.readMetadata(file);
        } catch (JpegProcessingException ex) {
            throw new ImageReadException("File" + file.getAbsolutePath() + " is not a jpg file. " + ex.getMessage());
        }

        Directory directory = metadata.getFirstDirectoryOfType (ExifIFD0Directory.class);
        if (directory == null) {
            directory = metadata.getFirstDirectoryOfType (XmpDirectory.class);
            if (directory == null) {
                return model;
            }
        }

        String tag_model = directory.getDescription(ExifIFD0Directory.TAG_MODEL);

        if (tag_model == null) {
            return model;
        } else {
            model = tag_model;
        }

        return model;
    }

    private String originalDate(File file) throws ImageReadException, IOException {

        String dateOriginal = null;

        Metadata metadata = null;

        if (fileExtension(file).equals("jpg")) {
            try {
                metadata = JpegMetadataReader.readMetadata(file);
            } catch (JpegProcessingException ex) {
                throw new ImageReadException("Can't read jpeg meta data from " + file.getAbsolutePath() + ": " + ex.getMessage());
            }

            Directory directory = metadata.getFirstDirectoryOfType (ExifSubIFDDirectory.class);
            if (directory != null) {
                dateOriginal = directory.getDescription(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
            }
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm");
            dateOriginal = sdf.format(file.lastModified());
        }
        return dateOriginal;
    }

    private void showAllMetadata(File file) throws IOException {
        Metadata metadata = null;
        try {
            metadata = JpegMetadataReader.readMetadata(file);
        } catch (JpegProcessingException ex) {
            ex.printStackTrace();
        }
        // iterate through metadata directories
        Iterable<Directory> directories = metadata.getDirectories();
        Iterator<Directory> it = directories.iterator();
        if (it != null) {
            while (it.hasNext()) {
                Directory directory = (Directory) it.next();
                // iterate through tags and print to System.out
                Collection<Tag> tagsCollection = directory.getTags();
                Iterator tags = tagsCollection.iterator();
                while (tags.hasNext()) {
                    Tag tag = (Tag) tags.next();
                    //System.out.println(directory.getClass().getName() + " " + tag.getTagName() + " : " + tag.getDescription());

                }
            }
        }
    }

    public static String humanDateTime(long millis) {
        Date date = new Date(millis);
        DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        return formatter.format(date);
    }


    public void saveProperty(String name, String value) {
        final AbstractFileConfiguration config = new PropertiesConfiguration();
        List<String> lines = new ArrayList<String>();
        config.setFileName("easybackup.properties");
        try {
            config.load();
        } catch (final ConfigurationException exception) {
        }
        for (Iterator it = config.getKeys(); it.hasNext(); ) {
            String key = (String) it.next();
            if (!key.equals(name)) {
                lines.add(key + "=" + config.getProperty(key));
            }

        }
        lines.add(name + "=" + value);
        try {
            final Path file = Paths.get("easybackup.properties");
            Files.write(file, lines, StandardCharsets.UTF_8);
        } catch (final IOException exception) {
            exception.printStackTrace();
        }

    }

    public String loadProperty(String name) {
        final AbstractFileConfiguration config = new PropertiesConfiguration();
        boolean result = false;
        config.setFileName("easybackup.properties");
        try {
            config.load();
        } catch (final ConfigurationException exception) {
        }
        return (String) config.getProperty(name);
    }

    public Map<String, File> removeFilesFromMapThatAreInDirectory(Map<String, File> map, File dirFile, String extension) throws IOException {
        removeFilesFromMap(map, null, dirFile, true, extension);
        return map;
    }

    public Map<String, File> removeFilesFromMapThatAreNotInDirectory(Map<String, File> map, File dirFile, String extension) throws IOException {
        Map<String, File> keepMap = new ConcurrentHashMap<String, File>();

        removeFilesFromMap(map, keepMap, dirFile, false, extension);
        //saveFileList(keepMap, "");
        return keepMap;
    }

    private boolean removeFilesFromMap(Map<String, File> map, Map<String, File> keepMap, File dirFile, boolean inOrNotIn, String extension) throws IOException {
        boolean successfull = true;
        //log.debug("Remove Files that are in " + dirFile);


        File listFile[] = dirFile.listFiles();
        if (listFile != null) {
            for (int i = 0; i < listFile.length; i++) {
                if (listFile[i].isDirectory()) {
                    successfull = removeFilesFromMap(map, keepMap, listFile[i], inOrNotIn, extension);
                } else {
                    if (listFile[i].getName().toLowerCase().endsWith(extension.toLowerCase())) {
                        String key = fileNameWithoutExtension(listFile[i]);
                        if (inOrNotIn) {
                            if (map.containsKey(key)) {
                                //System.out.println(key);
                                map.remove(key);
                            }
                        } else {
                            File file = map.get(key);
                            if (file != null) {
                                keepMap.put(key, file);
                            }
                        }
                    }
                }
            }
        }
        return successfull;
    }
}
