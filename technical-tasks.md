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

**User Story:** As a parent, I want to log a feeding with distinct options for breast milk (using a start/stop timer) and bottle feeding (entering amount directly).

* **Acceptance Criteria:**  
  * The feeding card displays both "Breast Milk" and "Bottle" options directly on the main screen.  
  * For breast milk: tapping "Start Feeding" begins a real-time timer, tapping "Stop Feeding" ends the session and logs the duration automatically.  
  * For bottle feeding: tapping "Log Bottle" opens a simple dialog to enter the amount (ml) and optional notes.  
  * Both feeding types show the last feeding details on the main screen.  
  * Real-time timer updates are visible during ongoing breast milk feeding sessions.  
* **Technical Tasks:**  
  * Create FeedingTrackingCard with separate breast milk and bottle sections.  
  * Implement start/stop timer functionality for breast milk feeding (similar to sleep tracking).  
  * Create simple bottle feeding dialog for amount entry.  
  * Add service methods to support both ongoing breast milk sessions and completed bottle feedings.  
  * Implement real-time synchronization for ongoing breast milk feeding across devices.

**User Story:** As a parent, I want to quickly log a diaper change, noting when poops occur.

* **Acceptance Criteria:**  
  * Tapping a "Log Poop" button allows instant logging of poop occurrence.  
  * Optional notes can be added for additional details (consistency, color, etc.).  
  * The event is logged instantly with timestamp and any notes.  
  * The dashboard shows the last poop occurrence time.  
* **Technical Tasks:**  
  * Design a simple and fast UI for logging poop occurrences (dialog with optional notes).  
  * Implement the function to write the poop event to Firestore.  
  * Create real-time synchronization for diaper activities across devices.  
  * Add diaper tracking card to the main dashboard.

**User Story:** As a parent, I want to be able to add a short, optional note to bottle feeding and diaper activities to remember important details.

* **Acceptance Criteria:**  
  * Bottle feeding logging dialog contains an optional text input field for notes. ✅ IMPLEMENTED
  * Diaper/poop logging dialog contains an optional text input field for notes. ✅ IMPLEMENTED
  * Notes are NOT required or available for sleep tracking and breast milk feeding.  
  * Any text entered in note fields is saved with the corresponding activity log. ✅ IMPLEMENTED
  * The note is visible when viewing the details of past bottle feeding and diaper activities. ✅ IMPLEMENTED
  * Users can edit notes for bottle feeding and diaper activities from the dashboard last activity display. ✅ IMPLEMENTED
  * Users can edit notes for bottle feeding and diaper activities in the activity history screen. ✅ IMPLEMENTED
* **Technical Tasks:**  
  * Add a note field to activity data models in Firestore (already implemented). ✅ COMPLETED
  * Include a TextField composable in bottle feeding and poop logging UI (already implemented). ✅ COMPLETED
  * Display notes in activity history for bottle feeding and diaper activities (already implemented). ✅ COMPLETED
  * Implement service method to update activity notes separately from time updates. ✅ COMPLETED
  * Add notes editing capability to EditActivityDialog component. ✅ COMPLETED
  * Enable notes editing from dashboard last activity displays. ✅ COMPLETED

**User Story:** As a parent, I want to edit recent activities directly from the main dashboard without navigating to a separate screen.

* **Acceptance Criteria:**
  * Last completed activities shown on each dashboard card are clickable for editing. ✅ IMPLEMENTED
  * Clicking opens a comprehensive edit dialog with time and notes editing (where applicable). ✅ IMPLEMENTED
  * Changes are saved and synchronized in real-time across all devices. ✅ IMPLEMENTED
  * Edit functionality is available for all activity types (Sleep, Feeding, Diaper). ✅ IMPLEMENTED
* **Technical Tasks:**
  * Make last activity displays clickable with edit icons. ✅ COMPLETED
  * Integrate EditActivityDialog with dashboard tracking cards. ✅ COMPLETED
  * Add ViewModel methods for updating completed activities from dashboard. ✅ COMPLETED
  * Ensure real-time listeners update dashboard after edits. ✅ COMPLETED

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

**User Story:** As a user, I want to see a clean, modern interface with a profile icon in the top-right corner like modern Android apps, rather than a large welcome card taking up screen space.

* **Acceptance Criteria:**  
  * The main dashboard displays a compact profile icon (avatar or initials) in the top-right corner of the app bar.  
  * The welcome card that previously showed user's name and profile picture is removed from the main content area.  
  * The profile icon shows the user's Google profile picture if available, or their initials as a fallback.  
  * The icon maintains the modern Android app design pattern.  
* **Technical Tasks:**  
  * Remove the WelcomeCard component from the DashboardScreen.  
  * Create a ProfileIcon composable that displays the user's avatar in a compact circular format.  
  * Integrate the ProfileIcon into the TopAppBar actions.  
  * Implement proper image loading with Coil for Google profile pictures.  
  * Add fallback display logic for users without profile pictures (showing initials).

