#!/bin/sh

cp GetEvents3.sql test_middle_procedures/GetEvents3.sql
cp InsertUser.sql test_middle_procedures/InsertUser.sql
cp InsertEvent.sql test_middle_procedures/InsertEvent.sql
cp EventTriggers.sql test_middle_procedures/EventTriggers.sqed

sed -i -- 's/ping/test_middle/g' test_middle_procedures/*
