package dk.cachet.carp.webservices.analysis_lib.application

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.services.ApiVersion
import dk.cachet.carp.common.application.services.ApplicationService
import dk.cachet.carp.common.application.services.IntegrationEvent
import kotlinx.serialization.Required
import kotlinx.serialization.Serializable

interface EventTaskService: ApplicationService<EventTaskService, EventTaskService.Event>
{
    companion object { val API_VERSION = ApiVersion( 1, 0 ) }

    @Serializable
    sealed class Event(override val aggregateId: String?) : IntegrationEvent<EventTaskService>
    {
        constructor( aggregateId: UUID) : this( aggregateId.stringRepresentation )

        @Required
        override val apiVersion: ApiVersion = API_VERSION

    }
}