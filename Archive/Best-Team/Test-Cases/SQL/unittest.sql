-- Coded by Richard Hamm and Zach Sirotto

-- The following unit tests test out the stored procedures:
-- InsertUser();
-- InsertEvent();
-- GetEvents();


-- Establishes TestUser variables (sets up the state for testing)
SET @UserName = concat('TestUser', TIME_TO_SEC(current_time()));
SET @Firstname = 'Test';
SET @LastName = 'User';

-- Start a transaction.
BEGIN;

-- Plans # of tests 
SELECT tap.plan( 4 );

SELECT tap.has_table('ping', 'users');
SELECT tap.has_table('test', 'users');

-- InsertUser and suppress returned UserID
CALL InsertUser_NoReturn(@UserName, @FirstName, @LastName);

-- Retrieve userid of test user
SET @userid = (SELECT userid from users where userName = @UserName and firstName = @FirstName and lastName = @LastName);

-- Tests if user is inserted
SELECT tap.ok(exists(SELECT * from users where userName = @UserName and firstName = @FirstName and lastName = @LastName),'Tests if user is inserted.');

-- Attempts to insert user with the same username
CALL InsertUser_NoReturn(@UserName, @FirstName, @LastName);
-- Retrieve number of users to determine uniqueness amongst usernames.
SET @userCount = (select Count(userid) from users where username = @UserName and FirstName = @FirstName and LastName = @LastName);

-- Tests that user is not duplicated upon additional insertions
SELECT tap.eq(@userCount, 1, 'Tests uniqueness amongst usernames.');

-- Display test has been completed 
SELECT '\n+--------------------------+';
SELECT '|SQL Unit Test 1 Completed!|';
SELECT '+--------------------------+';

-- Finish the tests and clean up.
CALL tap.finish();
SELECT '';

-- Run unit test 2 
SELECT 'Testing InsertEvent()';
SELECT '---------------------';

-- Setup parameters for event insertion
SET @EventName = 'TestEvent' + day(current_date()) + minute(current_time());
SET @EVENTDesc = 'Test Desc' + day(current_date()) + minute(current_time());
SET @pointlong = RAND(3) * -180;
SET @pointlat = RAND(3) * -90;

-- Plans # of tests 
SELECT tap.plan( 4 );

SELECT tap.has_table('ping', 'eventtable');
SELECT tap.has_table('test', 'eventtable');

-- INSERT EVENT AS TESTUSER
CALL InsertEvent(@userid, @EventName, @pointlat, @pointlong, SYSDATE(), @EVENTDesc); 

-- Test if event is successfully inserted into the database.
SELECT tap.ok(exists(SELECT * from eventtable where eventName = @EventName and latitude = @pointlat and longitude = @pointlong),'Tests event insertion.');

SELECT tap.pass('Creating 10 test events in a given radius.');

-- Insert 10 events in 10 different locations
call insertevent(@userid, 'Test Event 1', 5.04, 143.28, '2016-10-15 20:00', 'Test Desc');
call insertevent(@userid, 'Test Event 2', 6.09, 142.28, '2016-10-15 20:00', 'Test Desc');
call insertevent(@userid, 'Test Event 3', -2.05, 149.06, '2016-10-15 20:00', 'Test Desc');
call insertevent(@userid, 'Test Event 4', 9.99, 129.09, '2016-10-15 20:00', 'Test Desc');
call insertevent(@userid, 'Test Event 5', 32.02, 150.09, '2016-10-15 20:00', 'Test Desc');
call insertevent(@userid, 'Test Event 6', 29.00, 141.12, '2016-10-15 20:00', 'Test Desc');
call insertevent(@userid, 'Test Event 7', 25.23, 145.09, '2016-10-15 20:00', 'Test Desc');
call insertevent(@userid, 'Test Event 8', 20.45, 150.98, '2016-10-15 20:00', 'Test Desc');
call insertevent(@userid, 'Test Event 9', 15.25, 155.67, '2016-10-15 20:00', 'Test Desc');
call insertevent(@userid, 'Test Event 10', 10.54, 159.99, '2016-10-15 20:00', 'Test Desc');


-- Display test has been completed 
SELECT '\n+--------------------------+';
SELECT '|SQL Unit Test 2 Completed!|';
SELECT '+--------------------------+';


-- Finish the tests and clean up.
CALL tap.finish();

-- Run unit test 3 - 
SELECT '\nTesting GetEvents()';
SELECT '-------------------';
SELECT 'The Following 10 events were inserted by the test user:';
SELECT ' ';
SELECT 'Test Event 1, Lat: 5.04, Long: 143.28, 2016-10-15 20:00, Test Desc';
SELECT 'Test Event 2, Lat: 6.09, Long: 142.28, 2016-10-15 20:00, Test Desc';
SELECT 'Test Event 3, Lat: -2.05, Long: 149.06, 2016-10-15 20:00, Test Desc';
SELECT 'Test Event 4, Lat: 9.99, Long: 129.09, 2016-10-15 20:00, Test Desc';
SELECT 'Test Event 5, Lat: 32.02, Long: 150.09, 2016-10-15 20:00, Test Desc';
SELECT 'Test Event 6, Lat: 29.00, Long: 141.12, 2016-10-15 20:00, Test Desc';
SELECT 'Test Event 7, Lat: 25.23, Long: 145.09, 2016-10-15 20:00, Test Desc';
SELECT 'Test Event 8, Lat: 20.45, Long: 150.98, 2016-10-15 20:00, Test Desc';
SELECT 'Test Event 9, Lat: 15.25, Long: 155.67, 2016-10-15 20:00, Test Desc';
SELECT 'Test Event 10, Lat: 10.54, Long: 159.99, 2016-10-15 20:00, Test Desc';

-- Calling GetEvents() should return test events 6-10
SELECT '\nCalling GetEvents() with (30,140) as top left point and (10,160) as bottom right point.\n';
SELECT 'Should return Test Events 6-10 in the format:\n<eventID> <firstName> <lastName> <latitude> <longitude> <event name> <date/time> <event desc>\n';

-- Call to GetEvents(); returns the list of events in coordinate plane
CALL GetEvents(30,140,10,160);

-- Display test has been completed 
SELECT '\n+--------------------------+';
SELECT '|SQL Unit Test 3 Completed!|';
SELECT '+--------------------------+\n';


-- Finish the tests and clean up.
-- Clean up all events inserted.
DELETE from eventtable WHERE userID = @userid; 
ROLLBACK;
