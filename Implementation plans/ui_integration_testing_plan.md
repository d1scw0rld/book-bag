# Implementation Plan: UI & Integration Testing Expansion

This document presents a structured, step-by-step implementation plan for establishing a robust UI and Integration testing framework across all fragments, preference screens, and dialogs of the Book Bag application.

---

## 1. Objectives & Testing Strategy

*   **Testing Technology:** We will utilize **Robolectric + Espresso** for local JVM UI and integration testing. This approach combines Espresso's powerful view interaction API with Robolectric's rapid JVM execution environment, enabling full UI validation without the speed penalties of emulators or physical devices.
*   **Target Metrics:** Achieve 90%+ branch and interaction coverage for user-facing UI layers.
*   **Verification Points:** Ensure proper layout inflation, programmatic form validation, navigation execution, list binding, dialog interactions, and ViewModel-Fragment integration.

---

## 2. Phase 1: Test Environment Configuration

We will ensure the app module's dependencies are updated to support Espresso on the local JVM:

*   **Gradle Setup:**
    ```groovy
    // local UI testing dependencies
    testImplementation 'androidx.test.espresso:espresso-core:3.6.1'
    testImplementation 'androidx.test.ext:junit:1.2.1'
    testImplementation 'androidx.fragment:fragment-testing:1.8.2'
    ```

---

## 3. Phase 2: Fragment-by-Fragment Integration Test Suites

### Component A: `BookListFragment` (Main List Screen)
*   **Test Cases to Implement:**
    1.  `onViewCreated_initialState_loadsBooksFromViewModel` — Verify that books are fetched from `BookListViewModel` and populated into the `RecyclerView`.
    2.  `onSearchQueryChanged_inputQueryMatchesTitle_filtersListAdapter` — Simulate user input into the search bar using Espresso `typeText` and verify list count decreases to match filtered rows.
    3.  `onBookSwiped_itemSwipeInteraction_triggersDeleteConfirmationFlow` — Simulate a swipe-to-delete action on a list item and verify a confirmation dialog is prompted.
    4.  `onToolbarActionClicked_backupOrRestore_launchesFileSelectorDialog` — Click on database backup or restore in the toolbar menu and assert that `FileSelectorDialog` is inflated.

### Component B: `BookDetailFragment` (Details Display Screen)
*   **Test Cases to Implement:**
    1.  `onViewCreated_validBookIdPassed_rendersDetailFormGrid` — Verify that the detail fields (title, author, publisher, rating, formats) inflate matching the selected book's populated attributes.
    2.  `onRatingBarRendered_hasRatingProperty_populatesCorrectRatingStars` — Assert that the `RatingBar` matches the book's backing rating score and is configured as non-editable.
    3.  `onActionDeleteClicked_actionMenuItemSelected_promptsConfirmationDialogAndDeletes` — Click the delete button in the menu and confirm it initiates database deletion and navigates back to list screen.

### Component C: `EditBookFragment` (Add/Edit Form Screen)
*   **Test Cases to Implement:**
    1.  `onViewCreated_formInflated_populatesInputFieldsWithDefaults` — Verify text boxes, checkboxes, spinners, and date pickers load with correct default placeholders.
    2.  `onAddMultiFieldRowClicked_plusButtonTapped_appendsNewInputRow` — Click on the "Add" button inside the dynamic authors section, asserting that a new row view is inflated and focus links programmatically.
    3.  `onSave Tapped_validFormInput_verifiesViewModelSaveAndNavigatesBack` — Simulate typing title, format, and author, then click "Save" to ensure database write is requested and the fragment closes.
    4.  `onDateInputClicked_dateFieldTapped_opensDatePickerDialog` — Tap on a date selector view and verify `DatePickerDialog` is displayed.

### Component D: `SettingsFragment` (Preferences Screen)
*   **Test Cases to Implement:**
    1.  `onViewCreated_preferencesInflated_showsAppOptions` — Verify that preference switches and selectors are bound and rendered.
    2.  `onBackupPreferenceClicked_tapped_launchesBackupFileSelectorDialog` — Simulate tapping on the Backup database option and verify the `FileSelectorDialog` is triggered with `FileOperation.SAVE`.

---

## 4. Phase 3: Dialogs & Custom Views Integration Tests

### Component E: `FileSelectorDialog` (File Navigation Dialog)
*   **Test Cases to Implement:**
    1.  `onViewCreated_dialogOpened_showsFilesMatchingSelectedFilter` — Assert that opening the dialog with a `.db` filter restricts listed items, showing folders but hiding `.txt` files.
    2.  `onFolderClicked_itemSelected_navigatesToSubDirectoryAndUpdatesToolbarTitle` — Double-tap a folder item, verify the directory changes, and confirm the toolbar title reflects the active subfolder name.
    3.  `onNewFolderActionClicked_validNameEntered_createsDirectoryOnDisk` — Click the "New Folder" menu item, input a folder name in the sub-prompt, and assert the directory is programmatically created.
    4.  `onSaveConfirmed_emptyFileName_showsRequiredWarningPrompt` — Click "Save" with a blank input name field, asserting a validation warning dialog is shown to the user.

---

## 5. Phase 4: Execution & CI Verification

1.  **Test Run Verification:**
    Execute the entire UI suite locally from the workstation command line:
    ```bash
    ./gradlew :app:testDebugUnitTest --tests "org.d1scw0rld.bookbag.ui.*"
    ```
2.  **Lint and Warnings Clean Up:**
    Analyze all newly created UI test suites to verify zero unused imports or warnings.
