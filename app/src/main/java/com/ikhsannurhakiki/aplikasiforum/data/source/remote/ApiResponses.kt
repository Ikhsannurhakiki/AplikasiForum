package com.ikhsannurhakiki.aplikasiforum.data.source.remote

class ApiResponses<T>(val status: StatusResponses, var body: T?, val msg: String?) {
    companion object {
        fun <T> success(body: T): ApiResponses<T> = ApiResponses(StatusResponses.SUCCESS, body, null)

        fun <T> empty(msg: String): ApiResponses<T> = ApiResponses(StatusResponses.EMPTY, null, msg)

        fun <T> error(msg: String): ApiResponses<T> = ApiResponses(StatusResponses.ERROR, null, msg)
    }
}