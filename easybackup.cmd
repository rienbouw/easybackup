@echo off
@rem mvn clean package
@rem set PATH=%PATH%;C:\Users\rienb\.jdks\openjdk-21.0.2\bin
set jar=out\artifacts\easybackup_jar\easybackup.jar

echo.
java -Xms256m -Xmx3048m -cp %jar% easybackup.Main dng "d:/DNG Vault" "e:/Backup/DNG-Vault" "d:/MijnAfbeeldingen" -a

echo.
java -Xms256m -Xmx3048m -cp %jar% easybackup.Main proraw "C:/Lightroom_CC_Sync_catalog" "e:/backup/DNG-Vault" "d:/MijnAfbeeldingen" -a

echo.
java -Xms256m -Xmx3048m -cp %jar% easybackup.Main jpg d:/MijnAfbeeldingen e:/backup/MijnAfbeeldingen 

echo.
java -Xms6048m -Xmx6048m -cp %jar% easybackup.Main heic "C:/Lightroom_CC_Sync_catalog" "e:/backup/HEIC-Vault" "d:/MijnAfbeeldingen" 

echo.
java -Xms6048m -Xmx6048m -cp %jar% easybackup.Main dng "C:/Lightroom_CC_Sync_catalog" "e:/backup/DNG-Vault" "d:/MijnAfbeeldingen" 

