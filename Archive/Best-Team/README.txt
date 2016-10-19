# PinG Milestone3 README.txt
----------------------------

# Concrete instuctions on how to compile:
-----------------------------------------
Mobile Android Application:
- Download the Pingv2 folder from source code android section.
- Once downloaded, open Android Studio.
- Once open, click File->Open project.
- Open the Pingv2 folder and select the Android Studio file.
- Once project is selected, wait for all files to synch.
- Code is now ready.


Mobile iOS Application:
* 


# Concrete instructions on how to run our code:
-----------------------------------------------
Mobile Android Application:
- NOTE: If using phone to test, go to applications on phone and manually grant Pingv2 access to location.
- Once all the files sync and load, plug in an android phone or create an emulator to run the application.
- App is now ready for use.

Mobile iOS Application:
* 


# Concrete commands on how to run the unit test cases:
------------------------------------------------------
- MySQL Unit Tests:
  * The file unittest.sql contains all SQL tests for the back-end database.
    However, if you would like to run the test cases proceed to the following instructions:
    
  If you use Windows:
     A portable putty.exe is included. Use it to SSH to our Ubuntu server with the following criteria:
     IP: 162.243.15.139
     username: test
     password: test
     port: 22
 * If you use Linux / MAC OS:
     ssh to IP: 162.243.15.139
     username: test
     password: test
     port: 22
     
     You can type via command line: ` ssh test@162.243.15.139 ` then enter password: test)
     
     Once logged into test@162.243.15.139, type:
     make              <- runs sql tests
       
       
- Flask Unit Tests:

       
- Android Unit Tests:


- iOS Unit Tests:


# And acceptance tests for an external person to try (i.e., what inputs to use and what outputs are expected).
--------------------------------------------------------------------------------------------------------------
Mobile Android Application:
- When the application icon on the user's phone is clicked, the application will load and display a user login screen.
- The login screen shows the application name at the top center(with current version number attached to name), and 3 test
  text fileds that prompt the user for a userID, first name, and last name. Under each of the text fields are 3 input fields
  that allow a user to input their data.
- When the user clicks logIn, all their log in data is sent to the database and they are assigned a userID:
  If a user enters nothing for the fields, an empty user will be added to the database, but will still assign it a userID.
  If a user enters a custom user id, that user id will be added to the database and will be assigned a userID.
  If a user enters an already existing user id, they will be assigned the userID of that existing user.
- Once the user clicks the LogIn button, the view changes from the logIn screen to the map screen.
- The map will load if the user followed the correct set up stages found at the top of this document and the view will
  center to the user current location.
- The view will be locked to the user and no zooming is allowed. Rotation and titlting is available.
- At the top left of the screen there is a compas button that will reset the tilt and rotation of the camera when clicked.
- At the top right of the screen there is a go to current location button that returns the view to the user current location.
- The map will refresh and return to user current location every 10 seconds or on location changed.
- On the bottom right of the screen there is the ping button that when clicked brings up the Event Information Screen.
- In the Event Information screen, the user is presented with 4 text fields and 4 input fields:
  - The user can fill this out as they so wish
  - If Time of Event is not given in the following format: YYYY-DD-MM HH:MM:SS
    The database will save the time is None, otherwise it will store correctly.
- Once all the information is filled out, the user may click CANCEL to avoid placing a ping at current location, or
  click SUBMIT to place a ping at their current location:
  - NOTE: If any previous pings were placed on the map by current user, then clicking SUBMIT will replace the old ping with
    the new ping.
- When the ping is placed, its information is sent to the database for storage.
- Once a ping has been placed on the map, a user can click on the ping to see detailed information about the ping.

Mobile iOS Application:


