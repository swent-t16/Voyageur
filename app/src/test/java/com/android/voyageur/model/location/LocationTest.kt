import com.android.voyageur.model.location.Location
import org.junit.Assert.assertEquals
import org.junit.Test

class LocationTest {

  @Test
  fun testLocationCreationWithAllFields() {
    val location =
        Location(id = "1", name = "Big Ben Cafe", address = "London street", lat = 12.5, lng = 10.8)

    assertEquals("1", location.id)
    assertEquals("Big Ben Cafe", location.name)
    assertEquals("London street", location.address)
    assertEquals(12.5, location.lat, 0.10)
    assertEquals(10.8, location.lng, 0.10)
  }
}
