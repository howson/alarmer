#!/bin/sh
#-------------------------------------------------------------
# terminate exec for alarmer
#-------------------------------------------------------------

VAR=$1
APID=./pid

PID=`cat "$APID"`
kill -15 $PID 
echo "Terminate the alamer with pid $PID"
rm pid

