## **User Stories: Acceptance Criteria & Technical Tasks**

This document outlines the specific acceptance criteria and high-level technical tasks for each user story defined for the Newborn Tracker App.

### **Authentication & Setup**

**User Story:** As a new parent, I want to sign up and log in to the app using my Google account so that I don't have to create and remember a new password.

* **Acceptance Criteria:**  
  * When the app is opened for the first time, a "Sign in with Google" button is clearly visible.  
  * Tapping the button prompts the standard Android Google account selection dialog.  
  * After selecting an account, the user is successfully authenticated and navigated to the app's main dashboard.  
  * On subsequent app launches, the user remains logged in automatically.  
* **Technical Tasks:**  
  * Integrate the Firebase Authentication SDK for Android.  
  * Configure Google Sign-In as an authentication provider in the Firebase console.  
  * Implement the Google Sign-In UI flow using Jetpack Compose.  
  * Manage user session persistence to handle automatic sign-in.

**User Story:** As a parent, I want to invite my partner to share our baby's profile so that we can both see and log activities from our own phones.

* **Acceptance Criteria:**  
  * There is an "Invite Partner" option within the app's settings or profile screen.  
  * Tapping this option generates a unique code.  
  * The user can share this code using the native Android share sheet.  
  * When the recipient opens the link/code, they are guided through the process of joining the shared profile.  
* **Technical Tasks:**  
  * Design a Firestore data structure that allows a single baby profile to be associated with multiple user IDs.  
  * Implement a mechanism to generate and validate unique invitation tokens.  
  * Build the UI for generating, sharing, and redeeming invitations.

**User Story:** As a parent, I want the app to securely link our accounts to one shared baby profile so that all data is consolidated in one place.

* **Acceptance Criteria:**  
  * Once an invitation is accepted, both users are linked to the same baby profile.  
  * An activity logged by one parent appears on the other parent's device in near real-time (within seconds).  
  * Data is not accessible to any user who has not been explicitly invited.  
* **Technical Tasks:**  
  * Implement strict Firestore Security Rules to ensure only authorized users can read or write to a specific baby profile's data.  
  * Use Firestore's real-time listeners (onSnapshot) to keep all connected clients synchronized.

### **Activity Logging**

**User Story:** As a tired parent, I want to log my baby's sleep with just a start and stop button so that I can quickly record sleep sessions, even in the middle of the night.

* **Acceptance Criteria:**  
  * The main dashboard displays a "Start Sleep" button.  
  * Tapping "Start Sleep" changes its state to an active "Stop Sleep" button and starts a visible timer.  
  * Tapping "Stop Sleep" saves the session with the correct start and end times and updates the dashboard to reflect this last sleep event.  
* **Technical Tasks:**  
  * Design and implement the sleep tracking card UI on the dashboard.  
  * Use a ViewModel to manage the state of the ongoing sleep timer.  
  * Implement the logic to create and save a new sleep document in Firestore upon completion.

**User Story:** As a parent, I want to log a feeding, specifying whether it was breast milk or formula, and record the duration (for breastfeeding) or the amount (for bottle-feeding).

* **Acceptance Criteria:**  
  * Tapping a "Log Feeding" button opens a form or dialog.  
  * The user can select the feeding type (e.g., "Breast Milk," "Formula").  
  * Conditional UI appears: a timer for "Breast Milk" or a numeric input for "Formula" (in ml/oz).  
  * The saved event accurately reflects all the selected details.  
* **Technical Tasks:**  
  * Create a reusable composable for the feeding log form.  
  * Implement state logic to show the correct input field based on the selected feeding type.  
  * Define a Firestore data model for feeding events that can accommodate both duration and amount.

**User Story:** As a parent, I want to quickly log a diaper change, noting if it was wet, dirty, or both.

* **Acceptance Criteria:**  
  * Tapping a "Log Diaper" button presents clear options: "Wet," "Dirty," and "Both."  
  * The user can select an option with a single tap.  
  * The event is logged instantly with the correct type and timestamp.  
* **Technical Tasks:**  
  * Design a simple and fast UI for logging diaper changes (e.g., a bottom sheet with buttons).  
  * Implement the function to write the diaper event to Firestore.

