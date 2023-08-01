package application

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.services.ApiVersion
import dk.cachet.carp.common.application.services.ApplicationService
import dk.cachet.carp.common.application.services.IntegrationEvent
import kotlinx.serialization.Required
import kotlinx.serialization.Serializable

interface ScheduledTaskService: ApplicationService<ScheduledTaskService, ScheduledTaskService.Event>
{
    companion object { val API_VERSION = ApiVersion( 1, 0 ) }

    @Serializable
    sealed class Event(override val aggregateId: String?) : IntegrationEvent<ScheduledTaskService>
    {
        constructor( aggregateId: UUID) : this( aggregateId.stringRepresentation )

        @Required
        override val apiVersion: ApiVersion = API_VERSION

    }
}
