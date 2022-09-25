#!/bin/bash

contextname="submissionsystem"
webappdir="/var/lib/tomcat9/webapps/"
contextpath="$webappdir$contextname"
datadir="/srv/submissioninterface"

if [[ $UID = 0 ]]; then
	echo "This script should not be run as root." >&2
	exit 1
fi

if [[ ! -d "$contextpath" ]]; then
	latestcontextpath=$(find "$webappdir" -maxdepth 1 -type d -name "$contextname##*" | sort -r | head -n1)
	if [[ -z "$latestcontextpath" ]]; then
		echo "\"$contextpath\" not found" >&2
		exit 1
	fi
	contextpath="$latestcontextpath"
fi

if [ -f "$datadir/gate-runner.lck" ]; then
	echo "GATE runner is blocked globally"
	exit 0
fi
if lockfile -! -l 259200 -r 0 "$datadir/gate-runner.$HOSTNAME.lck"; then
	exit 0
fi
date > "$datadir/gate-runner-$HOSTNAME.log"
if ! java -Dfile.encoding=UTF-8 -Xmx1024m -cp "$contextpath/WEB-INF/classes/:$contextpath/WEB-INF/lib/*" de.tuclausthal.submissioninterface.util.TestRunner "$datadir" >> "$datadir/gate-runner-$HOSTNAME.log" 2>&1; then
	cat "$datadir/gate-runner-$HOSTNAME.log"
fi
date >> "$datadir/gate-runner-$HOSTNAME.log"
echo "finished" >> "$datadir/gate-runner-$HOSTNAME.log"
#date=$(date +%Y%m%d)
#cat "$datadir/gate-runner-$HOSTNAME.log" >> "$datadir/logs/gate-runner-$HOSTNAME.$date.log"
#find "$datadir/logs/" -mtime +5 -type f -delete
rm -f "$datadir/gate-runner.$HOSTNAME.lck"
