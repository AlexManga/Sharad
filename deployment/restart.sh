#! /usr/bin/sh
./stop.sh > /dev/null 2>&1
./start.sh $(cat logs/jarfile)
