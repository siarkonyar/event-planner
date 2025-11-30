package logic

import domainclasses.Venue
import java.time.LocalDateTime

// Convert Java List to Scala List for easier functional operations
import scala.jdk.CollectionConverters._

object SlotFinder {

  /**
   * Finds the first available venue for a given capacity and start date
   *
   * @param venues List of all available venues
   * @param requiredCapacity Minimum capacity needed
   * @return Option containing the first available venue (None if not found)
   */
  def findFirstAvailableVenue(
                               venues: java.util.List[Venue],
                               requiredCapacity: Int
                             ): Option[Venue] = {

    val venueList = venues.asScala.toList

    // Filter venues that meet the capacity requirement
    // The functions checks if the venue is not null and then it checks has enough capacity
    val suitableVenues = venueList.filter(v => v != null && v.getCapacity >= requiredCapacity)

    // For now, let's just return the first suitable venue
    // We'll add time conflict checking next
    suitableVenues.headOption
  }

  /**
   * Test method to verify it works
   */
  def testSlotFinder() = {
    println("SlotFinder is working!")
  }
}