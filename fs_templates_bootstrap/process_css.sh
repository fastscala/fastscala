
cat /home/david/Downloads/bootstrap-5.3.3-dist/css/bootstrap.css /home/david/Downloads/bootstrap-5.3.3-dist/css/bootstrap-grid.css |
  tr ',' '\n' |
  grep -E "^ *\\." |
  sed -e 's/ *//g' |
  sed -e 's/^\.//g'|
  sed -e 's/{.*//g'|
  sed -e 's/\[.*//g'|
  sed -e 's/,.*//g'|
  sed -e 's/>.*//g'|
  sed -e 's/\..*//g'|
  sed -e 's/:.*//g'|
  sed -e 's/~.*//g'|
  sed -e 's/\+.*//g'|
  sed -e 's/-/_/g' |
  sort -u |
  while read i;
    do echo -e "def $i: T = withClass(\"${i//_/-}\")";
  done
