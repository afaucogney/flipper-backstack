package fr.afaucogney.mobile.flipper.internal.model

import android.app.Activity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import com.facebook.flipper.core.FlipperObject

const val BACK_STACK = "backStack"
const val ENTRIES = "entries"
const val ADDED_FRAGMENTS = "addedFragments"
const val ACTIVE_FRAGMENTS = "activeFragments"

const val FRAGMENT_MANAGER = "fragmentManager"
const val FULL_NAME = "fullName"
const val FID = "id"
const val LIFE_CYCLE_EVENT = "lifeCycle"
const val NAME = "name"
const val TYPE = "type"

const val NEW_DATA_KEY = "newData"
const val NEW_EVENT_KEY = "newEvent"
const val FILTER_OPTION_KEY = "newTreeFilterOptions"

const val FILTER_OBJECT_TREE_KEY = "options"

// OPTIONS
const val APPLICATION = "Application"
const val ACTIVITIES = "Activities"
const val FRAGMENTS = "Fragments"
const val VIEW_MODELS = "ViewModels"
const val JOBS = "Jobs"
const val SERVICES = "Services"
const val VIEW_MODEL_MEMBERS = "LiveData"
const val BACKSTACK_JETPACK = "JetpackBackStacks"
const val BACKSTACK_LEGACY = "LegacyBackStacks"
const val TRASH = "Trash"

const val TIMESTAMP = "timestamp"

const val NA = "N/A"

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
