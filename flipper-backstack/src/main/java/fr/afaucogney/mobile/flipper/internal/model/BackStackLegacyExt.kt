package fr.afaucogney.mobile.flipper.internal.model

import android.app.Activity
import androidx.fragment.app.FragmentActivity
import com.facebook.flipper.core.FlipperObject

internal fun FlipperObject.Builder.addBackStackInfo(activity: Activity): FlipperObject.Builder {
    return this.apply {
        if (activity is FragmentActivity) {
            val backStack = FlipperObject.Builder()
            for (i in 0 until activity.supportFragmentManager.backStackEntryCount) {
                val entry = activity.supportFragmentManager.getBackStackEntryAt(i)
                backStack.put(entry.id.toString(), entry.name)
            }
            put(BACK_STACK, backStack)
        }
    }
}
