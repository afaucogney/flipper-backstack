package fr.afaucogney.mobile.flipper

import addServicesInfo
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.os.Handler
import android.os.Looper
import androidx.core.os.postDelayed
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.facebook.flipper.core.*
import fr.afaucogney.mobile.flipper.internal.callback.FlipperActivityCallback
import fr.afaucogney.mobile.flipper.internal.callback.FlipperFragmentCallback
import fr.afaucogney.mobile.flipper.internal.model.*
import fr.afaucogney.mobile.flipper.internal.model.ActivityLifeCycle
import fr.afaucogney.mobile.flipper.internal.model.FragmentLifeCycle
import fr.afaucogney.mobile.flipper.internal.model.name
import fr.afaucogney.mobile.flipper.internal.util.removeField
import fr.afaucogney.mobile.flipper.internal.util.toJsonObject
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

class BackStackFlipperPlugin(app: Application) :
    FlipperActivityCallback.IActivityLifeCycleCallbackFlipperHandler,
    FlipperFragmentCallback.IFragmentLifeCycleCallbackFlipperHandler,
    FlipperPlugin {

    ///////////////////////////////////////////////////////////////////////////
    // DATA
    ///////////////////////////////////////////////////////////////////////////

    private var connection: FlipperConnection? = null
    private val fragmentCallback = FlipperFragmentCallback(this)
    private val activityCallback = FlipperActivityCallback(this)

    ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTOR
    ///////////////////////////////////////////////////////////////////////////

    init {
        app.registerActivityLifecycleCallbacks(activityCallback)
    }

    ///////////////////////////////////////////////////////////////////////////
    // SPECIALIZATION
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Setup the unique id of the plugin
     */
    override fun getId(): String {
        return "LifecycleFlipper"
    }

    /**
     * onConnect is triggered every time the plugin is shown on Flipper
     * It does keep the connection
     */
    override fun onConnect(connection: FlipperConnection?) {
        this.connection = connection
        handleDesktopEvents()
        buildObjectTreeFilterMessage().sendObjectsFilters()
        buildObjectTreeMessage().sendObjectTree()
    }

    private fun handleDesktopEvents() {
        connection?.run {
            // Object Tree Filters
            receive(FILTER_OPTION_KEY) { params, _ ->
                updateClientObjectFiltersValues(params)
//                Handler(Looper.getMainLooper()).postDelayed(500) {
                    buildObjectTreeFilterMessage().sendObjectsFilters()
//                }
            }
        }
    }

    /**
     * Release the connection
     */
    override fun onDisconnect() {
        connection = null
    }

    /**
     * Plugin doe run in background
     */
    override fun runInBackground(): Boolean {
        return false
    }

    ///////////////////////////////////////////////////////////////////////////
    // BACK STACK
    ///////////////////////////////////////////////////////////////////////////

    private val backStackListener = FragmentManager.OnBackStackChangedListener {
    }

    ///////////////////////////////////////////////////////////////////////////
    // FLIPPER TRANSMISSION
    ///////////////////////////////////////////////////////////////////////////

    private fun FlipperObject.Builder.sendObjectTree() {
        this.build()
            .applyFilters()
            .apply { connection?.send(NEW_DATA_KEY, this) }
    }

    private fun FlipperArray.Builder.sendEvent() {
        this.build()
            .apply { connection?.send(NEW_EVENT_KEY, this) }
    }

    private fun FlipperObject.Builder.sendObjectsFilters() {
        this.build()
            .apply { connection?.send(FILTER_OPTION_KEY, this) }
    }

    ///////////////////////////////////////////////////////////////////////////
    // OBJECT LIFE CYCLE EVENT
    ///////////////////////////////////////////////////////////////////////////

    override fun pushActivityEvent(
        activity: Activity,
        event: ActivityLifeCycle
    ) {
        if (appName == null) {
            appName = activity.application.name
        }
        activity
            .saveAndMapToFlipperObjectBuilder(event)
            .sendObjectTree()
        activity
            .saveEvent(event)
            .sendEvent()
    }

    override fun pushFragmentEvent(
        fragment: Fragment,
        event: FragmentLifeCycle
    ) {
        fragment
            .saveAndMapToFlipperObjectBuilder(event)
            .sendObjectTree()
        fragment
            .saveEvent(event)
            .sendEvent()
    }

    override fun moveToTrashAndUpdate(fragment: Fragment) {
        fragment.moveToTrash()
        fragment
            .requireActivity()
            .saveAndMapToFlipperObjectBuilder()
            .sendObjectTree()
    }

    ///////////////////////////////////////////////////////////////////////////
    // OBJECT TREE BUILDER
    ///////////////////////////////////////////////////////////////////////////

    private fun buildObjectTreeMessage(): FlipperObject.Builder {
        return FlipperObject
            .Builder()
            .let {
                // Activities option just hide the application layer (not its content)
                if (optionActivities) {
                    it.addActivitiesInfo()
                } else {
                    it.addFragmentsInfo()
                }
            }
            .addJobsInfo()
            .addServicesInfo()
            .addTrashInfo()
            .let {
                if (optionApplication) {
                    FlipperObject
                        .Builder()
                        .put(
                            appName,
                            it
                        )
                } else {
                    // Application option just hide the application layer (not its content)
                    it
                }
            }
    }

    ///////////////////////////////////////////////////////////////////////////
    // HELPER
    ///////////////////////////////////////////////////////////////////////////

    @SuppressLint("RestrictedApi")
    private fun Fragment.saveAndMapToFlipperObjectBuilder(event: FragmentLifeCycle): FlipperObject.Builder {
        this.storeFragmentToMapIfNecessary(event)
        return buildObjectTreeMessage()
    }

    private fun Activity.saveAndMapToFlipperObjectBuilder(event: ActivityLifeCycle? = null): FlipperObject.Builder {
        storeActivityToMapIfNecessary(event)
        return buildObjectTreeMessage()
    }

    ///////////////////////////////////////////////////////////////////////////
    // FRAGMENT LIFE CYCLE BINDING
    ///////////////////////////////////////////////////////////////////////////

    override fun registerFragmentBackStackLifecycleCallback(activity: FragmentActivity) {
        activity
            .supportFragmentManager
            .addOnBackStackChangedListener(backStackListener)
    }

    override fun unregisterFragmentBackStackLifecycleCallback(activity: FragmentActivity) {
        activity
            .supportFragmentManager
            .removeOnBackStackChangedListener(backStackListener)
    }

    override fun registerFragmentLifecycleCallback(activity: FragmentActivity) {
        activity
            .supportFragmentManager
            .registerFragmentLifecycleCallbacks(
                fragmentCallback,
                true
            )
    }

    override fun unregisterFragmentLifecycleCallback(activity: FragmentActivity) {
        activity
            .supportFragmentManager
            .unregisterFragmentLifecycleCallbacks(fragmentCallback)
    }
}
