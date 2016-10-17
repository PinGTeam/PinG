import os
import requestmngr
import unittest
import tempfile
from sqlalchemy import *


'''
--Testing for Middle-Tier | Iteration One --
    Tests:
        1. Database is empty
        2. Database with some data
        3.

'''


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

    def test_getusers_db(self):
        rv = self.app.get('/')
        assert 'NoUsers' in rv.data

    def test_getallevents_db(self):
        rv = self.app.get('/getallevents')
        assert '{"features": [], "type": "FeatureCollection"}' in rv.data

class NonEmptyDatabaseTestCase(unittest.TestCase):

    def setUp(self):
        requestmngr.app.config['TESTING'] = True
        requestmngr.app.config['SQLALCHEMY_DATABASE_URI'] = 'mysql://juan:alfaro@localhost/test_middle'
        requestmngr.engine = create_engine('mysql://juan:alfaro@localhost/test_middle')
        requestmngr.db.drop_all()
        requestmngr.db.create_all()
        requestmngr.db.session.add(requestmngr.users(userName='testname',firstName='testing',lastName='test'))
        requestmngr.db.session.commit()
        requestmngr.db.session.add(requestmngr.eventtable(userID=1,latitude=12,longitude=120,eventName="testevent",time="2016-10-10 20:20:20",description="testdesc"))
        requestmngr.db.session.commit()
        self.app = requestmngr.app.test_client()
        #with requestmngr.app.app_context():

    def tearDown(self):
        requestmngr.db.drop_all()
        requestmngr.db.create_all()

    def test_getusers_db(self):
        rv = self.app.get('/')
        assert 'testname' in rv.data

    def test_getallevent_db(self):
        rv = self.app.get('/getallevents')
        assert 'testevent' in rv.data

    def test_getnearevent_db(self):
        rv = self.app.get('/getnearevents?topLongitude=12&topLatitude=120&bottomLongitude=12&bottomLatitude=120')
        assert 'testevent' in rv.data


if __name__ == '__main__':
    unittest.main()
