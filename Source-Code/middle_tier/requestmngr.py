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
app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False
#For calling MySQL Stored Procedures
engine = create_engine('mysql://juan:alfaro@localhost/ping')
db = SQLAlchemy(app)


#HOME
@app.route('/')
def getusers():
    str = "Hello! This is PinG."
    return str

#LOGIN
#Using POST method
@app.route('/login',methods=['POST'])
def login():
    #Getting login information from client
    username = request.form['userName']
    password = request.form['password']
    #Querying the database to check if username-password combination exists
    results = users.query.filter_by(userName = username,password = password).first()
    #Return -1 if the user-password combination is not in the database
    #Return user json object otherwise
    if not results:
        return "-1"
    else:
        return str(results)

#ADDING USER
#Calling InsertUser Stored procedure
#Using POST method
@app.route('/adduser',methods=['POST'])
def insertuser():
    #Using raw_connection to call MySQL Stored Procedures
    connection = engine.raw_connection()
    cursor = connection.cursor()
    #Getting user info from the client
    username = request.form['userName']
    name = request.form['firstName']
    lname = request.form['lastName']
    password = request.form['password']
    email = request.form['email']
    #Calling InsertUser Stored Procedure
    cursor.callproc("InsertUser", [username,password,name,lname,email])
    #Fetching return value from Stored Procedure and converting it into a list
    results = list(cursor.fetchall())
    cursor.close()
    connection.commit()
    connection.close()
    #Return -1 -- if Email exists in database
    #Return -2 -- if UserName exists in database
    #Return -3 -- if both Email and UserName already exist in database
    #Return  1 -- if Success
    return str(results[0][0])

#ADDING EVENT
#Calling InsertEvent Stored procedure
#Using POST method
@app.route('/addevent',methods=['POST'])
def insertevent():
    #Using raw_connection to call MySQL Stored Procedures
    connection = engine.raw_connection()
    cursor = connection.cursor()
    #Getting event geojson object from client
    event = request.form['event']
    locfeat = json.loads(event)
    #Calling InsertEvent Stored Procedure. Getting parameters from geojson object
    cursor.callproc("InsertEvent",\
    [locfeat['properties']['userID'],\
    locfeat['properties']['eventName'],\
    locfeat['geometry']['coordinates'][0],\
    locfeat['geometry']['coordinates'][1],\
    locfeat['properties']['startTime'],\
    locfeat['properties']['endTime'],\
    locfeat['properties']['description']])
    #Fetching return value from Stored Procedure and converting it into a list
    results = list(cursor.fetchall())
    cursor.close()
    connection.commit()
    connection.close()

    #Inserting attendance record of event creator into attendance table
    new_attendance =  attendancetable(eventID = results[0][0],userID = locfeat['properties']['userID'])
    db.session.add(new_attendance)
    db.session.commit()

    return "1"

#ADDING EVENT ALT
#Alternative version for Android client. Returns regular json instead of geojson.
#Calling InsertEvent Stored procedure
#Using POST method
@app.route('/addevent_alt',methods=['POST'])
def insertevent_alt():
    #Using raw_connection to call MySQL Stored Procedures
    connection = engine.raw_connection()
    cursor = connection.cursor()
    #Getting event json object from client
    event = request.form['event']
    locfeat = json.loads(event)
    #Calling InsertEvent Stored Procedure. Getting parameters from json object
    cursor.callproc("InsertEvent",\
    [locfeat['userID'],\
    locfeat['eventName'],\
    locfeat['latitude'],\
    locfeat['longitude'],\
    locfeat['startTime'],\
    locfeat['endTime'],\
    locfeat['description']])
    #Fetching return value from Stored Procedure and converting it into a list
    results = list(cursor.fetchall())
    cursor.close()
    connection.commit()
    connection.close()

    #Inserting attendance record of event creator into attendance table
    new_attendance = attendancetable(eventID = results[0][0],userID = locfeat['userID'])
    db.session.add(new_attendance)
    db.session.commit()

    return "1"

#EDITING EVENT
#Using SQLAlchemy
#Using POST method
@app.route('/editevent',methods=['POST'])
def editevent():
    #Getting event geojson object from client
    event_geojson = request.form['event']
    locfeat = json.loads(event_geojson)
    #Querying event by eventID to edit information
    event = eventtable.query.filter_by(eventID = locfeat['properties']['eventID']).first()
    #Editing the event with the info from the inputed geojson event object
    event.latitude = locfeat['geometry']['coordinates'][0]
    event.longitude = locfeat['geometry']['coordinates'][1]
    event.eventName = locfeat['properties']['eventName']
    event.startTime = locfeat['properties']['startTime']
    event.endTime = locfeat['properties']['endTime']
    event.description = locfeat['properties']['description']
    db.session.commit()

    return "1"

