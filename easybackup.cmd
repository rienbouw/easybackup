set PATH=%PATH%;C:\Users\rienb\.jdks\openjdk-21.0.2\bin

java -Xms256m -Xmx3048m -cp "out/production/easybackup" easybackup.Main jpg "/Volumes/Macintosh HD/Users/rienbouw/Pictures/MijnAfbeeldingen" "/Volumes/3TB/Backup/Mijn afbeeldingen"
echo ""
java -Xms6048m -Xmx6048m -cp "out/production/easybackup" easybackup.Main heic "/Users/rienbouw/Pictures/Lightroom_CC_Sync_catalog" "/Volumes/3TB/HEIC Vault" "/Volumes/Macintosh HD/Users/rienbouw/Pictures/MijnAfbeeldingen" -a
echo ""
java -Xms256m -Xmx3048m -cp "out/production/easybackup" easybackup.Main dng "/Volumes/Macintosh HD/Users/rienbouw/Pictures/DNG Vault" "/Volumes/3TB/DNG Vault" "/Volumes/Macintosh HD/Users/rienbouw/Pictures/MijnAfbeeldingen"




#java -classpath out/production/easybackup/easybackup.jar:out/production/easybackup/commons-configuration-1.10.jar:out/production/easybackup/commons-net-3.6.jar:out/production/easybackup/commons-lang-2.6.jar:out/production/easybackup/metadata-extractor-2.5.0-RC3.jar:out/production/easybackup/commons-logging-1.1.3.jar easybackup.Main jpg "/Volumes/Macintosh HD/Users/rienbouw/Pictures/MijnAfbeeldingen" "/Volumes/3TB/Backup/Mijn afbeeldingen"
#java -classpath out/production/easybackup/easybackup.jar:out/production/easybackup/commons-configuration-1.10.jar:out/production/easybackup/commons-net-3.6.jar:out/production/easybackup/commons-lang-2.6.jar:out/production/easybackup/metadata-extractor-2.5.0-RC3.jar:out/production/easybackup/commons-logging-1.1.3.jar easybackup.Main dng "/Volumes/Macintosh HD/Users/rienbouw/Pictures/DNG Vault" "/Volumes/3TB/Backup/DNG Vault" "/Volumes/Macintosh HD/Users/rienbouw/Pictures/MijnAfbeeldingen"
