# TravelMates_PAMN
 PAMN 24/25

Notes:
_Current location update is happening only in the moment when logging/registration happens. If the location changes you have to log out and log in again;
_"currentLocation" variable deleted because "location" variable makes it useless ("location" is updated automatically
 and is not modified by the user);
_In the homepage we could just show user position in a map without the necessity to have the exact city written somewhere;
_We have to write the presentation document for the app;
_Profile pictures of ex users remain in the storage;
_I created some test users (u can see them in firestore + the password of each user is name of the user repeated 2 times):
    -Nicolò, location: Gran Canaria
    -Yannick, location: Amsterdam
    -Ludovica, location: Gran Canaria
    -Katja, location: Valencia
    -Alexandra, location: Amsterdam
    -Agata, location: Valencia
    -Ricardo, location: Gran Canaria

To do:
_PeopleInTown is not showing anything for people in canary island? Check with emulator if with other people in other places is still working. I noticed that
 when I use my smartphone it updates the location correctly in database (puts gran canaria coordinates) but if there is another user in gran canaria he's not
 shown.