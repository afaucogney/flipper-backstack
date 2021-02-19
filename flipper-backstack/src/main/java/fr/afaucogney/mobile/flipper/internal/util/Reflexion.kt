package fr.afaucogney.mobile.flipper.internal.util

import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible


inline fun <reified T : Any, R> T.getPrivateProperty(name: String): R? =
    T::class
        .memberProperties
        .firstOrNull { it.name == name }
        ?.apply { isAccessible = true }
        ?.get(this) as? R