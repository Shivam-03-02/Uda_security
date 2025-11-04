param()

Write-Host "Running build-and-analyze.ps1"

# Ensure we run from the project root
Set-Location -Path $PSScriptRoot\..\

# Prefer installed Temurin/Adoptium JDK 11 if available
$possibleJdks = @("C:\Program Files\Eclipse Adoptium\jdk-11*", "C:\Program Files\Java\jdk-11*", "C:\Program Files\Java\jdk-11.*")
foreach ($p in $possibleJdks) {
    $match = Get-ChildItem -Directory -Path (Split-Path $p -Parent) -ErrorAction SilentlyContinue | Where-Object { $_.Name -like (Split-Path $p -Leaf) }
    if ($match) {
        $javaHome = $match[0].FullName
        break
    }
}

if (-not $javaHome) {
    Write-Host "Temurin JDK 11 not auto-detected. Using existing JAVA_HOME if set." -ForegroundColor Yellow
} else {
    Write-Host "Using JDK at: $javaHome"
    $env:JAVA_HOME = $javaHome
    $env:Path = $env:JAVA_HOME + '\\bin;' + $env:Path
}

Write-Host "Running: mvn -DskipTests=false clean package"
mvn -DskipTests=false clean package
if ($LASTEXITCODE -ne 0) { throw "Maven build failed" }

# Prepare SpotBugs
$spotbugsDir = Join-Path -Path $PWD -ChildPath 'spotbugs\spotbugs-4.7.3'
if (-not (Test-Path $spotbugsDir)) {
    Write-Host "Downloading SpotBugs 4.7.3..."
    $zip = Join-Path -Path $PWD -ChildPath 'spotbugs-4.7.3.zip'
    Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/com/github/spotbugs/spotbugs/4.7.3/spotbugs-4.7.3.zip' -OutFile $zip
    Expand-Archive -LiteralPath $zip -DestinationPath (Join-Path $PWD 'spotbugs') -Force
}

$sbExe = Join-Path -Path $spotbugsDir -ChildPath 'bin\spotbugs.bat'
if (-not (Test-Path $sbExe)) { throw "SpotBugs executable not found at $sbExe" }

Write-Host "Running SpotBugs CLI to produce target/site/spotbugs.html"
New-Item -ItemType Directory -Force -Path target\site | Out-Null
$aux = 'catpoint-security\target\catpoint-security-1.0-SNAPSHOT.jar'
& $sbExe -textui -effort:max -high -auxclasspath $aux -html -output "target\site\spotbugs.html" "catpoint-image\target\classes" "catpoint-security\target\classes"
if ($LASTEXITCODE -ne 0) { Write-Host "SpotBugs finished with non-zero exit code ($LASTEXITCODE)" -ForegroundColor Yellow }

Write-Host "Build and analysis complete. Artifacts:
- Shaded jar: catpoint-security\target\catpoint-security-1.0-SNAPSHOT.jar
- SpotBugs report: target\site\spotbugs.html"
