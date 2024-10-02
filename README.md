# Voyageur

### Description:
Travelers often struggle with organizing all their ideas and options when planning a trip, from restaurants and activities to detailed itineraries. The process becomes more complex with multiple destinations, budgeting concerns, and the need to coordinate with others. Voyageur is an all-in-one travel planner that allows users to store, categorize, and organize travel ideas (restaurants, museums, activities) and convert them into detailed, hour-by-hour itineraries. By leveraging Google Maps and LLM APIs, users can find places based on price, location, and preferences and even generate complete day-trip schedules. Multi-user collaboration enables users to invite friends or family to contribute and share trips.

### Target Audience:
- John, a 25 year-old who travels with a big group of friends and wants to have a way for them to collaborate easily on the travel itinerary.
- Lena, a 29-year-old food enthusiast who is keen on finding the best-rated restaurants in the cities she visits and would like to incorporate them into an itinerary to avoid sorting through a long list of saved spots in Google Maps.
- James, a 40-year-old business traveler looking to make the most of limited free time during work trips and needs a fast way to get a clear hour-by-hour schedule.

### Multi-user support

- Google authenticator for secure authentication during login.
- Users can create multiple trips, invite others to collaborate, and assign different permissions (view-only or editing rights).
- Role-based access control (RBAC) ensures that only authorized users can edit or view trip details.
- Real-time sync ensures seamless collaboration and updates when multiple users work on the same trip.

### Sensor use
GPS is used to find nearby restaurants, museums, and activities based on the user's current location.

### Ofline mode
- Users can save travel ideas (restaurants, museums, activities) offline and still access them without network connectivity.
- Previously planned schedules or itineraries can be downloaded and viewed offline, allowing for uninterrupted access even in areas with poor connectivity.
- A “Connected” vs. “Offline” status indicator informs users of their current connectivity, prompting a refresh when back online.
- While offline, users will see prompts or buttons to retry certain actions that depend on live API calls, like finding new locations or generating updated itineraries.
