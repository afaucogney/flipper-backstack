package fr.afaucogney.mobile.flipper.internal.util.rx

import com.facebook.flipper.plugins.crashreporter.CrashReporterPlugin
import timber.log.Timber

/**
 * A [Tree] for debug builds.
 * Automatically shows a Hyperlink to the calling Class and Linenumber in the Logs.
 * Allows quick lookup of the caller source just by clicking on the Hyperlink in the Log.
 * @param showMethodName Whether or not to show the method name as well
 */
class HyperlinkedDebugTree(private val showMethodName: Boolean = true) : Timber.DebugTree() {

    // Add link to Tag
    override fun createStackElementTag(element: StackTraceElement): String {
        return with(element) { "($fileName:$lineNumber) ${if (showMethodName) " $methodName()" else ""}" }
    }

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (tag != null && tag.startsWith("(Base")) {
            super.log(priority, tag, message, t)
            super.log(priority, tag.removeHyperLink(), message, t)
        } else {
            super.log(priority, tag, message, t)
        }
        t?.run { CrashReporterPlugin.getInstance().sendExceptionMessage(null, this) }
    }

    private fun String.removeHyperLink(): String {
        return this.replaceFirst("(", " ").replaceFirst(")", " ")
    }
}
