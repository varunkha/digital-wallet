#!/usr/bin/env bash

# example of the run script for running the fraud detection algorithm with a python file,
# but could be replaced with similar files from any major language

# I'll execute my programs, with the input directory paymo_input and output the files in the directory paymo_output
# Needs 6 command line arguments.
# 1) batch payment filename
# 2) stream payment filename
# 3) out1 filename
# 4) out2 filename
# 5) out3 filename
# 6) flag for updating the data structure(graph) using stream payment file. Should the trusted transactions in the stream file 
#be also used to update the friend relationship stored in graph. true for yes and false for no. default true. 
javac ./src/Antifraud.java
java -cp ./src Antifraud paymo_input/batch_payment.txt paymo_input/stream_payment.txt paymo_output/output1.txt paymo_output/output2.txt paymo_output/output3.txt true