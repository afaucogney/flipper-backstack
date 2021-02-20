package fr.afaucogney.mobile.flipper.internal.model

import android.app.Activity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import com.facebook.flipper.core.FlipperObject

const val FID = "id"
const val NAME = "name"
const val FULL_NAME = "fullName"
const val TYPE = "type"
const val NEW_DATA = "newData"

const val LIFE_CYCLE_EVENT = "lifeCycle"
const val BACK_STACK = "backStack"
const val TRASH = "trash"

const val ACTIVITIES = "activities"
const val FRAGMENTS = "fragments"
const val VIEW_MODELS = "viewModels"
const val JOBS = "jobs"
const val SERVICES = "services"
const val MEMBERS = "members"

internal fun Activity.toFlipperObjectBuilder(): FlipperObject.Builder {
    return FlipperObject.Builder()
        .put(FID, this.fid)
        .put(NAME, this.name)
        .put(FULL_NAME, this.fullName)
        .put(TYPE, this.type)
}

internal fun Fragment.toFlipperObjectBuilder(): FlipperObject.Builder {
    return FlipperObject.Builder()
        .put(FID, this.fid)
        .put(NAME, this.name)
        .put(FULL_NAME, this.fullName)
        .put(TYPE, this.type)
}

internal fun ViewModel.toFlipperObjectBuilder(): FlipperObject.Builder {
    return FlipperObject.Builder()
        .put(FID, this.fid)
        .put(NAME, this.name)
        .put(FULL_NAME, this.fullName)
        .put(TYPE, this.type)
}
