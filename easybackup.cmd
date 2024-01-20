@echo off
@rem mvn clean package
@rem set PATH=%PATH%;C:\Users\rienb\.jdks\openjdk-21.0.2\bin
set jar=target\easybackup-1.0-SNAPSHOT-jar-with-dependencies.jar
java -Xms256m -Xmx3048m -cp %jar% easybackup.Main jpg d:/MijnAfbeeldingen e:/backup/MijnAfbeeldingen
echo.
java -Xms6048m -Xmx6048m -cp %jar% easybackup.Main heic "C:/Lightroom_CC_Sync_catalog" "e:/backup/HEIC-Vault" "d:/MijnAfbeeldingen" -a
echo.
java -Xms256m -Xmx3048m -cp %jar% easybackup.Main dng "C:/DNG Vault" "e:/Backup/DNG-Vault" "d:/MijnAfbeeldingen"
