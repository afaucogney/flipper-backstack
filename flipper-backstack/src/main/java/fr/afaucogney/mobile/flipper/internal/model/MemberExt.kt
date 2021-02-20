package fr.afaucogney.mobile.flipper.internal.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.facebook.flipper.core.FlipperObject
import kotlin.reflect.KVisibility
import kotlin.reflect.full.memberProperties

internal fun ViewModel.addPublicMembers(): FlipperObject.Builder {
    return FlipperObject
        .Builder()
        .apply {
            this@addPublicMembers::class
                .memberProperties
                .filter { it.visibility == KVisibility.PUBLIC }
                .forEach {
                    this.put(
                        it.name,
                        FlipperObject.Builder()
                            .put("visibility", it.visibility)
                            .put("mutability", it.toString().split(" ").first())
                            .put("type", it.returnType)
                            .put(
                                "value",
                                it.getter
                                    .call(this@addPublicMembers)
                                    .let {
                                        when (it) {
                                            is LiveData<*> -> it.value
                                            else -> it.toString()
                                        }
                                    }
                            )
                    )
                }
        }
}

internal fun FlipperObject.Builder.addViewModelsMembers(viewModel: ViewModel): FlipperObject.Builder {
    return this
        .put(
            MEMBERS,
            viewModel.addPublicMembers()
        )
}
