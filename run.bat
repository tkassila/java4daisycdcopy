@echo off
call \java\jdk11fxcp.bat
set rundir=%~dp0
rem echo  rundir=%rundir%
rem echo java -cp %rundir%java4daisycdcopy.jar;%CLASSPATH% com.metait.java4daisycdcopy.Java4DaisyCdCopyApplication
java -cp %rundir%java4daisycdcopy.jar;%CLASSPATH% com.metait.java4daisycdcopy.Java4DaisyCdCopyApplication
rem pause