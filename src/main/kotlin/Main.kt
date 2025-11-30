import domainclasses.Event
import domainclasses.Participant
import domainclasses.Venue

fun main() {


    // Create a Venue
    val venue = Venue("Main Hall", "London", capacity = 2)
    val venue1 = Venue("Main Hall", "London", capacity = 15)
    val venue2 = Venue("Main Hall", "London", capacity = 20)


    // Create an Event referencing the savedVenue
    val event = Event("Party 2", "Social", "12:30", "16:30", venue)


    //val result = SlotFinder.findFirstAvailableVenue(arrayListOf(venue, venue1, venue2), 10)
    //println(result)


    try {
        // 1. Find the Scala object (Scala objects have a static field called MODULE$)
        val scalaClass = Class.forName("logic.SlotFinder$")
        val scalaObject = scalaClass.getField("MODULE$").get(null)

        // 2. Create the arguments (Convert Kotlin ArrayList to a standard Java List if needed)
        val venuesList = listOf(venue, venue1, venue2)

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
