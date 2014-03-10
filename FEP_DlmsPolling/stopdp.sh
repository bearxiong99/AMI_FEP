#!/bin/sh
echo now stop dp server......
kill -9 $(ps -ef|grep  cn.hexing.dp.Application|grep -v grep|cut -c 10-14)
echo now stop dp server success