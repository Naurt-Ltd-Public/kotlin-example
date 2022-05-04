package com.kotlin_example.utils

import android.R.attr
import android.content.Context
import android.content.res.ColorStateList
import com.kotlin_example.green
import com.kotlin_example.grey
import com.kotlin_example.hasPermissions
import com.kotlin_example.hasPermissionsSignal
import com.kotlin_example.isSessionSignal
import com.kotlin_example.red
import com.naurt.Sdk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal fun dispatchPermissionSignal(applicationContext: Context, permissions: Array<String>) {
    CoroutineScope(Dispatchers.Main).launch {
        colourPermissionSignal(applicationContext, permissions)

        while (!hasPermissions(applicationContext, permissions)) {
            delay(200)

            hasPermissionsSignal?.let { _ ->
                // Attempt to colour the state signal
                colourPermissionSignal(applicationContext, permissions)
            }
        }
    }
}

internal fun colourSessionSignal() {
    isSessionSignal?.let { it ->
        Sdk.INSTANCE.isCacheClean.thenAccept { cacheClean ->
//        Sdk.isCacheClean().thenAccept { cacheClean ->
//            if (cacheClean && !Sdk.isRunning) {
            if (cacheClean && !Sdk.INSTANCE.isRunning) {
                val colorStateList = ColorStateList(
                    arrayOf(
                        intArrayOf(attr.state_enabled),
                        intArrayOf(attr.state_enabled)
                    ), intArrayOf(
                        green,  // disabled
                        green // enabled
                    )
                )
                it.buttonTintList = colorStateList // set the color tint list
            }
            else if (cacheClean && !Sdk.INSTANCE.isRunning) {
//            else if (cacheClean && !Sdk.isRunning) {
                val colorStateList = ColorStateList(
                    arrayOf(
                        intArrayOf(attr.state_enabled),
                        intArrayOf(attr.state_enabled)
                    ), intArrayOf(
                        red,  // disabled
                        red // enabled
                    )
                )
                it.buttonTintList = colorStateList // set the color tint list
            }
            else {
                val colorStateList = ColorStateList(
                    arrayOf(
                        intArrayOf(attr.state_enabled),
                        intArrayOf(attr.state_enabled)
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

internal fun colourPermissionSignal(context: Context, permissions: Array<String>) {
    hasPermissionsSignal?.let { it ->
        if (hasPermissions(context, permissions)) {
            val colorStateList = ColorStateList(
                arrayOf(
                    intArrayOf(attr.state_enabled),
                    intArrayOf(attr.state_enabled)
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
                    intArrayOf(attr.state_enabled),
                    intArrayOf(attr.state_enabled)
                ), intArrayOf(
                    red,  // disabled
                    red // enabled
                )
            )
            it.buttonTintList = colorStateList // set the color tint list
        }
    }

}