#EDITING EVENT ALT
#Alternative version for Android client. Returns regular json instead of geojson.
#Using SQLAlchemy
#Using POST method
@app.route('/editevent_alt',methods=['POST'])
def editevent_alt():
    #Getting event json object from client
    event_geojson = request.form['event']
    locfeat = json.loads(event_geojson)
    #Querying event by eventID to edit information
    event = eventtable.query.filter_by(eventID = locfeat['eventID']).first()
    #Editing the event with the info from the inputed json event object
    event.latitude = locfeat['latitude']
    event.longitude = locfeat['longitude']
    event.eventName = locfeat['eventName']
    event.startTime = locfeat['startTime']
    event.endTime = locfeat['endTime']
    event.description = locfeat['description']
    db.session.commit()

    return "1"


#ATTENDING EVENT
#Using SQLAlchemy
#Using POST method
@app.route('/attend',methods=['POST'])
def attendevent():
    #Getting eventID and userID from the client
    userID = request.form['userID']
    eventID = request.form['eventID']
    #Querying the database to check if the user if currently attending the event
    attending = attendancetable.query.filter_by(eventID = eventID,userID = userID).first()
    #If the user is not attending then an attendance record will be added to the database
    #Return atteding to signal that the user is now attending
    if not attending:
        new_attendance =  attendancetable(eventID = eventID,userID = userID)
        db.session.add(new_attendance)
        db.session.commit()
        return "attending"
    #Else if the user is attending we delete the attendance record from the database
    #Return not attending to signal that the user is now not attending
    else:
        db.session.delete(attending)
        db.session.commit()
        return "not attending"

#GET ONE EVENT ALT
#Alternative version for Android client. Returns regular json instead of geojson.
#Quering for one event by id
@app.route('/getevent_alt')
def getoneevent():
    #Getting eventID and userID from user getting the info
    eventID = request.args.get('eventID')
    userID_caller = request.args.get('userID')
    #Querying the database for event info, host info and userCount for the event
    event = eventtable.query.filter_by(eventID = eventID).first()
    user = users.query.filter_by(userID = event.userID).first()
    userCount = attendancetable.query.filter_by(eventID = eventID).count()
    userAttending = attendancetable.query.filter_by(eventID = eventID,userID = userID_caller).first()
    if not userAttending:
        userAttending = 0
    else:
        userAttending = 1
    #Returning event json object
    return str(event_to_geojson_alt(event.latitude,\
    event.longitude,\
    event.userID,\
    event.eventID,\
    str(user.firstName),\
    str(user.lastName),\
    str(event.eventName),\
    str(event.startTime),\
    str(event.endTime),\
    str(event.description),\
    userCount,\
    userAttending))

#GET EVENTS
#Calling GetEvents Stored procedure
#Gets events in a 25 mile radius of the user
@app.route('/getnearevents',methods=['GET'])
def getnearevents():
    #Using raw_connection to call MySQL Stored Procedures
    connection = engine.raw_connection()
    cursor = connection.cursor()
    #Getting userID of caller
    userID_caller = 0 if not request.args.get('userID') else request.args.get('userID')
    #Calling GetEvents3 Stored procedure to get events in a 25 mile radius
    cursor.callproc("GetEvents3",\
    [request.args.get('latitude'),\
    request.args.get('longitude')])
    #Getting returned events and converting them into a list
    results = list(cursor.fetchall())
    cursor.close()
    connection.commit()
    connection.close()
    event_list = []
    #Creating event geojson objects
    for event in results:
        #Querying for user count and if caller user attendance value per event
        userCount = attendancetable.query.filter_by(eventID = event[3]).count()
        userAttending = attendancetable.query.filter_by(eventID = event[3],userID = userID_caller).first()
        if not userAttending:
            userAttending = 0
        else:
            userAttending = 1
        event_list.append(event_to_geojson(event[0],event[1],event[2],event[3],event[4],event[5],event[6],str(event[7]),str(event[8]),event[9],userCount,userAttending))
    #Returing a geojson FeatureCollection containing all events in a 25 mile radius
    return geojson.dumps(FeatureCollection(event_list),sort_keys=True)

