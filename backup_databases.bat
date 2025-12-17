@echo off
REM Database Backup Script
echo Creating database backups...

set BACKUP_DIR=database_backups
set TIMESTAMP=%date:~-4,4%%date:~-10,2%%date:~-7,2%_%time:~0,2%%time:~3,2%%time:~6,2%
set TIMESTAMP=%TIMESTAMP: =0%

REM Create backup directory with timestamp
mkdir "%BACKUP_DIR%\%TIMESTAMP%"

echo Backing up books_db...
mysqldump -uroot -proot books_db > "%BACKUP_DIR%\%TIMESTAMP%\books_db.sql"

echo Backing up category_db...
mysqldump -uroot -proot category_db > "%BACKUP_DIR%\%TIMESTAMP%\category_db.sql"

echo Backing up price_db...
mysqldump -uroot -proot price_db > "%BACKUP_DIR%\%TIMESTAMP%\price_db.sql"

echo Backing up inventory_db...
mysqldump -uroot -proot inventory_db > "%BACKUP_DIR%\%TIMESTAMP%\inventory_db.sql"

echo Backing up auth_db...
mysqldump -uroot -proot auth_db > "%BACKUP_DIR%\%TIMESTAMP%\auth_db.sql"

echo Backing up reviews_db...
mysqldump -uroot -proot reviews_db > "%BACKUP_DIR%\%TIMESTAMP%\reviews_db.sql"

echo.
echo ✓ Backup completed successfully!
echo Location: %BACKUP_DIR%\%TIMESTAMP%
echo.
pause
