package org.d1scw0rld.bookbag

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider

inline fun <reified T : Fragment> launchFragmentInHiltContainer(
    fragmentArgs: Bundle? = null,
    themeResId: Int = androidx.appcompat.R.style.Theme_AppCompat_Light_NoActionBar,
    crossinline action: T.() -> Unit = {}
): ActivityScenario<HiltTestActivity> {
    val startActivityIntent = Intent.makeMainActivity(
        ComponentName(
            ApplicationProvider.getApplicationContext(),
            HiltTestActivity::class.java
        )
    ).putExtra(
        "androidx.fragment.app.testing.FragmentScenario.EmptyFragmentActivity.THEME_EXTRAS_BUNDLE_KEY",
        themeResId
    )

    return ActivityScenario.launch<HiltTestActivity>(startActivityIntent).onActivity { activity ->
        val fragment: T = activity.supportFragmentManager.fragmentFactory.instantiate(
            HiltTestActivity::class.java.classLoader!!,
            T::class.java.name
        ) as T
        fragment.arguments = fragmentArgs
        activity.supportFragmentManager
            .beginTransaction()
            .add(android.R.id.content, fragment, "tag")
            .commitNow()

        fragment.action()
    }
}
