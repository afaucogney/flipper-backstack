package fr.afaucogney.mobile.flipper.internal.model

import android.annotation.SuppressLint
import androidx.fragment.app.Fragment
import com.facebook.flipper.core.FlipperArray
import com.facebook.flipper.core.FlipperObject
import fr.afaucogney.mobile.flipper.BackStackFlipperPlugin
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap


///////////////////////////////////////////////////////////////////////////
// DATA
///////////////////////////////////////////////////////////////////////////

internal val fragmentMap = mutableMapOf<String, HashMap<String, FlipperObject.Builder>>()

///////////////////////////////////////////////////////////////////////////
// TYPE
///////////////////////////////////////////////////////////////////////////

enum class FragmentLifeCycle {
    ON_FRAGMENT_ATTACHED,
    ON_FRAGMENT_CREATED,
    ON_FRAGMENT_VIEW_CREATED,
    ON_FRAGMENT_ACTIVITY_CREATED,
    ON_FRAGMENT_STARTED,
    ON_FRAGMENT_RESUMED,
    ON_FRAGMENT_PAUSED,
    ON_FRAGMENT_STOPPED,
    ON_FRAGMENT_SAVE_INSTANCE_STATE,
    ON_FRAGMENT_VIEW_DESTROYED,
    ON_FRAGMENT_DESTROYED,
    ON_FRAGMENT_DETACHED,
}

///////////////////////////////////////////////////////////////////////////
// DSL
///////////////////////////////////////////////////////////////////////////

internal val Fragment.name: String
    get() = this.javaClass.simpleName

internal val Fragment.fullName: String
    get() = this.toString()

internal val Fragment.fid: String
    get() = this.fullName.split("{")[1].split("}")[0]

internal val Fragment.type: String
    get() = FlipperObjectType.FRAGMENT.key

internal val FragmentLifeCycle.key: String
    get() = this.toString().toLowerCase(Locale.getDefault())

internal fun Fragment.storeFragmentToMapIfNecessary(event: FragmentLifeCycle?) {
    this.toFlipperObjectBuilder()
        .addLifeCycleEvent(event)
        .addNavBackStack(this)
        .addViewModelInfo(this)
        .also {
            if (!fragmentMap.containsKey(this.name)) {
                fragmentMap[this.name] = hashMapOf(this.fid to it)
            } else {
                fragmentMap[this.name]!![this.fid] = it
            }
        }
}

///////////////////////////////////////////////////////////////////////////
// BUILDER
///////////////////////////////////////////////////////////////////////////

internal fun MutableMap<String, HashMap<String, FlipperObject.Builder>>.toFlipperObjectBuilder(): FlipperObject.Builder {
    val result = FlipperObject.Builder()
    this.toSortedMap().forEach { (t, u) ->
        val f = FlipperObject.Builder()
        u.toSortedMap().forEach {
            f.put(it.key, it.value)
        }
        result.put(t, f)
    }
    return result
}

internal fun FlipperObject.Builder.addFragmentsInfo(): FlipperObject.Builder {
    return this.put(FRAGMENTS,  fragmentMap.toFlipperObjectBuilder())
}
