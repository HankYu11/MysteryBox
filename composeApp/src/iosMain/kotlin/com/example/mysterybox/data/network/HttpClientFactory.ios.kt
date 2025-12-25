package com.example.mysterybox.data.network

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin

internal actual val engine: HttpClientEngine = Darwin.create {}
