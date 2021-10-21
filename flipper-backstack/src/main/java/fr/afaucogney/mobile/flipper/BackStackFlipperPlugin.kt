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
import fr.afaucogney.mobile.flipper.internal.util.rx.HyperlinkedDebugTree
import fr.afaucogney.mobile.flipper.internal.util.rx.RxLogSubscriber
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.BehaviorSubject
import timber.log.Timber
import java.util.concurrent.TimeUnit

class BackStackFlipperPlugin(app: Application, showLog: Boolean = false) :
    FlipperActivityCallback.IActivityLifeCycleCallbackFlipperHandler,
    FlipperFragmentCallback.IFragmentLifeCycleCallbackFlipperHandler,
    FlipperPlugin {

    ///////////////////////////////////////////////////////////////////////////
    // CONST
    ///////////////////////////////////////////////////////////////////////////

    companion object {
        const val DEBOUNCE_DELAY = 200L
    }

    ///////////////////////////////////////////////////////////////////////////
    // DATA
    ///////////////////////////////////////////////////////////////////////////

    private var connection: FlipperConnection? = null
    private val fragmentCallback = FlipperFragmentCallback(this)
    private val activityCallback = FlipperActivityCallback(this)
    private val disposeBag = CompositeDisposable()

    private val dataStream = BehaviorSubject.create<FlipperObject.Builder>()
    private val eventStream = BehaviorSubject.create<FlipperArray.Builder>()
    private val objectFilterStream = BehaviorSubject.create<FlipperObject.Builder>()

    ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTOR
    ///////////////////////////////////////////////////////////////////////////

    init {
        if (showLog) Timber.plant(HyperlinkedDebugTree())
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
//        buildObjectTreeFilterMessage().sendObjectsFilters()
//        buildObjectTreeMessage().sendObjectTree()
        initSendProcess()
    }

    private fun initSendProcess() {
        if (disposeBag.size() == 0)
            disposeBag.addAll(
                dataStream
                    .subscribeOn(Schedulers.io())
                    .map { it.build().applyFilters() }
                    //.distinctUntilChanged()
                    .throttleFirst(DEBOUNCE_DELAY, TimeUnit.MILLISECONDS)
                    .doOnNext { connection?.send(NEW_DATA_KEY, it) }
                    .doOnSubscribe { buildObjectTreeMessage().sendObjectTree() }
                    .subscribeWith(RxLogSubscriber("dataStream")),
                eventStream
                    .subscribeOn(Schedulers.io())
                    .map { it.build() }
                    //.distinctUntilChanged()
                    .throttleFirst(DEBOUNCE_DELAY, TimeUnit.MILLISECONDS)
                    .doOnNext { connection?.send(NEW_EVENT_KEY, it) }
                    .subscribeWith(RxLogSubscriber("eventStream")),
                objectFilterStream
                    .subscribeOn(Schedulers.io())
                    .map { it.build() }
                    //.distinctUntilChanged()
                    .throttleFirst(DEBOUNCE_DELAY, TimeUnit.MILLISECONDS)
                    .doOnNext { connection?.send(FILTER_OPTION_KEY, it) }
                    // We update the tree when filters changed
                    .doOnNext { buildObjectTreeMessage().sendObjectTree() }
                    .doOnSubscribe { buildObjectTreeFilterMessage().sendObjectsFilters() }
                    .subscribeWith(RxLogSubscriber("objectFilterStream")),
            )
    }

    private fun handleDesktopEvents() {
        connection?.run {
            // Object Tree Filters
            receive(FILTER_OPTION_KEY) { params, _ ->
                Timber.tag("_objectFilterStream").d("receive : $params")
                updateClientObjectFiltersValues(params)
                buildObjectTreeFilterMessage().sendObjectsFilters()
            }
        }
    }

    /**
     * Release the connection
     */
    override fun onDisconnect() {
        disposeBag.clear()
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
        Timber.d("")
    }

    ///////////////////////////////////////////////////////////////////////////
    // FLIPPER TRANSMISSION
    ///////////////////////////////////////////////////////////////////////////

    private fun FlipperObject.Builder.sendObjectTree() {
        this.run { dataStream.onNext(this) }
    }

    private fun FlipperArray.Builder.sendEvent() {
        this.run { eventStream.onNext(this) }
    }

    private fun FlipperObject.Builder.sendObjectsFilters() {
        this.run { objectFilterStream.onNext(this) }
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

    override fun pushFragmentManagerEvent(
        fragment: Fragment,
        fm: FragmentManager
    ) {
        fragment
            .activity
            ?.saveAndMapToFlipperObjectBuilder()
            ?.sendObjectTree()

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
