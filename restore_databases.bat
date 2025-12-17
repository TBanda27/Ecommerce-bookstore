@echo off
REM Database Restore Script
echo ========================================
echo Database Restore Tool
echo ========================================
echo.

REM List available backups
echo Available backups:
dir /b /ad database_backups
echo.

set /p BACKUP_FOLDER="Enter backup folder name (from above): "

if not exist "database_backups\%BACKUP_FOLDER%" (
    echo ERROR: Backup folder not found!
    pause
    exit /b
)

echo.
echo WARNING: This will overwrite your current databases!
set /p CONFIRM="Are you sure? (yes/no): "

if /i not "%CONFIRM%"=="yes" (
    echo Restore cancelled.
    pause
    exit /b
)

echo.
echo Restoring databases...

echo Restoring books_db...
mysql -uroot -proot books_db < "database_backups\%BACKUP_FOLDER%\books_db.sql"

echo Restoring category_db...
mysql -uroot -proot category_db < "database_backups\%BACKUP_FOLDER%\category_db.sql"

echo Restoring price_db...
mysql -uroot -proot price_db < "database_backups\%BACKUP_FOLDER%\price_db.sql"

echo Restoring inventory_db...
mysql -uroot -proot inventory_db < "database_backups\%BACKUP_FOLDER%\inventory_db.sql"

echo Restoring auth_db...
mysql -uroot -proot auth_db < "database_backups\%BACKUP_FOLDER%\auth_db.sql"

echo Restoring reviews_db...
mysql -uroot -proot reviews_db < "database_backups\%BACKUP_FOLDER%\reviews_db.sql"

echo.
echo ✓ Restore completed successfully!
echo.
pause
