## **User Stories: Acceptance Criteria & Technical Tasks**

This document outlines the specific acceptance criter### **Real-time Dashboard**

**User Story:** As a parent, I want to open the app and immediately see when the baby last slept, ate, and had a diaper change on a single dashboard screen.

* **Acceptance Criteria:**  
  * The main dashboard shows the most recent activity for each of the 4 activity types: Sleep, Breast Feeding, Bottle Feeding, and Poop.
  * The dashboard displays cards in a responsive 2x2 grid layout that fits the phone screen without requiring vertical scrolling.
  * Cards automatically adapt their size based on screen dimensions (bigger on larger phones, smaller on compact phones).
  * Each card shows relevant information like last activity time, duration, and notes where applicable.
  * Real-time synchronization ensures the dashboard updates when the partner logs activities on their device.
* **Technical Tasks:**  
  * Split the existing FeedingTrackingCard into separate BreastFeedingCard and BottleFeedingCard components.
  * Update the DashboardScreen layout from vertical Column to responsive 2x2 LazyVerticalGrid.
  * Implement adaptive card sizing using dynamic height calculation based on screen size.
  * Ensure all cards have consistent visual design and information display.
  * Update navigation and state management to handle the separated feeding cards.

**User Story:** As a parent, I want to see a live timer for an ongoing sleep or feeding session so I know how long it has been.

* **Acceptance Criteria:**  
  * When a sleep session is ongoing, the Sleep card displays a live timer showing elapsed time.
  * When a breast feeding session is ongoing, the Breast Feeding card displays a live timer showing elapsed time.
  * Timers update every second and are synchronized across devices.
  * Timer displays in a clear, readable format (e.g., "15:23" for 15 minutes 23 seconds).
* **Technical Tasks:**  
  * Ensure timer functionality works correctly in the separated Sleep and Breast Feeding cards.
  * Maintain real-time timer synchronization across devices for ongoing activities.

**User Story:** As a parent, I want the dashboard to update in real-time when my partner logs a new activity on their device.

* **Acceptance Criteria:**  
  * Changes made by one parent appear on the other parent's dashboard within seconds.
  * All 4 activity cards (Sleep, Breast Feeding, Bottle Feeding, Poop) reflect the latest data in real-time.
  * No manual refresh is required to see partner's activity updates.
* **Technical Tasks:**  
  * Maintain Firebase Firestore real-time listeners for all 4 separated activity types.
  * Ensure proper state management for the separated feeding cards.

**User Story:** As a parent, I want to see 4 separate activity cards (Sleep, Breast Feeding, Bottle Feeding, Poop) displayed in a responsive grid layout that fits my phone screen without needing to scroll.

* **Acceptance Criteria:**  
  * Dashboard displays exactly 4 cards: Sleep (😴), Breast Feeding (🤱), Bottle Feeding (🍼), and Poop (💩). ✅ IMPLEMENTED
  * Cards are arranged in a 2x2 grid layout that automatically fits within the visible screen area. ✅ IMPLEMENTED
  * On larger screens, cards are bigger and more spacious; on smaller screens, cards are more compact but still usable. ✅ IMPLEMENTED
  * No vertical scrolling is required to see all 4 activity cards. ✅ IMPLEMENTED
  * Cards maintain consistent visual hierarchy and information display. ✅ IMPLEMENTED
  * All cards use consistent design patterns for improved user experience. ✅ IMPLEMENTED
  * **Action buttons positioned immediately after card titles** for better accessibility. ✅ IMPLEMENTED
  * **Icon-only buttons** for clean, minimalist design without text clutter. ✅ IMPLEMENTED
  * **Square action buttons** with 1:1 aspect ratio for larger touch targets and easier clicking. ✅ IMPLEMENTED
  * **Proportional icon sizes** (32.dp) that match the larger button size for better visibility. ✅ IMPLEMENTED
