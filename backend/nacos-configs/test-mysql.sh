#!/bin/bash
MYSQL="/opt/mysql/bin/mysql --socket=/opt/mysql/run/mysql.sock -uroot"
for p in 'change-me' 'root' '123456' 'password' 'mysql' 'vrd' 'Vrd@2026' 'root123' 'admin' 'root@123' 'Root@123' 'Root123' 'vrd123' 'Wl123456' 'Wl@123456' 'wl123456'; do
  RESULT=$($MYSQL -p"$p" -e 'select 1 as ok' 2>&1)
  echo "[$p]: $RESULT"
done
echo "---"
echo "trying no password:"
$MYSQL -e 'select 1 as ok' 2>&1
