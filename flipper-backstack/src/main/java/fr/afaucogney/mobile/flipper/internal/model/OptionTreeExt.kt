package fr.afaucogney.mobile.flipper.internal.model

import com.facebook.flipper.core.FlipperArray
import com.facebook.flipper.core.FlipperObject
import fr.afaucogney.mobile.flipper.internal.util.removeField
import fr.afaucogney.mobile.flipper.internal.util.toJsonObject

///////////////////////////////////////////////////////////////////////////
// DATA
///////////////////////////////////////////////////////////////////////////

internal var optionTrash = false
internal var optionApplication = false
internal var optionActivities = true
internal var optionFragments = true
    set(value) {
        field = value
        if (!field) {
            optionViewModels = false
            optionBackStackLegacy = false
            optionBackStackJetPack = false
        }
    }
internal var optionViewModels = true
    set(value) {
        field = value
        if (field) {
            optionFragments = true
        } else {
            optionViewModelMembers = false
        }
    }
internal var optionViewModelMembers = false
    set(value) {
        field = value
        if (field) {
            optionViewModels = true
        }
    }
internal var optionJobs = false
internal var optionServices = false
internal var optionBackStackJetPack = false
    set(value) {
        field = value
        if (field) {
            optionFragments = true
        }
    }
internal var optionBackStackLegacy = false
    set(value) {
        field = value
        if (field) {
            optionActivities = true
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

            optionTrash = contains(TRASH)
            optionJobs = contains(JOBS)
            optionServices = contains(SERVICES)
            optionActivities = contains(ACTIVITIES)
            optionApplication = contains(APPLICATION)

            contains(FRAGMENTS).let {
                if (it != optionFragments) {
                    optionFragments = it
                    return
                }
            }

            contains(VIEW_MODELS).let {
                if (it != optionViewModels) {
                    optionViewModels = it
                    return
                }
            }

            contains(VIEW_MODEL_MEMBERS).let {
                if (it != optionViewModelMembers) {
                    optionViewModelMembers = it
                    return
                }
            }

            contains(BACKSTACK_JETPACK).let {
                if (it != optionBackStackJetPack) {
                    optionBackStackJetPack = it
                    return
                }
            }

            contains(BACKSTACK_LEGACY).let {
                if (it != optionBackStackLegacy) {
                    optionBackStackLegacy = it
                    return
                }
            }
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
                    if (optionViewModelMembers /*&& optionViewModels*/) put(VIEW_MODEL_MEMBERS)
                    if (optionServices) put(SERVICES)
                    if (optionJobs) put(JOBS)
                    if (optionTrash) put(TRASH)
                    if (optionBackStackLegacy) put(BACKSTACK_LEGACY)
                    if (optionBackStackJetPack/* && optionFragments*/) put(BACKSTACK_JETPACK)
                }
        )
}

fun FlipperObject.applyFilters(): FlipperObject {
    val result = this.toJsonObject()
    // We first remove independent node
    if (!optionTrash) {
        result.removeField(TRASH)
    }
    if (!optionJobs) {
        result.removeField(JOBS)
    }
    if (!optionServices) {
        result.removeField(SERVICES)
    }
    // ViewModels
    if (!optionViewModels) {
        result.removeField(VIEW_MODELS)
    }
    // ViewModel members are optionals
    else if (!optionViewModelMembers) {
        result.removeField(VIEW_MODEL_MEMBERS)
    }
    // Fragments
    if (!optionFragments) {
        result.removeField(FRAGMENTS)
    }
    return FlipperObject(result)
}
