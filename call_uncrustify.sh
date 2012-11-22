#!/bin/sh
################################################################
# This script formats all source files in a given directory.
# It uses uncrustify (if found on path) with the configuration
# in the current directory.
################################################################

BASENAME=`basename $0`
CONFIG=./uncrustify.cfg
UNCRUSTIFY=`which uncrustify`

if [ ! -n "$1" ]; then
	echo "Usage: $BASENAME <dir|file> [<filesuffix>]"
	echo "   ex: $BASENAME tmpdir java"
	exit 1
fi

# make sure uncrustify and config exist
if [ ! -f $CONFIG ]; then
	echo "file not found: $CONFIG"
	exit 1
fi
if [ ! -f $UNCRUSTIFY ]; then
	echo "file not found: $UNCRUSTIFY"
	exit 1
fi

# make sure specified file/directory exists
if [ ! -d "$1" -a ! -f "$1" ]; then
  echo "not found: $1"
  exit 1
fi


if [ -d "$1" ]; then

  # check for optional suffix pattern (assume java if null)
	if [ -n "$2" ]; then
		filesuffix=$2
	else
		filesuffix="java"
	fi

  # uncrustify all files, including ones in subdirectories
	file_list=`find ${1} -name "*.${filesuffix}" -type f`
	for file_ in $file_list
	do
		"$UNCRUSTIFY" -f "${file_}" -c "$CONFIG" -o ${file_}.tmp && mv ${file_}.tmp ${file_}
	done

else
	"$UNCRUSTIFY" -f "$1" -c "$CONFIG" -o ${file_}.tmp && mv ${file_}.tmp ${file_}
fi
