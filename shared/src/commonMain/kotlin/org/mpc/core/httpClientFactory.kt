package org.mpc.core

import io.ktor.client.HttpClient
import org.mpc.data.ApiConfig

fun createHttpClient(config: ApiConfig): HttpClient =
    HttpClient() {

    }