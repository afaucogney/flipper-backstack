package fr.afaucogney.mobile.flipper.internal.model

import android.app.Activity
import androidx.fragment.app.FragmentActivity
import com.facebook.flipper.core.FlipperObject

internal fun FlipperObject.Builder.addStackInfo(activity: Activity): FlipperObject.Builder {
    return this.apply {
        if (activity is FragmentActivity) {
            val stack = FlipperObject.Builder()
            activity.supportFragmentManager.fragments.forEachIndexed { index, fragment ->
                stack.put(index.toString(), "${fragment.name} : ${fragment.id}")
            }
            put(STACK, stack)
        }
    }
}
