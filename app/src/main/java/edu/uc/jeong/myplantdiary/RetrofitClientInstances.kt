package edu.uc.jeong.myplantdiary

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClientInstances {

    private var retrofit: Retrofit? = null
    private val BASE_URL = "https://www.plantplaces.com"

    val retrofitInstance: Retrofit?
        get() {
            // has this object been created yet?
           if (retrofit == null) {
               // created it!
               retrofit = retrofit2.Retrofit.Builder()
                   .baseUrl(BASE_URL)
                   .addConverterFactory(GsonConverterFactory.create())
                   .build()
           }
            return retrofit
        }
}