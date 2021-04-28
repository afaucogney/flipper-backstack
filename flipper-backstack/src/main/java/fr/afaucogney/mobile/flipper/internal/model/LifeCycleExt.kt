package fr.afaucogney.mobile.flipper.internal.model

import com.facebook.flipper.core.FlipperObject
import java.util.*

internal enum class FlipperObjectType {
    ACTIVITY,
    FRAGMENT,
    VIEW_MODEL,
}

internal val FlipperObjectType.key: String
    get() = this.toString().toLowerCase(Locale.getDefault())


internal fun FlipperObject.Builder.addLifeCycleEvent(event: FragmentLifeCycle?): FlipperObject.Builder {
    return if (event != null)
        this.put(LIFE_CYCLE_EVENT, event.key)
    else this
}

internal fun FlipperObject.Builder.addLifeCycleEvent(event: ActivityLifeCycle?): FlipperObject.Builder {
    return if (event != null)
        this.put(LIFE_CYCLE_EVENT, event.key)
    else this
}