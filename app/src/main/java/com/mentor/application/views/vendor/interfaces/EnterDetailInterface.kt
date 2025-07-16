package com.mentor.application.views.vendor.interfaces

import androidx.fragment.app.Fragment
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent

interface EnterDetailInterface {
    fun onAddCertificateClick()
    fun onAddWorkClick()
    fun onDocumentClick(file:String)
    fun onWorkClick(file:String)
    fun onRemoveProfession(absoluteAdapterPosition: Int)
    fun onDeletePastWork(absoluteAdapterPosition: Int)
    fun onDeleteCertificate(absoluteAdapterPosition: Int)

}

@Module
@InstallIn(FragmentComponent::class)
object EnterDetailModule {
    @Provides
    fun provideCallback(context: Fragment) =
        context as EnterDetailInterface
}