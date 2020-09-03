#! /usr/bin/sh
java -jar $1 >> logs/tmp.log 2>&1 &
PID=$!
echo $PID > logs/pidfile
echo $1 > logs/jarfile
