import domainclasses.Venue
import java.sql.Statement

class JdbcVenueRepository : VenueRepository {

    override fun save(venue: Venue): Venue {
        return if (venue.id == null) {
            insert(venue) // If no ID, insert a new venue
        } else {
            update(venue) // If ID exists, update it
        }
    }

    private fun insert(venue: Venue): Venue {
        val sql = "INSERT INTO venues (venue_name, venue_address, venue_capacity) VALUES (?, ?, ?)"

        Database.getConnection().use { conn ->
            conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS).use { ps ->
                ps.setString(1, venue.name)
                ps.setString(2, venue.address)
                ps.setInt(3, venue.capacity)
                ps.executeUpdate()

                ps.generatedKeys.use { rs ->
                    if (rs.next()) {
                        val generatedId = rs.getLong(1)
                        return Venue(
                            name = venue.name,
                            address = venue.address,
                            capacity = venue.capacity,
                            id = generatedId
                        )
                    } else {
                        error("Failed to retrieve generated ID for venue")
                    }
                }
            }
        }
    }

    private fun update(venue: Venue): Venue {
        val sql = "UPDATE venues SET venue_name = ?, venue_address = ?, venue_capacity = ? WHERE id = ?"

        Database.getConnection().use { conn ->
            conn.prepareStatement(sql).use { ps ->
                ps.setString(1, venue.name)
                ps.setString(2, venue.address)
                ps.setInt(3, venue.capacity)
                ps.setLong(4, venue.id!!)
                ps.executeUpdate()
            }
        }
        return venue
    }

    override fun findById(id: Long): Venue? {
        val sql = "SELECT id, venue_name, venue_address, venue_capacity FROM venues WHERE id = ?"

        Database.getConnection().use { conn ->
            conn.prepareStatement(sql).use { ps ->
                ps.setLong(1, id)
                ps.executeQuery().use { rs ->
                    return if (rs.next()) {
                        Venue(
                            name = rs.getString("venue_name"),
                            address = rs.getString("venue_address"),
                            capacity = rs.getInt("venue_capacity"),
                            id = rs.getLong("id")
                        )
                    } else {
                        null // No matching venue found
                    }
                }
            }
        }
    }

    override fun findAll(): List<Venue> {
        val sql = "SELECT id, venue_name, venue_address, venue_capacity FROM venues"
        val venues = mutableListOf<Venue>()

        Database.getConnection().use { conn ->
            conn.createStatement().use { stmt ->
                stmt.executeQuery(sql).use { rs ->
                    while (rs.next()) {
                        venues.add(
                            Venue(
                                name = rs.getString("venue_name"),
                                address = rs.getString("venue_address"),
                                capacity = rs.getInt("venue_capacity"),
                                id = rs.getLong("id")
                            )
                        )
                    }
                }
            }
        }
        return venues
    }
}