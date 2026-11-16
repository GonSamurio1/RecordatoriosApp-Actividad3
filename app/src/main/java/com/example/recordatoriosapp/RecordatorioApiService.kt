package com.example.recordatoriosapp

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface RecordatorioApiService {

    @GET("todos")
    suspend fun obtenerRecordatorios(
        @Query("_limit") limite: Int = 10
    ): Response<List<RecordatorioDto>>

    @POST("todos")
    suspend fun crearRecordatorio(
        @Body nuevo: RecordatorioDto
    ): Response<RecordatorioDto>

    @PUT("todos/{id}")
    suspend fun actualizarRecordatorio(
        @Path("id") id: Int,
        @Body actualizado: RecordatorioDto
    ): Response<RecordatorioDto>

    @DELETE("todos/{id}")
    suspend fun eliminarRecordatorio(
        @Path("id") id: Int
    ): Response<Unit>
}

object RetrofitClient {

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(10, TimeUnit.SECONDS) 
        .readTimeout(10, TimeUnit.SECONDS)   
        .writeTimeout(10, TimeUnit.SECONDS)   
        .build()

    @Volatile
    private var apiServiceInstance: RecordatorioApiService? = null

    fun getApiService(context: android.content.Context): RecordatorioApiService {
        return apiServiceInstance ?: synchronized(this) {
            val baseUrl = context.getString(R.string.base_url) 
            val instance = Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(RecordatorioApiService::class.java)
            apiServiceInstance = instance
            instance
        }
    }
}
