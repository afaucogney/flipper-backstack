package fr.afaucogney.mobile.flipper.internal.model

import android.app.Activity
import com.facebook.flipper.core.FlipperObject
import java.util.*

///////////////////////////////////////////////////////////////////////////
// DATA
///////////////////////////////////////////////////////////////////////////

internal val activityMap = mutableMapOf<String, FlipperObject.Builder>()

///////////////////////////////////////////////////////////////////////////
// TYPE
///////////////////////////////////////////////////////////////////////////

enum class ActivityLifeCycle {
    ON_ACTIVITY_CREATED,
    ON_ACTIVITY_STARTED,
    ON_ACTIVITY_RESUMED,
    ON_ACTIVITY_PAUSED,
    ON_ACTIVITY_STOPPED,
    ON_ACTIVITY_SAVE_INSTANCE_STATE,
    ON_ACTIVITY_DESTROYED
}

///////////////////////////////////////////////////////////////////////////
// DSL
///////////////////////////////////////////////////////////////////////////

private val FlipperObject.name: String
    get() = this.getString(NAME)

internal val Activity.name: String
    get() = this.javaClass.simpleName

internal val Activity.fullName: String
    get() = this.toString()

internal val Activity.fid: String
    get() = this.fullName.split("@")[1]

internal val Activity.type: String
    get() = FlipperObjectType.ACTIVITY.key

internal val ActivityLifeCycle.key: String
    get() = this.toString().lowercase(Locale.getDefault())

internal fun Activity.storeActivityToMapIfNecessary(event: ActivityLifeCycle?) {
    if (!activityMap.containsKey(this.fid)) {
        activityMap[this.fid] = this.toFlipperObjectBuilder()
    }
    activityMap[this.fid]!!
        .addLifeCycleEvent(event)
        .addBackStackInfo(this)
        .addViewModelInfo(this)
        .addFragmentsInfo()
}

///////////////////////////////////////////////////////////////////////////
// BUILDER
///////////////////////////////////////////////////////////////////////////

// This do no support yet multiple activity instance
internal fun Map<String, FlipperObject.Builder>.toFlipperObjectBuilder(): FlipperObject.Builder {
    val result = FlipperObject.Builder()
    this.toSortedMap()
        .forEach { (activityId, activityObjectBuilder) ->
            val activityObject = activityObjectBuilder.build()
            result.put(
                activityObject.name,
                FlipperObject
                    .Builder()
                    .put(
                        activityId,
                        activityObjectBuilder
                    )
            )
        }
    return result
}

internal fun FlipperObject.Builder.addActivitiesInfo(): FlipperObject.Builder {
    return this.put(
        ACTIVITIES,
        activityMap
            .toFlipperObjectBuilder()
    )
}
