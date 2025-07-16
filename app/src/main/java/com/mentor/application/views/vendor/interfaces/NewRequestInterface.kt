package com.mentor.application.views.vendor.interfaces

import androidx.fragment.app.Fragment
import com.mentor.application.repository.models.BookingRequest
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent

interface NewRequestInterface {
    fun onItemClick(bookingId: String, requestSentTime: String, requestDurationTime: Int)
    fun onAccept(bookingId: BookingRequest)
    fun onReject(bookingId: BookingRequest)
    fun onCustomOffered(bookingId: BookingRequest)
    fun onRefresh()
    fun onLoadMore()

}

@Module
@InstallIn(FragmentComponent::class)
object NewRequestModule {
    @Provides
    fun provideCallback(context: Fragment) =
        context as NewRequestInterface
}