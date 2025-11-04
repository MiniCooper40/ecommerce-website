# Generate placeholder images for MinIO seed data
# This script creates simple colored rectangles as placeholder product images

$outputDir = "$PSScriptRoot\products"

# Ensure output directory exists
if (-not (Test-Path $outputDir)) {
    New-Item -ItemType Directory -Path $outputDir -Force | Out-Null
}

Write-Host "Generating placeholder product images..." -ForegroundColor Cyan

# Define images with colors (hex format)
$images = @(
    # Electronics - Blue tones
    @{ name = "headphones-1.jpg"; color = "#4A90E2" },
    @{ name = "headphones-2.jpg"; color = "#357ABD" },
    @{ name = "fitness-watch-1.jpg"; color = "#5B9BD5" },
    @{ name = "fitness-watch-2.jpg"; color = "#2E75B6" },
    @{ name = "fitness-watch-3.jpg"; color = "#1F4E78" },
    @{ name = "webcam-1.jpg"; color = "#4472C4" },
    @{ name = "webcam-2.jpg"; color = "#2F5496" },
    
    # Home & Kitchen - Red/Orange tones
    @{ name = "coffee-maker-1.jpg"; color = "#E74C3C" },
    @{ name = "coffee-maker-2.jpg"; color = "#C0392B" },
    @{ name = "cookware-1.jpg"; color = "#E67E22" },
    @{ name = "cookware-2.jpg"; color = "#D35400" },
    @{ name = "cookware-3.jpg"; color = "#CA6F1E" },
    @{ name = "robot-vacuum-1.jpg"; color = "#F39C12" },
    @{ name = "robot-vacuum-2.jpg"; color = "#E67E22" },
    
    # Sports & Outdoors - Green tones
    @{ name = "yoga-mat-1.jpg"; color = "#27AE60" },
    @{ name = "yoga-mat-2.jpg"; color = "#229954" },
    @{ name = "yoga-mat-3.jpg"; color = "#1E8449" },
    @{ name = "dumbbells-1.jpg"; color = "#52BE80" },
    @{ name = "dumbbells-2.jpg"; color = "#45B39D" },
    @{ name = "tent-1.jpg"; color = "#16A085" },
    @{ name = "tent-2.jpg"; color = "#138D75" },
    @{ name = "tent-3.jpg"; color = "#117A65" },
    
    # Books & Media - Purple tones
    @{ name = "book-programming-1.jpg"; color = "#8E44AD" },
    @{ name = "book-programming-2.jpg"; color = "#7D3C98" },
    
    # Fashion - Mixed tones
    @{ name = "backpack-1.jpg"; color = "#34495E" },
    @{ name = "backpack-2.jpg"; color = "#2C3E50" },
    @{ name = "backpack-3.jpg"; color = "#1C2833" },
    @{ name = "running-shoes-1.jpg"; color = "#E74C3C" },
    @{ name = "running-shoes-2.jpg"; color = "#EC7063" },
    @{ name = "running-shoes-3.jpg"; color = "#F1948A" }
)

# Check if ImageMagick is available
$imageMagickPath = Get-Command "magick" -ErrorAction SilentlyContinue

