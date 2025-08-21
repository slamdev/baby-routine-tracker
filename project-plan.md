# **Project Plan: Newborn Tracker App**

## **1\. Project Overview**

This document outlines the project plan for a personal Android application designed to help new parents track their newborn's daily activities, including sleep, feedings, and diaper changes. The app will feature real-time data synchronization between multiple devices using Firebase, allowing both parents to view and contribute to the same dataset. It will incorporate a modern user interface with light and dark modes, responsive layouts for various screen orientations and split-screen mode, and data visualization for historical trends. A key feature will be the integration of the Google Gemini API to provide AI-powered suggestions for building a healthy sleep routine for the baby.

## **2\. Goals and Objectives**

* **Primary Goal:** To create a simple, intuitive, and collaborative tool for parents to track their newborn's essential activities.  
* **Objectives:**  
  * To provide a shared, real-time view of the baby's activities for both parents.  
  * To reduce parental stress by providing clear, accessible data on the baby's patterns.  
  * To leverage AI to offer helpful, data-driven suggestions for establishing a sleep routine.  
  * To ensure a seamless user experience across different device orientations and screen sizes.  
  * To visualize historical data to help parents understand their baby's development and patterns.

## **3\. Scope**

### **In-Scope Features:**

* **User Authentication:**  
  * Secure sign-in using built-in Android Google accounts via Firebase Authentication.  
  * Automatic linking of the multiple accounts to a shared baby profile.  
* **Activity Tracking:**  
  * **Sleep:** Log start and end times, duration, and notes.  
  * **Feedings:** Log start time, duration (for breastfeeding), amount (for bottle-feeding), type (breast milk, bottle), and notes.  
  * **Diapers:** Log time for poop occurrences and notes.  
* **Real-time Dashboard:**  
  * A main screen showing the most recent activity for each category (last sleep, last feeding, last diaper change).  
  * Timers for ongoing activities (e.g., current sleep session).  
  * At-a-glance view of the current day's routine.  
* **Data Synchronization:**  
  * Real-time data updates between connected devices using Firebase Firestore.  
* **UI/UX:**  
  * Support for both light and dark modes, respecting the system setting.  
  * Fully responsive layout for portrait, landscape, and split-screen modes.  
  * Clean, modern, and intuitive user interface built with Jetpack Compose.
  * Modern Android app design with compact profile icon in top-right corner instead of large welcome cards.
  * Space-efficient dashboard layout that prioritizes activity tracking content.
  * Personal title bar showing baby's name instead of app name for contextual awareness.
  * Streamlined interface removing redundant headers to focus on activity tracking.
  * Consolidated baby profile management in the profile menu for cleaner organization.
  * **NEW: Swipe Navigation** - Horizontal swipe navigation between screens for modern mobile UX:
    * Main Dashboard (center) - Activity tracking with sleep, feeding, and diaper cards
    * Activity History (swipe left) - Complete history of all logged activities
    * Data Visualization (swipe right) - Charts and trends (coming soon)
    * AI Sleep Plans (swipe further right) - AI-powered sleep suggestions (coming soon)
  * Visual page indicators with labels showing current screen and available navigation options.  
* **AI-Powered Sleep Routine Builder:**  
  * Integration with the Google Gemini API.  
  * The app will send sleep data and baby age to the Gemini API.  
  * The API will return suggestions for a sleep schedule, including estimated nap times and bedtimes.  
* **Data Visualization:**  
  * Simple charts and graphs to visualize historical data (e.g., daily sleep totals, number of feedings per day).  
  * Ability to filter data by date range.  
* **Baby Profile Management:**  
  * Create and manage baby profile with name and birthdate.  
* **User Onboarding:**  
  * Guided setup process for first-time users.  
* **Data Management:**  
  * Account deletion and data cleanup options.  
* **App Settings & Preferences:**  
  * Customizable notification preferences.  
  * Default feeding types and amounts.  
* **Push Notifications:**  
  * Optional feeding reminders.  
  * Sleep schedule notifications.  
  * Partner activity updates.  
  * AI routine suggestion alerts.

## **4\. Target Audience**

* **Primary Users:** The user and their wife.  
* **User Profile:** New parents who are tech-savvy and want a digital solution to track their baby's needs and patterns.

## **5\. Technical Stack**

* **Platform:** Android (Native)  
* **Language:** Kotlin  
* **UI Toolkit:** Jetpack Compose  
* **Architecture:** MVVM (Model-View-ViewModel)  
* **Backend & Database:** Google Firebase  
  * **Firestore:** For real-time database.  
  * **Firebase Authentication:** For user sign-in.  
