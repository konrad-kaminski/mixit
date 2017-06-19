package mixit.web.handler

import mixit.model.*
import mixit.repository.EventRepository
import mixit.repository.UserRepository
import mixit.util.coroutine.collectMap
import org.springframework.stereotype.Component
import org.springframework.web.coroutine.function.server.CoroutineServerRequest
import org.springframework.web.coroutine.function.server.CoroutineServerResponse
import org.springframework.web.coroutine.function.server.language
import java.time.LocalDate

@Component
class SponsorHandler(val userRepository: UserRepository,
                     val eventRepository: EventRepository) {

    suspend fun viewWithSponsors(view: String, title: String?, req: CoroutineServerRequest) = eventRepository.findOne("mixit17")?.let { event ->
        userRepository.findMany(event.sponsors.map { it.sponsorId }).collectMap(User::login).let { sponsorsByLogin ->
            val sponsorsByEvent = event.sponsors.groupBy { it.level }
            CoroutineServerResponse.ok().render(view, mapOf(
                    Pair("sponsors-gold", sponsorsByEvent[SponsorshipLevel.GOLD]?.map { it.toDto(sponsorsByLogin[it.sponsorId]!!, req.language()) }),
                    Pair("sponsors-silver", sponsorsByEvent[SponsorshipLevel.SILVER]?.map { it.toDto(sponsorsByLogin[it.sponsorId]!!, req.language()) }),
                    Pair("sponsors-hosting", sponsorsByEvent[SponsorshipLevel.HOSTING]?.map { it.toDto(sponsorsByLogin[it.sponsorId]!!, req.language()) }),
                    Pair("sponsors-lanyard", sponsorsByEvent[SponsorshipLevel.LANYARD]?.map { it.toDto(sponsorsByLogin[it.sponsorId]!!, req.language()) }),
                    Pair("sponsors-party", sponsorsByEvent[SponsorshipLevel.PARTY]?.map { it.toDto(sponsorsByLogin[it.sponsorId]!!, req.language()) }),
                    Pair("sponsors-video", sponsorsByEvent[SponsorshipLevel.VIDEO]?.map { it.toDto(sponsorsByLogin[it.sponsorId]!!, req.language()) }),
                    Pair("title", title)
            ))
    }}

}

private class EventSponsoringDto(
        val level: SponsorshipLevel,
        val sponsor: UserDto,
        val subscriptionDate: LocalDate = LocalDate.now()
)

private fun EventSponsoring.toDto(sponsor: User, language: Language) =
        EventSponsoringDto(level, sponsor.toDto(language), subscriptionDate)
