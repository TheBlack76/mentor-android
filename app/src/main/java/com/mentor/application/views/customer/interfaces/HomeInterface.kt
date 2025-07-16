package com.mentor.application.views.customer.interfaces

import androidx.fragment.app.Fragment
import com.mentor.application.repository.models.Profession
import com.mentor.application.repository.models.SubProfession
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent

interface HomeInterface {
//    fun onItemClick(professionId:String,subProfessionId:String,professionName:String)
    fun onCategoryClick(profession: Profession, subProfession: SubProfession)
    fun onInstantClick()
}

@Module
@InstallIn(FragmentComponent::class)
object HomeModule {
    @Provides
    fun provideCallback(context: Fragment) =
        context as HomeInterface
}