**User Story:** As a parent, I want to be able to add a short, optional note to any activity (sleep, feeding, or diaper) to remember important details.

* **Acceptance Criteria:**  
  * Each activity logging screen/dialog contains an optional text input field for notes.  
  * Any text entered in this field is saved with the corresponding activity log.  
  * The note is visible when viewing the details of that past activity.  
* **Technical Tasks:**  
  * Add a note field to all activity data models in Firestore.  
  * Include a TextField composable in each logging UI.

**User Story:** As a parent, I should be able to adjust start and stop time of the activities during and after logging them in case I forgot to press start or stop buttons.

* **Acceptance Criteria:**  
  * For an in-progress activity, the user can tap the start time to open a time picker and adjust it.  
  * For a completed activity, the user can find it in a history list and select an "Edit" option.  
  * When editing, the user can modify both the start and end times using time pickers.  
  * Saving the changes correctly updates the event's duration and persists the changes across all devices.  
* **Technical Tasks:**  
  * Integrate a time picker dialog composable.  
  * Implement UI for an "Edit" flow for past activities.  
  * Use Firestore's updateDoc function to modify existing activity documents.

### **Real-time Dashboard**

**User Story:** As a parent, I want to open the app and immediately see when the baby last slept, ate, and had a diaper change on a single dashboard screen.

* **Acceptance Criteria:**  
  * The dashboard is the first screen after login.  
  * It has clear sections for Sleep, Feeding, and Diapers.  
  * Each section displays a human-readable summary of the last event (e.g., "Last fed at 10:30 AM \- 120ml").  
* **Technical Tasks:**  
  * Write Firestore queries to fetch the single most recent document for each activity type.  
  * Design the main dashboard layout in Jetpack Compose.  
  * Create utility functions to format timestamps and activity data for display.

**User Story:** As a parent, I want to see a live timer for an ongoing sleep or feeding session so I know how long it has been.

* **Acceptance Criteria:**  
  * When an activity is started, its corresponding card on the dashboard displays a timer that counts up in real-time (HH:MM:SS).  
  * The timer is accurate and updates every second.  
* **Technical Tasks:**  
  * Use a coroutine-based ticker (flow) in the ViewModel to update the UI every second.  
  * Calculate the elapsed time from the saved start timestamp.

**User Story:** As a parent, I want the dashboard to update in real-time when my partner logs a new activity on their device.

* **Acceptance Criteria:**  
  * If User A logs a diaper change, the dashboard on User B's phone updates with the new information within seconds, without requiring a manual refresh.  
  * If User A starts a sleep timer, the timer also appears and runs on User B's phone.  
* **Technical Tasks:**  
  * Ensure real-time onSnapshot listeners are correctly attached and managed within the lifecycle of the dashboard screen.  
  * The ViewModel must properly handle incoming data from listeners and update the UI state.

### **UI/UX & Responsiveness**

**User Story:** As a user, I want the app to automatically switch between light and dark mode based on my phone's system settings.

* **Acceptance Criteria:**  
  * The app's theme correctly reflects the device's current light or dark mode setting upon launch.  
  * If the system theme is changed while the app is open, the app's theme updates instantly to match.  
  * All UI components and text are legible and aesthetically pleasing in both themes.  
* **Technical Tasks:**  
  * Define complete color palettes for both light and dark themes in the app's theme files.  
  * Use theme-aware colors (MaterialTheme.colors) for all UI components instead of hardcoded color values.

**User Story:** As a user, I want the app to display correctly and be fully usable when I rotate my phone to landscape mode.

* **Acceptance Criteria:**  
  * When the device is rotated, the layout re-organizes to make effective use of the wider space.  
  * No content is clipped, and no UI elements overlap.  
  * All functionality remains accessible and works as expected.  
* **Technical Tasks:**  
  * Use adaptive layout composables (BoxWithConstraints, LazyVerticalGrid) that can adjust to different screen dimensions.  
  * Test layouts in Android Studio's layout validation tool for various device orientations.

**User Story:** As a user, I want to be able to use the app in split-screen mode alongside another app (like a messaging app or browser) without the layout breaking.

