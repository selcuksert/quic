#!/bin/zsh

CWD=${0:a:h}

cd "$CWD/../../" && \
# Remove the submodule entry from .git/config
git submodule deinit -f kwik && \
# Remove the submodule directory from the superproject's .git/modules directory
rm -rf .git/modules/kwik && \
# Remove the entry in .gitmodules and remove the submodule directory located at path/to/submodule
git rm -f ./kwik && \
git submodule add https://github.com/ptrd/kwik.git && \
git submodule update --init --recursive

# Build and add kwik.jar to local repo
cd "$CWD/../../kwik" && \
gradle clean build -x test && \
mvn install:install-file \
   -Dfile="$CWD/../../kwik/build/libs/kwik.jar" \
   -DgroupId=net.luminis.quic \
   -DartifactId=kwik \
   -Dversion=1.0.0 \
   -Dpackaging=jar \
   -DgeneratePom=true
