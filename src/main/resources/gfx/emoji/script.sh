#!/bin/bash
for file_desc in *.svg; 
do
    echo "emojiList.add (\"${file_desc/.svg/}\");"
done
