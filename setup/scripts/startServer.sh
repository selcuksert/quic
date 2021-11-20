#!/bin/zsh

CWD=${0:a:h}

java -cp "$CWD/../../kwik/build/libs/kwik.jar" net.luminis.quic.server.Server "$CWD/../certs/server-cert.pem" "$CWD/../certs/server-key.pem" 4433 "$CWD/../www"