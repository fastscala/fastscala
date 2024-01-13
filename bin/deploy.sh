if [[ $1 = *.sh ]];
then TARGET_SERVER="$1";
else TARGET_SERVER="default.sh";
fi

if [[ ! -f "./$TARGET_SERVER" ]];
then
echo "ERROR: Please specify target server."
exit 1
fi

source $TARGET_SERVER
source "bin/before.sh"

################################################

START=$(date +%s)

echo "Before packaging..."

rm -v $RPM_PATH/$RPM_FILE
$BEFORE_PACKAGING

echo "Packaging..."
sbt "$PACKAGE_CMD"

if [ $? -eq 0 ]; then
    echo -e "\e[1;32mPackaging SUCCEEDED\e[0m"
else
    echo -e "\e[1;31mPackaging FAILED\e[0m"
    exit 1
fi

if [ -f "$RPM_PATH/$RPM_FILE" ]
then
    echo "[$(( $(date +%s) - $START ))sec]: PACKAGED"
    du -sh $RPM_PATH/$RPM_FILE
else
    echo -e "\e[1;31mPackaging FAILED\e[0m: rpm file not found: $RPM_PATH/$RPM_FILE"
    exit 1;
fi

echo "DEPLOYING TO $TARGET_SERVER"
source $TARGET_SERVER

echo "SENDING ZIP..."
RSYNC -v "$RPM_PATH/$RPM_FILE" "$SSHUSER@$SSHHOST:/opt/"
echo "[$(( $(date +%s) - $START ))sec]: SENT RPM FILE"

echo "RESTARTING..."

RUN_S "
systemctl stop $NAME.service
rpm -ivh --force /opt/$RPM_FILE
systemctl enable $NAME.service
systemctl start $NAME.service
"

for i in "$@" ; do
    if [[ $i == "--notail" ]] ; then
        echo "Not tailing output"
        exit 0
    fi
done

bin/tail_log.sh "$TARGET_SERVER"
