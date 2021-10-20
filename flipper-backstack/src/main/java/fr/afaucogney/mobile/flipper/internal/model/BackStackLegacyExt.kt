package fr.afaucogney.mobile.flipper.internal.model

import android.app.Activity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.facebook.flipper.core.FlipperObject
import fr.afaucogney.mobile.flipper.internal.util.getPrivateFunction

///////////////////////////////////////////////////////////////////////////
// BUILDER
///////////////////////////////////////////////////////////////////////////

internal fun FlipperObject.Builder.addBackStackInfo(activity: Activity): FlipperObject.Builder {
    return this.apply {
        if (activity is FragmentActivity) {
            val backStack = FlipperObject.Builder()
            for (i in 0 until activity.supportFragmentManager.backStackEntryCount) {
                val entry = activity.supportFragmentManager.getBackStackEntryAt(i)
                backStack.put(entry.id.toString(), "${entry.name}")
            }
            val addedFragments = FlipperObject.Builder()
            activity.supportFragmentManager.fragments.forEachIndexed { index, fragment ->
                addedFragments.put("$index", "$fragment, ${fragment.tag}")
            }
            val activeFragments = FlipperObject.Builder()
            activity.supportFragmentManager.getActiveFragments()?.forEachIndexed { index, fragment ->
                    activeFragments.put("$index", "$fragment, ${fragment.tag}")
                }
            put(
                BACKSTACK_LEGACY,
                FlipperObject.Builder()
                    .put(
                        FRAGMENT_MANAGER,
                        activity.supportFragmentManager.toString()
//                        Integer.toHexString(System.identityHashCode(activity.supportFragmentManager))
                    )
                    .put(ENTRIES, backStack)
                    .put(ADDED_FRAGMENTS, addedFragments)
                    .put(ACTIVE_FRAGMENTS, activeFragments)
            )
        }
    }
}

private fun FragmentManager.getActiveFragments(): List<Fragment>? {
    return this.getPrivateFunction("getActiveFragments")
}
