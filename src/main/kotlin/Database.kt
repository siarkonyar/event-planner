// SQL imports
import java.sql.Connection
import java.sql.DriverManager

object Database {
    private const val URL = "jdbc:sqlite:data/events.db" // DO NOT CHANGE - This is our local db url

    // Run on objects first use
    init {
        createSchema();
    }

    // Connect to SQLite db
    fun getConnection(): Connection = DriverManager.getConnection(URL)

    // Encapsulate schema creation
    private fun createSchema() {
        getConnection().use { conn ->
            conn.createStatement().use { stmt ->

                // Create tables if they don't exist yet
                stmt.execute(
                    """
                    CREATE TABLE IF NOT EXISTS events (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        event_name TEXT NOT NULL,
                        event_type TEXT NOT NULL,
                        start_time TEXT NOT NULL,
                        end_time TEXT NOT NULL,
                        venue_id INTEGER NOT NULL,
                        FOREIGN KEY (venue_id) REFERENCES venues(id)
                    );
                    """.trimIndent()
                                )


                stmt.execute(
                    """
                    CREATE TABLE IF NOT EXISTS venues (
                        id INTEGER PRIMARY KEY,
                        venue_name TEXT NOT NULL,
                        venue_address TEXT NOT NULL,
                        venue_capacity INTEGER NOT NULL
                    );
                    """.trimIndent()
                )

                stmt.execute(
                    """
                    CREATE TABLE IF NOT EXISTS participants (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        name TEXT NOT NULL
                    );
                    """.trimIndent()
                )

                stmt.execute(
                    """
                    CREATE TABLE IF NOT EXISTS event_participants (
                        event_id INTEGER NOT NULL,
                        participant_id INTEGER NOT NULL,
                        PRIMARY KEY (event_id, participant_id),
                        FOREIGN KEY (event_id) REFERENCES events(id),
                        FOREIGN KEY (participant_id) REFERENCES participants(id)
                    );
                    """.trimIndent()
                )
            }
        }
    }
}