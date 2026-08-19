package org.d1scw0rld.bookbag.ui

import android.os.Bundle
import android.os.Looper.getMainLooper
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.advanceUntilIdle
import org.d1scw0rld.bookbag.DisplayNameRobolectricRunner
import org.d1scw0rld.bookbag.R
import org.d1scw0rld.bookbag.launchFragmentInHiltContainer
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.jupiter.api.DisplayName
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@HiltAndroidTest
@RunWith(DisplayNameRobolectricRunner::class)
@Config(application = HiltTestApplication::class, sdk = [28])
@OptIn(ExperimentalCoroutinesApi::class)
class BookFragmentIntegrationTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Before
    fun init() {
        hiltRule.inject()
    }

    @DisplayName("Action Edit - FAB Clicked - Navigates To EditBookFragment")
    @Test
    fun actionEdit_fabClicked_navigatesToEditBookFragment() = runTest {
        val mockNavController = mock(NavController::class.java)
        
        val args = Bundle().apply { putLong("bookID", 5L) }

        launchFragmentInHiltContainer<BookFragment>(
            fragmentArgs = args,
            themeResId = androidx.appcompat.R.style.Theme_AppCompat_Light_NoActionBar
        ) {
            Navigation.setViewNavController(requireView(), mockNavController)
        }
        
        Shadows.shadowOf(getMainLooper()).idle()

        onView(withId(R.id.fab_edit_book)).perform(click())

        val expectedAction = BookFragmentDirections.actionBookFragmentToEditBookFragment()
        expectedAction.bookID = 5L
        expectedAction.isCopy = false
        verify(mockNavController).navigate(expectedAction)
    }

    @DisplayName("Menu Copy - Copy Option Clicked - Navigates To EditBookFragment as Copy")
    @Test
    fun menuCopy_copyOptionClicked_navigatesToEditBookFragmentAsCopy() = runTest {
        val mockNavController = mock(NavController::class.java)
        val args = Bundle().apply { putLong("bookID", 10L) }

        launchFragmentInHiltContainer<BookFragment>(
            fragmentArgs = args,
            themeResId = androidx.appcompat.R.style.Theme_AppCompat_Light_NoActionBar
        ) {
            Navigation.setViewNavController(requireView(), mockNavController)
        }
        
        Shadows.shadowOf(getMainLooper()).idle()

        // Click overflow menu
        onView(withContentDescription("More options")).perform(click())
        Shadows.shadowOf(getMainLooper()).idle()

        // Click Copy
        onView(withText("Copy")).perform(click())

        val expectedAction = BookFragmentDirections.actionBookFragmentToEditBookFragment()
        expectedAction.bookID = 10L
        expectedAction.isCopy = true
        verify(mockNavController).navigate(expectedAction)
    }

    @DisplayName("Menu Delete - Delete Option Clicked - Navigates Up")
    @Test
    fun menuDelete_deleteOptionClicked_navigatesUp() = runTest {
        val mockNavController = mock(NavController::class.java)
        val args = Bundle().apply { putLong("bookID", 15L) }

        launchFragmentInHiltContainer<BookFragment>(
            fragmentArgs = args,
            themeResId = androidx.appcompat.R.style.Theme_AppCompat_Light_NoActionBar
        ) {
            Navigation.setViewNavController(requireView(), mockNavController)
        }
        
        Shadows.shadowOf(getMainLooper()).idle()

        // Click overflow menu
        onView(withContentDescription("More options")).perform(click())
        Shadows.shadowOf(getMainLooper()).idle()

        // Click Delete
        onView(withText("Delete")).perform(click())
        Shadows.shadowOf(getMainLooper()).idle()
        advanceUntilIdle() // Wait for coroutines to complete (delete operation is launched in lifecycleScope)

        verify(mockNavController).navigateUp()
    }
}
