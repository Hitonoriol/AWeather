package ua.edu.znu.hitonoriol.aweather.util

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Queue this `Call<T>` for execution and pass the response object to the `onSuccess` consumer.
 * Otherwise, pass the exception that has occurred as Throwable to the `onFailure` consumer
 * if the request failed.
 */
fun <T> Call<T>.execute(
    onSuccess: (T?) -> Unit,
    onFailure: (Throwable) -> Unit = { e -> e.printStackTrace() }
) {
    enqueue(object : Callback<T> {
        override fun onFailure(
            call: Call<T>,
            t: Throwable
        ) {
            onFailure(t)
        }

        override fun onResponse(
            call: Call<T>,
            response: Response<T>
        ) {
            onSuccess(response.body())
        }
    })
}