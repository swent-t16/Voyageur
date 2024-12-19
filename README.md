# Voyageur

## Non Technnical Summary:

#### Description:

Travelers often struggle with organizing all their ideas and options when planning a trip, from restaurants and activities to detailed itineraries. The process becomes more complex with multiple destinations, budgeting concerns, and the need to coordinate with others. Voyageur is an all-in-one travel planner that allows users to store, categorize, and organize travel ideas (restaurants, museums, activities) and convert them into detailed, hour-by-hour itineraries. By leveraging Google Maps and LLM APIs, users can find places based on price, location, and preferences and even generate complete day-trip schedules. Multi-user collaboration enables users to invite friends or family to contribute and share trips.

#### Target Audience:

- John, a 25 year-old who travels with a big group of friends and wants to have a way for them to collaborate easily on the travel itinerary.
- Lena, a 29-year-old food enthusiast who is keen on finding the best-rated restaurants in the cities she visits and would like to incorporate them into an itinerary to avoid sorting through a long list of saved spots in Google Maps.
- James, a 40-year-old business traveler looking to make the most of limited free time during work trips and needs a fast way to get a clear hour-by-hour schedule.

#### Multi-user support

- Google authenticator for secure authentication during login.
- Users can create multiple trips, invite others to collaborate
- Real-time sync ensures seamless collaboration and updates when multiple users work on the same trip.
- Participants can edit trip details / leave or delete the trip. They can also add photos that are shared with each other

#### Social Aspect

- Users can decide to make the trip public, so other people can discover it via their feed page.
- Friend requests and trip invites ensure that the users are not added to trips by bad actors / spammers.

#### Other features

- Users can search for their acquaintances and add them as "friends"
- They can also search for places and activities and view relevant information such as opening hours, address and photos
- They can add interests in the profile page that will be taken into account by the AI assistant when generating trip and activity suggestions

#### Sensor use

- GPS is used to find nearby restaurants, museums, and activities based on the user's current location.
- The camera is used to change the trip's thumbnail, change profile picture and add shared pictures.
-

#### Offline mode

- Previously planned schedules are still available offline, allowing for uninterrupted access even in areas with poor connectivity.
- The search for users and the feed still work even without connexion, though a bit slower
- When the user goes offline a red banner will be displayed until they reconnect and certain functions are disabled
- Photos for users and trips are cached so available offline.

## Technical Summary:

#### Tech Stack:

- Frontent: JetpackCompose
- Backend: Firestore
- Auth: Firebase Auth
- Sotrage: Firebase Cloud Storage
- Other APIs: Google Maps and Places API (for location search and displaying maps), Gemini (for JSON schema generation)
- CI/CD: Github Actions, SonarCloud (for code quality)

#### Architecture:

Architrecure type: MVVM; Diagram visible [here](https://github.com/swent-t16/Voyageur/blob/main/resources/architecture_diagram.png)

##### View Models:

- UserViewModel:

  - ViewModel responsible for managing user data and interactions with Firebase authentication and user repository.
  - It provides functionality to load, update, and observe user data, and to handle user-related actions such as signing in / out, adding/removing contacts, and searching users and listening for real time friend request updates.

- TripViewModel:

  - ViewModel for managing trip-related data and operations in the app.

  - This ViewModel interacts with the `TripRepository` to fetch, create, update, and delete trips.
  - Handles the management of trip-related states such as the selected trip, selected day, activities, and UI state.
  - It's responsible for handling file uploads to Firebase Storage and integrating with the AI assistant to generate trip activities.

- PlacesViewModel:
  - ViewModel for managing places-related data and operations in the app.
  - This ViewModel interacts with the `PlacesRepository` to fetch places, search for places, and get place details.
  - It handles the management of place-related states such as the selected place, search results, and UI state.

##### Repositories:

- Interfaces:

  - `UserRepository`: Interface for managing user (basic CRUD operations) interactions with a data source.
  - `TripRepository`: Interface for managing trip (basic CRUD operations) interactions with a data source.
  - `PlacesRepository`: Interface for managing places-related interactions with a location (map-based) data source.
  - `FriendRequestRepository` and `TripInviteRepository`: used managing friend requests and trip invites.

- Repositories (implementing the interfaces for Firestore and Google Maps API use cases):
  - `UserRepositoryFirebase`
  - `TripRepositoryFirebase`
  - `GooglePlacesRepository`
  - `FriendRequestRepositoryFirebase` and `TripInviteRepositoryFirebase`

##### Screens:

Figma design visible [here](https://www.figma.com/design/TZ4qU0PNmMv3T6EpesSrL9/Voyageur?node-id=79-2&p=f) includes these main screens / composables:

- `SignIn` screen: Allows users to sign in using Google (Firebase) authentication without the need to enter any additional details.
- `Overview` screen: Displays a list of trips and allows users, a search bar to filter for trips, and a floating action button to create a new trip. It is also possible to add trips to favourites.
- `Profile` screen: Displays user information, allows users to edit their profile, and view, accept or reject their friend requests and trips invites. Also allows users to manage their interests that may be used by the AI assistant.
- `AddTrip` and `Settings` (for a trip) screens: Allow users to create/edit a trip and configure settings (participants).
- `Search` screen: Allows users to search for other users and places based on location. It also displays a feed of public trips.
- Trip screens:
  - `WeeklyView` / `ByDay` screens: Display the trip itinerary in a weekly view or in a certain day, allowing users to view and edit activities. It also allows users to generate a schedule for a day using the AI assitant.
  - `ActivitiesForOneDay` screen: Displays a list of activities for a day, allowing users to view and edit details.
  - `Assistant` screen: Displays a list of suggested activities for a day, allowing users to view and add them to the trip as either drafts (missing date) or complete (with suggested dates).
  - `Map` screen: Displays a map with markers for places and activities, allowing users to view details in a trip.
  - `Phtos` screen: Displays photos shared by participants in a trip.
  - `Activities` screen: Displays a list of activities in a trip, allowing users to view and edit details.
  - `AddActivity` screen: Allows users to add a new activity to a trip, search for places, and view place details.

##### APK and releases

The apk and release for the M1 can be found [here](https://github.com/swent-t16/Voyageur/releases/tag/M1-Release)
The apk and release for the M2 can be found [here](https://github.com/swent-t16/Voyageur/releases/tag/M2-RELEASE)
The apk and release for the M3 can be found [here](https://github.com/swent-t16/Voyageur/releases/tag/M3Release)
