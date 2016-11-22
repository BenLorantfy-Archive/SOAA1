@echo off
REM ===================================
REM   Setup the Runtime Environment
REM ===================================
call setEnvironment

REM ===================================
REM   Run the SOA Register Server
REM ===================================
java KnockKnockClient

REM ===================================
REM         Show the Results
REM ===================================
pause

