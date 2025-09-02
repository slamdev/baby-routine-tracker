# Release Signing Setup Guide

## Overview
This guide explains how to set up proper app signing for release builds of the Baby Routine Tracker app.

## Prerequisites
- Java Development Kit (JDK) installed
- Android Studio or command line tools

## Step 1: Generate a Release Keystore

### Using Command Line (Recommended)
```bash
keytool -genkey -v -keystore release-key.keystore -alias baby-tracker-key -keyalg RSA -keysize 2048 -validity 10000
```

### Using Android Studio
1. Go to **Build > Generate Signed Bundle / APK**
2. Select **Android App Bundle** or **APK**
3. Click **Create new...**
4. Fill out the keystore information:
   - **Key store path**: `release-key.keystore` (in project root)
   - **Password**: Use a strong password
   - **Key alias**: `baby-tracker-key`
   - **Key password**: Can be same as keystore password
   - **Validity**: 25+ years (recommended for Play Store)

### Required Information
When generating the keystore, provide:
- **First and last name**: Your name or company name
- **Organizational unit**: Your department/team
- **Organization**: Your company name
- **City or Locality**: Your city
- **State or Province**: Your state/province
- **Country code**: Your country code (e.g., US, GB)

## Step 2: Secure the Keystore

### Option A: Local keystore.properties (For local development)
Create a file named `keystore.properties` in your project root:

```properties
KEYSTORE_PASSWORD=your_actual_keystore_password
KEY_ALIAS=baby-tracker-key
KEY_PASSWORD=your_actual_key_password
```

**IMPORTANT**: Add `keystore.properties` to your `.gitignore` file to prevent committing credentials.

### Option B: Environment Variables (For CI/CD)
Set these environment variables:
```bash
export KEYSTORE_PASSWORD="your_actual_keystore_password"
export KEY_ALIAS="baby-tracker-key"
export KEY_PASSWORD="your_actual_key_password"
```

## Step 3: Update .gitignore

Add these lines to your `.gitignore` file:
```
# Release signing
release-key.keystore
keystore.properties
*.jks
*.keystore
```

## Step 4: Build Release APK/AAB

### Using Gradle Command Line
```bash
# For APK
./gradlew assembleRelease

# For Android App Bundle (recommended for Play Store)
./gradlew bundleRelease
```

### Using Android Studio
1. Go to **Build > Generate Signed Bundle / APK**
2. Select your keystore file
3. Enter your credentials
4. Choose release build type
5. Select output format (AAB for Play Store, APK for direct distribution)

## Step 5: Verify the Build

### Check APK/AAB Location
- **APK**: `app/build/outputs/apk/release/`
- **AAB**: `app/build/outputs/bundle/release/`

### Verify Signing
```bash
# For APK
jarsigner -verify -verbose -certs app/build/outputs/apk/release/app-release.apk

# For AAB (after extracting)
bundletool verify --bundle=app/build/outputs/bundle/release/app-release.aab
```

## Security Best Practices

### Keystore Security
1. **Back up your keystore** in multiple secure locations
2. **Never commit keystore files** to version control
3. **Use strong passwords** (16+ characters, mixed case, numbers, symbols)
4. **Limit access** to keystore files (only necessary team members)
5. **Document recovery procedures** in case of keystore loss

### Password Management
- Use a password manager to generate and store keystore passwords
- Consider using different passwords for keystore and key
- Document your signing setup for team members

### CI/CD Security
- Use secure environment variable storage (GitHub Secrets, etc.)
- Limit access to signing credentials in CI/CD systems
- Regularly rotate passwords and keys if compromised

## Troubleshooting

### Common Issues
1. **"Keystore not found"**: Check the file path in `build.gradle`
2. **"Wrong password"**: Verify credentials in `keystore.properties` or environment variables
3. **"Key alias not found"**: Check the alias name matches exactly

### Recovery Options
If you lose your keystore:
- **Play Store**: Contact Google Play support (may require verification)
- **Direct Distribution**: You'll need to generate a new keystore and treat it as a new app

## Next Steps

After successful signing setup:
1. Test the release build on physical devices
2. Run thorough testing with release configuration
3. Upload to Play Console for internal testing
4. Prepare store listing metadata

## Important Notes

- The keystore file should be treated as highly confidential
- Once published to Play Store, you cannot change the signing key
- Always test release builds before distribution
- Consider using Play App Signing for additional security

For Play Store distribution, Android App Bundle (AAB) is recommended over APK for better optimization and smaller download sizes.
