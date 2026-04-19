# Icon Requirements for MyBudgetBuddy

## 🎯 App Icon Status: WORKING ✅

**Your app icon IS loading successfully!** If you can't see it, try:

### Quick Fixes

1. **Check the Dock** (bottom of screen) while app runs
2. **Press ⌘ + Tab** to see app switcher  
3. **Clear macOS cache**: `sudo rm -rf /Library/Caches/com.apple.iconservices.store && killall Dock`

### Get a Better Icon

- Download: [Budget Icon (64x64)](https://www.flaticon.com/free-icon/budget_2331966)
- Save as: `app-icon.png` in this folder
- Restart app

---

## Required Icon Files (16x16 or 24x24 PNG format)

### Application Icon

- **app-icon.png** - Main application icon (shows in taskbar/title bar)
  - Suggested: Dollar sign, piggy bank, or budget chart icon

### Button Icons (Goals View)

- **create-goal.png** - Plus (+) icon for creating new goals
- **edit-goal.png** - Pencil/edit icon for editing goals  
- **delete-goal.png** - Trash can icon for deleting goals
- **refresh.png** - Refresh/reload arrow icon

### Tab Icons (Main Navigation)

- **transactions-tab.png** - Money/dollar/credit card icon
- **reports-tab.png** - Chart/graph/analytics icon  
- **goals-tab.png** - Target/bullseye/trophy icon

## Where to Get Icons

1. **Free Icon Sources:**
   - Feather Icons (feathericons.com)
   - Heroicons (heroicons.com)
   - Phosphor Icons (phosphoricons.com)
   - Material Design Icons (material.io/icons)

2. **Icon Creation:**
   - Use tools like Canva, Figma, or GIMP
   - Keep them simple and consistent in style
   - Use a monochrome or limited color palette

3. **Download Instructions:**
   - Save as PNG format
   - Size: 16x16 or 24x24 pixels
   - Name exactly as listed above
   - Place in: src/main/resources/com/mybudgetbuddy/icons/

## Testing

Once you add the icon files, run:

```bash
mvn javafx:run
```

The application should display your icons in:

- ✅ Application taskbar/title bar
- ✅ Goals view buttons  
- ✅ Navigation tabs
