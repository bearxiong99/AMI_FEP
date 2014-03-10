#!/bin/sh
echo now stop fe server......
kill -9 $(ps -ef|grep  cn.hexing.fk.fe.Application|grep -v grep|cut -c 10-14)
echo now stop fe server success