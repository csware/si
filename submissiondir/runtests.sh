#!/bin/bash

webappdir="/var/lib/tomcat9/webapps/submissionsystem"
datadir="/srv/submissioninterface"

if lockfile -! -l 259200 -r 0 "$datadir/gate-runner.lck"; then
	exit 0
fi
date > "$datadir/gate-runner.log"
if ! java -Dfile.encoding=UTF-8 -Xmx1024m -cp "$webappdir/WEB-INF/classes/:$webappdir/WEB-INF/lib/*" de.tuclausthal.submissioninterface.util.TestRunner "$datadir" >> /var/log/gate-runner.log 2>&1; then
	cat "$datadir/gate-runner.log"
fi
date >> "$datadir/gate-runner.log"
echo "finished" >> "$datadir/gate-runner.log"
rm -f "$datadir/gate-runner.lck"