* **Technical Tasks:**  
  * Create separate BreastFeedingCard component with breast milk feeding functionality. ✅ COMPLETED
  * Create separate BottleFeedingCard component with bottle feeding functionality. ✅ COMPLETED
  * Update DashboardContent composable to use LazyVerticalGrid with 2 columns. ✅ COMPLETED
  * Implement dynamic card height based on screen size constraints. ✅ COMPLETED
  * Test responsive layout on various Android screen sizes and orientations. ⏳ PENDING
  * Update card spacing and padding for optimal grid layout appearance. ✅ COMPLETED
  * Standardize card design patterns for consistency across all 4 cards. ✅ COMPLETED
  * **Move action buttons to position right after card titles.** ✅ COMPLETED
  * **Remove button text, keeping only descriptive icons for cleaner design.** ✅ COMPLETED
  * **Make action buttons square (1:1 aspect ratio) for better touch targets.** ✅ COMPLETED
  * **Increase icon sizes (32.dp) to be proportional to larger square buttons.** ✅ COMPLETED

**User Story:** As a parent, I want to see how long ago each activity last happened in user-friendly time format so I can quickly assess when my baby last slept, ate, or had a diaper change without having to calculate time differences myself.

* **Acceptance Criteria:**  
  * Each activity card displays time elapsed since the last activity in a user-friendly format (e.g., "Happened 23m ago", "Happened 1h 23m ago", "Happened 1d ago"). ✅ IMPLEMENTED
  * Time ago information is positioned prominently on each card for easy visibility. ✅ IMPLEMENTED
  * Time calculations handle edge cases correctly (just happened, minutes, hours, days, weeks). ✅ IMPLEMENTED
  * For ongoing activities (sleep, breast feeding), time ago refers to when the activity started. ✅ IMPLEMENTED
  * For completed activities, time ago refers to when the activity ended. ✅ IMPLEMENTED
  * For instant activities (bottle feeding, diaper), time ago refers to when the activity was logged. ✅ IMPLEMENTED
* **Technical Tasks:**  
  * Create TimeUtils utility class with formatTimeAgo function for consistent time formatting. ✅ COMPLETED
  * Implement getRelevantTimestamp function to determine the correct timestamp for "time ago" calculation. ✅ COMPLETED
  * Update SleepTrackingCard to display time ago information for the last completed sleep. ✅ COMPLETED
  * Update BreastFeedingCard to display time ago information for the last completed feeding. ✅ COMPLETED
  * Update BottleFeedingCard to display time ago information for the last bottle feeding. ✅ COMPLETED
  * Update DiaperTrackingCard to display time ago information for the last diaper change. ✅ COMPLETED
  * Ensure time ago information updates automatically when new activities are logged. ✅ COMPLETED

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

**User Story:** As a parent, I want to swipe left from the main dashboard to access the activity history instead of having a button, making navigation more intuitive and saving screen space.

* **Acceptance Criteria:**  
  * The main dashboard supports horizontal swipe gestures. ✅ IMPLEMENTED  
  * Swiping left from the dashboard navigates to the activity history screen. ✅ IMPLEMENTED  
  * Swiping right from the history screen navigates back to the dashboard. ✅ IMPLEMENTED  
  * The swipe navigation feels smooth and responsive. ✅ IMPLEMENTED  
  * The "View Activity History" button is removed from the main dashboard. ✅ IMPLEMENTED  
* **Technical Tasks:**  
  * Implement HorizontalPager with Jetpack Compose to create swipeable screens. ✅ COMPLETED  
  * Create a container composable that manages the pager state and screen transitions. ✅ COMPLETED  
  * Remove the history button from the dashboard and integrate history as a pager page. ✅ COMPLETED  
  * Add page indicators or other visual cues to show available screens. ✅ COMPLETED

**User Story:** As a parent, I want to swipe to different screens (History, Data Visualization, AI Sleep Plans) for a modern mobile app experience.

* **Acceptance Criteria:**  
  * The app supports horizontal swipe navigation between main dashboard, history, data visualization, and AI sleep plans. ✅ IMPLEMENTED  
  * Data Visualization and AI Sleep Plans screens show "Coming Soon" placeholder content until implemented. ✅ IMPLEMENTED  
  * Smooth transitions between screens with proper state management. ✅ IMPLEMENTED  
  * Visual indicators show current screen position and available screens. ✅ IMPLEMENTED  
