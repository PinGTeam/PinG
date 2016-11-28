#Implemented by: Juan and Monica and most importantly Harlow
import os
import requestmngr
import unittest
import tempfile
from sqlalchemy import *



#--Testing for Middle-Tier | Iteration One --
#    Tests:
#        1. Database is empty [EmptyDatabaseTestCase]
#            - Tests getallevents and getnearevents
#        2. Database with some data [NonEmptyDatabaseTestCase]
#            - Tests getallevents and getnearevents
#        3. Adding with "adduser" and checking if users have been added [EmptyDatabaseWithAddUserTestCase]
#            - Tests adduser
#        4. Adding with "addevent" and checking if event has been added [EmptyDatabaseWithAddEventTestCase]
#            - Tests adduser, addevent, getallevents and getnearevents


class EmptyDatabaseTestCase(unittest.TestCase):

    def setUp(self):
        requestmngr.app.config['TESTING'] = True
        requestmngr.app.config['SQLALCHEMY_DATABASE_URI'] = 'mysql://juan:alfaro@localhost/test_middle'
        requestmngr.engine = create_engine('mysql://juan:alfaro@localhost/test_middle')
        requestmngr.db.drop_all()
        requestmngr.db.create_all()
        self.app = requestmngr.app.test_client()
        #with requestmngr.app.app_context():

    def tearDown(self):
        requestmngr.db.drop_all()
        requestmngr.db.create_all()

    def test_getnearevent_db(self):
        rv = self.app.get('/getnearevents?topLongitude=12&topLatitude=120&bottomLongitude=12&bottomLatitude=120')
        assert '{"features": [], "type": "FeatureCollection"}' in rv.data


class NonEmptyDatabaseTestCase(unittest.TestCase):

    def setUp(self):
        requestmngr.app.config['TESTING'] = True
        requestmngr.app.config['SQLALCHEMY_DATABASE_URI'] = 'mysql://juan:alfaro@localhost/test_middle'
        requestmngr.engine = create_engine('mysql://juan:alfaro@localhost/test_middle')
        requestmngr.db.drop_all()
        requestmngr.db.create_all()
        requestmngr.db.session.add(requestmngr.users(userName='testname',firstName='testing',lastName='test',email='test@email.com',password='dGhlUGFzc3dvcmQ='))
        requestmngr.db.session.commit()
        requestmngr.db.session.add(requestmngr.eventtable(userID=1,latitude=12,longitude=120,eventName="testevent",startTime="2018-10-10 20:20:20",endTime="2018-10-10 20:20:20",description="testdesc"))
        requestmngr.db.session.commit()
        self.app = requestmngr.app.test_client()
        #with requestmngr.app.app_context():

    def tearDown(self):
        requestmngr.db.drop_all()
        requestmngr.db.create_all()

    def test_getnearevent_db(self):
        rv = self.app.get('/getnearevents?longitude=120&latitude=12')
	assert '{"features": [{"geometry": {"coordinates": [120.0, 12.0], "type": "Point"}, "properties": {"attendance": 0, "attending": 0, "description": "testdesc", "endTime": "2018-10-10 20:20:20", "eventID": 1, "eventName": "testevent", "firstName": "testing", "lastName": "test", "startTime": "2018-10-10 20:20:20", "userID": 1}, "type": "Feature"}], "type": "FeatureCollection"}' in rv.data


    def test_login_success_db(self):
        rv = self.app.post('/login',data=dict(userName="testname",password="dGhlUGFzc3dvcmQ="))
        assert '{"userID":1,"userName":"testname","firstName":"testing","lastName":"test"}' in rv.data

    def test_login_fail_db(self):
        rv = self.app.post('/login',data=dict(userName="testname",password="dGhlUGFzc3dvQ="))
        assert '-1' in rv.data


