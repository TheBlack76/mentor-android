package com.mentor.application.views.customer.interfaces

import androidx.fragment.app.Fragment
import com.mentor.application.repository.models.User
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent

interface WorkersInterface {
    fun onItemClick(user: User)
    fun onLoadMore()
}

@Module
@InstallIn(FragmentComponent::class)
object WorkersModule {
    @Provides
    fun provideCallback(context: Fragment) =
        context as WorkersInterface
}