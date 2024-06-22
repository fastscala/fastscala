#!/bin/sh

NAME="fs_demo"

SSHUSER="root"
SSHHOST="REPLACE BY THE SERVER IP ADDRESS"

RPM_FILE=fs_demo-0.1.0-1.0.0.noarch.rpm
RPM_PATH=fs_demo/target/rpm/RPMS/noarch

SBT_PROJ_PATH="$(pwd)/"
PACKAGE_CMD="fs_demo/rpm:packageBin ; exit ;"

DEFAULT_CONFIG_FILE_CONTENTS="JAVA_OPTS=\"-Dcom.fastscala.demo.server.local=false\""
