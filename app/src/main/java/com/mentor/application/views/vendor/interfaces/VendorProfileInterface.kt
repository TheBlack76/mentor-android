package com.mentor.application.views.vendor.interfaces

import androidx.fragment.app.Fragment
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent

interface VendorProfileInterface {
   fun onCertificateClick(url: String)
   fun onPastWorkClick(url: String)

}

@Module
@InstallIn(FragmentComponent::class)
object VendorProfileModule {
    @Provides
    fun provideCallback(context: Fragment) =
        context as VendorProfileInterface
}