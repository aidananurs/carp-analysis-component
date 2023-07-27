package dk.cachet.carp.webservices.analysis_lib.domain

import dk.cachet.carp.common.application.services.ApplicationService
import dk.cachet.carp.common.application.services.IntegrationEvent


sealed interface Command
{
    interface Scheduled : Command
    {
        suspend fun execute()
    }

    interface Event : Command
    {
        suspend fun<TService : ApplicationService<TService, TEvent>, TEvent : IntegrationEvent<TService>> execute(event: TEvent, eventClass: Class<TEvent>)
    }

}