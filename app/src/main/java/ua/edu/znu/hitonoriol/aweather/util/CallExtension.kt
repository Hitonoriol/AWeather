package ua.edu.znu.hitonoriol.aweather.util

import androidx.core.util.Consumer
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

fun <T> Call<T>.execute(onSuccess: Consumer<T>,
                        onFailure: Consumer<Throwable> = Consumer { e -> e.printStackTrace() }) {
    enqueue(object : Callback<T> {
        override fun onFailure(
            call: Call<T>,
            t: Throwable
        ) {
            onFailure.accept(t)
        }

        override fun onResponse(
            call: Call<T>,
            response: Response<T>
        ) {
            onSuccess.accept(response.body())
        }
    })
}