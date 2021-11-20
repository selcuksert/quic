#!/bin/zsh

CWD=${0:a:h}

if [ -d "$CWD/../certs" ]; then
  rm -rf "$CWD/../certs"
fi

mkdir -p "$CWD/../certs"

openssl req -x509 -new -nodes -newkey rsa:4096 -days 1024 -keyout "$CWD/../certs/ca-key.pem" -out "$CWD/../certs/ca-cert.pem" -subj "/CN=localhost"
openssl req -nodes -newkey rsa:4096 -keyout "$CWD/../certs/server-key.pem" -out "$CWD/../certs/server-csr.pem" -subj "/CN=localhost"
openssl x509 -req -in "$CWD/../certs/server-csr.pem" -days 1024 -CA "$CWD/../certs/ca-cert.pem" -CAkey "$CWD/../certs/ca-key.pem" -out "$CWD/../certs/server-cert.pem" -CAcreateserial