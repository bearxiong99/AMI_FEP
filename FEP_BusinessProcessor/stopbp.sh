#!/bin/sh
echo now stop bp server......
kill -9 $(ps -ef|grep  cn.hexing.fk.bp.Application|grep -v grep|cut -c 10-14)
echo now stop bp server success