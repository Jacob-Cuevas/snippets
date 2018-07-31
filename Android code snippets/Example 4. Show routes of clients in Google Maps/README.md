In this class, the objective is show all the clients of the seller in two ways:<br>
- A sliding list where each element contain information about the client like his name, client code, store address and phone number.<br>
- A map in Google Maps where the seller can see all his clients that need to visit in all day, with customs makers that show the same information of each client(name, code, address, phone number)
and his position on map using the client's geolocation (latitude and longitude). At the same time, the seller can draw the best route to browse all the clients in map, either selecting one or more clients at the same time.<br>
It´s important to mention that it had to be considered:<br>
- The use of GPS in the Android device and the system permissions related to it.<br>
- Get all clients from SQLite Database in the same device.<br>
- Configure and use the Google SDK to integrate Google Maps inside the application. Creating the proyect inside Google Developers Console and configuring the project to get an API KEY valid to use.<br>
- Create a custom map with a specific configuration, such as predetermined height and width, a counter to control the zoom of the map and other stufs and draw a custom view to show the map.<br>
- Overwrite both the events and the display of the markers on the map to show all information of each client from database, changing the icon of the marker, changing the onClick event of the marker to draw a line between the position of client and the seller's current position considering the distance and traffic.
