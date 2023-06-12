#!/bin/bash

#
# Ignore SIGHUP to avoid stopping migration when terminal disconnects.
#

trap '' 1

if [ -e /proc/$$/fd/255 ]
then
	DB_MIGRATOR_PATH=`readlink /proc/$$/fd/255 2>/dev/null`
fi

if [ ! -n "${DB_MIGRATOR_PATH}" ]
then
	DB_MIGRATOR_PATH="$0"
fi

cd "$(dirname "${DB_MIGRATOR_PATH}")"

#
# Run database virtual instance migrator tool.
#

JVM_ARGS=""
PROGRAM_ARGS=""

ARGS=( "$@" )

for i in "${!ARGS[@]}"; do
case "${ARGS[i]}" in
	'') # Skip if element is empty (happens when it's unsetted before)
	  continue ;;
	-mem|--memory) # Memory setting argument. Unsetting value as we deal with it at this moment
	  param="${ARGS[i+1]}"
	  JVM_ARGS="-Xms${param} -Xmx${param}"
	  unset 'ARGS[i+1]'
	  continue ;;
	*) # If argument has not been matched it is a program argument
	  PROGRAM_ARGS="${PROGRAM_ARGS} ${ARGS[i]}"
	  continue ;;
esac
unset 'ARGS[i]'
done

java -jar $JVM_ARGS com.liferay.portal.tools.db.partition.virtual.instance.migrator.jar $PROGRAM_ARGS