java -Xms256m -Xmx3048m -cp "../sources/easybackup/dist/*" easybackup.Main jpg "/Volumes/Macintosh HD/Users/rienbouw/Pictures/MijnAfbeeldingen" "/Volumes/3TB/Backup/Mijn afbeeldingen" 
echo ""
java -Xms6048m -Xmx6048m -cp "../sources/easybackup/dist/*" easybackup.Main heic "/Users/rienbouw/Pictures/Lightroom_CC_Sync_catalog" "/Volumes/3TB/HEIC Vault" "/Volumes/Macintosh HD/Users/rienbouw/Pictures/MijnAfbeeldingen" -a
echo ""
java -Xms256m -Xmx3048m -cp "../sources/easybackup/dist/*" easybackup.Main dng "/Volumes/Macintosh HD/Users/rienbouw/Pictures/DNG Vault" "/Volumes/3TB/DNG Vault" "/Volumes/Macintosh HD/Users/rienbouw/Pictures/MijnAfbeeldingen" 




#java -classpath ../sources/easybackup/dist/easybackup.jar:../sources/easybackup/dist/commons-configuration-1.10.jar:../sources/easybackup/dist/commons-net-3.6.jar:../sources/easybackup/dist/commons-lang-2.6.jar:../sources/easybackup/dist/metadata-extractor-2.5.0-RC3.jar:../sources/easybackup/dist/commons-logging-1.1.3.jar easybackup.Main jpg "/Volumes/Macintosh HD/Users/rienbouw/Pictures/MijnAfbeeldingen" "/Volumes/3TB/Backup/Mijn afbeeldingen"
#java -classpath ../sources/easybackup/dist/easybackup.jar:../sources/easybackup/dist/commons-configuration-1.10.jar:../sources/easybackup/dist/commons-net-3.6.jar:../sources/easybackup/dist/commons-lang-2.6.jar:../sources/easybackup/dist/metadata-extractor-2.5.0-RC3.jar:../sources/easybackup/dist/commons-logging-1.1.3.jar easybackup.Main dng "/Volumes/Macintosh HD/Users/rienbouw/Pictures/DNG Vault" "/Volumes/3TB/Backup/DNG Vault" "/Volumes/Macintosh HD/Users/rienbouw/Pictures/MijnAfbeeldingen"
