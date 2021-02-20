package fr.afaucogney.mobile.flipper.internal.model

import androidx.fragment.app.Fragment
import fr.afaucogney.mobile.flipper.BackStackFlipperPlugin


///////////////////////////////////////////////////////////////////////////
// FRAGMENT
///////////////////////////////////////////////////////////////////////////

internal val Fragment.name: String
    get() = this.javaClass.simpleName

internal val Fragment.fullName: String
    get() = this.toString()

internal val Fragment.fid: String
    get() = this.fullName.split("{")[1].split("}")[0]

internal val Fragment.type: String
    get() = FlipperObjectType.FRAGMENT.key

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

internal val FragmentLifeCycle.key: String
    get() = this.toString().toLowerCase()
