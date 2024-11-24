package easybackup;

/**
 * java -classpath . easybackup.Main jpg "D:/Mijn afbeeldingen/2004" "K:/Backup/Mijn afbeeldingen/2004" [-a] [-s]
 *
 * @author Rien 2021
 */
public class Main {
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        if (args.length < 1) {
            usage();
            System.exit(-1);

        }

        Utils utils = new Utils();
        boolean verbose = false;
        String sinceProp = utils.loadProperty("since");
        long since = sinceProp != null ? Long.parseLong(sinceProp) : System.currentTimeMillis() - 900000000;

        if (args[args.length - 1].equals("-a")) {
            System.out.println("Backup all files, ingore timestamp speedup");
            since = 0;
        } else {
            System.out.println("Checked new files since last backup of " + Utils.humanDateTime(since));
        }

        int numberOfFilesBackupped = 0;

        try {
            if (args[0].equals("jpg") && args.length >= 3) {
                System.out.println("Backup all JPG files that do not have a related DNG file");
                System.out.println("from: " + args[1]);
                System.out.println("to  : " + args[2]);
                JPGBackup jb = new JPGBackup(utils);
                numberOfFilesBackupped = jb.jpgBackup(args[1], args[2], since);

            } else if (args[0].equals("dng") && args.length >= 4) {
                System.out.println("Backup all DNG files that have a related JPG file");
                System.out.println("from    : " + args[1]);
                System.out.println("to      : " + args[2]);
                System.out.println("jpg dir : " + args[3]);
                DNGBackup db = new DNGBackup(utils);
                numberOfFilesBackupped = db.dngBackup(args[1], args[2], args[3], since);
            } else if (args[0].equals("heic") && args.length >= 4) {
                System.out.println("Backup all HEIC files that have a related JPG file");
                System.out.println("from    : " + args[1]);
                System.out.println("to      : " + args[2]);
                System.out.println("jpg dir : " + args[3]);
                HEICBackup db = new HEICBackup(utils);
                numberOfFilesBackupped = db.heicBackup(args[1], args[2], args[3], since);
            } else if (args[0].equals("proraw") && args.length >= 4) {
                System.out.println("Backup all ProRaw files that have a related JPG file");
                System.out.println("from    : " + args[1]);
                System.out.println("to      : " + args[2]);
                System.out.println("jpg dir : " + args[3]);
                ProRawBackup db = new ProRawBackup(utils);
                numberOfFilesBackupped = db.proRawBackup(args[1], args[2], args[3], since, verbose);
            } else if (args[0].equals("ftp") && args.length >= 5) {
                System.out.println("Transfer files via FTP");
                System.out.println("from    : " + args[1]);
                System.out.println("to      : " + args[2]);
                System.out.println("server  : " + args[3]);
                System.out.println("user    : " + args[4]);
                System.out.println("password: " + args[5]);
                FTPTransfer ft = new FTPTransfer(utils);
                numberOfFilesBackupped = ft.ftpTransfer(args[1], args[2], args[3], args[4], args[5], since);
            } else if (args[0].equals("-s")) {
                System.out.println("Set timestamp, next time files after now are transfered");
            } else if (args[0].equals("-v")) {
                System.out.println("Set timestamp, next time files after now are transfered");
            } else {
                usage();
            }
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            ex.printStackTrace(System.out);
            return;
        }


        if (numberOfFilesBackupped > 0) {
            System.out.println("Ready, number of files backupped: " + numberOfFilesBackupped);
            utils.saveProperty("since", Long.toString(System.currentTimeMillis()));
        } else {
            System.out.println("Ready, no files found to backup");
        }
    }

    public static void usage() {
        System.out.println("Usage:");
        System.out.println("easybackup jpg <jpg source dir> <jpg backup dir> [-a]");
        System.out.println("easybackup dng <dng source dir> <dng backup dir> <jpg dir> [-a]");
        System.out.println("-a: backup all files, ignore timestamp speedup");
    }

}