#GET EVENTS
#Calling GetEvents Stored procedure
#Gets events in a 25 mile radius of the user
#Alternative version for Android client. Returns regular json instead of geojson.
@app.route('/getnearevents_alt',methods=['GET'])
def getnearevents_alt():
    #Using raw_connection to call MySQL Stored Procedures
    connection = engine.raw_connection()
    cursor = connection.cursor()
    #Getting userID of caller
    userID_caller = 0 if not request.args.get('userID') else request.args.get('userID')
    #Calling GetEvents3 Stored procedure to get events in a 25 mile radius
    cursor.callproc("GetEvents3",\
    [request.args.get('latitude'),\
    request.args.get('longitude')])
    #Getting returned events and converting them into a list
    results = list(cursor.fetchall())
    cursor.close()
    connection.commit()
    connection.close()
    event_list = []
    #Creating event json objects
    for event in results:
        #Querying for user count and if caller user attendance value per event
        userCount = attendancetable.query.filter_by(eventID = event[3]).count()
        userAttending = attendancetable.query.filter_by(eventID = event[3],userID = userID_caller).first()
        if not userAttending:
            userAttending = 0
        else:
            userAttending = 1
        event_list.append(event_to_geojson_alt(event[0],event[1],event[2],event[3],event[4],event[5],event[6],str(event[7]),str(event[8]),event[9],userCount,userAttending))
    #Returing a list of json objects containing all events in a 25 mile radius
    return str(event_list)

#GET ATTENDANCE
#List of attending users
@app.route('/getattendance',methods=['GET'])
def getattendance():
    #Getting eventID to get attendance list
    eventID = request.args.get('eventID')
    #Getting a list of userID that are attending the event
    attendees = list(attendancetable.query.filter_by(eventID = eventID))
    attendee_list = []
    #Querying database for info from users that are attending
    for ent in attendees:
        attendee = users.query.filter_by(userID = ent.userID).first()
        attendee_list.append(attendee)

    return str(attendee_list)

#HELPER FUNCTIONS

#takes an info to make a json feature that represents an event
def event_to_geojson(x,y,userID,eventID,firstName,lastName,eventName,startTime,endTime,description,attendance,attending):
    return Feature(geometry=Point((x,y)),\
    properties={"userID":userID,\
    "eventID":eventID,\
    "firstName":firstName,\
    "lastName":lastName,\
    "eventName":eventName,\
    "startTime":startTime,\
    "endTime":endTime,\
    "description":description,\
    "attendance":attendance,\
    "attending":attending})

#takes an info to make a json feature that represents an event
def event_to_geojson_alt(latitude,longitude,userID,eventID,firstName,lastName,eventName,startTime,endTime,description,attendance,attending):
    return {"latitude": latitude,\
    "longitude": longitude,\
    "eventID": int(eventID),\
    "userID": int(userID),\
    "firstName":firstName,\
    "lastName":lastName,\
    "eventName": eventName,\
    "startTime":str( startTime),\
    "endTime":str( endTime),\
    "description": description,\
    "attendance":int(attendance),\
    "attending":int(attending)}


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

    #Function return user json representation
    def __repr__(self):
        return '{{"userID":{},"userName":"{}","firstName":"{}","lastName":"{}"}}'.format(self.userID,self.userName,self.firstName,self.lastName)

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
    #Function returns event geojson representation
    def json_repr(self):
        return Feature(geometry=Point((self.latitude,self.longitude)),\
        properties={\
        "eventID":self.eventID,\
        "userID":self.userID,\
        "eventName":self.eventName,\
        "startTime":str(self.startTime),\
        "endTime":str(self.endTime),\
        "description":self.description})
    #Function returns event json representation
    def json_repr_alt(self):
        return {"latitude": self.latitude,\
        "longitude": self.longitude,\
        "userID": int(self.userID),\
        "eventID": int(self.eventID),\
        "eventName": str(self.eventName),\
        "startTime": str(self.startTime),\
        "endTime": str(self.endTime),\
        "description": str(self.description)}
    #Function returns event geojson representation
    def __repr__(self):
        geojson_rep = self.json_repr()
        return geojson.dumps(geojson_rep,sort_keys=True)

#Attendance table
class attendancetable(db.Model):
    __tablename__ = 'attendancetable'
    userID = db.Column('userID',db.BigInteger,ForeignKey("users.userID",ondelete="CASCADE"),primary_key=True)
    eventID = db.Column('eventID',db.BigInteger,ForeignKey("eventtable.eventID",ondelete="CASCADE"),primary_key=True)

#Run App
if __name__ == "__main__":
    app.run(host='0.0.0.0')