**User Story:** As a user, I want to tap the profile icon to see my account information and access the sign-out option in a dropdown menu.

* **Acceptance Criteria:**  
  * Tapping the profile icon opens a dropdown menu.  
  * The dropdown menu displays the user's display name and email address.  
  * The dropdown menu includes a "Sign Out" option with an appropriate icon.  
  * Tapping outside the dropdown menu dismisses it.  
  * The sign-out functionality works as expected.  
* **Technical Tasks:**  
  * Implement a DropdownMenu that appears when the ProfileIcon is tapped.  
  * Add user information display within the dropdown (name and email).  
  * Include a sign-out menu item with proper icon and functionality.  
  * Handle dropdown state management (show/hide).  
  * Ensure proper spacing and styling following Material Design guidelines.

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

**User Story:** As a parent, I should be able to adjust start and stop time of the activities during and after logging them in case I forgot to press start or stop buttons.

* **Acceptance Criteria:**
  * For an in-progress activity, the user can tap the start time to open a time picker and adjust it. ✅ IMPLEMENTED
  * For a completed activity, the user can find it in a history list and select an "Edit" option. ✅ IMPLEMENTED
  * For a completed activity displayed on the main dashboard, the user can tap on it to edit. ✅ IMPLEMENTED
  * When editing, the user can modify both the start and end times using time pickers. ✅ IMPLEMENTED
  * For activities that support notes (bottle feeding, diaper), users can edit notes in both dashboard and history views. ✅ IMPLEMENTED
  * Saving the changes correctly updates the event's duration and persists the changes across all devices. ✅ IMPLEMENTED
* **Technical Tasks:**
  * Integrate a time picker dialog composable. ✅ COMPLETED
  * Implement UI for an "Edit" flow for past activities. ✅ COMPLETED
  * Use Firestore's updateDoc function to modify existing activity documents. ✅ COMPLETED
  * Create comprehensive EditActivityDialog component supporting both time and notes editing. ✅ COMPLETED
  * Add activity history screen with full editing capabilities. ✅ COMPLETED
  * Implement service methods for updating activity times and notes separately. ✅ COMPLETED

### **Baby Profile Management**

**User Story:** As a new parent, I want to create my baby's profile with their name, and birthdate so I can personalize the app experience.

* **Acceptance Criteria:**  
  * After authentication, new users are prompted to create a baby profile.  
  * The profile creation form includes fields for baby's name and birthdate.  
  * The baby's age is automatically calculated and displayed based on the birthdate.  
  * Profile creation is required before accessing other app features.  
* **Technical Tasks:**  
  * Design and implement baby profile creation UI using Jetpack Compose.  
  * Create Firestore document structure for baby profiles.  
  * Build age calculation logic based on birthdate.  
  * Add profile validation and error handling.

**User Story:** As a parent, I want to edit my baby's information if I made a mistake.

* **Acceptance Criteria:**  
  * There is an "Edit Profile" option accessible from the baby profile or settings screen.  
  * Users can modify any field in the baby profile (name, birthdate).  
  * Changes are saved instantly and synchronized across all devices.  
  * Users receive confirmation when changes are successfully saved.  
* **Technical Tasks:**  
  * Create edit profile UI with pre-populated fields.  
  * Implement real-time validation for profile updates.  
  * Use Firestore transactions to ensure atomic updates.  
  * Add loading states and error handling for profile updates.

**User Story:** As a parent, I want to see my baby's age calculated automatically based on their birthdate so I can track their development stages.

* **Acceptance Criteria:**  
  * Baby's age is displayed prominently in the profile and dashboard.  
  * Age calculation shows appropriate units (days for newborns, weeks/months as appropriate).  
  * Age updates automatically and is always current.  
  * Development milestone hints are shown based on age ranges.  
* **Technical Tasks:**  
  * Implement precise age calculation algorithm accounting for leap years.  
  * Create age formatting logic for different time periods.  
  * Design age display component for consistent presentation.  
  * Add development milestone data and display logic.

### **User Onboarding & Tutorial**

**User Story:** As a new parent, I want step-by-step guidance on setting up my baby's profile and inviting my partner.

* **Acceptance Criteria:**  
  * Profile setup includes helpful tips and explanations for each field.  
  * Partner invitation process is clearly explained with visual guidance.  
  * Users receive feedback on successful completion of each step.  
  * Setup progress is saved if users need to pause and resume later.  
* **Technical Tasks:**  
  * Create progressive setup flow with clear steps and progress indicators.  
  * Implement contextual help for each setup stage.  
  * Add setup state persistence and resume functionality.  
  * Build step validation and guided error correction.

### **Data Management & Export**

**User Story:** As a privacy-focused user, I want to delete all our data and close our account if we decide to stop using the app.

* **Acceptance Criteria:**  
  * Account deletion option is clearly available in settings.  
  * Users are warned about data loss and asked for confirmation.  
  * All user data, baby profiles, and activity logs are permanently deleted.  
  * Users receive final confirmation of successful account closure.  
