package fr.afaucogney.mobile.flipper.internal.util

import com.facebook.flipper.core.FlipperObject
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

fun FlipperObject.Builder.copy(): FlipperObject.Builder {
    val result = FlipperObject.Builder()
    this.build().let { obj ->
        obj.keys().forEach {
            result.put(it, obj.get(it))
        }
    }
    return result
}

fun FlipperObject.copy(): FlipperObject {
    return FlipperObject(this.toJsonString())
}

fun FlipperObject.toJsonObject(): JSONObject {
    return JSONObject(this.toJsonString())
}


fun FlipperObject.removeField(field: String): FlipperObject {
    val result = this.toJsonString()
    val jsonResult = JSONObject(result)
    removeJSONField(jsonResult, field)
    return FlipperObject(jsonResult)
}

fun JSONObject.removeField(field: String): JSONObject {
    removeJSONField(this, field)
    return this
}

@Throws(JSONException::class)
fun removeJSONField(obj: JSONObject, field: String) {
    obj.remove(field)
    val it: Iterator<String> = obj.keys()
    while (it.hasNext()) {
        val key = it.next()
        val childObj: Any = obj.get(key)
        if (childObj is JSONArray) {
            val arrayChildObjs: JSONArray = childObj
            val size: Int = arrayChildObjs.length()
            for (i in 0 until size) {
                if (arrayChildObjs.get(i) is JSONObject) {
                    removeJSONField(arrayChildObjs.getJSONObject(i), field)
                }
            }
        }
        if (childObj is JSONObject) {
            removeJSONField(childObj, field)
        }
    }
}