* **AI Integration:** Google Gemini API  
* **Data Visualization:** [MPAndroidChart](https://github.com/PhilJay/MPAndroidChart) or a similar library.  
* **Analytics & Crash Reporting:** Firebase Analytics and Crashlytics.  
* **IDE:** Android Studio

## **5.1\. Data Model Definition**

### **Firestore Collections Structure:**

```
users/{userId}
  - displayName: String
  - email: String
  - profileImageUrl: String (optional)
  - createdAt: Timestamp
  - lastActiveAt: Timestamp

babies/{babyId}
  - name: String
  - birthDate: Timestamp
  - parentIds: Array<String>
  - createdAt: Timestamp
  - updatedAt: Timestamp

babies/{babyId}/activities/{activityId}
  - type: String ("sleep", "feeding", "diaper")
  - startTime: Timestamp
  - endTime: Timestamp (optional for ongoing activities)
  - notes: String (optional)
  - loggedBy: String (userId)
  - createdAt: Timestamp
  - updatedAt: Timestamp
  
  // Feeding-specific fields
  - feedingType: String ("breast_milk", "bottle")
  - amount: Number (ml, for bottle feeding)
  
  // Diaper-specific fields
  - diaperType: String ("poop")

babies/{babyId}/sleepPlans/{planId}
  - name: String
  - active: Boolean
  - createdBy: String (userId)
  - createdAt: Timestamp
  - aiGenerated: Boolean
  - schedule: Array<Object>
    - startTime: String (HH:mm format)
    - endTime: String (HH:mm format)
    - type: String ("nap", "bedtime")
    - notes: String (optional)

invitations/{invitationId}
  - babyId: String
  - invitedBy: String (userId)
  - invitationCode: String (unique)
  - status: String ("pending", "accepted", "expired")
  - createdAt: Timestamp
  - expiresAt: Timestamp
```

### **Security Rules Strategy:**
- Users can only access babies where their userId is in the parentIds array
- Invitations are publicly readable by invitation code but only writable by authenticated users
- Activity logs are only readable/writable by baby's parents

## **5.2\. Error Handling & Offline Support**

### **Error Scenarios:**
* Network connectivity loss during activity logging
* Firestore write/read failures
* Authentication token expiration
* AI API rate limiting or failures
* Device storage limitations
* Invalid data validation errors

### **Offline Strategy:**
* Cache recent activities locally using Room database
* Queue failed writes for retry when connection restored
* Provide offline indicators in UI
* Allow viewing recent data without network connection
* Graceful degradation for AI features when offline

### **Error Recovery:**
* Automatic retry mechanism for failed operations
* User-friendly error messages with actionable suggestions
* Fallback UI states for failed data loading
* Conflict resolution for simultaneous edits from multiple devices

## **5.3\. Performance Considerations**

### **Data Management:**
* Implement pagination for historical data (load data in chunks)
* Use Firestore's built-in caching to reduce network calls
* Implement efficient queries with proper indexing
* Set data retention policies to manage database size

### **UI Performance:**
* Use LazyColumn/LazyRow for large lists
* Implement proper state management to avoid unnecessary recompositions
* Optimize chart rendering for large datasets
* Use remember() and derivedStateOf() appropriately

### **Memory Management:**
* Proper lifecycle management for Firestore listeners
* Background task management for data synchronization

### **Battery Optimization:**
* Minimize background processing
* Efficient use of location services (if needed for future features)
* Proper handling of device sleep modes

## **6\. Development Phases & Timeline**

This project will be broken down into six manageable phases.

### **Phase 0: Project Setup & Architecture (3-5 Days)**

* \[ \] Set up development environment and project structure documentation.  
* \[ \] Create Android Studio project with proper package structure.  
* \[ \] Configure build.gradle files with all required dependencies.  
* \[ \] Set up Firebase project and configure for development/production environments.  
* \[ \] Implement basic navigation architecture using Compose Navigation.  
* \[ \] Configure Room database for offline caching.  
* \[ \] Implement basic error handling framework and utilities.  
* \[ \] Set up analytics and crash reporting (Firebase Analytics & Crashlytics).  
* \[ \] Create initial repository pattern structure.  
* \[ \] Set up CI/CD pipeline basics.

### **Phase 1: Authentication & Baby Profile Setup (1-2 Weeks)**

* \[ \] Implement Firebase Authentication with Google Sign-In flow.  
* \[ \] Create user onboarding screens.  
* \[ \] Design and implement baby profile creation and management.  
* \[ \] Implement Firestore database schema for users, babies, and activities.  
* \[ \] Create partner invitation system with unique codes.  
* \[ \] Implement baby profile sharing and multi-user access.  
* \[ \] Set up comprehensive error handling for authentication flows.  
* \[ \] Create basic navigation structure and app settings screen.

### **Phase 2: Core Activity Logging & Dashboard (2-3 Weeks)**

* \[ \] Create the basic UI for logging sleep, feedings, and diaper changes.  
* \[ \] Implement real-time timers for ongoing activities.  
* \[ \] Build the main dashboard to show the latest activities.  
* \[ \] Ensure real-time data synchronization between devices.  
* \[ \] Implement activity editing and time adjustment features.  
* \[ \] Add offline support with local caching and sync queue.  
* \[ \] Create activity history view with basic filtering.  
* \[ \] Implement comprehensive input validation and error handling.

### **Phase 3: UI/UX Enhancement & Responsive Design (1-2 Weeks)**

* \[ \] Implement light and dark themes with system setting detection.  
* \[ \] Develop responsive layouts that work in portrait, landscape, and split-screen modes.  
* \[ \] Refine the overall UI for an intuitive user experience.  
* \[ \] Implement app settings and user preferences screen.  
* \[ \] Create loading states and improved error UI.  
* \[ \] Implement push notification system and preferences.

### **Phase 4: Data Visualization & Analytics (1-2 Weeks)**

* \[ \] Integrate a charting library (e.g., MPAndroidChart).  
* \[ \] Create screens to display historical data for each activity type.  
* \[ \] Implement advanced date filters and data range selection.  
* \[ \] Design and implement the UI for the visualization screens.  
* \[ \] Implement data insights and pattern recognition.  
* \[ \] Create data management tools (cleanup).

### **Phase 5: AI Integration & Sleep Routine Builder (2-3 Weeks)**

* \[ \] Set up a Google Cloud project and enable the Gemini API.  
* \[ \] Implement the logic to securely send sleep data to the Gemini API.  
* \[ \] Design and implement the UI to display the AI-generated sleep routine suggestions.  
* \[ \] Handle API responses and potential errors gracefully.  
* \[ \] Create sleep plan customization and saving features.  
* \[ \] Implement AI suggestion feedback and improvement system.  
* \[ \] Add AI-powered insights for feeding and sleep patterns.

### **Phase 6: Testing, Polish & Deployment (1-2 Weeks)**

* \[ \] Test all features, including data synchronization, UI responsiveness, and AI integration.  
* \[ \] Perform multi-device testing with real users (you and your wife).  
* \[ \] Fix any identified bugs or issues.  
* \[ \] Conduct performance testing with large datasets.  
* \[ \] Implement final security review and penetration testing.  
* \[ \] Prepare the app for a production release (even if it's a private release).  
* \[ \] Create user documentation and help system.  
* \[ \] Deploy the app to your devices.

## **7\. Risks and Mitigation**

* **Risk:** The cost of using the Google Gemini API might become significant with frequent use.  
  * **Mitigation:** Implement logic to cache responses and only call the API when there is a significant amount of new data. Monitor API usage closely in the Google Cloud Console. Set up budget alerts and rate limiting.  
* **Risk:** Ensuring the privacy and security of sensitive baby data.  
  * **Mitigation:** Use Firebase's security rules to ensure that only authenticated users can access their data. Anonymize data sent to the Gemini API. Implement end-to-end encryption for sensitive data. Regular security audits.  
* **Risk:** The complexity of creating a perfectly responsive UI for all screen sizes and states.  
  * **Mitigation:** Use Jetpack Compose's adaptive layout components and test extensively on different emulators and physical devices. Implement progressive layout testing throughout development.  
* **Risk:** The AI-generated sleep suggestions may not be accurate or helpful.  
  * **Mitigation:** Clearly label the suggestions as AI-generated and include a disclaimer. Provide a way for users to give feedback on the suggestions to potentially fine-tune the prompts sent to the API in the future.  
* **Risk:** Data synchronization conflicts when both parents log activities simultaneously.  
  * **Mitigation:** Implement proper conflict resolution strategies using Firestore's transaction capabilities. Use optimistic updates with rollback mechanisms.  
* **Risk:** Performance degradation with large amounts of historical data.  
  * **Mitigation:** Implement pagination, data archiving policies, and efficient indexing. Use lazy loading for charts and historical views.  
* **Risk:** Device storage limitations affecting offline functionality.  
  * **Mitigation:** Implement intelligent data pruning for local cache. Monitor storage usage and provide cleanup options.  
* **Risk:** Battery drain from real-time synchronization and background processing.  
  * **Mitigation:** Optimize background tasks and implement efficient listener management. Use WorkManager for background operations.  
* **Risk:** Network connectivity issues affecting app usability.  
  * **Mitigation:** Implement comprehensive offline support with local caching and sync queue. Provide clear offline indicators and graceful degradation.  
