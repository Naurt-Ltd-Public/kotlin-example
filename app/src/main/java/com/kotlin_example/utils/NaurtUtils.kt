package com.kotlin_example

import android.R
import android.content.Context
import com.naurt.events.*

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.naurt.Sdk.INSTANCE as Sdk
import android.content.res.ColorStateList
import com.naurt.NaurtTrackingStatus

// ================================= Application Wide Variables ====================================
internal var hadPaused = false
internal var unorderedScope: CoroutineScope = CoroutineScope(Dispatchers.Default)
// ================================= Application Wide Variables ====================================

object NaurtLocationListener: EventListener<NaurtNewLocationEvent> {
    override fun onEvent(p0: NaurtNewLocationEvent) {
        unorderedScope.launch {
            val location = p0.newPoint

            println(
                "New Naurt Point! " +
                        "[${location.latitude}, ${location.longitude}] @ " + "${location.timestamp}, \n" +
                        "altitude: ${location.altitude}, verticalAccuracy: ${location.verticalAccuracy} \n" +
                        "heading: ${location.heading}, headingAccuracy: ${location.headingAccuracy}, \n" +
                        "horizontalAccuracy: ${location.horizontalAccuracy}, horizontalCovariance: ${location.horizontalCovariance}, \n" +
                        "speed: ${location.speed}, speedAccuracy: ${location.speedAccuracy}"
            )
        }
    }
}

object NaurtOnlineListener: EventListener<NaurtIsOnlineEvent> {
    override fun onEvent(p0: NaurtIsOnlineEvent) {
        unorderedScope.launch {
            hasInternetSignal?.let { it ->
                if (p0.isOnline) {
                    val colorStateList = ColorStateList(
                        arrayOf(
                            intArrayOf(-R.attr.state_enabled),
                            intArrayOf(R.attr.state_enabled)
                        ), intArrayOf(
                            green,  // disabled
                            green // enabled
                        )
                    )
                    it.buttonTintList = colorStateList // set the color tint list
                }
                else {
                    val colorStateList = ColorStateList(
                        arrayOf(
                            intArrayOf(-R.attr.state_enabled),
                            intArrayOf(R.attr.state_enabled)
                        ), intArrayOf(
                            red,  // disabled
                            red // enabled
                        )
                    )
                    it.buttonTintList = colorStateList // set the color tint list
                }
            }
        }
    }
}

object NaurtInitialisedListener: EventListener<NaurtIsInitialisedEvent> {
    override fun onEvent(p0: NaurtIsInitialisedEvent) {
        unorderedScope.launch {
            isInitialisedSignal?.let { it ->
                if (p0.isInitialised) {
                    val colorStateList = ColorStateList(
                        arrayOf(
                            intArrayOf(-R.attr.state_enabled),
                            intArrayOf(R.attr.state_enabled)
                        ), intArrayOf(
                            green,  // disabled
                            green // enabled
                        )
                    )
                    it.buttonTintList = colorStateList // set the color tint list
                }
                else {
                    val colorStateList = ColorStateList(
                        arrayOf(
                            intArrayOf(-R.attr.state_enabled),
                            intArrayOf(R.attr.state_enabled)
                        ), intArrayOf(
                            red,  // disabled
                            red // enabled
                        )
                    )
                    it.buttonTintList = colorStateList // set the color tint list
                }
            }
        }
    }
}

object NaurtValidatedListener: EventListener<NaurtIsValidatedEvent> {
    override fun onEvent(p0: NaurtIsValidatedEvent) {
        unorderedScope.launch {
            isValidatedSignal?.let { it ->
                if (p0.isValidated) {
                    val colorStateList = ColorStateList(
                        arrayOf(
                            intArrayOf(-R.attr.state_enabled),
                            intArrayOf(R.attr.state_enabled)
                        ), intArrayOf(
                            green,  // disabled
                            green // enabled
                        )
                    )
                    it.buttonTintList = colorStateList // set the color tint list
                }
                else {
                    val colorStateList = ColorStateList(
                        arrayOf(
                            intArrayOf(-R.attr.state_enabled),
                            intArrayOf(R.attr.state_enabled)
                        ), intArrayOf(
                            red,  // disabled
                            red // enabled
                        )
                    )
                    it.buttonTintList = colorStateList // set the color tint list
                }
            }
        }
    }
}