* **Technical Tasks:**  
  * Extend HorizontalPager to support 4 screens: Dashboard, History, Data Visualization, AI Sleep Plans. ✅ COMPLETED  
  * Create placeholder screens for Data Visualization and AI Sleep Plans. ✅ COMPLETED  
  * Implement proper state management to maintain screen state during swipes. ✅ COMPLETED  
  * Add visual indicators (dots or tabs) to show current position. ✅ COMPLETED

### **UI/UX & Responsiveness**

**User Story:** As a user, I want the app to automatically switch between light and dark mode based on my phone's system settings.

* **Acceptance Criteria:**  
  * The app's theme correctly reflects the device's current light or dark mode setting upon launch. ✅ IMPLEMENTED  
  * If the system theme is changed while the app is open, the app's theme updates instantly to match. ✅ IMPLEMENTED  
  * All UI components and text are legible and aesthetically pleasing in both themes. ✅ IMPLEMENTED  
* **Technical Tasks:**  
  * Define complete color palettes for both light and dark themes in the app's theme files. ✅ COMPLETED  
  * Use theme-aware colors (MaterialTheme.colors) for all UI components instead of hardcoded color values. ✅ COMPLETED

**User Story:** As a user, I want the app to display correctly and be fully usable when I rotate my phone to landscape mode.

* **Acceptance Criteria:**  
  * When the device is rotated, the layout re-organizes to make effective use of the wider space. ✅ IMPLEMENTED  
  * No content is clipped, and no UI elements overlap. ✅ IMPLEMENTED  
  * All functionality remains accessible and works as expected. ✅ IMPLEMENTED  
* **Technical Tasks:**  
  * Use adaptive layout composables (BoxWithConstraints, LazyVerticalGrid) that can adjust to different screen dimensions. ✅ COMPLETED  
  * Test layouts in Android Studio's layout validation tool for various device orientations. ✅ COMPLETED

**User Story:** As a user, I want to be able to use the app in split-screen mode alongside another app (like a messaging app or browser) without the layout breaking.

* **Acceptance Criteria:**  
  * The app UI resizes smoothly when entering and adjusting split-screen mode. ✅ IMPLEMENTED
  * The app remains fully functional, readable, and tappable even at its smallest allowed size. ✅ IMPLEMENTED
* **Technical Tasks:**  
  * Ensure the resizeableActivity flag is set to true in the AndroidManifest.xml. ✅ COMPLETED
  * Build the UI with flexible and responsive components that do not rely on fixed dimensions. ✅ COMPLETED

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

**User Story:** As a parent, I want to see my baby's name in the title bar instead of the app name, making the interface more personal and context-aware.

* **Acceptance Criteria:**  
  * When a baby profile is selected, the title bar displays the baby's name instead of "Baby Routine Tracker". ✅ IMPLEMENTED
  * When no baby profile is selected, the title bar falls back to showing "Baby Routine Tracker". ✅ IMPLEMENTED
  * The title bar updates automatically when switching between baby profiles. ✅ IMPLEMENTED
  * The baby's name is displayed with proper formatting and styling. ✅ IMPLEMENTED
* **Technical Tasks:**  
  * Modify the DashboardScreen TopAppBar title to dynamically show baby name when available. ✅ COMPLETED
  * Implement conditional logic to show baby name or app name based on selection state. ✅ COMPLETED
  * Ensure the title updates reactively when baby selection changes. ✅ COMPLETED

**User Story:** As a parent, I want a streamlined dashboard without redundant headers, focusing on activity tracking content for better space efficiency.

* **Acceptance Criteria:**  
  * The "${baby.name}'s Activities" header is removed from the main dashboard content area. ✅ IMPLEMENTED
  * Activity tracking cards are displayed directly without the intermediate header text. ✅ IMPLEMENTED
  * The dashboard content has improved space efficiency with more room for activity cards. ✅ IMPLEMENTED
  * The visual hierarchy remains clear without the redundant header. ✅ IMPLEMENTED
