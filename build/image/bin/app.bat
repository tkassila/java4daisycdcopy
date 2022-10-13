@echo off
set DIR="%~dp0"
set JAVA_EXEC="%DIR:"=%\java"



pushd %DIR% & %JAVA_EXEC% %CDS_JVM_OPTS%  -p "%~dp0/../app" -m com.metait.java4daisycdcopy/com.metait.java4daisycdcopy.Java4DaisyCdCopyApplication  %* & popd
