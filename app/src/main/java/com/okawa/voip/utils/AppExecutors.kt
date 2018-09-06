package com.okawa.voip.utils

import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppExecutors(private val diskIO: Executor,
                   private val networkIO: Executor,
                   private val mainThread: Executor) {

    @Inject
    constructor() : this(
            Executors.newSingleThreadExecutor(),
            Executors.newFixedThreadPool(3),
            MainThreadExecutor())

    fun getDiskIO() = diskIO

    fun getNetworkIO() = networkIO

    fun getMainThread() = mainThread

    class MainThreadExecutor : Executor {

        private val mainThreadHandler = Handler(Looper.getMainLooper())

        override fun execute(runnable: Runnable?) {
            mainThreadHandler.post(runnable)
        }

    }

}