* **Technical Tasks:**  
  * Remove the "${baby.name}'s Activities" Text component from the DashboardScreen content area. ✅ COMPLETED
  * Adjust spacing and layout to maintain visual balance after header removal. ✅ COMPLETED

**User Story:** As a parent, I want to access baby profile management options (create, join, invite) from the profile menu instead of cluttering the main dashboard, creating a cleaner and more organized interface.

* **Acceptance Criteria:**  
  * Baby profile management options are moved from the main dashboard to the profile icon dropdown menu. ✅ IMPLEMENTED
  * The profile dropdown menu includes "Create Baby Profile", "Join Profile", and "Invite Partner" options. ✅ IMPLEMENTED
  * Each menu item has appropriate icons for visual clarity. ✅ IMPLEMENTED
  * The "Invite Partner" option only appears when baby profiles exist. ✅ IMPLEMENTED
  * The Baby Profile Management card is removed from the main dashboard content area. ✅ IMPLEMENTED
  * The main dashboard focuses on activity tracking when no baby is selected. ✅ IMPLEMENTED
* **Technical Tasks:**  
  * Update ProfileIcon function to accept navigation callbacks for baby profile management. ✅ COMPLETED
  * Add baby profile management menu items to the profile dropdown. ✅ COMPLETED
  * Remove the Baby Profile Management card from the main dashboard content. ✅ COMPLETED
  * Update guidance text to direct users to the profile menu for baby profile options. ✅ COMPLETED  
  * Include a sign-out menu item with proper icon and functionality.  
  * Handle dropdown state management (show/hide).  
  * Ensure proper spacing and styling following Material Design guidelines.

### **Data Visualization**

**User Story:** As a parent, I want to view a chart showing the total hours my baby slept each day over the last week so I can spot trends.

* **Acceptance Criteria:**  
  * A "History" or "Charts" screen displays a bar chart for sleep. ✅ IMPLEMENTED
  * The chart's horizontal axis is labeled with the last 7 days. ✅ IMPLEMENTED
  * The vertical axis is labeled with hours. ✅ IMPLEMENTED
  * The height of each bar accurately represents the total sleep duration for that day. ✅ IMPLEMENTED
  * Users can filter data by different date ranges (Last Week, Last 2 Weeks, Last Month). ✅ IMPLEMENTED
  * Chart includes summary statistics showing average sleep hours and total sleep sessions. ✅ IMPLEMENTED
* **Technical Tasks:**  
  * Integrate a third-party charting library compatible with Jetpack Compose (e.g., MPAndroidChart). ✅ COMPLETED
  * Write a query to fetch all sleep events within the last 7 days. ✅ COMPLETED
  * Implement logic to process the data, aggregating total sleep duration per day. ✅ COMPLETED
  * Configure the chart component to display the aggregated data. ✅ COMPLETED
  * Create DataVisualizationViewModel for managing chart data and state. ✅ COMPLETED
  * Implement getActivitiesInDateRange service method for flexible date filtering. ✅ COMPLETED
  * Build responsive chart UI components with proper error handling and loading states. ✅ COMPLETED
  * Add date range selector with Last Week, Last 2 Weeks, and Last Month options. ✅ COMPLETED

**User Story:** As a parent, I want to see a graph of how many feedings my baby has had each day to ensure they are eating consistently.

* **Acceptance Criteria:**  
  * The "History" screen also contains a chart for feedings. ✅ IMPLEMENTED
  * The chart displays the total number of feeding sessions for each of the last 7 days. ✅ IMPLEMENTED
  * The chart is clearly titled and labeled. ✅ IMPLEMENTED
  * Chart distinguishes between breast feedings and bottle feedings with different colors. ✅ IMPLEMENTED
  * Chart includes summary statistics showing total feedings and average per day. ✅ IMPLEMENTED
* **Technical Tasks:**  
  * Write a query to fetch all feeding events within the last 7 days. ✅ COMPLETED
  * Implement logic to process the data by counting the number of events per day. ✅ COMPLETED
  * Configure a second chart component to display this data. ✅ COMPLETED
  * Separate breast feeding and bottle feeding data in the chart visualization. ✅ COMPLETED

