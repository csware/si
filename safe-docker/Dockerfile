# inspired by https://github.com/KITPraktomatTeam/Praktomat/tree/master/docker-image
# version e0d53616b7a81f15d6717f76ba53d9415913f7b5

FROM debian:buster

# make sure we have a fully patched image
RUN apt-get update -qq && apt-get dist-upgrade -y && apt-get clean && rm -rf /var/lib/apt/lists/*

# set up Haskell
RUN apt-get -qq update && apt-get install -qq --yes ghc libghc-test-framework-dev libghc-test-framework-hunit-dev libghc-test-framework-quickcheck2-dev && apt-get clean && rm -rf /var/lib/apt/lists/*

# set default language, but with UTF-8; needed for Haskell
ENV LC_ALL="C.UTF-8"

# set up new user, uid should be the same as for the host system tomcat user; also use fresh /home for every container
RUN chmod 1777 /home && adduser --uid 996 --home /home/tester --no-create-home --shell /bin/bash --disabled-password --system --group tester
USER tester
