@echo off
REM Build all Docker images using Jib

echo =====================================
echo Building All Service Docker Images
echo =====================================
echo.

cd /d %~dp0

echo Building eureka-server...
cd services\eureka-server
call mvn jib:dockerBuild -DskipTests -q
if errorlevel 1 goto :error
cd ..\..

echo Building gateway...
cd services\gateway
call mvn jib:dockerBuild -DskipTests -q
if errorlevel 1 goto :error
cd ..\..

echo Building security-service...
cd services\security-service
call mvn jib:dockerBuild -DskipTests -q
if errorlevel 1 goto :error
cd ..\..

echo Building catalog-service...
cd services\catalog-service
call mvn jib:dockerBuild -DskipTests -q
if errorlevel 1 goto :error
cd ..\..

echo Building cart-service...
cd services\cart-service
call mvn jib:dockerBuild -DskipTests -q
if errorlevel 1 goto :error
cd ..\..

echo Building order-service...
cd services\order-service
call mvn jib:dockerBuild -DskipTests -q
if errorlevel 1 goto :error
cd ..\..

echo.
echo =====================================
echo All images built successfully! 🎉
echo =====================================
echo.
echo Verify with: docker images ^| findstr ecommerce
goto :end

:error
echo.
echo =====================================
echo Build failed! ❌
echo =====================================
exit /b 1

:end