**User Story:** As a parent, I want to see a graph of how many diaper changes my baby has had each day to track their bathroom habits and health.

* **Acceptance Criteria:**  
  * The "Charts" screen contains a chart for diaper changes. ✅ IMPLEMENTED
  * The chart displays the total number of diaper changes for each day in the selected time period. ✅ IMPLEMENTED
  * The chart distinguishes between poop diapers and wet diapers with different colors. ✅ IMPLEMENTED
  * The chart includes summary statistics showing total diapers and average per day. ✅ IMPLEMENTED
  * Chart uses theme colors for consistency with the rest of the app. ✅ IMPLEMENTED
* **Technical Tasks:**  
  * Write a query to fetch all diaper events within the selected date range. ✅ COMPLETED
  * Implement logic to process the data by counting poop vs wet diapers per day. ✅ COMPLETED
  * Configure a third chart component to display diaper data. ✅ COMPLETED
  * Create DailyDiaperData model and processing logic in DataVisualizationViewModel. ✅ COMPLETED
  * Add DiaperChart component using MPAndroidChart with theme colors. ✅ COMPLETED
  * Include diaper statistics in the summary card. ✅ COMPLETED

**User Story:** As a parent, I want to be able to filter the historical data by a specific date range (e.g., this week, last month).

* **Acceptance Criteria:**  
  * The "History" screen provides UI controls (e.g., buttons, a calendar icon) to select a date range. ✅ IMPLEMENTED
  * The user can choose from predefined ranges or select a custom start and end date. ✅ IMPLEMENTED (predefined ranges)
  * Changing the date range updates all charts on the screen to reflect the data from the selected period. ✅ IMPLEMENTED
  * Available ranges include Last Week, Last 2 Weeks, and Last Month. ✅ IMPLEMENTED
* **Technical Tasks:**  
  * Implement a date range picker component. ✅ COMPLETED
  * Modify the Firestore queries to be dynamic, accepting start and end dates as parameters. ✅ COMPLETED
  * Update the ViewModel to refetch and re-process data whenever the selected date range changes. ✅ COMPLETED
  * Create DateRangeSelector component with predefined range options. ✅ COMPLETED

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

### **Activity History Screen Enhancements** ✅ **COMPLETED**

**User Story:** As a parent, I want to delete activity entries from the history screen so I can remove mistaken or duplicate entries.

* **Acceptance Criteria:**
  * Each activity item in the history screen has a delete button (trash icon). ✅ IMPLEMENTED
  * Tapping the delete button shows a confirmation dialog to prevent accidental deletion. ✅ IMPLEMENTED
  * Confirmation dialog warns that deletion is permanent and cannot be undone. ✅ IMPLEMENTED
  * After confirmation, the activity is permanently deleted from Firebase and disappears from all devices. ✅ IMPLEMENTED
  * Delete functionality works for all activity types (Sleep, Feeding, Diaper). ✅ IMPLEMENTED
* **Technical Tasks:**
  * Add deleteActivity method to ActivityService with proper authentication and access control. ✅ COMPLETED
  * Add deleteActivity method to ActivityHistoryViewModel with error handling. ✅ COMPLETED
  * Update ActivityHistoryItem component to include delete button alongside edit button. ✅ COMPLETED
  * Implement confirmation dialog with proper styling (red delete button). ✅ COMPLETED
  * Ensure real-time synchronization removes deleted activities from all connected devices. ✅ COMPLETED

**User Story:** As a parent, I want to edit both the date and time of activities in the history screen so I can correct activities that were logged on the wrong date.

* **Acceptance Criteria:**
  * The edit dialog for completed activities includes both date and time pickers. ✅ IMPLEMENTED
  * For instant activities (bottle feeding, diaper), shows single date picker and single time picker. ✅ IMPLEMENTED
  * For duration activities (sleep, breast feeding), shows separate date/time pickers for start and end. ✅ IMPLEMENTED
  * Date pickers use Material 3 DatePickerDialog with proper date selection. ✅ IMPLEMENTED
  * Time pickers preserve the existing time picker functionality but work with selected dates. ✅ IMPLEMENTED
  * Changes are validated (start time cannot be after end time) and saved to Firebase. ✅ IMPLEMENTED
