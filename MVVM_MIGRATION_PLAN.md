# Implementation Plan: Migrating Book Bag to MVVM Architecture

This document provides a comprehensive, step-by-step roadmap to migrate the **Book Bag** application from its current direct fragment-to-DAO query pattern to a modern, robust, and clean **Model-View-ViewModel (MVVM)** architecture.

---

## 🏗 Architectural Blueprint

```
            +-------------------------------------------+
            |                  View                     |
            |     (Fragments: List, Detail, Edit)        |
            +---------------------+---------------------+
                                  | Observes StateFlow (UI State)
                                  v
            +-------------------------------------------+
            |               ViewModel                   |
            |   (Holds UI State & handles user events)  |
            +---------------------+---------------------+
                                  | Invokes operations
                                  v
            +-------------------------------------------+
            |              Repository                   |
            |  (Manages data sourcing & business logic) |
            +---------------------+---------------------+
                                  | Queries
                                  v
            +-------------------------------------------+
            |                 Model                     |
            |         (Room entities / Room DAOs)       |
            +-------------------------------------------+
```

---

## 📝 Phased Roadmap

### Phase 1: Setup Dependency Injection (DI) & Base Components
To decouple our layers, we should use **Hilt** (or lightweight manual constructor injection) to supply databases, repositories, and ViewModels. Since Room is already in place, setting up Hilt is the standard approach.

1. **Add Dependencies (`app/build.gradle`)**:
   ```groovy
   // Hilt DI
   implementation "com.google.dagger:hilt-android:2.51"
   kapt "com.google.dagger:hilt-compiler:2.51"
   
   // ViewModel & Lifecycle KTX
   implementation "androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.2"
   implementation "androidx.lifecycle:lifecycle-runtime-ktx:2.8.2"
   ```
2. **Create the Application Class**:
   Create `@HiltAndroidApp class BookBagApplication : Application()` and register it in `AndroidManifest.xml`.
3. **Database Module**:
   Create a `DatabaseModule` to provide `AppDatabase` and `BookDao` as singletons:
   ```kotlin
   @Module
   @InstallIn(SingletonComponent::class)
   object DatabaseModule {
       @Provides
       @Singleton
       fun provideDatabase(@ApplicationContext context: Context): AppDatabase = 
           AppDatabase.getDatabase(context, CoroutineScope(Dispatchers.IO))

       @Provides
       fun provideBookDao(db: AppDatabase): BookDao = db.bookDao()
   }
   ```

---

### Phase 2: Implement the Repository Layer
Abstract data operations away from the UI. The Repository will act as the single source of truth.

1. **Define `BookRepository`**:
   ```kotlin
   interface BookRepository {
       fun getBookWithFields(bookId: Long): Flow<BookWithFields?>
       fun getAllBooksWithFields(): Flow<List<BookWithFields>>
       suspend fun saveBookWithFields(book: Book, properties: List<Property>)
       suspend fun deleteBookAndRelations(bookId: Long)
       suspend fun getFieldsByType(typeId: Int): List<FieldEntity>
   }
   ```
2. **Implement `BookRepositoryImpl`**:
   Inject `BookDao` into the repository and implement data transformation and write logic (such as saving entities and linking cross-references inside transactions).

---

### Phase 3: Create UI States & ViewModels
We will introduce `StateFlow` to represent states of each screen. This ensures the UI is entirely state-driven and preserves data on configuration changes (e.g., screen rotations).

1. **Create generic UI Wrapper**:
   ```kotlin
   sealed interface UiState<out T> {
       object Loading : UiState<Nothing>
       data class Success<out T>(val data: T) : UiState<T>
       data class Error(val exception: Throwable) : UiState<Nothing>
   }
   ```
2. **Create `BookListViewModel`**:
   ```kotlin
   @HiltViewModel
   class BookListViewModel @Inject constructor(
       private val repository: BookRepository
   ) : ViewModel() {
       private val _uiState = MutableStateFlow<UiState<List<BookWithFields>>>(UiState.Loading)
       val uiState: StateFlow<UiState<List<BookWithFields>>> = _uiState.asStateFlow()

       init {
           loadBooks()
       }

       fun loadBooks() {
           viewModelScope.launch {
               _uiState.value = UiState.Loading
               repository.getAllBooksWithFields()
                   .catch { e -> _uiState.value = UiState.Error(e) }
                   .collect { books -> _uiState.value = UiState.Success(books) }
           }
       }
       
       fun deleteBook(bookId: Long) {
           viewModelScope.launch {
               repository.deleteBookAndRelations(bookId)
           }
       }
   }
   ```
3. **Create `BookDetailViewModel`** and **`EditBookViewModel`** similarly to host screen-specific configurations and form-saving methods.

---

### Phase 4: Refactor Fragments (Views)
Fragments become completely "dumb" UI controllers. They only collect `StateFlow` states and forward user actions back to the ViewModel.

1. **Refactor `BookListFragment`**:
   - Inject the `BookListViewModel`.
   - Remove database calls and `lifecycleScope.launch` thread handling.
   - Observe UI State:
     ```kotlin
     viewLifecycleOwner.lifecycleScope.launch {
         viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
             viewModel.uiState.collect { state ->
                 when (state) {
                     is UiState.Loading -> showProgressBar()
                     is UiState.Success -> {
                         hideProgressBar()
                         setupRecyclerView(state.data)
                     }
                     is UiState.Error -> {
                         hideProgressBar()
                         showError(state.exception)
                     }
                 }
             }
         }
     }
     ```
2. **Refactor `BookDetailFragment`** & **`EditBookFragment`** in the exact same manner.

---

## 📈 Incremental Migration Strategy (Zero-Downtime)

To avoid breaking the application, we should migrate **incrementally, screen-by-screen**:

1. **Sprint 1 (Setup)**:
   - Add Gradle dependencies.
   - Configure Dependency Injection (Hilt / modules).
   - Set up the Application class.
2. **Sprint 2 (Data layer & List Screen)**:
   - Create `BookRepository`.
   - Implement `BookListViewModel`.
   - Fully refactor `BookListFragment` to gather state from `BookListViewModel`.
   - Verify importing databases works on the new reactive architecture.
3. **Sprint 3 (Details Screen)**:
   - Implement `BookDetailViewModel`.
   - Refactor `BookDetailFragment` to bind UI dynamically through the ViewModel.
4. **Sprint 4 (Edit / Creation Screen)**:
   - Implement `EditBookViewModel`.
   - Move form-validation, dynamic field creations, and multi-relation updates into the ViewModel.
   - Refactor `EditBookFragment` to bind values cleanly.

---

## 💎 Primary Benefits of this Migration

1. **Testability**: The business logic inside ViewModels and Repositories can be 100% unit-tested with standard JUnit tests (using mock DAOs) without requiring instrumentation tests (emulators).
2. **Robust State Preservation**: No data loss during device configuration changes (e.g., orientation, theme swaps, or screen resizing).
3. **Cleaner Separation of Concerns**: Separation of business flow (ViewModel), network/DB management (Repository), and raw presentation rendering (Fragment).
