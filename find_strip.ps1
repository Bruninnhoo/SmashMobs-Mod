Add-Type -AssemblyName System.Drawing
$img = [System.Drawing.Bitmap]::FromFile("src\main\resources\assets\smashmobs\textures\gui\hud_player.png")
$minY = 999; $maxY = 0;
for ($x = 100; $x -lt $img.Width; $x++) {
    for ($y = 0; $y -lt $img.Height; $y++) {
        $c = $img.GetPixel($x, $y)
        if ($c.A -gt 0) { # Any pixel on right half
           if ($y -lt $minY) { $minY = $y }; if ($y -gt $maxY) { $maxY = $y }
        }
    }
}
Write-Output "RIGHT_HALF_Y_BOUNDS: $minY to $maxY"
$img.Dispose()
