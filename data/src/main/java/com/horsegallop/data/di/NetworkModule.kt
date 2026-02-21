package com.horsegallop.data.di

import android.content.Context
import com.horsegallop.data.remote.LanguageInterceptor
import com.horsegallop.data.remote.AuthTokenInterceptor
import com.google.firebase.auth.FirebaseAuth
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

  @Provides @Singleton
  fun provideMoshi(): Moshi {
    return Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
  }

  @Provides @Singleton
  fun provideOkHttpClient(@ApplicationContext context: Context): OkHttpClient {
    val logger: HttpLoggingInterceptor = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
    val languageInterceptor = LanguageInterceptor(context)
    val authInterceptor = AuthTokenInterceptor(FirebaseAuth.getInstance())
    return OkHttpClient.Builder()
      .addInterceptor(languageInterceptor)
      .addInterceptor(authInterceptor)
      .addInterceptor(logger)
      .build()
  }

  @Provides @Singleton
  fun provideRetrofit(okHttpClient: OkHttpClient, moshi: Moshi): Retrofit {
    return Retrofit.Builder()
      .baseUrl(com.horsegallop.BuildConfig.BASE_URL)
      .addConverterFactory(MoshiConverterFactory.create(moshi))
      .client(okHttpClient)
      .build()
  }

  @Provides @Singleton
  fun provideApiService(retrofit: Retrofit): com.horsegallop.data.remote.ApiService {
    return retrofit.create(com.horsegallop.data.remote.ApiService::class.java)
  }
}
