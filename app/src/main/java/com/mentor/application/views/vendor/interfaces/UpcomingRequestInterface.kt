package com.mentor.application.views.vendor.interfaces

import androidx.fragment.app.Fragment
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent

interface UpcomingRequestInterface {
    fun onItemClick()
    fun onLoadMore()

}

@Module
@InstallIn(FragmentComponent::class)
object UpcomingRequestModule {
    @Provides
    fun provideCallback(context: Fragment) =
        context as UpcomingRequestInterface
}