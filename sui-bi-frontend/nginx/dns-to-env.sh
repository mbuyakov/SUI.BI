#! /bin/sh
set -x
set -e
unset `env | grep SERVICE_HOST | egrep -o '^[^=]+'`
unset `env | grep SERVICE_PORT | egrep -o '^[^=]+'`
export DNS_SERVER=$(cat /etc/resolv.conf |grep -i '^nameserver'|head -n1|cut -d ' ' -f2)
echo "Finded DNS server: $DNS_SERVER"
