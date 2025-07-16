package com.mentor.application.views.customer.interfaces

import androidx.fragment.app.Fragment
import com.mentor.application.repository.models.BookingOffer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent

interface BookingOffersInterface {
    fun onAccept(bookingOffer: BookingOffer)
    fun onReject(bookingOffer: BookingOffer)
}

@Module
@InstallIn(FragmentComponent::class)
object BookingOffersModule {
    @Provides
    fun provideCallback(context: Fragment) =
        context as BookingOffersInterface
}