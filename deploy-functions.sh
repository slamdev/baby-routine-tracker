#!/bin/bash

# Deploy Firebase Functions Script
# Run this from the project root directory

echo "🚀 Deploying Firebase Functions (2nd Generation)..."

# Navigate to functions directory
cd functions

# Install dependencies
echo "📦 Installing dependencies..."
npm install

# Go back to project root
cd ..

# Delete old functions if they exist (optional - uncomment if needed)
# echo "🗑️  Deleting old 1st generation functions..."
# firebase functions:delete sendPartnerNotifications --force
# firebase functions:delete cleanupExpiredTokens --force

# Deploy new functions
echo "☁️  Deploying to europe-west1 region..."
firebase deploy --only functions

# Check function status
echo "✅ Checking deployed functions..."
firebase functions:list

echo "🎉 Deployment complete!"
echo ""
echo "📱 Make sure your Android app is configured to use europe-west1:"
echo "    private val functions = Firebase.functions(\"europe-west1\")"
