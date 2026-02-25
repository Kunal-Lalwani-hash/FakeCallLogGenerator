# Fake Call Log Generator

A fully-featured Android app to create fake call log entries in your phone's call history.

## ✅ Features
- **Add call log entries** with:
  - Name (custom or from contacts)
  - Phone number (custom or from contacts)
  - Call Type: **Incoming**, **Outgoing**, **Missed**, **Did Not Connect** ✅ (new!)
  - Date & Time picker
  - Duration (seconds)
- **Pick contact** from your phone's contact list
- **Schedule call logs** for a future time using Android AlarmManager
- **View & cancel** scheduled call logs
- Works on Android 5.0+ (API 21+)

## 🔨 How to Build the APK

### Prerequisites
1. Install **Android Studio** (free): https://developer.android.com/studio
2. Make sure you have **JDK 11 or higher**

### Steps
1. Open Android Studio
2. Click **"Open"** and select this project folder (`FakeCallLogGenerator`)
3. Wait for Gradle sync to complete (~1-2 minutes)
4. To build a **debug APK**:
   - Go to **Build → Build Bundle(s) / APK(s) → Build APK(s)**
   - The APK will appear in: `app/build/outputs/apk/debug/app-debug.apk`
5. To build a **release APK**:
   - Go to **Build → Generate Signed Bundle / APK**
   - Choose APK → create or use a keystore → finish

### Install on Phone
- Enable **"Install from unknown sources"** on your Android phone
- Transfer the APK and tap to install
- Or use ADB: `adb install app-debug.apk`

## ⚠️ Permissions Required
- `READ_CONTACTS` — to pick contacts
- `WRITE_CALL_LOG` — to add fake entries
- `READ_CALL_LOG` — to read existing logs

## ℹ️ Notes on "Did Not Connect"
Android does not have a built-in `DID_NOT_CONNECT` call type constant. This app uses
call type `6` (which maps to `BLOCKED` on some Android versions) combined with
`MISSED_REASON = USER_MISSED_NO_ANSWER` on Android 10+ to simulate this. The display
in your phone's call history may vary by manufacturer.
