package com.mentor.application.repository.models

import androidx.fragment.app.Fragment


data class Tab(val tabFragment: Fragment?, val tabName: String = "", val tabIcon: Int = 0)
