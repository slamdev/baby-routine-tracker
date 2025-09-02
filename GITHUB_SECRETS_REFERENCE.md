# GitHub Secrets Reference Card

Quick reference for setting up GitHub repository secrets for the CI/CD pipeline.

**Location**: `Settings > Secrets and Variables > Actions > Repository Secrets`

## 🔐 Required Secrets

| Secret Name | Description | How to Get |
|-------------|-------------|------------|
| `KEYSTORE_BASE64` | Base64 encoded keystore file | `base64 -w 0 release-key.keystore` |
| `KEYSTORE_PASSWORD` | Keystore password | From your `keystore.properties` file |
| `KEY_ALIAS` | Key alias (usually `baby-tracker-key`) | From your `keystore.properties` file |
| `KEY_PASSWORD` | Key password | From your `keystore.properties` file |
| `GOOGLE_SERVICES_JSON` | Firebase configuration | Contents of `app/google-services.json` |
| `GOOGLE_PLAY_SERVICE_ACCOUNT_JSON` | Play Console service account | JSON file from Google Cloud Console |

## 🛠️ Quick Setup Commands

### 1. Encode Keystore
```bash
# macOS/Linux
base64 -w 0 release-key.keystore | pbcopy

# Windows (PowerShell)
[Convert]::ToBase64String([IO.File]::ReadAllBytes("release-key.keystore")) | Set-Clipboard
```

### 2. Copy Firebase Config
```bash
# Copy google-services.json content
cat app/google-services.json | pbcopy
```

### 3. Validate Setup
```bash
# Run validation script
./scripts/validate-ci-setup.sh
```

## 🔄 Service Account Setup

### Google Cloud Console Steps:
1. **Enable API**: Google Play Developer Reporting API
2. **Create Service Account**: IAM & Admin > Service Accounts
3. **Generate Key**: Keys tab > Add Key > JSON
4. **Download JSON**: Keep secure, add to GitHub Secrets

### Play Console Steps:
1. **API Access**: Setup > API access
2. **Link Project**: Link your Google Cloud project  
3. **Grant Permissions**: 
   - ✅ Release apps to testing tracks
   - ✅ View app information and download bulk reports

## ⚡ Quick Validation

After adding secrets, check:
- [ ] All 6 secrets are configured
- [ ] Secret names match exactly (case-sensitive)
- [ ] No extra spaces in secret values
- [ ] Service account has proper permissions
- [ ] Google Play API is enabled

## 🚨 Troubleshooting

| Error | Solution |
|--------|----------|
| `Keystore not found` | Check `KEYSTORE_BASE64` encoding |
| `Invalid password` | Verify `KEYSTORE_PASSWORD` and `KEY_PASSWORD` |
| `Permission denied` | Check service account permissions in Play Console |
| `API not enabled` | Enable Google Play Developer Reporting API |

## 📱 First Deployment

1. **Push to main**: `git push origin main`
2. **Check Actions**: GitHub repository > Actions tab
3. **Monitor workflow**: Watch for success/failure
4. **Verify in Play Console**: Release > Testing > Internal testing

---

🎯 **Need help?** See detailed instructions in [`CI_PIPELINE_SETUP.md`](./CI_PIPELINE_SETUP.md)