if ($null -eq $imageMagickPath) {
    Write-Host ""
    Write-Host "ImageMagick not found. Attempting to download..." -ForegroundColor Yellow
    
    # Try multiple sources for ImageMagick
    $sources = @(
        "https://download.imagemagick.org/ImageMagick/download/binaries/ImageMagick-7.1.1-39-portable-Q16-HDRI-x64.zip",
        "https://imagemagick.org/archive/binaries/ImageMagick-7.1.1-portable-Q16-HDRI-x64.zip"
    )
    
    $imageMagickZip = "$env:TEMP\imagemagick.zip"
    $imageMagickDir = "$PSScriptRoot\.imagemagick"
    $downloaded = $false
    
    foreach ($url in $sources) {
        try {
            Write-Host "Trying: $url" -ForegroundColor Cyan
            $ProgressPreference = 'SilentlyContinue'
            Invoke-WebRequest -Uri $url -OutFile $imageMagickZip -UseBasicParsing -TimeoutSec 30
            $ProgressPreference = 'Continue'
            $downloaded = $true
            Write-Host "✓ Download successful!" -ForegroundColor Green
            break
        } catch {
            Write-Host "  Failed: $($_.Exception.Message)" -ForegroundColor Yellow
            continue
        }
    }
    
    if ($downloaded) {
        try {
            Write-Host "Extracting ImageMagick..." -ForegroundColor Cyan
            if (Test-Path $imageMagickDir) {
                Remove-Item $imageMagickDir -Recurse -Force
            }
            Expand-Archive -Path $imageMagickZip -DestinationPath $imageMagickDir -Force
            Remove-Item $imageMagickZip
            
            # Find magick.exe
            $magickExe = Get-ChildItem -Path $imageMagickDir -Filter "magick.exe" -Recurse | Select-Object -First 1
            if ($magickExe) {
                $imageMagickPath = @{ Source = $magickExe.FullName }
                Write-Host "✓ ImageMagick ready!" -ForegroundColor Green
                Write-Host ""
            } else {
                throw "magick.exe not found in extracted files"
            }
        } catch {
            Write-Host "Failed to extract: $($_.Exception.Message)" -ForegroundColor Red
            $downloaded = $false
        }
    }
    
    if (-not $downloaded) {
        Write-Host ""
        Write-Host "Auto-download failed. Manual options:" -ForegroundColor Yellow
        Write-Host ""
        Write-Host "OPTION 1 - Install via Chocolatey (Recommended):" -ForegroundColor Cyan
        Write-Host "  1. Open PowerShell as Administrator" -ForegroundColor White
        Write-Host "  2. Run: choco install imagemagick -y" -ForegroundColor White
        Write-Host "  3. Re-run this script" -ForegroundColor White
        Write-Host ""
        Write-Host "OPTION 2 - Manual Download:" -ForegroundColor Cyan
        Write-Host "  1. Download from: https://imagemagick.org/script/download.php#windows" -ForegroundColor White
        Write-Host "  2. Choose 'ImageMagick-...-portable-Q16-HDRI-x64.zip'" -ForegroundColor White
        Write-Host "  3. Extract to: $imageMagickDir" -ForegroundColor White
        Write-Host "  4. Re-run this script" -ForegroundColor White
        Write-Host ""
        Write-Host "OPTION 3 - Use your own images:" -ForegroundColor Cyan
        Write-Host "  Place 33 images in: $outputDir" -ForegroundColor White
        Write-Host "  See NEEDED_IMAGES.txt for list" -ForegroundColor White
        Write-Host ""
        
        # Create list of needed images
        $listPath = Join-Path $outputDir "NEEDED_IMAGES.txt"
        "The following images are needed for bootstrap data:`n" | Out-File $listPath
        foreach ($img in $images) {
            "$($img.name) - Color: $($img.color)" | Out-File $listPath -Append
        }
        
        Write-Host "Created: $listPath" -ForegroundColor Green
        exit 0
    }
}

Write-Host "Found ImageMagick at: $($imageMagickPath.Source)" -ForegroundColor Green
Write-Host ""

$count = 0
foreach ($img in $images) {
    $outputPath = Join-Path $outputDir $img.name
    $text = ($img.name -replace '-\d+\.jpg$', '') -replace '-', ' '
    
    # Create a 800x600 colored rectangle with text
    & $imageMagickPath.Source -size 800x600 "xc:$($img.color)" `
        -gravity center `
        -pointsize 48 `
        -fill white `
        -annotate +0+0 $text `
        -quality 85 `
        $outputPath
    
    $count++
    Write-Host "[$count/$($images.Count)] Created $($img.name)" -ForegroundColor Green
}

Write-Host ""
Write-Host "✓ Successfully generated $count placeholder images" -ForegroundColor Green
Write-Host "  Location: $outputDir" -ForegroundColor Cyan


Write-Host ""
Write-Host "Next steps:" -ForegroundColor Yellow
Write-Host "1. Review images in: $outputDir" -ForegroundColor White
Write-Host "2. Replace with actual product images if desired" -ForegroundColor White
Write-Host "3. Restart minio-setup container to upload: docker-compose restart minio-setup" -ForegroundColor White