* **Technical Tasks:**
  * Enhance EditActivityDialog to include DatePickerDialog components alongside TimePickerDialog. ✅ COMPLETED
  * Add date picker state management with rememberDatePickerState. ✅ COMPLETED
  * Implement date/time combination logic to preserve time when changing dates. ✅ COMPLETED
  * Update UI layout to show both date and time pickers in responsive row layout. ✅ COMPLETED
  * Ensure validation works across both date and time changes. ✅ COMPLETED

**User Story:** As a parent, I want to filter the activity history by activity type so I can focus on specific activities like just sleep or just feedings.

* **Acceptance Criteria:**
  * History screen includes filter chips at the top: "All", "😴 Sleep", "🍼 Feeding", "💩 Diaper". ✅ IMPLEMENTED
  * Selected filter chip is visually highlighted and affects the displayed list. ✅ IMPLEMENTED
  * Filtering works in real-time without requiring a refresh or reload. ✅ IMPLEMENTED
  * "All" filter shows all activities; individual filters show only that activity type. ✅ IMPLEMENTED
  * Filter state persists while on the history screen but resets when navigating away. ✅ IMPLEMENTED
  * When no activities match the filter, shows helpful message "No activities found for the selected filter". ✅ IMPLEMENTED
* **Technical Tasks:**
  * Add selectedActivityType and filteredActivities to ActivityHistoryUiState. ✅ COMPLETED
  * Implement filterByActivityType method in ActivityHistoryViewModel. ✅ COMPLETED
  * Create ActivityTypeFilter composable with FilterChip components in LazyRow. ✅ COMPLETED
  * Update activity loading to populate both activities and filteredActivities. ✅ COMPLETED
  * Implement filtering logic that works with ActivityType enum values. ✅ COMPLETED
  * Update UI to display filteredActivities instead of all activities. ✅ COMPLETED

### **Baby Profile Management**

**User Story:** As a new parent, I want to create my baby's profile with their name and birthdate so I can personalize the app experience.

* **Acceptance Criteria:**  
  * After authentication, new users are prompted to create a baby profile. ✅ IMPLEMENTED
  * The profile creation form includes fields for baby's name and birthdate. ✅ IMPLEMENTED
  * **ENHANCED**: Optional due date field for premature babies with corrected age calculation. ✅ IMPLEMENTED
  * The baby's age is automatically calculated and displayed based on the birthdate. ✅ IMPLEMENTED
  * **ENHANCED**: Displays both real age and corrected age (from due date) for premature babies. ✅ IMPLEMENTED
  * Profile creation is required before accessing other app features. ✅ IMPLEMENTED
* **Technical Tasks:**  
  * Design and implement baby profile creation UI using Jetpack Compose. ✅ COMPLETED
  * Create Firestore document structure for baby profiles. ✅ COMPLETED
  * Build age calculation logic based on birthdate. ✅ COMPLETED
  * **ENHANCED**: Add due date support and corrected age calculation algorithms. ✅ COMPLETED
  * Add profile validation and error handling. ✅ COMPLETED
  * **ENHANCED**: Create BabyAgeDisplay components for various age display formats. ✅ COMPLETED

**User Story:** As a parent, I want to edit my baby's information if I made a mistake.

* **Acceptance Criteria:**  
  * There is an "Edit Profile" option accessible from the profile icon dropdown menu. ✅ IMPLEMENTED
  * Users can modify any field in the baby profile (name, birthdate, due date). ✅ IMPLEMENTED
  * **ENHANCED**: Edit screen loads baby data reliably and handles navigation correctly. ✅ IMPLEMENTED
  * Changes are saved instantly and synchronized across all devices. ✅ IMPLEMENTED
  * Users receive confirmation when changes are successfully saved. ✅ IMPLEMENTED
  * **ENHANCED**: Age information updates automatically when profile changes are made. ✅ IMPLEMENTED
* **Technical Tasks:**  
  * Create edit profile UI with pre-populated fields. ✅ COMPLETED
  * **ENHANCED**: Fix navigation issues by implementing proper baby data loading in EditBabyProfileScreen. ✅ COMPLETED
  * **ENHANCED**: Add loadBabyForEditing method to InvitationViewModel for reliable data loading. ✅ COMPLETED
  * **ENHANCED**: Fix Firebase document ID mapping to ensure proper baby identification. ✅ COMPLETED
  * Implement real-time validation for profile updates. ✅ COMPLETED
  * Use Firestore transactions to ensure atomic updates. ✅ COMPLETED
  * Add loading states and error handling for profile updates. ✅ COMPLETED

**User Story:** As a parent, I want to see my baby's age calculated automatically based on their birthdate so I can track their development stages.

* **Acceptance Criteria:**  
  * Baby's age is displayed prominently in the profile and dashboard. ✅ IMPLEMENTED
  * Age calculation shows appropriate units (days for newborns, weeks/months as appropriate). ✅ IMPLEMENTED
  * **ENHANCED**: Smart age formatting with "X months old" format for dashboard titles. ✅ IMPLEMENTED
  * Age updates automatically and is always current. ✅ IMPLEMENTED
  * **ENHANCED**: Corrected age calculation for premature babies based on due date. ✅ IMPLEMENTED
  * **ENHANCED**: Clear distinction between real age and corrected age display. ✅ IMPLEMENTED
  * Development milestone hints are shown based on age ranges. ⏳ PENDING
* **Technical Tasks:**  
  * Implement precise age calculation algorithm accounting for leap years. ✅ COMPLETED
  * **ENHANCED**: Create AgeInfo data class and comprehensive age calculation methods. ✅ COMPLETED
  * **ENHANCED**: Implement getRealAge() and getAdjustedAge() methods in Baby model. ✅ COMPLETED
  * Create age formatting logic for different time periods. ✅ COMPLETED
  * **ENHANCED**: Build multiple age display components (BabyAgeDisplay, CompactBabyAgeDisplay, DashboardAgeDisplay). ✅ COMPLETED
  * Design age display component for consistent presentation. ✅ COMPLETED
  * **ENHANCED**: Add age information to dashboard title bars and profile screens. ✅ COMPLETED
  * Add development milestone data and display logic. ⏳ PENDING

**ENHANCEMENT SUMMARY:**
* **Due Date Support**: Full support for premature babies with optional due date field
* **Corrected Age Calculation**: Sophisticated algorithms to calculate both real age and corrected age
* **Enhanced UI**: Multiple age display components for different contexts (dashboard, profile, compact views)
* **Reliable Navigation**: Fixed edit baby functionality with proper data loading and navigation
* **Firebase Integration**: Proper document ID mapping and real-time synchronization
* **Age Display**: Smart age formatting with contextual information display

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
  * Users can choose to receive notifications when partner logs activities. ✅ IMPLEMENTED
  * Notifications specify the activity type and basic details. ✅ IMPLEMENTED
  * Users can customize which activities trigger partner notifications. ✅ IMPLEMENTED
  * Notifications respect quiet hours and user preferences. ✅ IMPLEMENTED
* **Technical Tasks:**  
  * Implement real-time activity notifications using Firebase Cloud Messaging. ✅ COMPLETED
  * Create partner notification preferences and settings UI. ✅ COMPLETED
  * Build activity event listeners and notification triggers. ✅ COMPLETED
  * Add quiet hours scheduling and notification filtering. ✅ COMPLETED
  * Implement notification batching to avoid spam. ✅ COMPLETED

### **Offline Support & Sync** - ❌ **REMOVED**

**User Story:** As a parent with unreliable internet, I want to continue logging activities even when offline so I don't miss recording important events.

* **Status**: ❌ **REMOVED** - Offline support was implemented but subsequently removed due to requirement change
* **Acceptance Criteria:**  
  * ~~All core logging functions work without internet connection~~ ❌ REMOVED
  * ~~Offline activities are stored locally and queued for sync~~ ❌ REMOVED
  * ~~UI clearly indicates offline status with appropriate messaging~~ ❌ REMOVED
  * ~~Recent data remains accessible for viewing when offline~~ ❌ REMOVED
