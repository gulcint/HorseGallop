package com.example.data.di

import android.content.Context
import com.example.data.remote.LanguageInterceptor
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.squareup.okhttp3.OkHttpClient
import com.squareup.okhttp3.logging.HttpLoggingInterceptor
import com.squareup.retrofit2.Retrofit
import com.squareup.retrofit2.converter.moshi.MoshiConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import okhttp3.OkHttpClient as Ok3
import okhttp3.logging.HttpLoggingInterceptor as Ok3Logger
import retrofit2.Retrofit as Rt
import retrofit2.converter.moshi.MoshiConverterFactory as RtMoshi

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

  @Provides @Singleton
  fun provideMoshi(): Moshi {
    return Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
  }

  @Provides @Singleton
  fun provideOkHttpClient(@ApplicationContext context: Context): Ok3 {
    val logger: Ok3Logger = Ok3Logger().apply { level = Ok3Logger.Level.BODY }
    val languageInterceptor = LanguageInterceptor(context)
    return Ok3.Builder()
      .addInterceptor(languageInterceptor)
      .addInterceptor(logger)
      .build()
  }

  @Provides @Singleton
  fun provideRetrofit(okHttpClient: Ok3, moshi: Moshi): Rt {
    return Rt.Builder()
      .baseUrl("https://api.example.com/") // TODO change per env
      .addConverterFactory(RtMoshi.create(moshi))
      .client(okHttpClient)
      .build()
  }

  @Provides @Singleton
  fun provideApiService(retrofit: Rt): com.example.data.remote.ApiService {
    return retrofit.create(com.example.data.remote.ApiService::class.java)
  }
}
