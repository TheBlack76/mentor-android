package com.mentor.application.views.customer.interfaces

import androidx.fragment.app.Fragment
import com.mentor.application.repository.models.Message
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent

interface MessagesInterface {
    fun onLoadMore()
    fun onImageClick(message: Message)
}

@Module
@InstallIn(FragmentComponent::class)
object MessagesModule {
    @Provides
    fun provideCallback(context: Fragment) =
        context as MessagesInterface
}