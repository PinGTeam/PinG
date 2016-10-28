# Implemented by: Juan and Monica
import geojson
import json
from geojson import Feature, Point, FeatureCollection
from flask import Flask, url_for, request
from flask_sqlalchemy import SQLAlchemy
from sqlalchemy import *
from sqlalchemy.orm import *
from datetime import datetime

#need run flask with following command
#flask run --host=0.0.0.0
#need to access http://162.243.15.139:5000/ to get response

app = Flask(__name__)
app.config['SQLALCHEMY_DATABASE_URI'] = 'mysql://juan:alfaro@localhost/ping'
engine = create_engine('mysql://juan:alfaro@localhost/ping')
app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False
db = SQLAlchemy(app)


#NOT INCLUDED IN ITERATION - FUNCTION MADE FOR TESTING - MARKED FOR DELETION

#LIST OF USERS
#Quering list of users from the database
@app.route('/')
def getusers():
    str = ""
    results = users.query.all()
    for ev in results:
      str = str + ev.userName + " ,"
    if str == "":
        return "NoUsers"
    else:
        return str


#ADDING USER
#Calling InsertUser Stored procedure
#Using POST method
@app.route('/adduser',methods=['POST'])
def insertuser():
    connection = engine.raw_connection()
    cursor = connection.cursor()
    username = request.form['UserName']
    name = request.form['FName']
    lname = request.form['LName']
    password = request.form['Password']
    email = request.form['Email']
    cursor.callproc("InsertUser", [username,password,name,lname,email])
    results = list(cursor.fetchall())
    cursor.close()
    connection.commit()
    connection.close()
    return str(results[0][0])

#ADDING EVENT
#Calling InsertEvent Stored procedure
#Using POST method
#CALL InsertEvent(userID <int[25]>, EventName <string[255]>, Latitude <double>, Longitude <double>, Date/Time <year-month-day hour:min:second>, Description <string[1024]>);
@app.route('/addevent',methods=['POST'])
def insertevent():
    connection = engine.raw_connection()
    cursor = connection.cursor()
    location = request.form['Event']
    locfeat = json.loads(location)
    cursor.callproc("InsertEvent", [locfeat['properties']['userID'],locfeat['properties']['eventName'],locfeat['geometry']['coordinates'][0],locfeat['geometry']['coordinates'][1],locfeat['properties']['endTime'],locfeat['properties']['startTime'],locfeat['properties']['description']])
    results = list(cursor.fetchall())
    cursor.close()
    connection.commit()
    connection.close()
    return "hello"

#GET EVENTS
#Quering list of events from the database
@app.route('/getallevents')
def getevents():
    events = eventtable.query.all()
    event_list = []
    for event in events:
        event_list.append(event_to_geojson(event.latitude,event.longitude,event.userID,event.eventName,str(event.startTime),str(event.endTime),event.description))
    return geojson.dumps(FeatureCollection(event_list),sort_keys=True)

#GET EVENTS
#Calling GetEvents Stored procedure
@app.route('/getnearevents',methods=['GET'])
def getnearevents():
    connection = engine.raw_connection()
    cursor = connection.cursor()
    cursor.callproc("GetEvents", [request.args.get('topLatitude'),request.args.get('topLongitude'),request.args.get('bottomLatitude'),request.args.get('bottomLongitude')])
    results = list(cursor.fetchall())
    cursor.close()
    connection.commit()
    connection.close()
    event_list = []
    for event in results:
        #THIS MIGHT HAVE TO BE FIXED TO TAKE INTO ACCOUNT NEW EVENTTABLE TABLE
        event_list.append(event_to_geojson(event[4],event[3],event[0],event[5],str(event[6]),str(event[7]),event[8]))
    return geojson.dumps(FeatureCollection(event_list),sort_keys=True)

#HELPER FUNCTIONS

#takes an info to make a json feature that represents an event
def event_to_geojson(x,y,userID,eventName,startTime,endTime,description):
    return Feature(geometry=Point((x,y)),properties={"userID":userID,"eventName":eventName,"startTime":startTime,"endTime":endTime,"description":description})

#TABLES
#Users table
class users(db.Model):
    __tablename__ = 'users'
    userID = db.Column('userID',db.BigInteger, primary_key=True, autoincrement=True)
    userName = db.Column('userName',db.String(20), nullable=False)
    password = db.Column('password',db.String(60), nullable=False)
    firstName = db.Column('firstName',db.String(25), nullable=False)
    lastName = db.Column('lastName',db.String(25), nullable=False)
    email = db.Column('email',db.String(320), nullable=False)

    def __repr__(self):
        return '<User {}>'.format(self.userID)

#Events table
class eventtable(db.Model):
    __tablename__ = 'eventtable'
    eventID = db.Column('eventID',db.BigInteger,primary_key=True, autoincrement=True)
    userID = db.Column('userID',db.BigInteger,ForeignKey("users.userID",ondelete="CASCADE"),nullable=False)
    latitude = db.Column('latitude',db.Float,nullable=False)
    longitude = db.Column('longitude',db.Float,nullable=False)
    eventName = db.Column('eventName',db.String(255),nullable=False)
    startTime = db.Column('startTime',db.DateTime,nullable=False)
    endTime = db.Column('endTime',db.DateTime,nullable=False)
    description = db.Column('description',db.String(1024),nullable=True)

    def __repr__(self):
        return '<User {}>'.format(self.userID)

#Run App
if __name__ == "__main__":
    app.run(host='0.0.0.0')
