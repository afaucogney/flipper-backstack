package fr.afaucogney.mobile.flipper

import addServicesInfo
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.facebook.flipper.core.FlipperArray
import com.facebook.flipper.core.FlipperConnection
import com.facebook.flipper.core.FlipperObject
import com.facebook.flipper.core.FlipperPlugin
import fr.afaucogney.mobile.flipper.internal.callback.FlipperActivityCallback
import fr.afaucogney.mobile.flipper.internal.callback.FlipperFragmentCallback
import fr.afaucogney.mobile.flipper.internal.model.*
import fr.afaucogney.mobile.flipper.internal.model.ActivityLifeCycle
import fr.afaucogney.mobile.flipper.internal.model.FragmentLifeCycle
import fr.afaucogney.mobile.flipper.internal.model.name
import java.sql.Time
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.HashMap

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
    private val timeStampFormatter = SimpleDateFormat("HH:mm:ss.SSS", Locale.FRANCE)

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
    // MAP
    ///////////////////////////////////////////////////////////////////////////

    private val activityMap = mutableMapOf<String, FlipperObject.Builder>()
    private val fragmentMap = mutableMapOf<String, HashMap<String, FlipperObject.Builder>>()
    private val trashMap = FlipperArray.Builder()
    private val eventList = FlipperArray.Builder()

    ///////////////////////////////////////////////////////////////////////////
    // OPTION
    ///////////////////////////////////////////////////////////////////////////

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


    private val backStackListener = FragmentManager.OnBackStackChangedListener {
    }

    ///////////////////////////////////////////////////////////////////////////
    // FLIPPER TRANSMISSION
    ///////////////////////////////////////////////////////////////////////////

    override fun pushActivityEvent(
        activity: Activity,
        event: ActivityLifeCycle
    ) {
        activity
            .saveAndMapToFlipperObjectBuilder(event)
            .send()
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
            .send()
        fragment
            .saveEvent(event)
            .sendEvent()
    }

    override fun moveToTrashAndUpdate(fragment: Fragment) {
        fragment.moveToTrash()
        fragment
            .requireActivity()
            .saveAndMapToFlipperObjectBuilder()
            .send()
    }

    private fun FlipperObject.Builder.send() {
        this.build().apply { connection?.send(NEW_DATA, this) }
    }

    private fun FlipperArray.Builder.sendEvent() {
        this.build().apply { connection?.send(NEW_EVENT, this) }
    }

    private fun MutableMap<String, HashMap<String, FlipperObject.Builder>>.toFlipperObjectBuilder(): FlipperObject.Builder {
        val result = FlipperObject.Builder()
        this.toSortedMap().forEach { (t, u) ->
            val f = FlipperObject.Builder()
            u.toSortedMap().forEach {
                f.put(it.key, it.value)
            }
            result.put(t, f)
        }
        return result
    }

    ///////////////////////////////////////////////////////////////////////////
    // ACTIVITY HELPER
    ///////////////////////////////////////////////////////////////////////////

    private fun Activity.storeActivityToMapIfNecessary() {
        if (!activityMap.containsKey(this.fid)) {
            activityMap[this.fid] = this.toFlipperObjectBuilder()
        }
    }

    private fun Activity.saveAndMapToFlipperObjectBuilder(event: ActivityLifeCycle? = null): FlipperObject.Builder {
        storeActivityToMapIfNecessary()
        return FlipperObject
            .Builder()
            .put(
                application.name,
                FlipperObject
                    .Builder()
                    .put(
                        ACTIVITIES,
                        activityMap[this.fid]!!
                            .addLifeCycleEvent(event)
                            .addBackStackInfo(this)
                            .addViewModelInfo(this)
                            .put(FRAGMENTS, fragmentMap.toFlipperObjectBuilder())
                            .let {
                                FlipperObject.Builder()
                                    .put(this.fid, it)

                            }
                            .let {
                                FlipperObject.Builder()
                                    .put(this.name, it)
                                    .put(TRASH, trashMap)
                            }
                    )
                    .addJobsInfo()
                    .addServicesInfo()
            )
    }

    private fun Activity.saveEvent(event: ActivityLifeCycle): FlipperArray.Builder {
        return FlipperObject.Builder()
            .put(
                TIMESTAMP,
                timeStampFormatter.format(System.currentTimeMillis())
            )
            .put(TYPE, this.type)
            .put(NAME, this.name)
            .put(FID, this.fid)
            .put(LIFE_CYCLE_EVENT, event)
            .let { eventList.put(it) }
    }

    ///////////////////////////////////////////////////////////////////////////
    // FRAGMENT
    ///////////////////////////////////////////////////////////////////////////

    private fun FlipperObject.Builder.storeFragmentToMapIfNecessary(fragment: Fragment): FlipperObject.Builder {
        return this
            .also { builder ->
                if (!fragmentMap.containsKey(fragment.name)) {
                    fragmentMap[fragment.name] = hashMapOf(fragment.fid to builder)
                } else {
                    fragmentMap[fragment.name]!![fragment.fid] = builder
                }
            }
    }


    @SuppressLint("RestrictedApi")
    private fun Fragment.saveAndMapToFlipperObjectBuilder(event: FragmentLifeCycle): FlipperObject.Builder {
        return this
            .toFlipperObjectBuilder()
            .addLifeCycleEvent(event)
            .addNavBackStack(this)
            .addViewModelInfo(this)
            .storeFragmentToMapIfNecessary(this)
            .let {
                this.requireActivity()
                    .saveAndMapToFlipperObjectBuilder()
            }
    }

    private fun Fragment.saveEvent(event: FragmentLifeCycle): FlipperArray.Builder {
        return FlipperObject.Builder()
            .put(
                TIMESTAMP,
                timeStampFormatter.format(System.currentTimeMillis())
            )
            .put(TYPE, this.type)
            .put(NAME, this.name)
            .put(FID, this.fid)
            .put(LIFE_CYCLE_EVENT, event)
            .let { eventList.put(it) }
    }

    ///////////////////////////////////////////////////////////////////////////
    // VIEWMODEL
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // TRASH
    ///////////////////////////////////////////////////////////////////////////

    private fun Fragment.moveToTrash() {
        trashMap.put(fragmentMap[this.name]!![this.fid])
        fragmentMap[this.name]!!.remove(this.fid)
        this.requireActivity()
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
