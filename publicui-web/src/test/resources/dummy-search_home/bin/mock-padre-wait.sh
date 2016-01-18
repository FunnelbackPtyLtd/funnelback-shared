#!/bin/sh
if [ "$#" -eq 3 ]; then
	echo "started" >> $3
fi
sleep $2
cat $1
