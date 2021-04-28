package fr.afaucogney.mobile.flipper.internal.model

import com.facebook.flipper.core.FlipperArray
import com.facebook.flipper.core.FlipperObject
import fr.afaucogney.mobile.flipper.internal.util.removeField
import fr.afaucogney.mobile.flipper.internal.util.toJsonObject

///////////////////////////////////////////////////////////////////////////
// DATA
///////////////////////////////////////////////////////////////////////////

var optionTrash = true
var optionApplication = true
var optionActivities = true
var optionFragments = true
var optionViewModels = true
var optionViewModelMembers = true
    set(value) {
        field = value
        if (field) {
            optionViewModels = true
        }
    }
var optionJobs = false
var optionServices = false
var optionBackStackJetPack = true
    set(value) {
        field = value
        if (field) {
            optionFragments = true
        }
    }
var optionBackStackLegacy = true
    set(value) {
        field = value
        if (field) {
            optionFragments = true
        }
    }

///////////////////////////////////////////////////////////////////////////
// HELPER
///////////////////////////////////////////////////////////////////////////

internal fun updateClientObjectFiltersValues(params: FlipperObject) {
    params
        .getArray(FILTER_OBJECT_TREE_KEY)
        .toStringList()
        .run {
            optionViewModelMembers = contains(MEMBERS)
            optionViewModels = contains(VIEW_MODELS)
            optionFragments = contains(FRAGMENTS)
            optionActivities = contains(ACTIVITIES)
            optionBackStackJetPack = contains(JETPACK_BACKSTACK)
            optionBackStackLegacy = contains(LEGACY_BACKSTACK)
            optionTrash = contains(TRASH)
            optionJobs = contains(JOBS)
            optionServices = contains(SERVICES)
            optionApplication = contains(APPLICATION)
        }
}

///////////////////////////////////////////////////////////////////////////
// BUILDER
///////////////////////////////////////////////////////////////////////////

internal fun buildObjectTreeFilterMessage(): FlipperObject.Builder {
    return FlipperObject.Builder()
        .put(
            FILTER_OBJECT_TREE_KEY,
            FlipperArray
                .Builder()
                .apply {
                    if (optionApplication) put(APPLICATION)
                    if (optionActivities) put(ACTIVITIES)
                    if (optionFragments) put(FRAGMENTS)
                    if (optionViewModels) put(VIEW_MODELS)
                    if (optionViewModelMembers) put(MEMBERS)
                    if (optionServices) put(SERVICES)
                    if (optionJobs) put(JOBS)
                    if (optionTrash) put(TRASH)
                    if (optionBackStackLegacy) put(LEGACY_BACKSTACK)
                    if (optionBackStackJetPack) put(JETPACK_BACKSTACK)
                }
        )
}

fun FlipperObject.applyFilters(): FlipperObject {
    val result = this.toJsonObject()
    if (!optionViewModelMembers) {
        result.removeField(MEMBERS)
    }
    if (!optionViewModels) {
        result.removeField(VIEW_MODELS)
    }
    if (!optionFragments) {
        result.removeField(FRAGMENTS)
    }
    if (!optionActivities) {
        result.removeField(ACTIVITIES)
    }
    if (!optionJobs) {
        result.removeField(JOBS)
    }
    if (!optionServices) {
        result.removeField(SERVICES)
    }
    if (!optionTrash) {
        result.removeField(TRASH)
    }
    if (!optionApplication) {
        result.removeField(result.keys().next())
    }
    return FlipperObject(result)
}
