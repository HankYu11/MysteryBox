package com.example.mysterybox.di

import android.app.Application

object KoinApp {
    private lateinit var app: Application

    fun init(app: Application) {
        this.app = app
    }

    fun get(): Application = app
}
