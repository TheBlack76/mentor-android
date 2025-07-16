package com.mentor.application.views.customer.interfaces

import androidx.fragment.app.Fragment
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent

interface AssetsInterface {
    fun onItemClick(title: String?)
    fun onLoadMore()
}

@Module
@InstallIn(FragmentComponent::class)
object AssetsModule {
    @Provides
    fun provideCallback(context: Fragment) =
        context as AssetsInterface
}