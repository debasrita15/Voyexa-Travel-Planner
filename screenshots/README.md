# Screenshot Capture Guide

## How to Capture App Screenshots

### Method 1: Using ADB (Recommended)

1. **Connect your device** via USB or use an emulator
2. **Navigate to the screen** you want to capture
3. **Run the capture command:**
   ```bash
   adb shell screencap -p /sdcard/screenshot.png
   adb pull /sdcard/screenshot.png screenshots/home.png
   ```

### Method 2: Android Studio

1. Open **Logcat** panel
2. Click the **camera icon** 📷
3. Save to `screenshots/` directory

### Required Screenshots

Please capture the following screens:

- ✅ `home.png` - Home screen with trip list
- ✅ `create_trip.png` - Create trip screen with date pickers
- ✅ `trip_detail.png` - Trip detail with itinerary
- ✅ `ai_enrichment.png` - Activities showing AI ✨ badges
- ✅ `suggestions.png` - Suggestions screen with budget overview
- ✅ `settings.png` - Settings screen with LLM configuration
- ✅ `year_planner.png` - Year planner calendar view
- ✅ `budget_tracking.png` - Budget progress display

### Screenshot Specifications

- **Resolution:** 1080x1920 (portrait) or device native
- **Format:** PNG
- **File size:** < 500KB each (compress if needed)
- **Content:** Clean UI, no personal data

### Quick Batch Capture Script

```bash
#!/bin/bash
# Save as capture_screenshots.sh

screens=("home" "create_trip" "trip_detail" "ai_enrichment" "suggestions" "settings" "year_planner" "budget_tracking")

for screen in "${screens[@]}"; do
    echo "Capture $screen screen and press Enter..."
    read
    adb shell screencap -p /sdcard/temp.png
    adb pull /sdcard/temp.png "screenshots/$screen.png"
    echo "✓ Saved $screen.png"
done

adb shell rm /sdcard/temp.png
echo "✓ All screenshots captured!"
```

### Image Optimization (Optional)

To reduce file size:
```bash
# Install pngquant if needed
brew install pngquant

# Compress all screenshots
pngquant --quality=65-80 --ext .png --force screenshots/*.png
```

### After Capturing

1. Verify all 8 screenshots are in `screenshots/` directory
2. Check image quality and visibility
3. Commit to git: `git add screenshots/ && git commit -m "docs: add app screenshots"`
4. README will automatically display them!
