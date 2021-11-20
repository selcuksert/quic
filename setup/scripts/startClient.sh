#!/bin/zsh

CWD=${0:a:h}

java -jar "$CWD/../../kwik/build/libs/kwik.jar" -H /index.html --noCertificateCheck localhost:4433