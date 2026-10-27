param([string]$StorageRoot='D:\Codex')
$ErrorActionPreference='Stop'
if(-not (Test-Path -LiteralPath $StorageRoot)){throw "Storage root is unavailable: $StorageRoot"}
$env:TEMP=Join-Path $StorageRoot 'tmp'
$env:TMP=$env:TEMP
$env:GRADLE_USER_HOME=Join-Path $StorageRoot 'cache\gradle'
$env:ANDROID_USER_HOME=Join-Path $StorageRoot 'cache\android'
$env:ANDROID_AVD_HOME=Join-Path $StorageRoot 'work\android-avds'
New-Item -ItemType Directory -Path $env:TEMP,$env:GRADLE_USER_HOME,$env:ANDROID_USER_HOME -Force | Out-Null
if(-not $env:JAVA_HOME){
    $jdk=Get-ChildItem -LiteralPath (Join-Path $StorageRoot 'cache\jdk17') -Directory -ErrorAction SilentlyContinue | Select-Object -First 1
    if($jdk){$env:JAVA_HOME=$jdk.FullName}
}
if(-not $env:JAVA_HOME -or -not (Test-Path "$env:JAVA_HOME\bin\java.exe")){throw 'Set JAVA_HOME to JDK 17.'}
if(-not $env:ANDROID_HOME -and (Test-Path 'D:\Android\Sdk')){$env:ANDROID_HOME='D:\Android\Sdk'}
if(-not $env:ANDROID_HOME){throw 'Set ANDROID_HOME to an Android SDK containing Platform 35.'}
$tempJava=$env:TEMP.Replace('\','/')
$homeJava=(Join-Path $StorageRoot 'cache\tool-home').Replace('\','/')
$env:JAVA_TOOL_OPTIONS="-Djava.io.tmpdir=$tempJava -Duser.home=$homeJava"
New-Item -ItemType Directory -Path "$PSScriptRoot\signing","$PSScriptRoot\outputs" -Force | Out-Null
if(-not (Test-Path "$PSScriptRoot\signing\personal-debug.keystore")){
    & "$env:JAVA_HOME\bin\keytool.exe" -genkeypair -keystore "$PSScriptRoot\signing\personal-debug.keystore" -storepass android -keypass android -alias androiddebugkey -keyalg RSA -keysize 2048 -validity 10000 -dname 'CN=Pixel Remote Keyboard Development'
    if($LASTEXITCODE -ne 0){throw 'Debug key generation failed'}
}
$gradle=Join-Path $StorageRoot 'cache\gradle-distributions\gradle-8.12\bin\gradle.bat'
if(-not (Test-Path $gradle)){$gradle=Join-Path $PSScriptRoot 'gradlew.bat'}
Push-Location $PSScriptRoot
try {
    & $gradle ':protocol:test' ':phone:assembleDebug' ':watch:assembleDebug' ':phone:lintDebug' ':watch:lintDebug' ':watch:assembleDebugAndroidTest' --console=plain
    if($LASTEXITCODE -ne 0){throw 'Android checks or build failed'}
    Copy-Item -LiteralPath 'phone\build\outputs\apk\debug\phone-debug.apk' -Destination 'outputs\pixel-keyboard-phone.apk' -Force
    Copy-Item -LiteralPath 'watch\build\outputs\apk\debug\watch-debug.apk' -Destination 'outputs\pixel-keyboard-watch.apk' -Force
} finally {Pop-Location}
