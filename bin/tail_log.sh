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
DEBUG_RUN=false

RUN_S "journalctl -n 1000 -fu $NAME"


