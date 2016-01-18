#!/bin/sh
if [ "$#" -gt 2 ]; then
	echo "started" >> $3
fi
sleep $2
cat $1
