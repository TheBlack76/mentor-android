package com.mentor.application.views.customer.interfaces

import androidx.fragment.app.Fragment
import com.mentor.application.repository.models.NotificationListing
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent

interface NotificationInterface {
    fun onItemClick(notificationListing: NotificationListing)
    fun onLoadMore()
}

@Module
@InstallIn(FragmentComponent::class)
object NotificationModule {
    @Provides
    fun provideCallback(context: Fragment) =
        context as NotificationInterface
}