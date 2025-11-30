import domainclasses.Event
import domainclasses.Participant
import domainclasses.Venue
import java.sql.Statement

class JdbcEventRepository(
    private val participantRepository: ParticipantRepository,
    private val venueRepository: VenueRepository
) : EventRepository {

    override fun save(event: Event): Event {
        return if (event.id == null) {
            insert(event)
        } else {
            update(event)
        }
    }

    private fun insert(event: Event): Event {
        // Save venue first, so we have an ID for venue_id
        val savedVenue = venueRepository.save(event.venue)

        val sql = """
            INSERT INTO events (event_name, event_type, start_time, end_time, venue_id)
            VALUES (?, ?, ?, ?, ?)
        """.trimIndent()

        Database.getConnection().use { conn ->
            // 1) Insert into events
            val eventId: Long
            conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS).use { ps ->
                ps.setString(1, event.name)
                ps.setString(2, event.eventType)
                ps.setString(3, event.startTime)
                ps.setString(4, event.endTime)
                ps.setLong(5, savedVenue.id!!) // use the saved venue ID

                ps.executeUpdate()

                ps.generatedKeys.use { rs ->
                    if (rs.next()) {
                        eventId = rs.getLong(1)
                    } else {
                        error("Failed to retrieve generated ID for event")
                    }
                }
            }

            // 2) Insert into event_participants
            val linkSql = "INSERT INTO event_participants (event_id, participant_id) VALUES (?, ?)"
            conn.prepareStatement(linkSql).use { ps ->
                for (participant in event.getParticipants()) {
                    val pid = participant.id
                        ?: error("Participant must be saved (have an ID) before being added to an event")
                    ps.setLong(1, eventId)
                    ps.setLong(2, pid)
                    ps.addBatch()
                }
                ps.executeBatch()
            }

            // 3) Build a new Event object with ID and same participants
            val savedEvent = Event(
                name = event.name,
                eventType = event.eventType,
                startTime = event.startTime,
                endTime = event.endTime,
                venue = savedVenue,
                id = eventId
            )

            // Re-add participants so toString() etc. show them
            event.getParticipants().forEach { savedEvent.addParticipant(it) }

            return savedEvent
        }
    }

    private fun update(event: Event): Event {
        val savedVenue = venueRepository.save(event.venue) // ensure venue has an ID

        val sql = """
            UPDATE events
            SET event_name = ?, event_type = ?, start_time = ?, end_time = ?, venue_id = ?
            WHERE id = ?
        """.trimIndent()

        Database.getConnection().use { conn ->
            // 1) Update event row
            conn.prepareStatement(sql).use { ps ->
                ps.setString(1, event.name)
                ps.setString(2, event.eventType)
                ps.setString(3, event.startTime)
                ps.setString(4, event.endTime)
                ps.setLong(5, savedVenue.id!!)
                ps.setLong(6, event.id!!)
                ps.executeUpdate()
            }

            // 2) Replace entries in event_participants (simple approach: delete + reinsert)
            val deleteSql = "DELETE FROM event_participants WHERE event_id = ?"
            conn.prepareStatement(deleteSql).use { ps ->
                ps.setLong(1, event.id!!)
                ps.executeUpdate()
            }

            val insertLinkSql = "INSERT INTO event_participants (event_id, participant_id) VALUES (?, ?)"
            conn.prepareStatement(insertLinkSql).use { ps ->
                for (participant in event.getParticipants()) {
                    val pid = participant.id
                        ?: error("Participant must be saved (have an ID) before being added to an event")
                    ps.setLong(1, event.id!!)
                    ps.setLong(2, pid)
                    ps.addBatch()
                }
                ps.executeBatch()
            }

            // Return a fresh Event instance with updated venue + same participants
            val updatedEvent = Event(
                name = event.name,
                eventType = event.eventType,
                startTime = event.startTime,
                endTime = event.endTime,
                venue = savedVenue,
                id = event.id
            )
            event.getParticipants().forEach { updatedEvent.addParticipant(it) }

            return updatedEvent
        }
    }

    override fun findById(id: Long): Event? {
        val sql = "SELECT id, event_name, event_type, start_time, end_time, venue_id FROM events WHERE id = ?"

        Database.getConnection().use { conn ->
            conn.prepareStatement(sql).use { ps ->
                ps.setLong(1, id)
                ps.executeQuery().use { rs ->
                    if (!rs.next()) return null

                    val eventId = rs.getLong("id")
                    val venueId = rs.getLong("venue_id")

                    val venue = venueRepository.findById(venueId)
                        ?: error("Venue not found for ID $venueId")

                    // Base event with no participants yet
                    val event = Event(
                        name = rs.getString("event_name"),
                        eventType = rs.getString("event_type"),
                        startTime = rs.getString("start_time"),
                        endTime = rs.getString("end_time"),
                        venue = venue,
                        id = eventId
                    )

                    // Load participants via event_participants
                    val linkSql = "SELECT participant_id FROM event_participants WHERE event_id = ?"
                    conn.prepareStatement(linkSql).use { linkPs ->
                        linkPs.setLong(1, eventId)
                        linkPs.executeQuery().use { linkRs ->
                            while (linkRs.next()) {
                                val participantId = linkRs.getLong("participant_id")
                                val participant = participantRepository.findById(participantId)
                                if (participant != null) {
                                    event.addParticipant(participant)
                                }
                            }
                        }
                    }

                    return event
                }
            }
        }
    }

    override fun findAll(): List<Event> {
        val sql = "SELECT id FROM events"
        val result = mutableListOf<Event>()

        Database.getConnection().use { conn ->
            conn.createStatement().use { stmt ->
                stmt.executeQuery(sql).use { rs ->
                    while (rs.next()) {
                        val id = rs.getLong("id")
                        findById(id)?.let { result.add(it) }
                    }
                }
            }
        }

        return result
    }
}
