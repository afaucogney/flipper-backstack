package fr.afaucogney.mobile.flipper.internal.model

import androidx.fragment.app.Fragment
import com.facebook.flipper.core.FlipperArray
import com.facebook.flipper.core.FlipperObject

///////////////////////////////////////////////////////////////////////////
// DATA
///////////////////////////////////////////////////////////////////////////

internal val trashMap = FlipperArray.Builder()

///////////////////////////////////////////////////////////////////////////
// DSL
///////////////////////////////////////////////////////////////////////////]

internal fun FlipperObject.Builder.addTrashInfo(): FlipperObject.Builder {
    return this.put(TRASH, trashMap)
}

internal fun Fragment.moveToTrash() {
    trashMap.put(fragmentMap[this.name]!![this.fid])
    fragmentMap[this.name]!!.remove(this.fid)
}