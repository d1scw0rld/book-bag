package org.d1scw0rld.bookbag.di

import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultRegistry
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@Module
@InstallIn(ActivityComponent::class)
object ActivityModule {
    @Provides
    fun provideActivityResultRegistry(activity: Activity): ActivityResultRegistry {
        return (activity as ComponentActivity).activityResultRegistry
    }
}
