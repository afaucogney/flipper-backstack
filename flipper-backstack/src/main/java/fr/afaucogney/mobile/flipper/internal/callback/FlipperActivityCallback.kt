package fr.afaucogney.mobile.flipper.internal.callback

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import fr.afaucogney.mobile.flipper.internal.model.ActivityLifeCycle

internal class FlipperActivityCallback(
    private val flipperPlugin: IActivityLifeCycleCallbackFlipperHandler
) : Application.ActivityLifecycleCallbacks {

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        flipperPlugin.pushActivityEvent(activity, ActivityLifeCycle.ON_ACTIVITY_CREATED)
        if (activity is FragmentActivity) {
            flipperPlugin.registerFragmentLifecycleCallback(activity)
            flipperPlugin.registerFragmentBackStackLifecycleCallback(activity)
        }
        if (activity is AppCompatActivity) {
            (activity as AppCompatActivity).viewModelStore
        }
    }

    override fun onActivityStarted(activity: Activity) {
        flipperPlugin.pushActivityEvent(activity, ActivityLifeCycle.ON_ACTIVITY_STARTED)
    }

    override fun onActivityResumed(activity: Activity) {
        flipperPlugin.pushActivityEvent(activity, ActivityLifeCycle.ON_ACTIVITY_RESUMED)
    }

    override fun onActivityPaused(activity: Activity) {
        flipperPlugin.pushActivityEvent(activity, ActivityLifeCycle.ON_ACTIVITY_PAUSED)
    }

    override fun onActivityStopped(activity: Activity) {
        flipperPlugin.pushActivityEvent(activity, ActivityLifeCycle.ON_ACTIVITY_STOPPED)
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        flipperPlugin.pushActivityEvent(activity, ActivityLifeCycle.ON_ACTIVITY_SAVE_INSTANCE_STATE)
    }

    override fun onActivityDestroyed(activity: Activity) {
        flipperPlugin.pushActivityEvent(
            activity,
            ActivityLifeCycle.ON_ACTIVITY_DESTROYED
        )
        if (activity is FragmentActivity) {
            flipperPlugin.unregisterFragmentLifecycleCallback(activity)
            flipperPlugin.unregisterFragmentBackStackLifecycleCallback(activity)
        }
    }

    internal interface IActivityLifeCycleCallbackFlipperHandler {
        fun pushActivityEvent(activity: Activity, event: ActivityLifeCycle)
        fun registerFragmentLifecycleCallback(activity: FragmentActivity)
        fun registerFragmentBackStackLifecycleCallback(activity: FragmentActivity)
        fun unregisterFragmentLifecycleCallback(activity: FragmentActivity)
        fun unregisterFragmentBackStackLifecycleCallback(activity: FragmentActivity)
    }
}