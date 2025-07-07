#!/bin/bash
YELLOW='\e[1;33m'
RESET='\e[0m'

export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64

mvn clean package
