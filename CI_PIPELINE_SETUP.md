# CI/CD Pipeline Setup Guide

This guide explains how to set up the automated CI/CD pipeline that builds and deploys the Baby Routine Tracker app to Google Play Console internal testing on every commit to the main branch.

## Overview

The CI/CD pipeline automatically:
- ✅ Runs unit tests on every push/PR
- ✅ Builds signed release AAB (Android App Bundle) 
- ✅ Uploads to Google Play Console internal testing track
- ✅ Creates GitHub releases with version tracking
- ✅ Automatically increments version codes based on commit count

## Prerequisites

Before setting up the pipeline, you'll need:

1. **Google Play Console Account** with the app registered
2. **Google Cloud Project** with Play Developer Reporting API enabled  
3. **Service Account** for automated deployments
4. **Keystore File** for app signing (already exists in your project)
5. **GitHub Repository** with Actions enabled

## Step 1: Google Play Console Setup

### 1.1 Enable Google Play Developer Reporting API
1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Select your Firebase project (or create a new one)
3. Navigate to "APIs & Services" > "Library"
4. Search for "Google Play Developer Reporting API"
5. Click "Enable"

### 1.2 Create Service Account
1. In Google Cloud Console, go to "IAM & Admin" > "Service Accounts"
2. Click "Create Service Account"
3. Fill in details:
   - **Name**: `github-actions-play-deploy`
   - **Description**: `Service account for GitHub Actions to deploy to Play Console`
4. Click "Create and Continue"
5. Skip granting additional roles (we'll set this up in Play Console)
6. Click "Done"

### 1.3 Generate Service Account Key
1. Click on the newly created service account
2. Go to "Keys" tab
3. Click "Add Key" > "Create New Key"
4. Choose "JSON" format
5. Download the JSON file (keep it secure!)

### 1.4 Link Service Account to Play Console
1. Go to [Google Play Console](https://play.google.com/console/)
2. Navigate to "Setup" > "API access"
3. Click "Link" next to your Google Cloud project
4. Find your service account in the list
5. Grant the following permissions:
   - **Release apps to testing tracks** (required)
   - **View app information and download bulk reports** (optional but recommended)

## Step 2: GitHub Repository Secrets

Add the following secrets in your GitHub repository:
**Settings > Secrets and Variables > Actions > Repository Secrets**

### 2.1 App Signing Secrets

#### `KEYSTORE_BASE64`
```bash
# Convert your keystore to base64
base64 -i release-key.keystore | pbcopy  # macOS
base64 -w 0 release-key.keystore         # Linux
```
Copy the output and paste it as the secret value.

#### `KEYSTORE_PASSWORD`
Your keystore password (from keystore.properties)

#### `KEY_ALIAS` 
Your key alias (from keystore.properties, probably `baby-tracker-key`)

#### `KEY_PASSWORD`
Your key password (from keystore.properties)

### 2.2 Firebase Configuration Secret

#### `GOOGLE_SERVICES_JSON`
```bash
# Copy the contents of your google-services.json
cat app/google-services.json | pbcopy  # macOS
cat app/google-services.json          # Linux
```
Copy the entire JSON content and paste it as the secret value.

### 2.3 Google Play Deploy Secret

#### `GOOGLE_PLAY_SERVICE_ACCOUNT_JSON`
Copy the entire contents of the service account JSON file you downloaded in Step 1.3 and paste it as the secret value.

## Step 3: Configure Google Play Console Tracks

### 3.1 Set up Internal Testing Track
1. In Google Play Console, go to your app
2. Navigate to "Release" > "Testing" > "Internal testing"
3. Create a new release if you haven't already
4. Add testers (email addresses of people who will test the app)
5. Make sure the track is active

### 3.2 Upload Initial Release (One-time Setup)
For the first release, you may need to upload manually:
1. Build a release locally: `./gradlew bundleRelease`
2. Upload the AAB file to Internal Testing track
3. Fill out required store listing information
4. Once approved, the automated pipeline can take over

## Step 4: Test the Pipeline

### 4.1 Verify Secrets
Run this command in your repository root to verify all secrets are properly configured:

```bash
# This will be added to the workflow to validate setup
echo "Secrets check will be performed during first workflow run"
```

### 4.2 Trigger First Deployment
1. Make a small commit to the main branch
2. Push to GitHub: `git push origin main`
3. Check the "Actions" tab in your GitHub repository
4. Monitor the workflow execution

### 4.3 Verify Deployment
1. Check Google Play Console > Internal Testing
2. Verify new build appears with correct version code
3. Test download and installation on a device

## Step 5: Version Management

### Automatic Version Codes
The pipeline automatically calculates version codes using:
```
VERSION_CODE = (Number of commits in repository) + 100
```

This ensures each build has a unique, incrementing version code.

### Manual Version Name Updates
To release a new version (1.0.0 → 1.1.0):
1. Update `versionName` in `app/build.gradle`
2. Commit and push to main branch
3. The pipeline will automatically use the new version name

## Step 6: Monitoring and Troubleshooting

### Common Issues

#### 🔴 "Keystore not found"
- Verify `KEYSTORE_BASE64` secret is correctly set
- Ensure base64 encoding was done correctly

#### 🔴 "Google Play API permission denied"
- Check service account permissions in Play Console
- Verify `GOOGLE_PLAY_SERVICE_ACCOUNT_JSON` is correct
- Ensure Google Play Developer Reporting API is enabled

#### 🔴 "App not found in Play Console"  
- Verify the `packageName` in the workflow matches your app ID
- Ensure the app exists in Google Play Console

#### 🔴 "Invalid keystore password"
- Double-check `KEYSTORE_PASSWORD`, `KEY_ALIAS`, and `KEY_PASSWORD` secrets
- Verify they match your local keystore.properties

### Success Indicators

✅ **Workflow completes successfully**
✅ **New build appears in Play Console Internal Testing** 
✅ **GitHub release is created automatically**
✅ **Version code increments properly**
✅ **App installs and runs correctly**

### Monitoring
- **GitHub Actions**: Monitor workflow runs in the "Actions" tab
- **Play Console**: Check "Release" > "Testing" > "Internal testing" for new builds
- **GitHub Releases**: View automatically created releases with build information

## Advanced Configuration

### Custom Release Notes
Edit the `releaseNotes` section in `.github/workflows/android-release.yml` to customize the release notes format.

### Different Testing Tracks
To deploy to different tracks (alpha, beta, production), modify the `track` parameter in the workflow.

### Manual Deployment
You can also manually trigger deployments:
1. Go to GitHub repository > Actions
2. Select "Android CI/CD - Internal Testing Release"
3. Click "Run workflow" > "Run workflow"

## Security Best Practices

- 🔒 **Never commit secrets** to the repository
- 🔒 **Use repository secrets** for sensitive information
- 🔒 **Regularly rotate** service account keys  
- 🔒 **Monitor access logs** in Google Cloud Console
- 🔒 **Review permissions** periodically

## Support

If you encounter issues:
1. Check the workflow logs in GitHub Actions
2. Review Google Play Console error messages
3. Verify all secrets are correctly configured
4. Test local builds to ensure your keystore setup works

---

🎉 **Congratulations!** Your automated CI/CD pipeline is now set up and ready to deploy to Google Play Console internal testing on every commit to main branch.
