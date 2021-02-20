import com.facebook.flipper.core.FlipperObject
import fr.afaucogney.mobile.flipper.internal.model.SERVICES

internal fun FlipperObject.Builder.addServicesInfo(): FlipperObject.Builder {
    return this.put(SERVICES, "N/A")
}