class EmptyDatabaseWithAddsTestCase(unittest.TestCase):

    def setUp(self):
        requestmngr.app.config['TESTING'] = True
        requestmngr.app.config['SQLALCHEMY_DATABASE_URI'] = 'mysql://juan:alfaro@localhost/test_middle'
        requestmngr.engine = create_engine('mysql://juan:alfaro@localhost/test_middle')
        requestmngr.db.drop_all()
        requestmngr.db.create_all()
        self.app = requestmngr.app.test_client()
        #with requestmngr.app.app_context():

    def tearDown(self):
        requestmngr.db.drop_all()
        requestmngr.db.create_all()

    def test_adduser_db(self):
        rv = self.app.post('/adduser', data=dict(userName='testUserName',firstName='testName',lastName='testLName',password='dGhlcGFzc3dvcmQ=',email='email@asd.com'))
        assert '1' in rv.data

    def test_adduser_same_username_db(self):
        rv = self.app.post('/adduser', data=dict(userName='testUserName',firstName='testName',lastName='testLName',password='dGhlcGFzc3dvcmQ=',email='email@asd.com'))
        rv = self.app.post('/adduser', data=dict(userName='testUserName',firstName='testName',lastName='testLName',password='dGhlcGFzc3dvcmQ=',email='email2@asd.com'))
        assert '-2' in rv.data

    def test_adduser_same_email_db(self):
        rv = self.app.post('/adduser', data=dict(userName='testUserName',firstName='testName',lastName='testLName',password='dGhlcGFzc3dvcmQ=',email='email@asd.com'))
        rv = self.app.post('/adduser', data=dict(userName='testUserName',firstName='testName',lastName='testLName',password='dGhlcGFzc3dvcmQ=',email='email2@asd.com'))
        rv = self.app.post('/adduser', data=dict(userName='testUserName2',firstName='testName',lastName='testLName',password='dGhlcGFzc3dvcmQ=',email='email@asd.com'))
        assert '-1' in rv.data

    def test_adduser_same_email_and_username_db(self):
        rv = self.app.post('/adduser', data=dict(userName='testUserName',firstName='testName',lastName='testLName',password='dGhlcGFzc3dvcmQ=',email='email@asd.com'))
        rv = self.app.post('/adduser', data=dict(userName='testUserName',firstName='testName',lastName='testLName',password='dGhlcGFzc3dvcmQ=',email='email2@asd.com'))
        rv = self.app.post('/adduser', data=dict(userName='testUserName2',firstName='testName',lastName='testLName',password='dGhlcGFzc3dvcmQ=',email='email@asd.com'))
        rv = self.app.post('/adduser', data=dict(userName='testUserName',firstName='testName',lastName='testLName',password='dGhlcGFzc3dvcmQ=',email='email@asd.com'))
        assert '-3' in rv.data

    def test_addevent(self):
        rv = self.app.post('/adduser', data=dict(userName='testUserName',firstName='testName',lastName='testLName',password='dGhlcGFzc3dvcmQ=',email='email@asd.com'))
        rv = self.app.post('/addevent', data=dict(event='{"geometry": {"coordinates": [-3.5123, 175.5], "type": "Point"}, "properties": {"description": "This is a test event","eventName": "Party_at_Juans_House","startTime": "2018-10-08 16:37:00","endTime": "2018-10-08 16:37:00", "userID": 1}, "type": "Feature"}'))
        assert '1' in rv.data

    def test_login_success_db(self):
        rv = self.app.post('/adduser', data=dict(userName='testUserName',firstName='testName',lastName='testLName',password='dGhlcGFzc3dvcmQ=',email='email@asd.com'))
        rv = self.app.post('/login',data=dict(userName="testUserName",password="dGhlcGFzc3dvcmQ="))
        assert '{"userID":1,"userName":"testUserName","firstName":"testName","lastName":"testLName"}' in rv.data

    def test_login_fail_db(self):
        rv = self.app.post('/adduser', data=dict(userName='testUserName',firstName='testName',lastName='testLName',password='dGhlcGFzc3dvcmQ=',email='email@asd.com'))
        rv = self.app.post('/login',data=dict(userName="testUserName",password="dGhlcGFzc3dcmQ="))
        assert '-1' in rv.data

