package fr.afaucogney.mobile.flipper.internal.model

import android.app.Activity
import com.facebook.flipper.core.FlipperObject
import java.util.*

internal val Activity.name: String
    get() = this.javaClass.simpleName

internal val Activity.fullName: String
    get() = this.toString()

internal val Activity.fid: String
    get() = this.fullName.split("@")[1]

internal val Activity.type: String
    get() = FlipperObjectType.ACTIVITY.key

enum class ActivityLifeCycle {
    ON_ACTIVITY_CREATED,
    ON_ACTIVITY_STARTED,
    ON_ACTIVITY_RESUMED,
    ON_ACTIVITY_PAUSED,
    ON_ACTIVITY_STOPPED,
    ON_ACTIVITY_SAVE_INSTANCE_STATE,
    ON_ACTIVITY_DESTROYED
}

internal val ActivityLifeCycle.key: String
    get() = this.toString().toLowerCase(Locale.getDefault())


