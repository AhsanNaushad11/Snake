# =============================================================================
#  deploy_android.ps1
#  All-in-one deployment script for Snake Game
#  Project: C:\Users\AhsanBinNaushad\Documents\Codex\2026-05-15\create-a-snake-game-project-utilize
# =============================================================================

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

# Paths
$PROJECT_ROOT   = "C:\Users\AhsanBinNaushad\Documents\Codex\2026-05-15\create-a-snake-game-project-utilize"
$DEPLOY_DIR     = Join-Path $PROJECT_ROOT "deployment"
$KEYSTORE_FILE  = Join-Path $DEPLOY_DIR   "release.jks"
$PUBLIC_KEY_PEM = Join-Path $DEPLOY_DIR   "public_key.pem"
$GRADLE_APP     = Join-Path $PROJECT_ROOT "app\build.gradle"
$GRADLEW        = Join-Path $PROJECT_ROOT "gradlew.bat"
$APK_DEST       = Join-Path $DEPLOY_DIR   "snake-game-release.apk"
$SUMMARY_FILE   = Join-Path $DEPLOY_DIR   "deployment-summary.txt"
$KEY_ALIAS      = "snake-release-key"
$PACKAGE_NAME   = "com.ankanalytic.snake"

function Get-Keytool {
    $candidates = @(
        $env:JAVA_HOME,
        "C:\Program Files\Android\Android Studio\jbr",
        "C:\Program Files\Android\Android Studio\jre"
    )
    foreach ($dir in $candidates) {
        if ($dir) {
            $kt = Join-Path $dir "bin\keytool.exe"
            if (Test-Path $kt) { return $kt }
        }
    }
    $kt = Get-Command keytool -ErrorAction SilentlyContinue
    if ($kt) { return $kt.Source }
    throw "keytool not found. Set JAVA_HOME or install Android Studio."
}

function Write-Step($msg) { Write-Host "`n>>> $msg" -ForegroundColor Cyan }
function Write-OK($msg)   { Write-Host "    [OK] $msg" -ForegroundColor Green }
function Write-Warn($msg) { Write-Host "    [!!] $msg" -ForegroundColor Yellow }

Write-Host "`n=== Android Deployment Script - Snake Game ===" -ForegroundColor Magenta

# Validate project
Write-Step "Validating project root..."
if (-not (Test-Path $PROJECT_ROOT)) { Write-Error "Project not found: $PROJECT_ROOT"; exit 1 }
if (-not (Test-Path $GRADLEW))      { Write-Error "gradlew.bat not found."; exit 1 }
Write-OK "Project found."

# Create deployment folder
Write-Step "Creating deployment folder..."
New-Item -ItemType Directory -Force -Path $DEPLOY_DIR | Out-Null
Write-OK "Folder ready: $DEPLOY_DIR"

# Collect credentials
Write-Step "Enter signing credentials"
Write-Host "    Remember these passwords - you need them for every future update." -ForegroundColor DarkGray
$storePassSec = Read-Host "    Keystore password (min 6 chars)" -AsSecureString
$keyPassSec   = Read-Host "    Key password" -AsSecureString

$SP = [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($storePassSec))
$KP = [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($keyPassSec))

if ($SP.Length -lt 6 -or $KP.Length -lt 6) { Write-Error "Passwords must be at least 6 characters."; exit 1 }

# Generate keystore
Write-Step "Generating release keystore..."
$keytool = Get-Keytool

