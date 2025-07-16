package com.mentor.application.repository.networkrequests

import com.mentor.application.repository.networkrequests.WebConstants.ACTION_BASE_URL
import com.mentor.application.utils.ApplicationGlobal
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RestClient {

    @Provides
    fun provideBaseUrl() = ACTION_BASE_URL

    @Singleton
    @Provides
    fun providesHttpLoggingInterceptor() = HttpLoggingInterceptor()
        .apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

    @Singleton
    @Provides
    fun providesOkHttpClient(httpLoggingInterceptor: HttpLoggingInterceptor): OkHttpClient =
        OkHttpClient.Builder().addInterceptor { chain ->
            val requestBuilder = chain.request().newBuilder()
            // Check if the accessToken is not blank before adding the Authorization header
            if (ApplicationGlobal.accessToken.isNotBlank()) {
                requestBuilder.addHeader("Authorization", "Bearer " + ApplicationGlobal.accessToken)
            }
            requestBuilder.addHeader("locale", ApplicationGlobal.deviceLocale)
            requestBuilder.addHeader("timeZone", ApplicationGlobal.timeZone)
            val request = requestBuilder.build()
            chain.proceed(request)
        }
            .readTimeout(10, TimeUnit.SECONDS)
            .connectTimeout(10, TimeUnit.SECONDS)
            .addInterceptor(httpLoggingInterceptor)
            .build()


    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl(ACTION_BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .client(okHttpClient)
        .build()


    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): Apis = retrofit.create(Apis::class.java)


}