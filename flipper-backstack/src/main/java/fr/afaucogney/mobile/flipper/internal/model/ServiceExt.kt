import com.facebook.flipper.core.FlipperObject
import fr.afaucogney.mobile.flipper.internal.model.NA
import fr.afaucogney.mobile.flipper.internal.model.SERVICES

///////////////////////////////////////////////////////////////////////////
// BUILDER
///////////////////////////////////////////////////////////////////////////

internal fun FlipperObject.Builder.addServicesInfo(): FlipperObject.Builder {
    return this.put(SERVICES, NA)
}
