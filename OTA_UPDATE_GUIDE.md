# 🚀 GitHub OTA (Over-The-Air) In-App Auto-Update Guide

**Primo Visitor Management System — Android Mobile App**  
**Version:** 1.0  

---

## 🌟 OTA Feature Ka overview

Aapke Android app me ab **GitHub OTA Auto-Update system** poori tarah integrate ho chuka hai!

### 📱 Staff Ke Phone Me Kya Hoga?
1. Staff jaise hi app open karega, app background me GitHub se `version.json` check karega.
2. Agar aapne naya update release kiya hai to phone screen par popup aayega:
   > **🚀 New Update Available!**  
   > **v1.0 → v1.1**  
   > *What's New: [Aapke likhe huye features/bug fixes]*  
   > `[ Update Now ]`  `[ Later ]`
3. Staff **"Update Now"** dabayega -> App download progress bar dikhayega (0% → 100%).
4. Download complete hote hi Android Installer khul jayega: *"Do you want to install an update to this application?"* -> Staff **"Update"** dabayega aur app naye version me upgrade ho jayega!
5. Staff dashboard ke footer me maujood **"Check for Updates 🚀"** button ko tap karke bhi manually check kar sakta hai!

---

## 🛠️ Naya Update Push Karne Ka Asaan Tarika (Sirf 3 Steps)

Jab bhi aap app me koi naya feature dalein ya update nikalna chahein:

### Step 1: Version Number Badhayein
File open karein: `android-app/app/build.gradle.kts`  
Lines 14-15 par version badha dijiye:
```kotlin
versionCode = 2          // (Har baar +1 karein: 1, 2, 3...)
versionName = "1.1"      // (Aapka version name: 1.1, 1.2...)
```

### Step 2: Naya APK Build Karein
Project root folder me maujood **`build-apk.bat`** par double click karein.  
Naya APK ready hokar `PrimoStaffScanner.apk` ban jayega.

### Step 3: GitHub Par Upload Karein
1. **`version.json` Update Karein:**  
   Project root me maujood `version.json` me naya version number aur release notes likhein:
   ```json
   {
     "versionCode": 2,
     "versionName": "1.1",
     "apkUrl": "https://github.com/primoartgallery-v2/Primo-Visitor-Management-System-Apk/releases/latest/download/PrimoStaffScanner.apk",
     "releaseNotes": "Faster QR scanning, improved visitor search, and new exhibition cards.",
     "forceUpdate": false
   }
   ```
   Ise apne GitHub repository me push/upload kar dijiye.

2. **GitHub Release Banayein:**
   - Link kholein: https://github.com/primoartgallery-v2/Primo-Visitor-Management-System-Apk/releases/new
   - Tag version likhein: `v1.0` (ya naye update ke liye `v1.1`)
   - Release Title: `Primo Staff Scanner v1.0`
   - **Attach binaries:** `PrimoStaffScanner.apk` ko drag & drop karein.
   - **"Publish release"** par click kar dein!

---

## ⚙️ Configured URLs
App me aapka GitHub repository successfully set ho chuka hai:
```kotlin
const val DEFAULT_VERSION_URL =
    "https://raw.githubusercontent.com/primoartgallery-v2/Primo-Visitor-Management-System-Apk/main/version.json"

const val DEFAULT_FALLBACK_APK_URL =
    "https://github.com/primoartgallery-v2/Primo-Visitor-Management-System-Apk/releases/latest/download/PrimoStaffScanner.apk"
```
*(Tip: Agar repository private hai to aap `version.json` aur APK ko kisi public hosting jaise Cloudflare R2, Google Drive public link, ya Firebase hosting par bhi rakh sakte hain, tab bhi OTA bilkul aise hi kaam karega!)*
