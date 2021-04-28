package fr.afaucogney.mobile.flipper.internal.util

import com.facebook.flipper.core.FlipperArray
import com.facebook.flipper.core.FlipperObject
import org.junit.Test

class FlipperObjectExtKtTest {

    @Test
    fun copyTest() {
        // Given
        val toBeCopiedBuilder = FlipperObject
            .Builder()
            .put("1st_level_int", 1)
            .put("1st_level_str", "1")
            .put(
                "1st_level_array_builder",
                FlipperArray
                    .Builder()
                    .put(2)
                    .put("2")
            )
            .put(
                "1st_level_array_obj",
                FlipperArray.Builder()
                    .put(2)
                    .put("2")
                    .build()
            )
            .put(
                "1st_level_obj_builder",
                FlipperObject.Builder()
                    .put("2nd_level_int", 2)
                    .put("2nd_level_str", "2")
            )
            .put(
                "1st_level_obj",
                FlipperObject.Builder()
                    .put("2nd_level_int", 2)
                    .put("2nd_level_str", "2")
                    .build()
            )

        // When
        val resultBuilder = toBeCopiedBuilder.copy()

        // Then
        val toBeCopied = toBeCopiedBuilder.build()
        val result = resultBuilder.build()

        toBeCopied.keys().forEach {
            assert(result.get(it) == toBeCopied.get(it))
        }
    }
}