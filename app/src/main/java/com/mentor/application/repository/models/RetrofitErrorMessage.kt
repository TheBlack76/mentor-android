package com.mentor.application.repository.models

import androidx.annotation.StringRes

data class RetrofitErrorMessage(@StringRes val errorResId: Int? = null,
                                val errorMessage: String? = null)