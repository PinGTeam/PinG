# PinG Milestone3 README.txt
----------------------------

# Concrete instuctions on how to compile:
-----------------------------------------
Back-End MySQL Database:
* Does not require compilation.
* If you wish to duplicate the state of the database on an external MySQL DB, a mysql dump is included in the archive.

Middle-Tier Flask Application:
* Does not require compilation.

Mobile Android Application:
* 

Mobile iOS Application:
* 


# Concrete instructions on how to run our code:
-----------------------------------------------
Mobile Android Application:
* 

Mobile iOS Application:
* 


# Concrete commands on how to run the unit test cases:
------------------------------------------------------
- MySQL Unit Tests and Flask Unit Tests:
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
     make              <- runs sql and flask tests
     make sqltests      <- runs sql tests
     make flasktests    <- runs flask tests
       
- Android Unit Tests:


- iOS Unit Tests:


# And acceptance tests for an external person to try (i.e., what inputs to use and what outputs are expected).
--------------------------------------------------------------------------------------------------------------
Mobile Android Application:

Mobile iOS Application:


