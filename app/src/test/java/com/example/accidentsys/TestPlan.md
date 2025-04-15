# ACCIDENTSYS Android App Test Plan

## UI and Navigation Tests

- [ ] Launch app and verify main screen loads correctly
- [ ] Open navigation drawer and verify all menu items are present
- [ ] Navigate to System Status screen and verify UI elements
- [ ] Navigate to Accident Reports screen and verify tabs (List and Map) are present
- [ ] Test back navigation from all screens

## System Status Feature Tests

- [ ] Verify system status data is fetched from API
- [ ] Test refresh functionality
- [ ] Verify proper display of active/inactive status
- [ ] Verify sensor information display

## Accident Reports Feature Tests

### List View
- [ ] Verify accident reports are fetched from API
- [ ] Test filter functionality (All, Alcohol Detected, Seatbelt Unfastened, High Severity)
- [ ] Test sort functionality (Newest First, Oldest First, Highest G-Force, By Status)
- [ ] Verify proper display of accident details in list items
- [ ] Test "View Details" button navigation

### Map View
- [ ] Verify map loads correctly with accident markers
- [ ] Test marker click functionality to open accident details
- [ ] Verify "My Location" button centers the map

## Accident Details Tests

- [ ] Verify all accident data is displayed correctly
- [ ] Test map marker on detail screen
- [ ] Verify proper formatting of sensor data
- [ ] Verify safety flags display correctly

## Notification Tests

- [ ] Verify FCM token is generated and logged
- [ ] Test receiving a notification when app is in foreground
- [ ] Test receiving a notification when app is in background
- [ ] Verify notification opens correct screen when tapped

## API Integration Tests

- [ ] Verify authentication with backend API
- [ ] Test error handling for network failures
- [ ] Verify offline behavior
