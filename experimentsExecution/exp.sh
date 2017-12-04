#!/bin/sh

java -jar WorldServer3D.jar &
sleep 6 

max=10
for i in {1..10}
do
	echo "##### - Starting Experiment $i - #####"
    java -jar MotivationalSystemWithWorldServer3D.jar
    echo "##### - Finishing Experiment $i - #####"
done