package domainclasses

class Event(val name: String,
            val eventType: String,
            var startTime: String,
            val endTime: String,
            val venue: Venue,
            val id: Long? = null
) {

    // Dynamic list of participant
    private val participants: MutableList<Participant> = mutableListOf()

    /*
    * Create a new instance of participant.
    * Add the new participant to the list.
    * Returns true if added, false if the event is full.
    */
    fun addParticipant(participant: Participant): Boolean {
        return if (participants.size < venue.capacity) {
            participants.add(participant)
            true
        } else {
            false
        }
    }

    fun getParticipants(): List<Participant> = participants.toList()

    fun updateStartTime(newStartTime: String) {
        this.startTime = newStartTime;
    }

    override fun toString(): String {
        return "$name ($eventType) at ${venue.name} " +
                "from $startTime to $endTime, " +
                "participants: ${participants.size}/${venue.capacity}"
    }

}