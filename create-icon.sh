#!/bin/bash
# Script to create a proper app icon for MyBudgetBuddy

# Create a simple but visible 64x64 app icon using ImageMagick (if available)
# This creates a green circle with a white dollar sign

if command -v convert >/dev/null 2>&1; then
    echo "Creating a better app icon using ImageMagick..."
    
    # Create base icon directory if not exists
    mkdir -p "src/main/resources/com/mybudgetbuddy/icons"
    
    # Create a 64x64 green icon with white dollar sign
    convert -size 64x64 xc:transparent \
            -fill "#2E7D32" \
            -draw "circle 32,32 32,8" \
            -fill white \
            -pointsize 36 \
            -gravity center \
            -annotate 0 '$' \
            src/main/resources/com/mybudgetbuddy/icons/app-icon.png
    
    echo "✅ New app icon created at src/main/resources/com/mybudgetbuddy/icons/app-icon.png"
else
    echo "ImageMagick not found. Please install it with: brew install imagemagick"
    echo "Or manually create a 64x64 PNG icon and save it as app-icon.png"
fi