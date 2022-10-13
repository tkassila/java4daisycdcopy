#!/bin/sh
rundir=$(pwd)
export rundir
#echo rundir=$rundir
# icu is not needed any more:
# $rundir/lib/icu4j-70.1.jar:
java -cp $rundir/java4daisycdcopy.jar:$CLASSPATH com.metait.java4daisycdcopy.Java4DaisyCdCopyApplication
