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
  * **Feedings:** Log start time, duration (for breastfeeding), amount (for bottle-feeding), type (breast milk, formula), and notes.  
  * **Diapers:** Log time, type (wet, dirty, or both), and notes.  
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
* **AI-Powered Sleep Routine Builder:**  
  * Integration with the Google Gemini API.  
  * The app will send sleep data and baby age to the Gemini API.  
  * The API will return suggestions for a sleep schedule, including estimated nap times and bedtimes.  
* **Data Visualization:**  
  * Simple charts and graphs to visualize historical data (e.g., daily sleep totals, number of feedings per day).  
  * Ability to filter data by date range.

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
* **IDE:** Android Studio

## **6\. Development Phases & Timeline**

This project will be broken down into five manageable phases.

### **Phase 1: Foundation & Core Features (1-2 Weeks)**

* \[ \] Set up a new Android Studio project with Kotlin and Jetpack Compose.  
* \[ \] Integrate the Firebase SDK into the project.  
* \[ \] Set up Firebase Authentication with Google Sign-In.  
* \[ \] Design and implement the Firestore database schema for activities.  
* \[ \] Create the basic UI for logging sleep, feedings, and diaper changes.

### **Phase 2: UI/UX & Real-time Dashboard (2-3 Weeks)**

* \[ \] Implement the main dashboard to show the latest activities.  
* \[ \] Ensure real-time data synchronization between devices.  
* \[ \] Implement light and dark themes.  
* \[ \] Develop responsive layouts that work in portrait, landscape, and split-screen modes.  
* \[ \] Refine the overall UI for an intuitive user experience.

### **Phase 3: Data Visualization (1-2 Weeks)**

* \[ \] Integrate a charting library (e.g., MPAndroidChart).  
* \[ \] Create screens to display historical data for each activity type.  
* \[ \] Implement date filters for the historical data.  
* \[ \] Design and implement the UI for the visualization screens.

### **Phase 4: AI Integration (2-3 Weeks)**

* \[ \] Set up a Google Cloud project and enable the Gemini API.  
* \[ \] Implement the logic to securely send sleep data to the Gemini API.  
* \[ \] Design and implement the UI to display the AI-generated sleep routine suggestions.  
* \[ \] Handle API responses and potential errors gracefully.

### **Phase 5: Testing & Deployment (1 Week)**

* \[ \] Conduct thorough testing on both devices.  
* \[ \] Test all features, including data synchronization, UI responsiveness, and AI integration.  
* \[ \] Fix any identified bugs or issues.  
* \[ \] Prepare the app for a production release (even if it's a private release).  
* \[ \] Deploy the app to your devices.

## **7\. Risks and Mitigation**

* **Risk:** The cost of using the Google Gemini API might become significant with frequent use.  
  * **Mitigation:** Implement logic to cache responses and only call the API when there is a significant amount of new data. Monitor API usage closely in the Google Cloud Console.  
* **Risk:** Ensuring the privacy and security of sensitive baby data.  
  * **Mitigation:** Use Firebase's security rules to ensure that only authenticated users can access their data. Anonymize data sent to the Gemini API.  
* **Risk:** The complexity of creating a perfectly responsive UI for all screen sizes and states.  
  * **Mitigation:** Use Jetpack Compose's adaptive layout components and test extensively on different emulators and physical devices.  
* **Risk:** The AI-generated sleep suggestions may not be accurate or helpful.  
  * **Mitigation:** Clearly label the suggestions as AI-generated and include a disclaimer. Provide a way for users to give feedback on the suggestions to potentially fine-tune the prompts sent to the API in the future.
