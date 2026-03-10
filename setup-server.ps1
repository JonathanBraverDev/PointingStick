$ErrorActionPreference = "Stop"

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "   PaperMC Server One-Click Installer" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

# Prerequisite: Fetch the latest Game and Build Version
Write-Host "--- Fetching latest Minecraft version and build..."
$versionUrl = "https://api.papermc.io/v2/projects/paper"
$versionResponse = Invoke-RestMethod -Uri $versionUrl
$latestVersion = $versionResponse.versions[-1]

$buildUrl = "https://api.papermc.io/v2/projects/paper/versions/$latestVersion"
$buildResponse = Invoke-RestMethod -Uri $buildUrl
$latestBuild = $buildResponse.builds[-1]
Write-Host "      Target: Paper $latestVersion (Build $latestBuild)" -ForegroundColor Green

# Prerequisite: Check for Java
Write-Host "--- Checking Java environment..."
$javaPath = Get-Command java -ErrorAction SilentlyContinue
if (-not $javaPath) {
    Write-Host "      Error: Java not found in PATH." -ForegroundColor Red
    Write-Host "      Please install Java 21 or higher (required for Minecraft $latestVersion)." -ForegroundColor Yellow
    Write-Host "      Download from: https://adoptium.net/"
    Write-Host "      (Note: Adoptium provides the community-standard open-source Temurin JDK)" -ForegroundColor Gray
    Pause
    exit
}
Write-Host "      Java found at: $($javaPath.Source)" -ForegroundColor Green

# 1. Download the server jar
Write-Host "[1/4] Downloading the latest server jar..."
$jarName = "paper-$latestVersion-$latestBuild.jar"
$downloadUrl = "https://api.papermc.io/v2/projects/paper/versions/$latestVersion/builds/$latestBuild/downloads/$jarName"
$outputJar = "server.jar"

Invoke-WebRequest -Uri $downloadUrl -OutFile $outputJar
Write-Host "      Download complete -> $outputJar" -ForegroundColor Green

# 2. Accept EULA
Write-Host "[2/4] Accepting Minecraft EULA..."
$eulaFile = "eula.txt"
Set-Content -Path $eulaFile -Value "eula=true"
Write-Host "      Created eula.txt (eula=true)" -ForegroundColor Green

# 3. Pre-seed Server Properties
Write-Host "[3/4] Pre-seeding server.properties with overrides..."
$targetFile = "server.properties"

$newProps = @{
    "allow-flight" = "true"
    "difficulty" = "hard"
    "max-players" = "5"
    "online-mode" = "false"
    "simulation-distance" = "16"
    "spawn-protection" = "0"
    "sync-chunk-writes" = "false"
    "use-native-transport" = "true"
    "view-distance" = "32"
    "white-list" = "true"
}

$existingProps = @{}
if (Test-Path $targetFile) {
    $content = Get-Content $targetFile
    foreach ($line in $content) {
        if ($line -match "^([^#=]+)=(.*)$") {
            $existingProps[$matches[1].Trim()] = $matches[2].Trim()
        }
    }
}

foreach ($key in $newProps.Keys) {
    $existingProps[$key] = $newProps[$key]
}

$finalContent = @()
foreach ($key in ($existingProps.Keys | Sort-Object)) {
    $finalContent += "$key=$($existingProps[$key].ToString())"
}
Set-Content -Path $targetFile -Value $finalContent
Write-Host "      Edited server.properties." -ForegroundColor Green

# 4. Finalize Configuration
Write-Host "[4/4] Creating startup script..."

# Create start.bat
$startBatContent = @"
@ECHO OFF
java -Xms1024M -Xmx12G -jar server.jar nogui
"@
Set-Content -Path "start.bat" -Value $startBatContent
Write-Host "      Created start.bat" -ForegroundColor Green

Write-Host ""
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "Setup Complete!" -ForegroundColor Green
Write-Host "To start your server, run the 'start.bat' file."
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""
Pause
