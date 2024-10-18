import com.android.voyageur.model.activity.Activity
import com.android.voyageur.model.activity.ActivityType
import com.android.voyageur.model.location.Location
import com.google.firebase.Timestamp
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ActivityTest {

  private lateinit var location: Location
  private lateinit var startTime: Timestamp
  private lateinit var endDate: Timestamp

  @Before
  fun setUp() {
    // Assuming Location has a constructor that takes latitude and longitude for simplicity
    location = Location()
    startTime = Timestamp.now()
    endDate = Timestamp.now()
  }

  @Test
  fun testActivityCreation() {
    val activity =
        Activity(
            title = "Visit the Louvre",
            description = "Exploring the famous art museum.",
            location = location,
            startTime = startTime,
            endDate = endDate,
            estimatedPrice = 15.50,
            activityType = ActivityType.MUSEUM)

    assertEquals("Visit the Louvre", activity.title)
    assertEquals("Exploring the famous art museum.", activity.description)
    assertEquals(location, activity.location)
    assertEquals(startTime, activity.startTime)
    assertEquals(endDate, activity.endDate)
    assertEquals(15.50, activity.estimatedPrice)
    assertEquals(ActivityType.MUSEUM, activity.activityType)
  }

  @Test
  fun testActivityWithDifferentType() {
    val activity =
        Activity(
            title = "Dinner at Le Jules Verne",
            description = "Fine dining at the Eiffel Tower.",
            location = location,
            startTime = startTime,
            endDate = endDate,
            estimatedPrice = 250,
            activityType = ActivityType.RESTAURANT)

    assertEquals("Dinner at Le Jules Verne", activity.title)
    assertEquals("Fine dining at the Eiffel Tower.", activity.description)
    assertEquals(location, activity.location)
    assertEquals(startTime, activity.startTime)
    assertEquals(endDate, activity.endDate)
    assertEquals(250, activity.estimatedPrice)
    assertEquals(ActivityType.RESTAURANT, activity.activityType)
  }
}
