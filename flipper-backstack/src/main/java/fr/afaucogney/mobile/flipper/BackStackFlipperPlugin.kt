package fr.afaucogney.mobile.flipper

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentManager.FragmentLifecycleCallbacks
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStore
import androidx.navigation.fragment.findNavController
import com.facebook.flipper.core.FlipperArray
import com.facebook.flipper.core.FlipperConnection
import com.facebook.flipper.core.FlipperObject
import com.facebook.flipper.core.FlipperPlugin
import fr.afaucogney.mobile.flipper.internal.util.getPrivateProperty
import kotlin.reflect.KVisibility
import kotlin.reflect.full.memberProperties

class BackStackFlipperPlugin(app: Application) :
    Application.ActivityLifecycleCallbacks,
    FragmentLifecycleCallbacks(),
    FlipperPlugin {

    init {
        app.registerActivityLifecycleCallbacks(this)
    }

    ///////////////////////////////////////////////////////////////////////////
    // DATA
    ///////////////////////////////////////////////////////////////////////////

    private var connection: FlipperConnection? = null

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
     * And parse Ktp scope tree to then push it to the Desktop Flipper Client
     */
    override fun onConnect(connection: FlipperConnection?) {
        this.connection = connection
//        activityMap.forEach { (_, u) -> connection?.send("newData", u.build()) }
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
        return true
    }

    ///////////////////////////////////////////////////////////////////////////
    // FLIPPER
    ///////////////////////////////////////////////////////////////////////////

    companion object {
        const val FID = "id"
        const val NAME = "name"
        const val FULL_NAME = "fullName"
        const val FRAGMENTS = "fragments"
        const val TYPE = "type"
        const val LIFE_CYCLE_EVENT = "lifeCycle"
        const val BACK_STACK = "backStack"
        const val NEW_DATA = "newData"
        const val TRASH = "trash"
        const val VIEWMODELS = "viewmodels"
    }

    private enum class FlipperObjectType {
        ACTIVITY,
        FRAGMENT,
        VIEWMODEL,
    }

    private val FlipperObjectType.key: String
        get() = this.toString().toLowerCase()

    ///////////////////////////////////////////////////////////////////////////
    // ACTIVITY
    ///////////////////////////////////////////////////////////////////////////

    private val Activity.name: String
        get() = this.javaClass.simpleName

    private val Activity.fullName: String
        get() = this.toString()

    private val Activity.fid: String
        get() = this.fullName.split("@")[1]

    private val Activity.type: String
        get() = FlipperObjectType.ACTIVITY.key

    private enum class ActivityLifeCycle {
        ON_ACTIVITY_CREATED,
        ON_ACTIVITY_STARTED,
        ON_ACTIVITY_RESUMED,
        ON_ACTIVITY_PAUSED,
        ON_ACTIVITY_STOPPED,
        ON_ACTIVITY_SAVE_INSTANCE_STATE,
        ON_ACTIVITY_DESTROYED
    }

    private val ActivityLifeCycle.key: String
        get() = this.toString().toLowerCase()

    ///////////////////////////////////////////////////////////////////////////
    // FRAGMENT
    ///////////////////////////////////////////////////////////////////////////

    private val Fragment.name: String
        get() = this.javaClass.simpleName

    private val Fragment.fullName: String
        get() = this.toString()

    private val Fragment.fid: String
        get() = this.fullName.split("{")[1].split("}")[0]

    private val Fragment.type: String
        get() = FlipperObjectType.FRAGMENT.key

    private enum class FragmentLifeCycle {
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

    private val FragmentLifeCycle.key: String
        get() = this.toString().toLowerCase()

    private val trashMap = FlipperArray.Builder()
    private val activityMap = mutableMapOf<String, FlipperObject.Builder>()
    private val activityViewModelMap = mutableMapOf<String, FlipperObject.Builder>()

    //    private val fragmentMap = mutableMapOf<String, Map<String, FlipperObject.Builder>>()
    private val fragmentMap = mutableMapOf<String, HashMap<String, FlipperObject.Builder>>()
    private val fragmentViewModelMap =
        mutableMapOf<String, HashMap<String, FlipperObject.Builder>>()

    private val backStackListener = FragmentManager.OnBackStackChangedListener {

    }

    ///////////////////////////////////////////////////////////////////////////
    // VIEW MODEL
    ///////////////////////////////////////////////////////////////////////////

    // ViewModel
    private val ViewModel.name: String
        get() = this.javaClass.simpleName

    private val ViewModel.fullName: String
        get() = this.toString()

    private val ViewModel.fid: String
        get() = this.fullName.let {
            when {
                it.contains("@") -> it.split("@")[1]
                it.contains("""\{.*\}""".toRegex()) -> it.split("{")[1].split("}")[0]
                else -> it
            }
        }

    private val ViewModel.type: String
        get() = FlipperObjectType.VIEWMODEL.key

    private enum class ViewModelLifeCycle {
        ON_VIEWMODEL_CREATED,
        ON_VIEWMODEL_CLEARED,
    }

    private val ViewModelLifeCycle.key: String
        get() = this.toString().toLowerCase()

    ///////////////////////////////////////////////////////////////////////////
    // FLIPPER TRANSMISSION
    ///////////////////////////////////////////////////////////////////////////

    private fun pushActivityEvent(
        activity: Activity,
        event: ActivityLifeCycle
    ) {
        activity.saveAndMapToFlipperObjectBuilder(event)
            .build()
            .send()
    }

    private fun pushFragmentEvent(
        fragment: Fragment,
        event: FragmentLifeCycle
    ) {
        fragment.saveAndMapToFlipperObjectBuilder(event)
            .build()
            .send()
    }

    private fun FlipperObject.send() {
        this.apply { connection?.send(NEW_DATA, this) }
    }

    private fun Fragment.moveToTrash() {
        trashMap.put(fragmentMap[this.name]!![this.fid])
        fragmentMap[this.name]!!.remove(this.fid)
        this.requireActivity()
    }

    ///////////////////////////////////////////////////////////////////////////
    // ACTIVITY HELPER
    ///////////////////////////////////////////////////////////////////////////

    private fun Activity.toFlipperObjectBuilder(): FlipperObject.Builder {
        return FlipperObject.Builder()
            .put(FID, this.fid)
            .put(NAME, this.name)
            .put(FULL_NAME, this.fullName)
            .put(TYPE, this.type)
    }

    private fun FlipperObject.Builder.addLifeCycleEvent(event: ActivityLifeCycle?): FlipperObject.Builder {
        return if (event != null)
            this.put(LIFE_CYCLE_EVENT, event.key)
        else this
    }

    private fun FlipperObject.Builder.addBackStackInfo(activity: Activity): FlipperObject.Builder {
        return this.apply {
            if (activity is FragmentActivity) {
                val backStack = FlipperObject.Builder()
                for (i in 0 until activity.supportFragmentManager.backStackEntryCount) {
                    val entry = activity.supportFragmentManager.getBackStackEntryAt(i)
                    backStack.put(entry.id.toString(), entry.name)
                }
                put(BACK_STACK, backStack)
            }
        }
    }

    private fun FlipperObject.Builder.addViewModelInfo(activity: Activity): FlipperObject.Builder {
        return this.apply {
            val activityViewModels = FlipperObject.Builder()
            if (activity is AppCompatActivity) {
                activity
                    .viewModelStore
                    .getPrivateProperty<ViewModelStore, HashMap<String, ViewModel>>("mMap")
                    ?.forEach {
                        activityViewModels.put(
                            it.value.name,
                            FlipperObject
                                .Builder()
                                .put(
                                    it.value.fid,
                                    it.value
                                        .toFlipperObjectBuilder()
                                        .put(
                                            LIFE_CYCLE_EVENT,
                                            ViewModelLifeCycle.ON_VIEWMODEL_CREATED.key
                                        )
                                        .addViewModelsMembers(it.value)
                                )
                        )
                    }
                put(VIEWMODELS, activityViewModels)
            }
        }
    }

    private fun FlipperObject.Builder.addViewModelInfo(fragment: Fragment): FlipperObject.Builder {
        return this.apply {
            val fragmentsViewModels = FlipperObject.Builder()
            fragment
                .viewModelStore
                .getPrivateProperty<ViewModelStore, HashMap<String, ViewModel>>("mMap")
                ?.forEach {
                    fragmentsViewModels.put(
                        it.value.name,
                        FlipperObject
                            .Builder()
                            .put(
                                it.value.fid,
                                it.value
                                    .toFlipperObjectBuilder()
                                    .put(
                                        LIFE_CYCLE_EVENT,
                                        ViewModelLifeCycle.ON_VIEWMODEL_CREATED.key
                                    )
                                    .addViewModelsMembers(it.value)
                            )
                    )
                }
            put(VIEWMODELS, fragmentsViewModels)
        }
    }

    private fun ViewModel.toFlipperObjectBuilder(): FlipperObject.Builder {
        return FlipperObject.Builder()
            .put(FID, this.fid)
            .put(NAME, this.name)
            .put(FULL_NAME, this.fullName)
            .put(TYPE, this.type)
    }

    private fun Activity.saveAndMapToFlipperObjectBuilder(event: ActivityLifeCycle? = null): FlipperObject.Builder {
        if (!activityMap.containsKey(this.fid)) {
            activityMap[this.fid] = this.toFlipperObjectBuilder()
        }
        return FlipperObject.Builder()
            .put(
                this.application.javaClass.simpleName,
                FlipperObject.Builder()
                    .put(
                        "activities",
                        activityMap[this.fid]!!
                            .addLifeCycleEvent(event)
                            .addBackStackInfo(this)
                            .addViewModelInfo(this)
                            .put(FRAGMENTS, fragmentMap.toFO())
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
                    .put("jobs","N/A")
                    .put("services","N/A")
            )
    }
//
//    private fun Activity.toFlipperObject(event: ActivityLifeCycle? = null): FlipperObject.Builder {
//        if (!activityMap.containsKey(this.fid)) {
//            activityMap[this.fid] = this.toFlipperObjectBuilder()
//        }
//        return activityMap[this.fid]!!
//            .addLifeCycleEvent(event)
//            .addBackStackInfo(this)
//            .put(FRAGMENTS, fragmentMap.toFO())
//            .let {
//                FlipperObject.Builder()
//                    .put(this.fid, it)
//
//            }
//            .let {
//                FlipperObject.Builder()
//                    .put(this.name, it)
//                    .put(TRASH, trashMap)
//            }
//    }

    ///////////////////////////////////////////////////////////////////////////
    // FRAGMENT HELPER
    ///////////////////////////////////////////////////////////////////////////

    private fun Fragment.toFlipperObjectBuilder(): FlipperObject.Builder {
        return FlipperObject.Builder()
            .put(FID, this.fid)
            .put(NAME, this.name)
            .put(FULL_NAME, this.fullName)
            .put(TYPE, this.type)
    }

    private fun FlipperObject.Builder.addLifeCycleEvent(event: FragmentLifeCycle): FlipperObject.Builder {
        return this.put(LIFE_CYCLE_EVENT, event.key)
    }

    @SuppressLint("RestrictedApi")
    private fun FlipperObject.Builder.addNavBackStack(fragment: Fragment): FlipperObject.Builder {
        return this
            .let {
                if (fragment.name == "NavHostFragment") {
                    try {
                        val result = FlipperObject.Builder()
                        fragment.findNavController()
                            .backStack
                            .forEachIndexed { index, navBackStackEntry ->
                                result.put(
                                    index.toString(),
                                    navBackStackEntry.destination.displayName
                                )
                            }
                        it.put(BACK_STACK, result)
                    } catch (e: IllegalStateException) {
                        it
                    }
                } else {
                    it
                }
            }
    }

    @SuppressLint("RestrictedApi")
    private fun Fragment.saveAndMapToFlipperObjectBuilder(event: FragmentLifeCycle): FlipperObject.Builder {
        return this.toFlipperObjectBuilder()
            .addLifeCycleEvent(event)
            .addNavBackStack(this)
            .addViewModelInfo(this)
            .also { builder ->
                if (!fragmentMap.containsKey(this.name)) {
                    fragmentMap[this.name] = hashMapOf(this.fid to builder)
                } else {
                    fragmentMap[this.name]!![this.fid] = builder
                }
            }
            .let {
                this.requireActivity()
                    .saveAndMapToFlipperObjectBuilder()
            }
    }

    private fun MutableMap<String, HashMap<String, FlipperObject.Builder>>.toFO(): FlipperObject {
        val result = FlipperObject.Builder()
        this.toSortedMap().forEach { (t, u) ->
            val f = FlipperObject.Builder()
            u.toSortedMap().forEach {
                f.put(it.key, it.value)
            }
            result.put(t, f)
        }
        return result.build()
    }

    ///////////////////////////////////////////////////////////////////////////
    // HELPER
    ///////////////////////////////////////////////////////////////////////////

    private fun registerFragmentBackStackLifecycleCallback(activity: FragmentActivity) {
        activity.supportFragmentManager.addOnBackStackChangedListener(backStackListener)
    }

    private fun unregisterFragmentBackStackLifecycleCallback(activity: FragmentActivity) {
        activity.supportFragmentManager.removeOnBackStackChangedListener(backStackListener)
    }

    private fun registerFragmentLifecycleCallback(activity: FragmentActivity) {
        activity.supportFragmentManager
            .registerFragmentLifecycleCallbacks(
                fragmentCallback,
                true
            )
    }

    private fun unregisterFragmentLifecycleCallback(activity: FragmentActivity) {
        activity.supportFragmentManager
            .unregisterFragmentLifecycleCallbacks(fragmentCallback)
    }

    ///////////////////////////////////////////////////////////////////////////
    // ACTIVITY
    ///////////////////////////////////////////////////////////////////////////

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        pushActivityEvent(activity, ActivityLifeCycle.ON_ACTIVITY_CREATED)
        if (activity is FragmentActivity) {
            registerFragmentLifecycleCallback(activity)
            registerFragmentBackStackLifecycleCallback(activity)
        }
        if (activity is AppCompatActivity) {
            (activity as AppCompatActivity).viewModelStore
        }
    }

    override fun onActivityStarted(activity: Activity) {
        pushActivityEvent(activity, ActivityLifeCycle.ON_ACTIVITY_STARTED)
    }

    override fun onActivityResumed(activity: Activity) {
        pushActivityEvent(activity, ActivityLifeCycle.ON_ACTIVITY_RESUMED)
    }

    override fun onActivityPaused(activity: Activity) {
        pushActivityEvent(activity, ActivityLifeCycle.ON_ACTIVITY_PAUSED)
    }

    override fun onActivityStopped(activity: Activity) {
        pushActivityEvent(activity, ActivityLifeCycle.ON_ACTIVITY_STOPPED)
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        pushActivityEvent(activity, ActivityLifeCycle.ON_ACTIVITY_SAVE_INSTANCE_STATE)
    }

    override fun onActivityDestroyed(activity: Activity) {
        pushActivityEvent(activity, ActivityLifeCycle.ON_ACTIVITY_DESTROYED)
        if (activity is FragmentActivity) {
            unregisterFragmentLifecycleCallback(activity)
            unregisterFragmentBackStackLifecycleCallback(activity)
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // FRAGMENT
    ///////////////////////////////////////////////////////////////////////////

    private val fragmentCallback = object : FragmentManager.FragmentLifecycleCallbacks() {

        override fun onFragmentAttached(fm: FragmentManager, f: Fragment, context: Context) {
            pushFragmentEvent(f, FragmentLifeCycle.ON_FRAGMENT_ATTACHED)
        }

        override fun onFragmentCreated(
            fm: FragmentManager,
            f: Fragment,
            savedInstanceState: Bundle?
        ) {
            pushFragmentEvent(f, FragmentLifeCycle.ON_FRAGMENT_CREATED)
        }

        override fun onFragmentActivityCreated(
            fm: FragmentManager,
            f: Fragment,
            savedInstanceState: Bundle?
        ) {
            pushFragmentEvent(f, FragmentLifeCycle.ON_FRAGMENT_ACTIVITY_CREATED)
        }

        override fun onFragmentViewCreated(
            fm: FragmentManager,
            f: Fragment,
            v: View,
            savedInstanceState: Bundle?
        ) {
            pushFragmentEvent(f, FragmentLifeCycle.ON_FRAGMENT_VIEW_CREATED)
        }

        override fun onFragmentStarted(fm: FragmentManager, f: Fragment) {
            pushFragmentEvent(f, FragmentLifeCycle.ON_FRAGMENT_STARTED)
        }

        override fun onFragmentResumed(fm: FragmentManager, f: Fragment) {
            pushFragmentEvent(f, FragmentLifeCycle.ON_FRAGMENT_RESUMED)
        }

        override fun onFragmentPaused(fm: FragmentManager, f: Fragment) {
            pushFragmentEvent(f, FragmentLifeCycle.ON_FRAGMENT_PAUSED)
        }

        override fun onFragmentStopped(fm: FragmentManager, f: Fragment) {
            pushFragmentEvent(f, FragmentLifeCycle.ON_FRAGMENT_STOPPED)
        }

        override fun onFragmentSaveInstanceState(
            fm: FragmentManager,
            f: Fragment,
            outState: Bundle
        ) {
            pushFragmentEvent(f, FragmentLifeCycle.ON_FRAGMENT_SAVE_INSTANCE_STATE)
        }

        override fun onFragmentViewDestroyed(fm: FragmentManager, f: Fragment) {
            pushFragmentEvent(f, FragmentLifeCycle.ON_FRAGMENT_VIEW_DESTROYED)
        }

        override fun onFragmentDestroyed(fm: FragmentManager, f: Fragment) {
            pushFragmentEvent(f, FragmentLifeCycle.ON_FRAGMENT_DESTROYED)
        }

        override fun onFragmentDetached(fm: FragmentManager, f: Fragment) {
            pushFragmentEvent(f, FragmentLifeCycle.ON_FRAGMENT_DETACHED)
            f.moveToTrash()
            f.requireActivity().saveAndMapToFlipperObjectBuilder().build().send()
        }
    }

    private fun FlipperObject.Builder.addViewModelsMembers(viewModel: ViewModel): FlipperObject.Builder {
        val viewModelMembers = FlipperObject.Builder()
        viewModel::class
            .memberProperties
            .filter { it.visibility == KVisibility.PUBLIC }
            .forEach {
                viewModelMembers.put(
                    it.name,
                    FlipperObject.Builder()
                        .put("visibility", it.visibility)
                        .put("mutability", it.toString().split(" ").first())
                        .put("type", it.returnType)
                        .put("value", it.getter.call(viewModel).let {
                            when (it) {
                                is LiveData<*> -> it.value
                                else -> it.toString()
                            }
                        })
                )
            }

        return this.put(
            "members",
            viewModelMembers
        )
    }
}