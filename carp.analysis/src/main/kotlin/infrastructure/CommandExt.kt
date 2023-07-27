package dk.cachet.carp.webservices.analysis_lib.infrastructure


import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import java.time.ZoneId

fun zonedNow(): Instant
{
    val zoneId = ZoneId.of("Europe/Copenhagen")
    return Clock.System.now()
}