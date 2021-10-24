package fr.afaucogney.mobile.flipper.internal.model

import android.app.Activity
import androidx.fragment.app.Fragment
import com.facebook.flipper.core.FlipperArray
import com.facebook.flipper.core.FlipperObject
import java.text.SimpleDateFormat
import java.util.*

///////////////////////////////////////////////////////////////////////////
// DATA
///////////////////////////////////////////////////////////////////////////

internal val eventList = FlipperArray.Builder()
private val timeStampFormatter = SimpleDateFormat("HH:mm:ss.SSS", Locale.FRANCE)

///////////////////////////////////////////////////////////////////////////
// DSL
///////////////////////////////////////////////////////////////////////////

internal fun Activity.saveEvent(event: ActivityLifeCycle): FlipperArray.Builder {
    return FlipperObject.Builder()
        .put(
            TIMESTAMP,
            timeStampFormatter.format(System.currentTimeMillis())
        )
        .put(TYPE, this.type)
        .put(NAME, this.name)
        .put(FID, this.fid)
        .put(LIFE_CYCLE_EVENT, event)
        .let { eventList.put(it) }
}

internal fun Fragment.saveEvent(event: FragmentLifeCycle): FlipperArray.Builder {
    return FlipperObject.Builder()
        .put(
            TIMESTAMP,
            timeStampFormatter.format(System.currentTimeMillis())
        )
        .put(TYPE, this.type)
        .put(NAME, this.name)
        .put(FID, this.fid)
        .put(LIFE_CYCLE_EVENT, event)
        .let { eventList.put(it) }
}