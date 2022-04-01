package com.kotlin_example

import android.content.Context
import androidx.databinding.Observable
import androidx.databinding.ObservableField
import com.naurt_kotlin_sdk.Naurt.INSTANCE as Naurt
import com.naurt_kotlin_sdk.*

/** Utility function to clean up ObservableField in Kotlin,
 * See: https://proandroiddev.com/the-ugly-onpropertychangedcallback-63c78c762394
 * For details & Rationale
 * */
fun <T: Observable> T.addOnPropertyChanged(callback: (T) -> Unit) =
    object: Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(observable: Observable?, i: Int) =
            callback(observable as T)
    }.also { addOnPropertyChangedCallback(it) }

/** Utility function to clean up ObservableField in Kotlin,
 * See: https://proandroiddev.com/the-ugly-onpropertychangedcallback-63c78c762394
 * For details & Rationale
 * */
fun <T: Observable> T.removeOnPropertyChanged(callback: (T) -> Unit) =
    object: Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(observable: Observable?, i: Int) =
            callback(observable as T)
    }.also { removeOnPropertyChangedCallback(it) }

typealias NaurtCallback = (ObservableField<NaurtLocation>) -> Unit

// ================================= Application Wide Variables ====================================
internal var hadPaused = false
internal var naurtCallback: NaurtCallback? = null
// ================================= Application Wide Variables ====================================

/** Initialise Naurt with a given context */
internal fun initialiseNaurt(context: Context) {
    // Guarded return, to prevent duplicate Initialisations
    if (Naurt.isInitialised.get()) {
        return
    }

    Naurt.initialise(
        BuildConfig.API_KEY,
        context,
        6
    )
    naurtCallback = createNaurtCallback()
}

/** Create a Naurt callback, used for observing changes in the Naurt Point */
internal fun createNaurtCallback(): NaurtCallback {
    return { it: ObservableField<NaurtLocation> ->
        val location = it.get()

        if (location != null) {
            println("New Naurt Point! [${location.latitude}, ${location.longitude}] at time: ${location.timestamp}")
        }
    }
}

/** Resume the Naurt Engine with a given context*/
internal fun resumeNaurt(context: Context) {
    // If we had previously initialised Naurt, resume the engine
    if (hadPaused) {
        Naurt.resume(context)

        if (Naurt.isRunning.get()) {
            naurtCallback = createNaurtCallback()
            Naurt.naurtPoint.addOnPropertyChanged(naurtCallback!!)
        }

        hadPaused = false
    }
}

/** Pause the Naurt Engine */
internal fun pauseNaurt() {
    if (Naurt.isInitialised.get()) {
        Naurt.pause()
        Naurt.naurtPoint.removeOnPropertyChanged(naurtCallback!!)
        hadPaused = true
    }
}