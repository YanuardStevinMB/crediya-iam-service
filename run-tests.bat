@echo off
echo ==========================================
echo  IAM Service - Test Execution Script
echo ==========================================
echo.

echo [1/4] Cleaning project...
call gradlew clean

echo.
echo [2/4] Running all tests...
call gradlew test

echo.
echo [3/4] Generating coverage reports...
call gradlew jacocoMergedReport

echo.
echo [4/4] Running mutation tests...
call gradlew pitestReportAggregate

echo.
echo ==========================================
echo  Test Results Summary:
echo ==========================================
echo - Unit tests: build/reports/tests/test/index.html
echo - Coverage: build/reports/jacocoMergedReport/html/index.html  
echo - Mutation testing: build/reports/pitest/index.html
echo - Architecture violations: [check module]/build/issues.json
echo ==========================================

pause
