import domainclasses.Event
import domainclasses.Participant
import domainclasses.Venue


fun main() {


    // Initialize repositories
    val venueRepository = JdbcVenueRepository()
    val participantRepository = JdbcParticipantRepository()
    val eventRepository = JdbcEventRepository(participantRepository, venueRepository)

    // Create a Venue
    val venue1 = Venue("Main Hall", "London", capacity = 2)

    // Save the venue into the database
    val savedVenue = venueRepository.save(venue1)
    println("Saved venue: $savedVenue")

    // Create an Event referencing the savedVenue
    val event = Event("Party 2", "Social", "12:30", "16:30", savedVenue)

    // Create and save Participants (so they have IDs)
    val p1 = participantRepository.save(Participant("Sukhi"))
    val p2 = participantRepository.save(Participant("Alex"))
    val p3 = participantRepository.save(Participant("Third Person"))

    // Add Participants to the event
    println("Adding p1: ${event.addParticipant(p1)}")  // true
    println("Adding p2: ${event.addParticipant(p2)}")  // true
    println("Adding p3: ${event.addParticipant(p3)}")  // false, event is full

    println("Event before saving: $event")

    // Save the event (which will link to participants via event_participants)
    val savedEvent = eventRepository.save(event)
    println("Saved event: $savedEvent")

    // Retrieve and print event from the database
    val loadedEvent = eventRepository.findById(savedEvent.id!!)
    println("Loaded event: $loadedEvent")

    // Retrieve and print all events
    println("All events in DB:")
    eventRepository.findAll().forEach { println(it) }

    // Retrieve and print all venues
    println("All venues in DB:")
    venueRepository.findAll().forEach { println(it) }


    try {
        // 1. Find the Scala object (Scala objects have a static field called MODULE$)
        val scalaClass = Class.forName("logic.SlotFinder$")
        val scalaObject = scalaClass.getField("MODULE$").get(null)

        // 2. Create the arguments (Convert Kotlin ArrayList to a standard Java List if needed)
        val venuesList = listOf(venue1)

        // 3. Find the method 'findFirstAvailableVenue'
        // Note: ensure your Scala method accepts java.util.List or similar.
        // If your Scala method takes a Scala List, you might need "scala.jdk.CollectionConverters" in Scala.
        // For now, let's assume it takes a generic Object or Java List for simplicity.
        val method = scalaClass.getMethod("findFirstAvailableVenue", List::class.java, Int::class.javaPrimitiveType)

        // 4. Invoke the method
        val result = method.invoke(scalaObject, venuesList, 1)

        println("Result from Scala: $result")

    } catch (e: Exception) {
        e.printStackTrace()
        println("Could not call Scala logic: " + e.message)
    }

}
