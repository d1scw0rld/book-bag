# Implementation Plan: Introducing Unit Testing to Book Bag

This document outlines the architecture, roadmap, configuration, and sample implementations for introducing robust Unit, ViewModel, Repository, and Database testing into the **Book Bag** application.

---

## 1. Architectural Strategy

We will follow the official Android Testing pyramid and guidelines:
* **70% Unit & ViewModel Tests (Local JVM):** Extremely fast, runs on your computer without an emulator. Covers presentation logic, mappings, state-flows, and business logic.
* **20% Database & Repository Tests (Local/Instrumented):** Tests database migrations, multi-table transactions, cascades, and data orchestrations using in-memory isolated SQLite instances.
* **10% UI Tests (Espresso/Robolectric):** Tests user journeys, UI rendering, and click actions.

```
       /\
      /  \     <-- 10% Instrumented UI / Espresso Tests
     /----\
    /      \   <-- 20% Database & Repository Integration Tests
   /--------\
  /          \ <-- 70% Fast Unit & ViewModel Tests (Local JVM)
 /------------\
```

---

## 2. Test Environment Configuration

Add the following testing libraries to your `app/build.gradle` file:

```groovy
dependencies {
    // Core JUnit 4 testing
    testImplementation 'junit:junit:4.13.2'
    testImplementation 'androidx.test:core-ktx:1.6.1'
    
    // Mockito for clean, decoupled dependency mocking
    testImplementation 'org.mockito:mockito-core:5.11.0'
    testImplementation 'org.mockito.kotlin:mockito-kotlin:5.2.1'
    
    // Coroutine testing tools (TestDispatcher, runTest)
    testImplementation 'org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1'
    androidTestImplementation 'org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1'
    
    // Robolectric to mock the Android framework (Context, Resources, Database) on the local JVM
    testImplementation 'org.robolectric:robolectric:4.14'
    
    // Room Test Helpers
    testImplementation "androidx.room:room-testing:2.7.0-alpha12"
    androidTestImplementation "androidx.room:room-testing:2.7.0-alpha12"
}
```

---

## 3. High-Priority Testing Targets

| Target Component | Test Environment | Key Scenarios to Cover |
| :--- | :--- | :--- |
| **`BookRelationsMapper`** | Local JVM (JUnit 4) | - Title-based sorting (`SRT_TTL`) and lowercase edge-cases.<br>- Author-based sorting (`SRT_AUT`) with missing author fallbacks.<br>- Validating correct read/unread year and status grouping. |
| **`BookListViewModel`** | Local JVM (Coroutines Test) | - Verify state transitions from `UiState.Loading` to `UiState.Success` upon loading books.<br>- Ensure exceptions caught in Flow streams result in `UiState.Error`. |
| **`EditBookViewModel`** | Local JVM (Coroutines Test) | - Verify duplicating books resets the item's primary key to `0L`.<br>- Confirm blank fields are automatically stripped before triggering repository saves. |
| **`BookRepositoryImpl`** | Local JVM (Robolectric / In-Memory DB) | - Test saving books correctly decomposes `Book` DTOs into `BookEntity` and `FieldEntity` mappings.<br>- Ensure `deleteBookAndRelations` successfully executes atomic in-transaction cascade deletes.<br>- Verify Flow-based reactive queries cleanly propagate updates. |
| **`BookDao`** | Local JVM (Robolectric) | - Verify in-memory insertion, updates, upsertions, and primary-key cascades.<br>- Validate complex multi-table transaction deletions (`deleteBookAndFields`). |

---

## 4. Repository Layer Integration Testing Strategy

The repository layer acts as the orchestrator between data sources and domain models (DTOs). Testing **`BookRepositoryImpl`** guarantees that data mappings and transaction boundaries are correct:
* **Target Environment:** Local JVM tests utilizing **Robolectric** paired with a real, isolated **In-Memory Room Database** instance.
* **Test Scenarios to Cover:**
  * **DTO Decomposition:** Verify that calling `saveBookWithFields(book)` correctly splits a unified, multi-field `Book` DTO into database-level `BookEntity`, multiple `FieldEntity` rows, and junction `BookFieldCrossRef` associations.
  * **Atomic Transactions:** Validate that multi-query transaction methods (such as deleting a book and its relation records) run atomically and cleanly purge cross-reference keys without orphaned records.
  * **Reactive Stream Projection:** Verify that `getAllBooksWithFieldsFlow()` cleanly propagates real-time database modifications down to downstream Flow collectors in ViewModels.

---

## 5. Execution Roadmap

1. **Sprint 1: Dependency Setup & Static Mapping Tests**
   * Configure gradle imports.
   * Write and complete 100% coverage tests for `BookRelationsMapper` and format utils.
2. **Sprint 2: ViewModels Verification**
   * Build complete ViewModel tests verifying loading sequences, saving routines, and correct handling of Hilt scopes.
3. **Sprint 3: Database & Cascades Tests**
   * Build exhaustive DAO unit tests covering custom SQL joins, composite primary keys, and transaction flows.
