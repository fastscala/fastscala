
###############################

# ===== FUNCTIONS =====

DEBUG_RUN=false
SILENT=false

function RUN {
if [ "$DEBUG_RUN" = "true" ];
then
echo "> Run '$*' (Y/n)?"; 
read ANS;
else
echo -e "[Running]:\n$*"; 
ANS=""
fi
if [[ ! "$DEBUG_RUN" = "true" ]] || [[ ! "$ANS" = "n" ]]; 
then 
[[ "$SILENT" = true ]] && command ssh -t -t $SSHAUTH $SSHUSER@$SSHHOST "$*" > /tmp/run_output 2>&1
[[ ! "$SILENT" = true ]] && command ssh -t -t $SSHAUTH $SSHUSER@$SSHHOST "$*"
fi
}

function RUN_NOTTY { command ssh $SSHAUTH $SSHUSER@$SSHHOST "$*"; }

function RUN_S {
if [ "$DEBUG_RUN" = "true" ];
then
echo "> [ROOT] Run '$*' (Y/n)?"; 
read ANS;
else
echo -e "[Running]:\n$*"; 
ANS=""
fi
if [[ ! "$DEBUG_RUN" = "true" ]] || [[ ! "$ANS" = "n" ]];
then 
  RCMD="$(mktemp)"
  echo "$*" > "$RCMD"
  [[ "$SILENT" = true ]] && ssh -t $SSHAUTH $SSHUSER@$SSHHOST "sudo bash" < "$RCMD" > /tmp/run_output 2>&1
  [[ ! "$SILENT" = true ]] && ssh -t $SSHAUTH $SSHUSER@$SSHHOST "sudo bash" < "$RCMD"
fi
}

function PSQL {
if [ ! "$DB_HOST" = "" ];
then
export PGPASSWORD="$DB_PASS"
psql -h "$DB_HOST" "$DB_NAME" "$DB_USER"
else
RUN "sudo -u postgres psql \"$NAME\" -U \"$NAME\""
fi
}

function PSQL_S {
if [ ! "$DB_HOST" = "" ];
then
export PGPASSWORD="$DB_PASS"
psql -h "$DB_HOST" "$DB_NAME" "$DB_USER"
else
RUN "sudo -u postgres psql postgres -U postgres"
fi
}

function PSQL_S_CMD {
if [ ! "$DB_HOST" = "" ];
then
export PGPASSWORD="$DB_PASS"
psql -h "$DB_HOST" "$DB_NAME" "$DB_USER"
else
RUN "sudo -u postgres psql postgres -U postgres -c '$*'"
fi
}

function RSYNC { rsync --compress-level=7 --progress -avzh -e "ssh $SSHAUTH" $*; }

function RUN_PACKAGE { 
	TMP="$(mktemp)"
	P="$(pwd)"
	cd "$SBT_PROJ_PATH"	
	echo -e $PACKAGE_CMD | sbt &> "$TMP"
	cat "$TMP"
	if [ "$(cat "$TMP" | grep -i error)" != "" ]; 
	then echo "FATAL: Compile errors"; exit 1; fi
	cd "$P"
}




