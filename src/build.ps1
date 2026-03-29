# Compiles all sources into ./out (no external dependencies)
$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$src = Join-Path $root "src\main\java"
$out = Join-Path $root "out"
if (-not (Test-Path $out)) { New-Item -ItemType Directory -Path $out | Out-Null }
$files = @(Get-ChildItem -Path $src -Filter "*.java" -Recurse | ForEach-Object { $_.FullName })
& javac -encoding UTF-8 -d $out $files
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
Write-Host "OK: classes in $out"
