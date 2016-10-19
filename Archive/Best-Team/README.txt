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

Mobile iOS Application:


