# GATE

GATE is an e-assessment system specifically designed to support programming education.

This system is able to help with the process of exercise management and assessment for large programming classes at university level. The implemented system can be configured to allow for self-assessments of students and can check solutions submitted by studens for plagiarism.

## How to setup GATE?

Please read the whole procedure in advance to executing it.

### Installation steps of required software

- Install Java
  - On *nix (such as Debian) use the `openjdk-11-jdk-headless` package
  - On Windows you can use [Liberica OpenJDK](https://bell-sw.com/pages/downloads/)
- Install [Apache Tomcat](https://tomcat.apache.org) (on *nix use the `tomcat-9` package)
  - After installation Tomcat usually listens on locahost:8080
  - For development it is recommended to integrate Tomcat into the used IDE, e.g. into Eclipse.
- Install MariaDB (recommended on *nix) or MySQL (on Windows, <https://dev.mysql.com/downloads/mysql/>)
  - For a development machine on Windows [XAMPP](https://www.apachefriends.org/) is recommended which comes with [phpMyAdmin](https://www.phpmyadmin.net/) a nice administration interface for MySQL
- For production use, it is recommended to run Apache Tomcat behind Apache httpd, connecting Tomcat using [mod_proxy_ajp](https://httpd.apache.org/docs/2.4/mod/mod_proxy_ajp.html), terminte SSL/TLS in httpd and propably use Shibboleth for Single-Sign on.
- For building the whole package you need [maven](https://maven.apache.org/) (also often available as a package on *nix systems)
- For running the cron task regularly with the predefined script `submissiondir/runtests.sh` you need the `lockfile` tools (usually part of the `procmail` package on *nix systems)

### Database
- Decide what user and password to use (for production use a dedicated user is recommended)
- Create a new database
- Import `mysql.sql`

### Configuration files
- `src/main/resources/hibernate.cfg.xml` for the database configuration
  - Set the database name, username and password here
- `src/main/webapp/WEB-INF/web.xml` for Tomcat and GATE specific configurations
  - Adjust the `datapath` here, please make sure that thbis is accessible by Tomcat, use e.g. `/srv/gate/` or `c:\gate` here
    - On *nix the system Tomcat runs as the user `tomcat`, so make sure that user has appropriate permissions, also on Debian-based systems, Tomcat might be sandboxed by systemd. Create a file `/etc/systemd/system/tomcat9.service.d/gate.gate.conf` containing: ```
[Service]
ReadWritePaths=/srv/submissioninterface/
``` (afterwards, issue `systemctl daemon-reload` and `systemctl restart tomcat`)
  - For development:
    - Adjust `login` to `de.tuclausthal.submissioninterface.authfilter.authentication.login.impl.Form`
    - Adjust `verify` to `de.tuclausthal.submissioninterface.authfilter.authentication.verify.impl.FakeVerify` to disable any authentication
  - For production use:
    - Check the other configuration options, especially the mail related ones
    - For LDAP authentication set `login` to `de.tuclausthal.submissioninterface.authfilter.authentication.login.impl.Form` and `verify` to `de.tuclausthal.submissioninterface.authfilter.authentication.verify.impl.LDAPVerify`, configure LDAP related settings for the `AuthenticationFilter` (e.g. `PROVIDER_URL`, `SECURITY_AUTHENTICATION`, `SECURITY_PRINCIPAL`, and `userAttribute`), please also look at `src/main/java/de/tuclausthal/submissioninterface/authfilter/authentication/verify/impl/LDAPVerify.java` whether special adjustmens are needed (e.g. first and last name generation)
 - `submissiondir/runtests.sh` for adjusting the paths
 - `src/main/webapp/studiengaenge.js`

### Building GATE for deploying to Tomcat

- Clone the repository
- Adjust the config files
- Run `mvn clean verify`
- The build `.war` file can then be found in the `target` directory

Alternatively you can also use GitLab CI/CD functionality and download the build `.war` file.

### Installation steps for GATE

- Prepate the `datadir` in which the submissions will be stored (see the "Configuration files" section above)
  - Copy `submissiondir/*` and `SecurityManager/NoExitSecurityManager.jar` to that folder
  - If you want to use the [JPlag plagiarism system](https://github.com/jplag/jplag), compile it into a single .jar (using the maven goal `assembly:assembly` inside the `jplag` directory and copy the final `.jar` (from the target folder) as `jplag.jar` into the root of the `datadir`
- On Windows: Make sure the `JAVA_HOME` environment variable is set and points to the JDK directory (e.g. `C:\Program Files\BellSoft\LibericaJDK-11\`)
- Prepare Tomcat
  - Rename the build `.war` file to e.g. `gate`, because the name is then used to build the URL under which GATE will be accessible using Tomcat
  - Copy the `.war` file to the `webapps` folder of Tomcat (usually in `C:\Program Files\Apache Tomcat 9\webapps` or `/var/lib/tomcat9/webapps/`)
  - New versions of GATE can be easily deployed by just overriding the `.war` file in the `webapps` folder
- Create first user with superuser permissions: Execute `util.CreateFirstUser` with parameters: `loginname emailaddress firtsname lastname`. Alternatively you can also manually insert a new row into the `users` table.
- Set up the cron task that performs regular tasks such as automatically start the plagiarism check in background
  - You can use the shipped script `submissiondir/runtests.sh` which should be in `datadir` and configure `/etc/crontab`: `*/10 * * * *   tomcat   /srv/submissioninterface/runtests.sh`
  - Please make sure that you receive mails for the tomcat user in order to get errors of the cron task runner
- Restart Tomcat

Now GATE can be access using e.g. <http://localhost:8080/gate/> depending on your concrete local setup.
