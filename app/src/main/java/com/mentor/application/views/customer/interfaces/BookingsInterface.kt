package com.mentor.application.views.customer.interfaces

import androidx.fragment.app.Fragment
import com.mentor.application.repository.models.Booking
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent

interface BookingsInterface {
    fun onItemClick(bookingId: String)
    fun onCancelClick(booking: Booking,professionalId:String)
    fun onLoadMore()
}

@Module
@InstallIn(FragmentComponent::class)
object BookingsModule {
    @Provides
    fun provideCallback(context: Fragment) =
        context as BookingsInterface
}