* **Technical Tasks:**  
  * ~~Implement Room database for offline data storage~~ ❌ REMOVED
  * ~~Create sync queue management system for pending operations~~ ❌ REMOVED
  * ~~Build offline detection and UI state management~~ ❌ REMOVED
  * ~~Implement local caching strategy for recent activities~~ ❌ REMOVED
  * ~~Add offline data validation and storage optimization~~ ❌ REMOVED

**User Story:** As a user, I want to see a clear indicator when the app is offline so I know my data isn't syncing yet.

* **Status**: ❌ **REMOVED** - Offline support was implemented but subsequently removed due to requirement change
* **Acceptance Criteria:**  
  * ~~Prominent offline indicator appears when network is unavailable~~ ❌ REMOVED
  * ~~Indicator shows number of pending items waiting to sync~~ ❌ REMOVED
  * ~~Online status restoration is clearly communicated to users~~ ❌ REMOVED
  * ~~Sync progress is visible during online restoration~~ ❌ REMOVED
* **Technical Tasks:**  
  * ~~Implement network connectivity monitoring with ConnectivityManager~~ ❌ REMOVED
  * ~~Create offline status UI components and indicators~~ ❌ REMOVED
  * ~~Build sync progress tracking and display~~ ❌ REMOVED
  * ~~Add retry mechanisms for failed sync operations~~ ❌ REMOVED
  * ~~Implement connection restoration handling and notifications~~ ❌ REMOVED

### **App Settings & Preferences**

**User Story:** As a parent, I want to set default feeding amounts and types to speed up logging routine activities.

* **Acceptance Criteria:**  
  * Default bottle feeding amount can be set at the baby profile level. ✅ IMPLEMENTED
  * Setting is accessible from the baby profile edit screen. ✅ IMPLEMENTED
  * Bottle feeding logging dialog pre-populates with the default value. ✅ IMPLEMENTED
  * Default value can be quickly overridden during individual logging sessions. ✅ IMPLEMENTED
  * Setting is optional and stored per baby profile for multi-baby support. ✅ IMPLEMENTED
* **Technical Tasks:**  
  * Add defaultBottleAmount field to Baby data model. ✅ COMPLETED
  * Update InvitationService.updateBabyProfile to handle default bottle amount. ✅ COMPLETED
  * Update InvitationViewModel to support default bottle amount in UI state. ✅ COMPLETED
  * Add default bottle amount field to EditBabyProfileScreen. ✅ COMPLETED
  * Update BottleFeedingDialog to pre-populate with default amount. ✅ COMPLETED
  * Pass baby object to BottleFeedingCard for access to default amount. ✅ COMPLETED

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

**User Story:** As a parent, I want confirmation when important actions are completed (like successfully logging an activity) so I know it worked.

* **Acceptance Criteria:**  
  * Success messages are displayed prominently when activities are logged successfully. ✅ IMPLEMENTED
  * Success notifications replace the card content temporarily instead of competing with it. ✅ IMPLEMENTED
  * Success messages auto-dismiss after 3 seconds to avoid cluttering the interface. ✅ IMPLEMENTED
  * Success messages are shown for instant activities (bottle feeding, diaper changes) where immediate confirmation is most valuable. ✅ IMPLEMENTED
  * Success messages use clear, encouraging language and visual design. ✅ IMPLEMENTED
* **Technical Tasks:**  
  * Create SuccessContentDisplay component for prominent success message display. ✅ COMPLETED
  * Modify ActivityCard to show success messages as main content instead of additional overlay cards. ✅ COMPLETED
  * Add success message state management to all relevant ViewModels. ✅ COMPLETED
  * Implement auto-dismissal of success messages after 3 seconds. ✅ COMPLETED
  * Ensure success messages are cleared when starting new activity logging operations. ✅ COMPLETED
  * Use consistent success message text across bottle feeding and diaper activities. ✅ COMPLETED

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