* **Technical Tasks:**  
  * Implement secure account deletion with multi-step confirmation.  
  * Create data cleanup service for Firestore and Firebase Storage.  
  * Add audit logging for deletion operations.  
  * Ensure GDPR compliance for data deletion requests.

### **Smart Notifications & Reminders**

**User Story:** As a busy parent, I want optional reminders for feeding times based on our baby's usual schedule.

* **Acceptance Criteria:**  
  * App analyzes feeding patterns to suggest optimal reminder times.  
  * Users can enable/disable feeding reminders and customize frequency.  
  * Reminders are intelligent and adapt to changing patterns.  
  * Notifications include snooze and dismiss options.  
* **Technical Tasks:**  
  * Rely on Gemini AI for pattern analysis algorithm for feeding schedules.  
  * Create intelligent notification scheduling with WorkManager.  
  * Build notification customization UI with frequency settings.  
  * Add notification interaction handling (snooze, dismiss, quick log).  
  * Implement adaptive scheduling based on user responses.

**User Story:** As a partner, I want to be notified when my spouse logs an important activity so I'm aware of what's happening.

* **Acceptance Criteria:**  
  * Users can choose to receive notifications when partner logs activities.  
  * Notifications specify the activity type and basic details.  
  * Users can customize which activities trigger partner notifications.  
  * Notifications respect quiet hours and user preferences.  
* **Technical Tasks:**  
  * Implement real-time activity notifications using Firebase Cloud Messaging.  
  * Create partner notification preferences and settings UI.  
  * Build activity event listeners and notification triggers.  
  * Add quiet hours scheduling and notification filtering.  
  * Implement notification batching to avoid spam.

### **Offline Support & Sync**

**User Story:** As a parent with unreliable internet, I want to continue logging activities even when offline so I don't miss recording important events.

* **Acceptance Criteria:**  
  * All core logging functions work without internet connection.  
  * Offline activities are stored locally and queued for sync.  
  * UI clearly indicates offline status with appropriate messaging.  
  * Recent data remains accessible for viewing when offline.  
* **Technical Tasks:**  
  * Implement Room database for offline data storage.  
  * Create sync queue management system for pending operations.  
  * Build offline detection and UI state management.  
  * Implement local caching strategy for recent activities.  
  * Add offline data validation and storage optimization.

**User Story:** As a user, I want to see a clear indicator when the app is offline so I know my data isn't syncing yet.

* **Acceptance Criteria:**  
  * Prominent offline indicator appears when network is unavailable.  
  * Indicator shows number of pending items waiting to sync.  
  * Online status restoration is clearly communicated to users.  
  * Sync progress is visible during online restoration.  
* **Technical Tasks:**  
  * Implement network connectivity monitoring with ConnectivityManager.  
  * Create offline status UI components and indicators.  
  * Build sync progress tracking and display.  
  * Add retry mechanisms for failed sync operations.  
  * Implement connection restoration handling and notifications.

### **App Settings & Preferences**

**User Story:** As a parent, I want to set default feeding amounts and types to speed up logging routine activities.

* **Acceptance Criteria:**  
  * Default feeding preferences can be set in app settings.  
  * Logging screens pre-populate with user's default values.  
  * Multiple default profiles can be created for different feeding types.  
  * Defaults can be quickly overridden during individual logging sessions.  
* **Technical Tasks:**  
  * Create feeding preferences data model and storage.  
  * Implement settings UI for managing default values.  
  * Build default value injection in logging forms.  
  * Add quick override functionality in logging interface.  
  * Create preference validation and reasonable limits.

### **Error Handling & User Feedback**

**User Story:** As a user, I want clear, helpful error messages when something goes wrong so I know how to fix the issue.

* **Acceptance Criteria:**  
  * Error messages are user-friendly and avoid technical jargon.  
  * Messages include specific actions users can take to resolve issues.  
  * Critical errors are distinguished from minor warnings.  
  * Error reporting includes context about what the user was trying to do.  
* **Technical Tasks:**  
  * Implement centralized error handling and messaging system.  
  * Create error message repository with context-aware responses.  
  * Build error classification system (critical, warning, info).  
  * Add automatic error context collection and logging.  
  * Implement user-friendly error display components.

**User Story:** As a parent, I want the app to recover gracefully from errors without losing the activity I was logging.

* **Acceptance Criteria:**  
  * Activity logging data is preserved during error conditions.  
  * Users can resume logging from where they left off after error recovery.  
  * Draft activities are automatically saved and recoverable.  
  * Error recovery options are clearly presented to users.  
* **Technical Tasks:**  
  * Implement automatic draft saving for in-progress activities.  
  * Create error recovery workflows and UI.  
  * Build data persistence for interrupted operations.  
  * Add transaction rollback and data integrity protection.  
  * Implement graceful degradation strategies for various error types.

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