class EmptyAttendanceTable(unittest.TestCase):

    def setUp(self):
	requestmngr.app.config['TESTING'] = True
        requestmngr.app.config['SQLALCHEMY_DATABASE_URI'] = 'mysql://juan:alfaro@localhost/test_middle'
        requestmngr.engine = create_engine('mysql://juan:alfaro@localhost/test_middle')
        requestmngr.db.drop_all()
        requestmngr.db.create_all()
        self.app = requestmngr.app.test_client()

    def tearDown(self):
        requestmngr.db.drop_all()
        requestmngr.db.create_all()

    def test_adduser_db(self):
        rv = self.app.post('/adduser', data=dict(userName='testUserName',firstName='testName',lastName='testLName',password='dGhlcGFzc3dvcmQ=',email='email@asd.com'))
        assert '1' in rv.data

    def test_empty_at(self):
        rv = self.app.post('/adduser', data=dict(userName='testUserName',firstName='testName',lastName='testLName',password='dGhlcGFzc3dvcmQ=',email='email@asd.com'))
        rv = self.app.post('/addevent', data=dict(event='{"geometry": {"coordinates": [-3.5123, 175.5], "type": "Point"}, "properties": {"description": "This is a test event","eventName": "Party_at_Juans_House","startTime": "2018-10-08 16:37:00","endTime": "2018-10-08 16:37:00", "userID": 1}, "type": "Feature"}'))
	rv = self.app.get('/getattendance', data=dict(eventID = 1))
	assert '[]' in rv.data

class EmptyAttendanceTableWithAddTestCase(unittest.TestCase):

    def setUp(self):
        requestmngr.app.config['TESTING'] = True
        requestmngr.app.config['SQLALCHEMY_DATABASE_URI'] = 'mysql://juan:alfaro@localhost/test_middle'
        requestmngr.engine = create_engine('mysql://juan:alfaro@localhost/test_middle')
        requestmngr.db.drop_all()
        requestmngr.db.create_all()
        requestmngr.db.session.add(requestmngr.users(userName='testname',firstName='testing',lastName='test',email='test@email.com',password='dGhlUGFzc3dvcmQ='))
        requestmngr.db.session.commit()
        requestmngr.db.session.add(requestmngr.eventtable(userID=1,latitude=12,longitude=120,eventName="testevent",startTime="2018-10-10 20:20:20",endTime="2018-10-10 20:20:20",description="testdesc"))
        requestmngr.db.session.commit()
        self.app = requestmngr.app.test_client()

    def tearDown(self):
        requestmngr.db.drop_all()
        requestmngr.db.create_all()

    def test_empty_at(self):
        rv = self.app.post('/adduser', data=dict(userName='testUserName',firstName='testName',lastName='testLName',password='dGhlcGFzc3dvcmQ=',email='email@asd.com'))
        rv = self.app.post('/addevent', data=dict(event='{"geometry": {"coordinates": [-3.5123, 175.5], "type": "Point"}, "properties": {"description": "This is a test event","eventName": "Party_at_Juans_House","startTime": "2018-10-08 16:37:00","endTime": "2018-10-08 16:37:00", "userID": 1}, "type": "Feature"}'))
        rv = self.app.get('/getattendance', data=dict(eventID = 1))
        assert '[]' in rv.data

    def test_add_attend(self):
        rv = self.app.post('/adduser', data=dict(userName='testUserName',firstName='testName',lastName='testLName',password='dGhlcGFzc3dvcmQ=',email='email@asd.com'))
        rv = self.app.post('/addevent', data=dict(event='{"geometry": {"coordinates": [-3.5123, 175.5], "type": "Point"}, "properties": {"description": "This is a test event","eventName": "Party_at_Juans_House","startTime": "2018-10-08 16:37:00","endTime": "2018-10-08 16:37:00", "userID": 1}, "type": "Feature"}'))
        rv = self.app.post('/attend', data=dict(userID = 1, eventID = 1))
	assert 'attending' is rv.data

    def test_add_attendee(self):
        rv = self.app.post('/adduser', data=dict(userName='testUserName',firstName='testName',lastName='testLName',password='dGhlcGFzc3dvcmQ=',email='email@asd.com'))
        rv = self.app.post('/addevent', data=dict(event='{"geometry": {"coordinates": [-3.5123, 175.5], "type": "Point"}, "properties": {"description": "This is a test event","eventName": "Party_at_Juans_House","startTime": "2018-10-08 16:37:00","endTime": "2018-10-08 16:37:00", "userID": 1}, "type": "Feature"}'))
        rv = self.app.post('/attend', data=dict(userID = 1, eventID = 1))
        assert 'attending' is rv.data

    def test_toggle_attend(self):
        rv = self.app.post('/adduser', data=dict(userName='testUserName',firstName='testName',lastName='testLName',password='dGhlcGFzc3dvcmQ=',email='email@asd.com'))
        rv = self.app.post('/addevent', data=dict(event='{"geometry": {"coordinates": [-3.5123, 175.5], "type": "Point"}, "properties": {"description": "This is a test event","eventName": "Party_at_Juans_House","startTime": "2018-10-08 16:37:00","endTime": "2018-10-08 16:37:00", "userID": 1}, "type": "Feature"}'))
        rv = self.app.post('/attend', data=dict(userID = 1, eventID = 1))
	rv = self.app.post('/attend', data=dict(userID = 1, eventID = 1))
	assert 'not attending' == rv.data

