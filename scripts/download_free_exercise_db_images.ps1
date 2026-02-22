$ErrorActionPreference = 'Stop'

$workspaceRoot = Split-Path -Parent $PSScriptRoot
$jsonPath = Join-Path $workspaceRoot 'app/src/main/assets/free_exercise_db/exercises.json'
$imagesRoot = Join-Path $workspaceRoot 'app/src/main/assets/free_exercise_db/images'
$baseUrl = 'https://raw.githubusercontent.com/yuhonas/free-exercise-db/main/exercises/'

if (-not (Test-Path $jsonPath)) {
    throw "Missing source JSON: $jsonPath"
}

New-Item -ItemType Directory -Path $imagesRoot -Force | Out-Null

$payload = Get-Content -Path $jsonPath -Raw | ConvertFrom-Json
$downloaded = 0
$skipped = 0
$failed = 0

foreach ($exercise in $payload) {
    if ($null -eq $exercise.images) {
        continue
    }

    $limit = [Math]::Min(2, $exercise.images.Count)
    for ($i = 0; $i -lt $limit; $i++) {
        $relativePath = [string]$exercise.images[$i]
        if ([string]::IsNullOrWhiteSpace($relativePath)) {
            continue
        }

        $targetPath = Join-Path $imagesRoot $relativePath
        $targetDir = Split-Path -Parent $targetPath
        if (-not (Test-Path $targetDir)) {
            New-Item -ItemType Directory -Path $targetDir -Force | Out-Null
        }

        if (Test-Path $targetPath) {
            $skipped++
            continue
        }

        $url = "$baseUrl$relativePath"
        try {
            Invoke-WebRequest -Uri $url -OutFile $targetPath
            $downloaded++
        } catch {
            $failed++
            Write-Warning "Failed: $relativePath"
        }
    }
}

Write-Host "Download complete. downloaded=$downloaded skipped=$skipped failed=$failed"
