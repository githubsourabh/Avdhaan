# Release Checklist for Avdhaan

## Database Changes
- [ ] Update `AppDatabase` version from 1 to 2
- [ ] Add migration for new tables:
  ```java
  // Migration for permission_state_log table
  database.execSQL("CREATE TABLE IF NOT EXISTS `permission_state_log` " +
          "(`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
          "`hasPermission` INTEGER NOT NULL, " +
          "`timestamp` INTEGER NOT NULL, " +
          "`reason` TEXT)");
  ```
- [ ] Remove `fallbackToDestructiveMigration()` from database builder
- [ ] Test migration on existing installations
- [ ] Backup strategy for user data during migration

## Version Updates
- [ ] Update `versionCode` in build.gradle
- [ ] Update `versionName` to reflect new features
- [ ] Update changelog/release notes

## Recent Feature Changes to Document
1. Work Persistence Improvements:
   - Changed to ExistingPeriodicWorkPolicy.REPLACE
   - Optimized interval to 55 minutes
   - Added battery optimization

2. Usage Tracking Features:
   - Added permission handling
   - Added first-time user experience
   - Added usage tracking preferences
   - Added external permission change handling

## Testing Checklist
- [ ] Test app update scenario
- [ ] Test fresh installation
- [ ] Test permission flows:
  - [ ] First-time grant
  - [ ] External permission removal
  - [ ] Re-enabling via notification
  - [ ] Re-enabling via app settings
- [ ] Test data persistence across updates
- [ ] Test battery optimization settings

## Documentation
- [ ] Update user documentation
- [ ] Update API documentation
- [ ] Document migration paths
- [ ] Document known issues/limitations

## Pre-release Testing
- [ ] Run on different Android versions
- [ ] Test on different screen sizes
- [ ] Check memory usage
- [ ] Check battery consumption
- [ ] Verify all strings are in strings.xml
- [ ] Test all language translations

## Security
- [ ] Review permission usage
- [ ] Review data storage security
- [ ] Review external API interactions
- [ ] Check for data leaks

## Store Listing
- [ ] Update screenshots
- [ ] Update feature list
- [ ] Update app description
- [ ] Update privacy policy if needed

*Note: This checklist will be updated as new features are added or requirements change.* 