$ErrorActionPreference='Stop'
$base=$PSScriptRoot
New-Item -ItemType Directory -Path "$base\outputs" -Force | Out-Null
foreach($name in @('README.md','THIRD_PARTY.md','CHANGELOG.md','RELEASING.md','PRIVACY.md','SECURITY.md','LICENSE','LICENSING.md')){
    Copy-Item -LiteralPath "$base\$name" -Destination "$base\outputs\$name" -Force
}
Add-Type -AssemblyName System.IO.Compression
$stream=[IO.File]::Open("$base\outputs\pixel-keyboard-source.zip",[IO.FileMode]::Create)
$zip=[IO.Compression.ZipArchive]::new($stream,[IO.Compression.ZipArchiveMode]::Create)
try{
    $files=@(Get-ChildItem -LiteralPath "$base\phone\src","$base\watch\src","$base\protocol\src","$base\shared-ui\src","$base\gradle","$base\.github","$base\docs","$base\licenses" -File -Recurse)
    foreach($rel in @('AGENTS.md','README.md','THIRD_PARTY.md','CHANGELOG.md','RELEASING.md','PRIVACY.md','SECURITY.md','CONTRIBUTING.md','LICENSE','LICENSING.md','settings.gradle','build.gradle','app-signing.gradle','app-notices.gradle','version.properties','gradle.properties','gradlew','gradlew.bat','build.ps1','package.ps1','.gitignore','.gitattributes','phone\build.gradle','watch\build.gradle','protocol\build.gradle')){
        $files+=Get-Item -LiteralPath (Join-Path $base $rel)
    }
    foreach($file in $files){
        $relative=$file.FullName.Substring($base.Length+1).Replace('\','/')
        $entry=$zip.CreateEntry($relative,[IO.Compression.CompressionLevel]::Optimal)
        if($relative -eq 'gradlew'){$entry.ExternalAttributes=([Convert]::ToInt32('100755',8) -shl 16)}
        $output=$entry.Open();$input=[IO.File]::OpenRead($file.FullName)
        try{$input.CopyTo($output)}finally{$input.Dispose();$output.Dispose()}
    }
}finally{$zip.Dispose();$stream.Dispose()}
Get-ChildItem -LiteralPath "$base\outputs" -File | Where-Object Extension -In '.apk','.zip' | Get-FileHash -Algorithm SHA256 |
    ForEach-Object{"$($_.Hash.ToLower())  $([IO.Path]::GetFileName($_.Path))"} |
    Set-Content -LiteralPath "$base\outputs\SHA256SUMS.txt" -Encoding utf8
