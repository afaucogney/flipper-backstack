package fr.afaucogney.mobile.flipper.internal.callback

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import fr.afaucogney.mobile.flipper.internal.model.FragmentLifeCycle

internal class FlipperFragmentCallback(
    private val flipperPlugin: IFragmentLifeCycleCallbackFlipperHandler
) : FragmentManager.FragmentLifecycleCallbacks() {

    override fun onFragmentAttached(fm: FragmentManager, f: Fragment, context: Context) {
        flipperPlugin.pushFragmentEvent(f, FragmentLifeCycle.ON_FRAGMENT_ATTACHED)
        flipperPlugin.pushFragmentManagerEvent(f, fm)
    }

    override fun onFragmentCreated(
        fm: FragmentManager,
        f: Fragment,
        savedInstanceState: Bundle?
    ) {
        flipperPlugin.pushFragmentEvent(f, FragmentLifeCycle.ON_FRAGMENT_CREATED)
        flipperPlugin.pushFragmentManagerEvent(f, fm)
    }

    override fun onFragmentActivityCreated(
        fm: FragmentManager,
        f: Fragment,
        savedInstanceState: Bundle?
    ) {
        flipperPlugin.pushFragmentEvent(f, FragmentLifeCycle.ON_FRAGMENT_ACTIVITY_CREATED)
        flipperPlugin.pushFragmentManagerEvent(f, fm)
    }

    override fun onFragmentViewCreated(
        fm: FragmentManager,
        f: Fragment,
        v: View,
        savedInstanceState: Bundle?
    ) {
        flipperPlugin.pushFragmentEvent(f, FragmentLifeCycle.ON_FRAGMENT_VIEW_CREATED)
        flipperPlugin.pushFragmentManagerEvent(f, fm)
    }

    override fun onFragmentStarted(fm: FragmentManager, f: Fragment) {
        flipperPlugin.pushFragmentEvent(f, FragmentLifeCycle.ON_FRAGMENT_STARTED)
        flipperPlugin.pushFragmentManagerEvent(f, fm)
    }

    override fun onFragmentResumed(fm: FragmentManager, f: Fragment) {
        flipperPlugin.pushFragmentEvent(f, FragmentLifeCycle.ON_FRAGMENT_RESUMED)
        flipperPlugin.pushFragmentManagerEvent(f, fm)
    }

    override fun onFragmentPaused(fm: FragmentManager, f: Fragment) {
        flipperPlugin.pushFragmentEvent(f, FragmentLifeCycle.ON_FRAGMENT_PAUSED)
        flipperPlugin.pushFragmentManagerEvent(f, fm)
    }

    override fun onFragmentStopped(fm: FragmentManager, f: Fragment) {
        flipperPlugin.pushFragmentEvent(f, FragmentLifeCycle.ON_FRAGMENT_STOPPED)
        flipperPlugin.pushFragmentManagerEvent(f, fm)
    }

    override fun onFragmentSaveInstanceState(
        fm: FragmentManager,
        f: Fragment,
        outState: Bundle
    ) {
        flipperPlugin.pushFragmentEvent(f, FragmentLifeCycle.ON_FRAGMENT_SAVE_INSTANCE_STATE)
        flipperPlugin.pushFragmentManagerEvent(f, fm)
    }

    override fun onFragmentViewDestroyed(fm: FragmentManager, f: Fragment) {
        flipperPlugin.pushFragmentEvent(f, FragmentLifeCycle.ON_FRAGMENT_VIEW_DESTROYED)
        flipperPlugin.pushFragmentManagerEvent(f, fm)
    }

    override fun onFragmentDestroyed(fm: FragmentManager, f: Fragment) {
        flipperPlugin.pushFragmentEvent(f, FragmentLifeCycle.ON_FRAGMENT_DESTROYED)
        flipperPlugin.pushFragmentManagerEvent(f, fm)
    }

    override fun onFragmentDetached(fm: FragmentManager, f: Fragment) {
        flipperPlugin.pushFragmentEvent(f, FragmentLifeCycle.ON_FRAGMENT_DETACHED)
        flipperPlugin.moveToTrashAndUpdate(f)
        flipperPlugin.pushFragmentManagerEvent(f, fm)
    }

    internal interface IFragmentLifeCycleCallbackFlipperHandler {
        fun pushFragmentEvent(fragment: Fragment, event: FragmentLifeCycle)
        fun pushFragmentManagerEvent(fragment: Fragment, fm: FragmentManager)
        fun moveToTrashAndUpdate(fragment: Fragment)
    }
}
