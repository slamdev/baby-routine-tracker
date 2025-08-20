## **Core Feature Summary**

The Newborn Tracker App is a collaborative tool for parents to monitor their baby's daily activities. The core features include:

* **Shared Real-time Data:** Allows multiple users (parents) to log and view the baby's sleep, feeding, and diaper change events in real-time using a shared Firebase backend.  
* **Secure Authentication:** Users will sign in securely using their existing Google accounts on their Android phones.  
* **Intuitive Activity Logging:** Simple UI to quickly log key events like the start/end of sleep, feeding details (type, amount/duration), and diaper status.  
* **At-a-Glance Dashboard:** A main screen that provides an immediate overview of the baby's current status, showing the last logged event for each category and timers for ongoing activities.  
* **Adaptive UI:** A modern interface built with Jetpack Compose that supports both light and dark modes and is fully responsive across different screen orientations (portrait, landscape) and modes (split-screen).  
* **AI-Powered Sleep Guidance:** Integrates with the Google Gemini API to analyze sleep patterns and provide intelligent, personalized suggestions for establishing a healthy sleep routine.  
* **Historical Data Visualization:** Presents historical activity data through simple charts and graphs, allowing parents to easily identify trends and patterns over time.

## **User Stories**

Here is a list of user stories broken down by feature for a developer to follow.

### **Authentication & Setup**

* **As a new parent,** I want to sign up and log in to the app using my Google account so that I don't have to create and remember a new password.  
* **As a parent,** I want to invite my partner to share our baby's profile so that we can both see and log activities from our own phones.  
* **As a parent,** I want the app to securely link our accounts to one shared baby profile so that all data is consolidated in one place.

### **Activity Logging**

* **As a tired parent,** I want to log my baby's sleep with just a start and stop button so that I can quickly record sleep sessions, even in the middle of the night.  
* **As a parent,** I want to log a feeding, specifying whether it was breast milk or formula, and record the duration (for breastfeeding) or the amount (for bottle-feeding).  
* **As a parent,** I want to quickly log a diaper change, noting if it was wet, dirty, or both.  
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
