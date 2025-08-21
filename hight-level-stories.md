## **Core Feature Summary**

The Newborn Tracker App is a collaborative tool for parents to monitor their baby's daily activities. The core features include:

* **Shared Real-time Data:** Allows multiple users (parents) to log and view the baby's sleep, feeding, and diaper change events in real-time using a shared Firebase backend.  
* **Secure Authentication:** Users will sign in securely using their existing Google accounts on their Android phones.  
* **Intuitive Activity Logging:** Simple UI to quickly log key events like the start/end of sleep, feeding details (type, amount/duration), and diaper status.  
* **At-a-Glance Dashboard:** A main screen that provides an immediate overview of the baby's current status, showing the last logged event for each category and timers for ongoing activities.  
* **Adaptive UI:** A modern interface built with Jetpack Compose that supports both light and dark modes and is fully responsive across different screen orientations (portrait, landscape) and modes (split-screen).  
* **AI-Powered Sleep Guidance:** Integrates with the Google Gemini API to analyze sleep patterns and provide intelligent, personalized suggestions for establishing a healthy sleep routine.  
* **Historical Data Visualization:** Presents historical activity data through simple charts and graphs, allowing parents to easily identify trends and patterns over time.  
* **Baby Profile Management:** Create and manage detailed baby profiles with key information, and growth tracking.  
* **Comprehensive Onboarding:** Guided setup process that helps new users understand features and get started quickly.  
* **Data Management:** Delete account and associated data.  
* **Smart Notifications:** Customizable reminders and alerts for feeding times, sleep schedules, and partner activity updates.  
* **Offline Support:** Continue logging activities even without internet connection, with automatic sync when connection is restored.  
* **User Preferences:** Customizable app settings for notifications, default values, and user interface preferences.

## **User Stories**

Here is a list of user stories broken down by feature for a developer to follow.

### **Authentication & Setup**

* **As a new parent,** I want to sign up and log in to the app using my Google account so that I don't have to create and remember a new password.  
* **As a parent,** I want to invite my partner to share our baby's profile so that we can both see and log activities from our own phones.  
* **As a parent,** I want the app to securely link our accounts to one shared baby profile so that all data is consolidated in one place.

### **Activity Logging**

* **As a tired parent,** I want to log my baby's sleep with just a start and stop button so that I can quickly record sleep sessions, even in the middle of the night.  
* **As a parent,** I want to log a feeding with distinct options for breast milk (using a start/stop timer) and bottle feeding (entering amount directly).  
* **As a parent,** I want to quickly log a poop occurrence with optional notes for tracking my baby's bowel movements.  
* **As a parent,** I want to be able to add a short, optional note to any activity (sleep, feeding, or diaper) to remember important details.  
* **As a parent**, I should be able to adjust start and stop time of the activities during and after logging them in case I forgot to press start or stop buttons.

### **Real-time Dashboard**

* **As a parent,** I want to open the app and immediately see when the baby last slept, ate, and had a diaper change on a single dashboard screen.  
* **As a parent,** I want to see a live timer for an ongoing sleep or feeding session so I know how long it has been.  
* **As a parent,** I want the dashboard to update in real-time when my partner logs a new activity on their device.

### **UI/UX & Responsiveness**

* **As a user,** I want the app to automatically switch between light and dark mode based on my phone's system settings.  
* **As a user,** I want the app to display correctly and be fully usable when I rotate my phone to landscape mode.  
* **As a user,** I want to be able to use the app in split-screen mode alongside another app (like a messaging app or browser) without the layout breaking.

### **AI Sleep Routine Builder**

* **As a new parent,** I want to get suggestions for a sleep routine based on my baby's actual sleep data and age so I can get help establishing a healthy schedule.  
* **As a parent,** I want to see the AI-suggested nap times and bedtime clearly presented within the app.  
* **As a parent,** I want to build the sleep plan using AI suggestions and my own customizations.

### **Data Visualization**

* **As a parent,** I want to view a chart showing the total hours my baby slept each day over the last week so I can spot trends.  
* **As a parent,** I want to see a graph of how many feedings my baby has had each day to ensure they are eating consistently.  
* **As a parent,** I want to be able to filter the historical data by a specific date range (e.g., this week, last month) to analyze different periods.

### **Baby Profile Management**

* **As a new parent,** I want to create my baby's profile with their name, and birthdate so I can personalize the app experience.  
* **As a parent,** I want to edit my baby's information if I made a mistake.  
* **As a parent,** I want to see my baby's age calculated automatically based on their birthdate so I can track their development stages.  

### **User Onboarding & Tutorial**

* **As a new user,** I want a guided tour of the main features so I can learn how to use the app effectively.  
* **As a new parent,** I want step-by-step guidance on setting up my baby's profile and inviting my partner.  

### **Data Management & Export**

* **As a privacy-focused user,** I want to delete all our data and close our account if we decide to stop using the app.  

### **Smart Notifications & Reminders**

* **As a busy parent,** I want optional reminders for feeding times based on our baby's usual schedule.  
* **As a partner,** I want to be notified when my spouse logs an important activity so I'm aware of what's happening.  
* **As a parent,** I want to be reminded when it might be time for a nap based on our baby's sleep patterns.  
* **As a user,** I want to customize which notifications I receive and how often so I'm not overwhelmed.  
* **As a parent,** I want to temporarily disable notifications during certain hours (like when the baby is sleeping) so we're not disturbed.  
* **As a parent,** I want notifications about AI-generated sleep routine suggestions when there's new advice available.

### **Offline Support & Sync**

* **As a parent with unreliable internet,** I want to continue logging activities even when offline so I don't miss recording important events.  
* **As a user,** I want to see a clear indicator when the app is offline so I know my data isn't syncing yet.  
* **As a parent,** I want my offline activities to automatically sync when internet connection is restored.  
* **As a user,** I want to be notified if there are sync conflicts and have a way to resolve them.  
* **As a parent on-the-go,** I want to view recent activities even without internet so I can still reference our baby's schedule.

### **App Settings & Preferences**

* **As a parent,** I want to set default feeding amounts and types to speed up logging routine activities.  
* **As a user,** I want to control notification settings for different types of alerts and reminders.  

### **Error Handling & User Feedback**

* **As a user,** I want clear, helpful error messages when something goes wrong so I know how to fix the issue.  
* **As a parent,** I want the app to recover gracefully from errors without losing the activity I was logging.  
* **As a parent,** I want confirmation when important actions are completed (like successfully logging an activity) so I know it worked.  
