@echo off
cd %MR_POM_DIR%
call mvn %MR_STRING% %MR_PIPE%
REM if defined MR_COPY_STRING ( copy %MR_COPY_STRING% %MR_PIPE% )

exit
