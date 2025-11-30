import domainclasses.Event
import domainclasses.Venue
import domainclasses.Participant

/*
* Allows our program to talk to the database.
* The following interfaces are just contracts.
* If you don't understand this, please watch a video on Kotlin interfaces.
* They are essentially blueprints that define a common contract for classes that implement them.
*/

interface ParticipantRepository {
    fun save(participant: Participant): Participant
    fun findById(id: Long): Participant?
    fun findAll(): List<Participant>
}

interface EventRepository {
    fun save(event: Event): Event
    fun findById(id: Long): Event?
    fun findAll(): List<Event>
}

interface VenueRepository {
    fun save(venue: Venue): Venue
    fun findById(id: Long): Venue?
    fun findAll(): List<Venue>
}