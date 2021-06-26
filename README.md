# GATE

GATE is an e-assessment system specifically designed to support programming education.

This system is able to help with the process of exercise management and assessment for large programming classes at university level. The implemented system can be configured to allow for self-assessments of students and can check solutions submitted by studens for plagiarism.

## How to setup GATE?

Please read the whole procedure in advance to executing it.

### Installation steps of required software

- Install Java
  - On *nix (such as Debian) use the `openjdk-11-jre-headless` package
  - On Windows you can use [Liberica OpenJDK 11](https://bell-sw.com/pages/downloads/)
- Install [Apache Tomcat](https://tomcat.apache.org) (e.g., on Debian-based systems use the `tomcat9` package)
  - After installation Tomcat usually listens on locahost:8080
  - For development it is recommended to integrate Tomcat into the used IDE, e.g. into Eclipse.
- Install MariaDB (recommended on *nix) or MySQL (on Windows, <https://dev.mysql.com/downloads/mysql/>)
  - For a development machine on Windows [XAMPP](https://www.apachefriends.org/) is recommended which comes with [phpMyAdmin](https://www.phpmyadmin.net/) a nice administration interface for MySQL
- For development [Eclipse](https://www.eclipse.org/downloads/packages/) (IDE for Enterprise Java Developers) is recommended with the plugin `m2e-apt`
- For production use, it is recommended to run Apache Tomcat behind Apache httpd, connecting Tomcat using [mod_proxy_ajp](https://httpd.apache.org/docs/2.4/mod/mod_proxy_ajp.html), terminate SSL/TLS in httpd and probably use Shibboleth for Single-Sign on.
  - Configure the VHost with mod_proxy_ajp:
    ```
    ProxyPreserveHost On
    ProxyStatus On

    ProxyPass /Shibboleth.sso/ !
    ProxyPass /Shibboleth/ !
    ProxyPass /shibboleth-sp/ !

    ProxyPass / ajp://localhost:8009/ secret=SECRET
    ```
    - Make sure Tomcat is not accessible directly (e.g., ports 8080, 8009, 8443); e.g. make port `8009` listen on `::1` only and comment all other `Connector`s in `server.xml`.
  - How to configure Shibboleth:
    - Install shibd and configure it (cf. https://www.switch.ch/aai/guides/sp/installation/), make sure the `ApplicationDefault` configuration in `shibboleth2.xml` contains `attributePrefix="AJP_"` (cf. <https://wiki.shibboleth.net/confluence/display/SHIB2/NativeSPJavaInstall>; please also make sure the `AJP` connector in Tomcat `server.xml` does not block the Shibboleth request attributes, e.g. by setting `allowedRequestAttributesPattern=".*"`) and `uid` for the `REMOTE_USER` (must match `userAttribute`, see below); also make sure `/Shibboleth.sso/Logout` can be used for logout (cf. `src/main/java/de/tuclausthal/submissioninterface/servlets/controller/Logout.java`)
    - Enable Shibboleth protection for the `Overview` servlet:
      ```
      <Location /gate/servlets/Overview>
         AuthType shibboleth
         hibRequireSession On
         require valid-user
      </Location>
      ```
   - For debugging: Please not that the Shibboleth attributes passed to Tomcat don't show up when iterating over the request variables. They need to be explicitly named (e.g., `uid` or `Shib-Identity-Provider`).
- For building the whole package you need [maven](https://maven.apache.org/) (also often available as a package on *nix systems)
- For running the cron task regularly with the predefined script `submissiondir/runtests.sh` you need the `lockfile` tool (usually part of the `procmail` package on *nix systems)

### Database
- Decide what user and password to use (for production use a dedicated user is recommended)
- Create a new database
- Import `mysql.sql`

### Configuration files
- `src/main/resources/hibernate.cfg.xml` for the database configuration
  - Set the database name, username and password here
- `src/main/webapp/WEB-INF/web.xml` for Tomcat and GATE specific configurations
  - Adjust the `datapath` here, please make sure that this is accessible by Tomcat, use e.g. `/srv/gate/` or `c:\gate` here
    - On *nix the system Tomcat runs as the user `tomcat`, so make sure that the user has appropriate permissions, also on Debian-based systems, Tomcat might be sandboxed by systemd. Create a file `/etc/systemd/system/tomcat9.service.d/gate.conf` containing:
      ```
      [Service]
      ReadWritePaths=/srv/submissioninterface/
      ```
      (afterwards, issue `systemctl daemon-reload` and `systemctl restart tomcat`)
  - For development:
    - Adjust `login` to `de.tuclausthal.submissioninterface.authfilter.authentication.login.impl.Form`
    - Adjust `verify` to `de.tuclausthal.submissioninterface.authfilter.authentication.verify.impl.FakeVerify` to disable any authentication
  - For production use:
    - Check the other configuration options, especially the mail related ones
    - For LDAP authentication set `login` to `de.tuclausthal.submissioninterface.authfilter.authentication.login.impl.Form` and `verify` to `de.tuclausthal.submissioninterface.authfilter.authentication.verify.impl.LDAPVerify`, configure LDAP related settings for the `AuthenticationFilter` (`PROVIDER_URL`, `SECURITY_AUTHENTICATION`, `SECURITY_PRINCIPAL`, `matrikelNumberAttribute` (optional), and `userAttribute`), please also look at `src/main/java/de/tuclausthal/submissioninterface/authfilter/authentication/verify/impl/LDAPVerify.java` whether special adjustments are needed (e.g. first and last name generation)
    - For Shibboleth set `login` to `de.tuclausthal.submissioninterface.authfilter.authentication.login.impl.Shibboleth` and `verify` to `de.tuclausthal.submissioninterface.authfilter.authentication.verify.impl.ShibbolethVerify`, configure Shibboleth related settings for the `AuthenticationFilter` (`userAttribute` and (optional) `matrikelNumberAttribute`); required fields from the Identity-Provider are `sn`, `givenName`, `mail`, and the configured `userAttribute` (usually `uid`).
 - `submissiondir/runtests.sh` for adjusting the paths
 - `src/main/webapp/WEB-INF/studiengaenge.txt` for updating the list of study programs that are used for auto completion for the users of GATE

### Building GATE for deploying to Tomcat

- Clone the repository
- Adjust the config files
- Run `mvn clean verify`
- The built `.war` file can then be found in the `target` directory

Alternatively you can also use GitLab CI/CD functionality and download the built `.war` file.

### Installation steps for GATE

- Prepare the `datapath` in which the submissions will be stored (see the "Configuration files" section above)
  - Copy `submissiondir/*` and `SecurityManager/NoExitSecurityManager.jar` to that folder
  - If you want to use the [JPlag plagiarism system](https://github.com/jplag/jplag), compile JPlag 2.12 into a single .jar (using the maven goal `assembly:assembly` inside the `jplag` directory and copy the final `.jar` (from the target folder) as `jplag.jar` into the root of the `datapath`
- On Windows: Make sure the `JAVA_HOME` environment variable is set and points to the JDK directory (e.g. `C:\Program Files\BellSoft\LibericaJDK-11\`)
- Prepare Tomcat
  - If you want to have the user names on the Tomcat access log, you can use `%{username}r` (as a replacement for `%u`) for configuring the `AccessLogValve` `pattern`, cf. https://tomcat.apache.org/tomcat-9.0-doc/config/valve.html#Access_Log_Valve
  - Rename the built `.war` file to e.g. `gate`, because the name is then used to build the URL under which GATE will be accessible using Tomcat
  - Copy the `.war` file to the `webapps` folder of Tomcat (usually in `C:\Program Files\Apache Tomcat 9\webapps` or `/var/lib/tomcat9/webapps/`)
  - New versions of GATE can be easily deployed by just overriding the `.war` file in the `webapps` folder
- Create first user with superuser permissions: Execute `util.CreateFirstUser` with parameters: `loginname emailaddress firtsname lastname`. Alternatively, you can also manually insert a new row into the `users` table.
- Set up the cron task that performs regular tasks such as automatically start the plagiarism check in background
  - You can use the shipped script `submissiondir/runtests.sh` which should be in `datapath` and configure `/etc/crontab`: `*/10 * * * *   tomcat   /srv/submissioninterface/runtests.sh`
  - Please make sure that you receive mails for the tomcat user in order to get errors of the cron task runner
- Restart Tomcat

Now GATE can be access using e.g. <http://localhost:8080/gate/> depending on your concrete local setup.
