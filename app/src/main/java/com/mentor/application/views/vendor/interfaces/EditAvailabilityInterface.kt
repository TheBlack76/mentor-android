package com.mentor.application.views.vendor.interfaces

import androidx.fragment.app.Fragment
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent

interface EditAvailabilityInterface {
    fun onDeleteSlot(listSize: Int, slotPosition: Int)
    fun onDeleteDate(position: Int)
    fun onGenerateSlot()
    fun onCancelSlot(position: Int)
    fun onShowError(value: String)

}

@Module
@InstallIn(FragmentComponent::class)
object EditAvailabilityModule {
    @Provides
    fun provideCallback(context: Fragment) =
        context as EditAvailabilityInterface
}