object NaurtNewJourneyListener: EventListener<NaurtNewJourneyEvent> {
    override fun onEvent(p0: NaurtNewJourneyEvent) {
        unorderedScope.launch {
            println("New NaurtNewJourneyEvent! ${p0.newUuid}")
        }
    }
}

object NaurtRunningListener: EventListener<NaurtIsRunningEvent> {
    override fun onEvent(p0: NaurtIsRunningEvent) {
        unorderedScope.launch {
            isRunningSignal?.let { it ->
                if (p0.isRunning) {
                    val colorStateList = ColorStateList(
                        arrayOf(
                            intArrayOf(-R.attr.state_enabled),
                            intArrayOf(R.attr.state_enabled)
                        ), intArrayOf(
                            green,  // disabled
                            green // enabled
                        )
                    )
                    it.buttonTintList = colorStateList // set the color tint list
                }
                else {
                    val colorStateList = ColorStateList(
                        arrayOf(
                            intArrayOf(-R.attr.state_enabled),
                            intArrayOf(R.attr.state_enabled)
                        ), intArrayOf(
                            grey,  // disabled
                            grey // enabled
                        )
                    )
                    it.buttonTintList = colorStateList // set the color tint list
                }
            }
        }
    }
}

object NaurtHasLocationProviderListener: EventListener<NaurtHasLocationProviderEvent> {
    override fun onEvent(p0: NaurtHasLocationProviderEvent) {
        unorderedScope.launch {
            hasLocationSignal?.let { it ->
                if (p0.hasLocationProvider) {
                    val colorStateList = ColorStateList(
                        arrayOf(
                            intArrayOf(-R.attr.state_enabled),
                            intArrayOf(R.attr.state_enabled)
                        ), intArrayOf(
                            green,  // disabled
                            green // enabled
                        )
                    )
                    it.buttonTintList = colorStateList // set the color tint list
                }
                else {
                    val colorStateList = ColorStateList(
                        arrayOf(
                            intArrayOf(-R.attr.state_enabled),
                            intArrayOf(R.attr.state_enabled)
                        ), intArrayOf(
                            red,  // disabled
                            red // enabled
                        )
                    )
                    it.buttonTintList = colorStateList // set the color tint list
                }
            }
        }
    }
}

internal fun addListeners() {
    Sdk.on("NAURT_NEW_POINT", NaurtLocationListener)
    Sdk.on("NAURT_NEW_JOURNEY", NaurtNewJourneyListener)
    Sdk.on("NAURT_IS_INITIALISED", NaurtInitialisedListener)
    Sdk.on("NAURT_IS_VALIDATED", NaurtValidatedListener)
    Sdk.on("NAURT_IS_RUNNING", NaurtRunningListener)
    Sdk.on("NAURT_IS_ONLINE", NaurtOnlineListener)
    Sdk.on("NAURT_HAS_LOCATION", NaurtHasLocationProviderListener)
}

internal fun initialiseNaurt(context: Context) {
    unorderedScope.launch {
        addListeners()

        if (!Sdk.isInitialised) {
            Sdk.initialise(
                BuildConfig.API_KEY,
                context,
                6
            ).thenAccept { success ->
                println("Initialised SDK! $success")
            }.join()
        }
    }
}

/** Resume the Naurt Engine with a given context*/
internal fun resumeNaurt(context: Context) {
    // If we had previously initialised Naurt, resume the engine
    if (hadPaused) {
        Sdk.resume(context).thenAccept { status ->
            if (
                status == NaurtTrackingStatus.FULL ||
                status == NaurtTrackingStatus.DEGRADED ||
                status == NaurtTrackingStatus.MINIMAL ||
                status == NaurtTrackingStatus.COMPROMISED
            ) {
                Sdk.on("NAURT_NEW_POINT", NaurtLocationListener)
            }

            println("Sdk Resume: $status")
        }

        hadPaused = false
    }
}

/** Pause the Naurt Engine */
internal fun pauseNaurt() {
    if (Sdk.isInitialised) {
        Sdk.pause().thenAccept { status ->
            if (status == NaurtTrackingStatus.PAUSED) {
                Sdk.removeAllListeners()
                hadPaused = true
            }

            println("Sdk Pause: $status")
        }
    }
}