* **Acceptance Criteria:**  
  * The app UI resizes smoothly when entering and adjusting split-screen mode.  
  * The app remains fully functional, readable, and tappable even at its smallest allowed size.  
* **Technical Tasks:**  
  * Ensure the resizeableActivity flag is set to true in the AndroidManifest.xml.  
  * Build the UI with flexible and responsive components that do not rely on fixed dimensions.

### **AI Sleep Routine Builder**

**User Story:** As a new parent, I want to get suggestions for a sleep routine based on my baby's actual sleep data and age so I can get help establishing a healthy schedule.

* **Acceptance Criteria:**  
  * A button or section labeled "Get Sleep Plan" is available.  
  * Tapping it triggers a call to the AI service, sending recent sleep data and the baby's age.  
  * A loading indicator is displayed during the process.  
  * The app displays the AI-generated suggestions upon a successful response.  
* **Technical Tasks:**  
  * Set up and configure the Google Gemini API.  
  * Implement a repository or service class to handle the network request to the API.  
  * Create data models for serializing the request and deserializing the response.  
  * Implement UI states for loading, success, and error.

**User Story:** As a parent, I want to see the AI-suggested nap times and bedtime clearly presented within the app.

* **Acceptance Criteria:**  
  * The suggestions are formatted in a simple, easy-to-scan list or timeline view.  
  * Nap times are clearly distinguished from the final bedtime.  
  * A disclaimer is visible, indicating the suggestions are AI-generated and not medical advice.  
* **Technical Tasks:**  
  * Design and build a Jetpack Compose UI to display the structured sleep plan.  
  * Format the data from the API response into a user-friendly presentation.

**User Story:** As a parent, I want to build the sleep plan using AI suggestions and my own customizations.

* **Acceptance Criteria:**  
  * After the AI generates a plan, the user can edit the proposed times.  
  * The user can add new sleep windows or remove suggested ones.  
  * There is a "Save" button to persist the customized plan.  
  * The saved plan can be viewed later from a dedicated screen.  
* **Technical Tasks:**  
  * Create a new Firestore collection to store user-customized sleep plans.  
  * Build an editable UI for the sleep plan that allows for modification of the AI-generated data.  
  * Implement the logic to save, fetch, and display the customized plan.

### **Data Visualization**

**User Story:** As a parent, I want to view a chart showing the total hours my baby slept each day over the last week so I can spot trends.

* **Acceptance Criteria:**  
  * A "History" or "Charts" screen displays a bar chart for sleep.  
  * The chart's horizontal axis is labeled with the last 7 days.  
  * The vertical axis is labeled with hours.  
  * The height of each bar accurately represents the total sleep duration for that day.  
* **Technical Tasks:**  
  * Integrate a third-party charting library compatible with Jetpack Compose (e.g., MPAndroidChart).  
  * Write a query to fetch all sleep events within the last 7 days.  
  * Implement logic to process the data, aggregating total sleep duration per day.  
  * Configure the chart component to display the aggregated data.

**User Story:** As a parent, I want to see a graph of how many feedings my baby has had each day to ensure they are eating consistently.

* **Acceptance Criteria:**  
  * The "History" screen also contains a chart for feedings.  
  * The chart displays the total number of feeding sessions for each of the last 7 days.  
  * The chart is clearly titled and labeled.  
* **Technical Tasks:**  
  * Write a query to fetch all feeding events within the last 7 days.  
  * Implement logic to process the data by counting the number of events per day.  
  * Configure a second chart component to display this data.

**User Story:** As a parent, I want to be able to filter the historical data by a specific date range (e.g., this week, last month).

* **Acceptance Criteria:**  
  * The "History" screen provides UI controls (e.g., buttons, a calendar icon) to select a date range.  
  * The user can choose from predefined ranges or select a custom start and end date.  
  * Changing the date range updates all charts on the screen to reflect the data from the selected period.  
* **Technical Tasks:**  
  * Implement a date range picker component.  
  * Modify the Firestore queries to be dynamic, accepting start and end dates as parameters.  
  * Update the ViewModel to refetch and re-process data whenever the selected date range changes.
