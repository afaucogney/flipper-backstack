package fr.afaucogney.mobile.flipper.internal.model

import android.annotation.SuppressLint
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.facebook.flipper.core.FlipperObject

@SuppressLint("RestrictedApi")
internal fun FlipperObject.Builder.addNavBackStack(fragment: Fragment): FlipperObject.Builder {
    return this
        .let {
            if (fragment.name == "NavHostFragment") {
                try {
                    val result = FlipperObject.Builder()
                    fragment.findNavController()
                        .backStack
                        .forEachIndexed { index, navBackStackEntry ->
                            result.put(
                                index.toString(),
                                navBackStackEntry.destination.displayName
                            )
                        }
                    it.put(BACK_STACK, result)
                } catch (e: IllegalStateException) {
                    it
                }
            } else {
                it
            }
        }
}