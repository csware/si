# GATE

GATE is an e-assessment system specifically designed to support programming education.

This system is able to help with the process of exercise management and assessment for large programming classes at university level. The implemented system can be configured to allow for self-assessments of students and can check solutions submitted by studens for plagiarism.

## How to setup GATE?

Please read the whole procedure in advance to executing it.

### Installation steps of required software

- Install Java
  - On *nix (such as Debian) use the `openjdk-11-jre-headless` package
  - On Windows you can use [Liberica OpenJDK 11](https://bell-sw.com/pages/downloads/)
- Install [Apache Tomcat 9](https://tomcat.apache.org) (e.g., on Debian-based systems use the `tomcat9` package)
  - After installation Tomcat usually listens on locahost:8080
  - For development it is recommended to download the binary archive and integrate Tomcat into the used IDE, e.g. into Eclipse.
- Install MariaDB
  - For a development machine on Windows [XAMPP](https://www.apachefriends.org/) is recommended which comes with [phpMyAdmin](https://www.phpmyadmin.net/) a nice administration interface for MariaDB
- For development [Eclipse](https://www.eclipse.org/downloads/packages/) (IDE for Enterprise Java Developers) >= 2022-09 is recommended
  - It may be necessary to build GATE once using the Maven compile goal to ensure that all required dependencies are available locally (in particular, hibernate-jpamodelgen).
- For production use, it is recommended to run Apache Tomcat behind Apache httpd, connecting Tomcat using [mod_proxy_ajp](https://httpd.apache.org/docs/2.4/mod/mod_proxy_ajp.html), terminate SSL/TLS in httpd and probably use Shibboleth for Single-Sign on.
  - Configure the VHost with mod_proxy_ajp:
    ```
    ProxyPreserveHost On
    ProxyStatus On

    ProxyPass / ajp://localhost:8009/ secret=SECRET
    ```
    - Make sure Tomcat is not accessible directly (e.g., ports 8080, 8009, 8443); e.g. make port `8009` listen on `::1` only and comment all other `Connector`s in `server.xml`.
    - Clustering is possible (needs in `Server/Engine[@jvmRoute]` to be configured in `server.xml` as `jvm1` or `jvm2`):
      ```
      <Proxy "balancer://mycluster">
          BalancerMember ajp://srv1:8009 secret=SECRET route=jvm1
          BalancerMember ajp://srv2:8009 secret=SECRET route=jvm2
          ProxySet lbmethod=byrequests stickysession=JSESSIONID
      </Proxy>
      ProxyPass "/" "balancer://mycluster/"
      ```
  - How to configure Shibboleth:
    - Install shibd and configure it (cf. https://www.switch.ch/aai/guides/sp/installation-2.5/), make sure the `ApplicationDefault` configuration in `shibboleth2.xml` contains `attributePrefix="AJP_"` (cf. <https://wiki.shibboleth.net/confluence/display/SHIB2/NativeSPJavaInstall>; please also make sure the `AJP` connector in Tomcat `server.xml` does not block the Shibboleth request attributes, e.g. by setting `allowedRequestAttributesPattern=".*"` or more restrictive `"^(Shib-.*|givenName|eppn|sn|mail|persistent-id)$"`), has a large enough proxys packet size (`packetSize="65536"`), and `eppn` is set for the `REMOTE_USER` in `shibboleth2.xml` (should match `userAttribute`, see below); also make sure `/Shibboleth.sso/Logout` can be used for logout (cf. `src/main/java/de/tuclausthal/submissioninterface/servlets/controller/Logout.java`)
    - Enable Shibboleth protection for the `Overview` servlet:
      ```
      ProxyPass /Shibboleth.sso/ !
      ProxyPass /Shibboleth/ !
      ProxyPass /shibboleth-sp/ !

      <Location /gate/servlets/Overview>
         AuthType shibboleth
         ShibRequireSession On
         require valid-user
      </Location>
      ProxyIOBufferSize 65536
      ```
    - For debugging: Please not that the Shibboleth attributes passed to Tomcat don't show up when iterating over the request variables. They need to be explicitly named (e.g., `uid` or `Shib-Identity-Provider`).
- For building the whole package you need [maven](https://maven.apache.org/) (also often available as a package on *nix systems)
- For running the cron task regularly with the predefined script `submissiondir/runtests.sh` you need the `lockfile` tool (usually part of the `procmail` package on *nix systems)
- For using Docker-based tests you need to install Docker

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
      ReadWritePaths=/srv/gate/
      ```
      (afterwards, issue `systemctl daemon-reload` and `systemctl restart tomcat`)
  - For development:
    - Adjust `login` to `de.tuclausthal.submissioninterface.authfilter.authentication.login.impl.Form`
    - Adjust `verify` to `de.tuclausthal.submissioninterface.authfilter.authentication.verify.impl.FakeVerify` to disable any authentication
  - For production use:
    - Check the other configuration options, especially the mail related ones
    - For LDAP authentication set `login` to `de.tuclausthal.submissioninterface.authfilter.authentication.login.impl.Form` and `verify` to `de.tuclausthal.submissioninterface.authfilter.authentication.verify.impl.LDAPVerify`, configure LDAP related settings for the `AuthenticationFilter` (`PROVIDER_URL`, `SECURITY_AUTHENTICATION`, `SECURITY_PRINCIPAL`, `matrikelNumberAttribute` (optional), and `userAttribute`), please also look at `src/main/java/de/tuclausthal/submissioninterface/authfilter/authentication/verify/impl/LDAPVerify.java` whether special adjustments are needed (e.g. first and last name generation)
    - For Shibboleth set `login` to `de.tuclausthal.submissioninterface.authfilter.authentication.login.impl.Shibboleth` and `verify` to `de.tuclausthal.submissioninterface.authfilter.authentication.verify.impl.ShibbolethVerify`, configure Shibboleth related settings for the `AuthenticationFilter` (`userAttribute` and (optional) `matrikelNumberAttribute`); required fields from the Identity-Provider are `sn`, `givenName`, `mail`, and the configured `userAttribute` (usually `eppn`).
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
- For using Docker-based tests
    - Build the local `safe-docker` image, based on `safe-docker/Dockerfile` (e.g., `docker build --tag safe-docker .` in the `safe-docker` folder of this repository). So far, the image is only prepared for supporting Haskell, you might want to extend it according to your needs.
    - Copy `safe-docker/safe-docker` to `/usr/local/bin/safe-docker` and make sure it is owned by `root:root` and has the permissions `700`, you might want to check the parameters passed to `docker`. Requires the packages `libipc-run-perl` and `libdata-guid-perl packages` on Debian-based systems.
    - On Debian-based systems you might need to lift some systemd restrictions by creating the file `/etc/systemd/system/tomcat9.service.d/gate-safe-docker.conf` containing:
      ```
      [Service]
      NoNewPrivileges=false
      PrivateTmp=no
      ReadWritePaths=/tmp/
      ```
    - Set up `sudo` by inserting `tomcat ALL= NOPASSWD: /usr/local/bin/safe-docker` in `/etc/sudoers.d/gate-safe-docker`

Now GATE can be access using e.g. <http://localhost:8080/GATE/> depending on your concrete local setup.
It is recommended to run the self-test to check whether everything looks ok: <http://localhost:8080/GATE/servlets/SelfTest>
