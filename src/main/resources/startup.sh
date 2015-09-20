#!/bin/sh
#-------------------------------------------------------------
# start exec for alarmer
#-------------------------------------------------------------

VAR=$1


java -jar account-server_1.0.0.jar & >/dev/null 2>&1
echo $! > pid
echo "bugly-restore-service_rqd starts with pid $!"