class EmptyAttendanceTableWithMultipleAdds(unittest.TestCase):

    def setUp(self):
        requestmngr.app.config['TESTING'] = True
        requestmngr.app.config['SQLALCHEMY_DATABASE_URI'] = 'mysql://juan:alfaro@localhost/test_middle'
        requestmngr.engine = create_engine('mysql://juan:alfaro@localhost/test_middle')
        requestmngr.db.drop_all()
        requestmngr.db.create_all()
        requestmngr.db.session.add(requestmngr.users(userName='testname',firstName='testing',lastName='test',email='test@email.com',password='dGhlUGFzc3dvcmQ='))
        requestmngr.db.session.commit()
        requestmngr.db.session.add(requestmngr.eventtable(userID=1,latitude=12,longitude=120,eventName="testevent",startTime="2018-10-10 20:20:20",endTime="2018-10-10 20:20:20",description="testdesc"))
        requestmngr.db.session.commit()
        self.app = requestmngr.app.test_client()

    def tearDown(self):
        requestmngr.db.drop_all()
        requestmngr.db.create_all()

    def test_multiple_adds(self):
	rv = self.app.post('/adduser', data=dict(userName='testUserName',firstName='testName',lastName='testLName',password='dGhlcGFzc3dvcmQ=',email='email1@asd.com'))
	rv = self.app.post('/addevent', data=dict(event='{"geometry": {"coordinates": [-3.5123, 175.5], "type": "Point"}, "properties": {"description": "This is a test event","eventName": "Party_at_Juans_House","startTime": "2018-10-08 16:37:00","endTime": "2018-10-08 16:37:00", "userID": 1}, "type": "Feature"}'))
	rv = self.app.post('/adduser', data=dict(userName='boogers1',firstName='testName',lastName='testLName',password='dGhlcGFzc3dvcmQ=',email='email2@asd.com'))
	rv = self.app.post('/adduser', data=dict(userName='boogers2',firstName='testName',lastName='testLName',password='dGhlcGFzc3dvcmQ=',email='email3@asd.com'))
	rv = self.app.post('/adduser', data=dict(userName='lingering fart smell',firstName='testName',lastName='testLName',password='dGhlcGFzc3dvcmQ=',email='email4@asd.com'))
	rv = self.app.post('/attend', data=dict(userID = 4, eventID = 1))
	rv = self.app.post('/attend', data=dict(userID = 3, eventID = 1))
	rv = self.app.post('/attend', data=dict(userID = 2, eventID = 1))
	rv = self.app.get('/getattendance', data=dict(eventID = 1))
	results = requestmngr.attendancetable.query.all()

	for row in attendancetable.query.all()
	    print(row.userID, row.eventID)

	assert '1' in rv.data

if __name__ == '__main__':
    unittest.main()
