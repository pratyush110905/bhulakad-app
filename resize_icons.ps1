Add-Type -AssemblyName System.Drawing
$source = "C:\Users\praty\.gemini\antigravity\brain\74e8c60b-d325-4e06-b81b-6f531610e6bf\.user_uploaded\media_1791539594393_82274716.jpg"
$resDir = "d:\AIML\ANTIGRAVITY\APP\app\src\main\res"

$img = [System.Drawing.Image]::FromFile($source)

# Crop 12% from all sides to remove the watermark and center the logo
$cropPercent = 0.12
$cropX = [int][math]::Round($img.Width * $cropPercent)
$cropY = [int][math]::Round($img.Height * $cropPercent)
$cropW = [int]($img.Width - ($cropX * 2))
$cropH = [int]($img.Height - ($cropY * 2))

$rect = New-Object System.Drawing.Rectangle($cropX, $cropY, $cropW, $cropH)
$bmpCropped = New-Object System.Drawing.Bitmap($cropW, $cropH)
$gCrop = [System.Drawing.Graphics]::FromImage($bmpCropped)
$gCrop.DrawImage($img, (New-Object System.Drawing.Rectangle(0, 0, $cropW, $cropH)), $rect, [System.Drawing.GraphicsUnit]::Pixel)
$gCrop.Dispose()

$sizes = @{
    "mipmap-mdpi" = 48
    "mipmap-hdpi" = 72
    "mipmap-xhdpi" = 96
    "mipmap-xxhdpi" = 144
    "mipmap-xxxhdpi" = 192
}

foreach ($key in $sizes.Keys) {
    $folder = "$resDir\$key"
    if (-not (Test-Path $folder)) {
        New-Item -ItemType Directory -Force -Path $folder | Out-Null
    }
    $size = $sizes[$key]
    $bmp = New-Object System.Drawing.Bitmap($size, $size)
    $g = [System.Drawing.Graphics]::FromImage($bmp)
    $g.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
    $g.DrawImage($bmpCropped, 0, 0, $size, $size)
    
    $bmp.Save("$folder\ic_launcher.png", [System.Drawing.Imaging.ImageFormat]::Png)
    $bmp.Save("$folder\ic_launcher_round.png", [System.Drawing.Imaging.ImageFormat]::Png)
    
    $g.Dispose()
    $bmp.Dispose()
}

$bmpCropped.Dispose()
$img.Dispose()
Write-Output "Icons generated successfully with watermark removed."
