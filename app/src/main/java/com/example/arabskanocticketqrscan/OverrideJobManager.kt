package com.example.arabskanocticketqrscan

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch

class OverrideJobManager {

    @Volatile
    private var job: Job? = null

    fun<R> launchInstead(asyncFun: suspend () -> R, onReqSuccess: suspend (res: R) -> Unit) {
        job?.cancel()
        job = CoroutineScope(Dispatchers.Default).launch {
            try {
                onReqSuccess(asyncFun())
            } catch (e: CancellationException) { }
        }
    }
}