if (Test-Path $KEYSTORE_FILE) {
    Write-Warn "Keystore already exists - skipping to avoid overwrite."
} else {
    & $keytool -genkey -v `
        -keystore $KEYSTORE_FILE `
        -alias $KEY_ALIAS `
        -keyalg RSA -keysize 2048 -validity 10000 `
        -storepass $SP -keypass $KP `
        -dname "CN=Ahsan Bin Naushad, OU=Dev, O=AhsanDev, L=Karachi, S=Sindh, C=PK"

    if ($LASTEXITCODE -ne 0) { Write-Error "Keystore generation failed."; exit 1 }
    Write-OK "Keystore saved: $KEYSTORE_FILE"
}

# Export public key PEM
Write-Step "Exporting public key for Play Console..."
& $keytool -export -rfc `
    -keystore $KEYSTORE_FILE `
    -alias $KEY_ALIAS `
    -storepass $SP `
    -file $PUBLIC_KEY_PEM

if ($LASTEXITCODE -ne 0) { Write-Error "Public key export failed."; exit 1 }
Write-OK "Public key saved: $PUBLIC_KEY_PEM"

# Patch build.gradle
Write-Step "Patching app/build.gradle with signing config..."
if (-not (Test-Path $GRADLE_APP)) { Write-Error "app\build.gradle not found."; exit 1 }

$content = Get-Content $GRADLE_APP -Raw

if ($content -match "signingConfigs") {
    Write-Warn "signingConfigs already present - skipping patch."
} else {
    # Build the signing block as a plain string (no here-string to avoid parser issues)
    $nl = [System.Environment]::NewLine
    $signingBlock  = $nl
    $signingBlock += "    signingConfigs {" + $nl
    $signingBlock += "        release {" + $nl
    $signingBlock += "            storeFile file(`"../deployment/release.jks`")" + $nl
    $signingBlock += "            storePassword `"$SP`"" + $nl
    $signingBlock += "            keyAlias `"$KEY_ALIAS`"" + $nl
    $signingBlock += "            keyPassword `"$KP`"" + $nl
    $signingBlock += "        }" + $nl
    $signingBlock += "    }" + $nl

    # Insert after 'android {' using a callback-safe approach
    $androidIdx = $content.IndexOf("android {")
    if ($androidIdx -ge 0) {
        $insertAt = $androidIdx + "android {".Length
        $content = $content.Substring(0, $insertAt) + $signingBlock + $content.Substring($insertAt)
    }

    # Add signingConfig line inside release buildType
    $releasePattern = "buildTypes {"
    $releaseIdx = $content.IndexOf($releasePattern)
    if ($releaseIdx -ge 0) {
        $releaseBodyStart = $content.IndexOf("release {", $releaseIdx)
        if ($releaseBodyStart -ge 0) {
            $insertAt2 = $releaseBodyStart + "release {".Length
            $signingLine = $nl + "            signingConfig signingConfigs.release"
            $content = $content.Substring(0, $insertAt2) + $signingLine + $content.Substring($insertAt2)
        }
    }

    Set-Content $GRADLE_APP $content -Encoding UTF8
    Write-OK "build.gradle patched."
}

# Build release APK
Write-Step "Building signed release APK (this may take a few minutes)..."
Push-Location $PROJECT_ROOT
try {
    & $GRADLEW assembleRelease 2>&1 | Tee-Object -Variable buildOutput
    $buildOutput | Out-File (Join-Path $DEPLOY_DIR "build.log") -Encoding UTF8
} finally {
    Pop-Location
}

if ($LASTEXITCODE -ne 0) {
    Write-Host "`n[FAIL] Build failed. See deployment\build.log" -ForegroundColor Red
    exit $LASTEXITCODE
}
Write-OK "Build successful."

# Copy APK
Write-Step "Copying APK to deployment folder..."
$apkFound = Get-ChildItem -Path (Join-Path $PROJECT_ROOT "app\build\outputs\apk\release\") `
                -Filter "*.apk" -ErrorAction SilentlyContinue | Select-Object -First 1

if (-not $apkFound) {
    $apkFound = Get-ChildItem -Path (Join-Path $PROJECT_ROOT "app\build\outputs\apk\") `
                    -Filter "*.apk" -Recurse -ErrorAction SilentlyContinue | Select-Object -First 1
}

if (-not $apkFound) { Write-Error "APK not found after build. Check deployment\build.log."; exit 1 }

Copy-Item $apkFound.FullName $APK_DEST -Force
$apkSizeMB = [Math]::Round($apkFound.Length / 1MB, 2)
Write-OK "APK saved: $APK_DEST ($apkSizeMB MB)"

# Write summary
Write-Step "Writing deployment summary..."
$ts = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
$summaryLines = @(
    "=== Deployment Summary - Snake Game ===",
    "",
    "Generated on : $ts",
    "Package name : $PACKAGE_NAME",
    "Key alias    : $KEY_ALIAS",
    "",
    "Files in deployment\ folder:",
    "  release.jks               - Signing keystore  [KEEP SAFE, NEVER SHARE]",
    "  public_key.pem            - Paste into Play Console > Add key",
    "  snake-game-release.apk    - Signed release APK ($apkSizeMB MB)",
    "  build.log                 - Full Gradle output",
    "  deployment-summary.txt    - This file",
    "",
    "Next steps:",
    "  1. Play Console > Android developer verification > Add key",
    "     Paste the full contents of public_key.pem",
    "",
    "  2. Play Console > Your app > Store listing",
    "     Add title, description, screenshots, icon (512x512), feature graphic (1024x500)",
    "",
    "  3. Play Console > Release > Internal testing > Create new release",
    "     Upload: snake-game-release.apk",
    "",
    "  4. Fill content rating + privacy policy URL",
    "",
    "  5. Promote to Production when ready",
    "",
    "IMPORTANT: Back up release.jks - without it you cannot update the app on Play Store."
)
$summaryLines | Out-File $SUMMARY_FILE -Encoding UTF8
Write-OK "Summary saved: $SUMMARY_FILE"

Write-Host "`n=== Deployment Complete! ===" -ForegroundColor Green
Write-Host "  Folder  : $DEPLOY_DIR"
Write-Host "  APK     : snake-game-release.apk ($apkSizeMB MB)"
Write-Host "  Keystore: release.jks"
Write-Host "  PEM Key : public_key.pem"
Write-Host "`n  Next: paste public_key.pem into Play Console > Add key" -ForegroundColor Yellow
