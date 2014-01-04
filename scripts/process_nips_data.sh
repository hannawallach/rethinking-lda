#!/bin/bash

FILE_NAME=$1

i=0

while read LINE; do

  i=$(( $i + 1 ))

  if (( "$i" == 1 )); then
    DOC_NAME=${LINE}
  else
    if (( "$i" == 2)); then
      TOKENS=`echo "${LINE}" | awk '{ for (f=1; f<=NF; f++) { printf("%s ", $f); } printf("\n"); }'`
    else
      i=$(( 0 ))
      echo -e "${DOC_NAME}\t${DOC_NAME}\t${TOKENS}"
    fi
  fi
done < ${FILE_NAME}
