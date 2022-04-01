package com.kotlin_example

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

/** Check to see if the given context has been granted all permissions in the input array */
internal fun hasPermissions(context: Context?, permissions: Array<String>): Boolean {
    if (context != null) {
        for (permission in permissions) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
    }
    return true
}
