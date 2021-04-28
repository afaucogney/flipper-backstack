package fr.afaucogney.mobile.flipper.internal.model

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStore
import com.facebook.flipper.core.FlipperObject
import fr.afaucogney.mobile.flipper.internal.util.getPrivateProperty
import java.util.*

internal val ViewModel.name: String
    get() = this.javaClass.simpleName

internal val ViewModel.fullName: String
    get() = this.toString()

internal val ViewModel.fid: String
    get() = this.fullName.let {
        when {
            it.contains("@") -> it.split("@")[1]
            it.contains("""\{.*\}""".toRegex()) -> it.split("{")[1].split("}")[0]
            else -> it
        }
    }

internal val ViewModel.type: String
    get() = FlipperObjectType.VIEW_MODEL.key

internal enum class ViewModelLifeCycle {
    ON_VIEW_MODEL_CREATED,
    ON_VIEW_MODEL_CLEARED,
}

internal val ViewModelLifeCycle.key: String
    get() = this.toString().toLowerCase(Locale.getDefault())

// ACTIVITY
internal fun FlipperObject.Builder.addViewModelInfo(activity: Activity): FlipperObject.Builder {
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
                                        ViewModelLifeCycle.ON_VIEW_MODEL_CREATED.key
                                    )
                                    .addViewModelsMembers(it.value)
                            )
                    )
                }
            put(VIEW_MODELS, activityViewModels)
        }
    }
}

// FRAGMENT
internal fun FlipperObject.Builder.addViewModelInfo(fragment: Fragment): FlipperObject.Builder {
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
                                    ViewModelLifeCycle.ON_VIEW_MODEL_CREATED.key
                                )
                                .addViewModelsMembers(it.value)
                        )
                )
            }
        put(VIEW_MODELS, fragmentsViewModels)
    }
}