#!/bin/bash

contextname="submissionsystem"
webappdir="/var/lib/tomcat9/webapps/"
contextpath="$webappdir$contextname"
datadir="/srv/submissioninterface"

if [[ ! -d "$contextpath" ]]; then
	latestcontextpath=$(find "$webappdir" -maxdepth 1 -type d -name "$contextname##*" | sort -r | head -n1)
	if [[ -z "$latestcontextpath" ]]; then
		echo "\"$contextpath\" not found" >&2
		exit 1
	fi
	contextpath="$latestcontextpath"
fi

if lockfile -! -l 259200 -r 0 "$datadir/gate-runner.lck"; then
	exit 0
fi
date > "$datadir/gate-runner.log"
if ! java -Dfile.encoding=UTF-8 -Xmx1024m -cp "$contextpath/WEB-INF/classes/:$contextpath/WEB-INF/lib/*" de.tuclausthal.submissioninterface.util.TestRunner "$datadir" >> "$datadir/gate-runner.log" 2>&1; then
	cat "$datadir/gate-runner.log"
fi
date >> "$datadir/gate-runner.log"
echo "finished" >> "$datadir/gate-runner.log"
rm -f "$datadir/gate-runner.lck"
