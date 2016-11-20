# Implemented by: Juan and Monica
import geojson
import json
import base64
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

#LOGIN
#Checking login
@app.route('/login',methods=['POST'])
def login():
    username = request.form['userName']
    password = request.form['password']
    results = users.query.filter_by(userName = username,password = password).first()
    if not results:
        return "-1"
    else:
        return str(results)

#ADDING USER
#Calling InsertUser Stored procedure
#Using POST method
@app.route('/adduser',methods=['POST'])
def insertuser():
    connection = engine.raw_connection()
    cursor = connection.cursor()
    username = request.form['userName']
    name = request.form['firstName']
    lname = request.form['lastName']
    password = request.form['password']
    email = request.form['email']
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
    event = request.form['event']
    locfeat = json.loads(event)

    cursor.callproc("InsertEvent",\
    [locfeat['properties']['userID'],\
    locfeat['properties']['eventName'],\
    locfeat['geometry']['coordinates'][0],\
    locfeat['geometry']['coordinates'][1],\
    locfeat['properties']['startTime'],\
    locfeat['properties']['endTime'],\
    locfeat['properties']['description']])

    results = list(cursor.fetchall())
    cursor.close()
    connection.commit()
    connection.close()

    new_attendance =  attendancetable(eventID = results[0][0],userID = locfeat['properties']['userID'])
    db.session.add(new_attendance)
    db.session.commit()

    return "1"

#ADDING EVENT ALT
#Calling InsertEvent Stored procedure
#Using POST method
#CALL InsertEvent(userID <int[25]>, EventName <string[255]>, Latitude <double>, Longitude <double>, Date/Time <year-month-day hour:min:second>, Description <string[1024]>);
@app.route('/addevent_alt',methods=['POST'])
def insertevent_alt():
    connection = engine.raw_connection()
    cursor = connection.cursor()
    event = request.form['event']
    locfeat = json.loads(event)

    cursor.callproc("InsertEvent",\
    [locfeat['userID'],\
    locfeat['eventName'],\
    locfeat['latitude'],\
    locfeat['longitude'],\
    locfeat['startTime'],\
    locfeat['endTime'],\
    locfeat['description']])

    results = list(cursor.fetchall())
    cursor.close()
    connection.commit()
    connection.close()

    new_attendance = attendancetable(eventID = results[0][0],userID = locfeat['userID'])
    db.session.add(new_attendance)
    db.session.commit()

    return "1"

#EDITING EVENT
#In a future implementation we can reduce overhead by updating
#individual elements of the event rather than all the elements
@app.route('/editevent',methods=['POST'])
def editevent():
    event_geojson = request.form['event']
    locfeat = json.loads(event_geojson)

    event = eventtable.query.filter_by(eventID = locfeat['properties']['eventID']).first()

    event.latitude = locfeat['geometry']['coordinates'][0]
    event.longitude = locfeat['geometry']['coordinates'][1]
    event.eventName = locfeat['properties']['eventName']
    event.startTime = locfeat['properties']['startTime']
    event.endTime = locfeat['properties']['endTime']
    event.description = locfeat['properties']['description']
    db.session.commit()

    return "1"

#EDITING EVENT ALT
#In a future implementation we can reduce overhead by updating
#individual elements of the event rather than all the elements
@app.route('/editevent_alt',methods=['POST'])
def editevent_alt():
    event_geojson = request.form['event']
    locfeat = json.loads(event_geojson)

    event = eventtable.query.filter_by(eventID = locfeat['eventID']).first()

    event.latitude = locfeat['latitude']
    event.longitude = locfeat['longitude']
    event.eventName = locfeat['eventName']
    event.startTime = locfeat['startTime']
    event.endTime = locfeat['endTime']
    event.description = locfeat['description']
    db.session.commit()

    return "1"


#ATTENDING EVENT
#Using SQL Alchemy
#Using POST method
@app.route('/attend',methods=['POST'])
def attendevent():
    userID = request.form['userID']
    eventID = request.form['eventID']
    attending = attendancetable.query.filter_by(eventID = eventID,userID = userID).first()

    if not attending:
        new_attendance =  attendancetable(eventID = eventID,userID = userID)
        db.session.add(new_attendance)
        db.session.commit()
        return "attending"
    else:
        db.session.delete(attending)
        db.session.commit()
        return "not attending"

#EDITING EVENT

#GET EVENTS
#Quering list of events from the database
@app.route('/getallevents')
def getevents():
    events = eventtable.query.all()
    event_list = []
    for event in events:
        event_list.append(event.json_repr())
    return geojson.dumps(FeatureCollection(event_list),sort_keys=True)

#GET ONE EVENT
#Quering for one event by id
@app.route('/getevent_alt')
def getoneevent():
    eventID = request.args.get('eventID')
    event = eventtable.query.filter_by(eventID = eventID).first()

    return str(event.json_repr_alt())

#GET EVENTS
#Calling GetEvents Stored procedure
@app.route('/getnearevents',methods=['GET'])
def getnearevents():
    connection = engine.raw_connection()
    cursor = connection.cursor()

    userID_caller = 0 if not request.args.get('userID') else request.args.get('userID')

    cursor.callproc("GetEvents3",\
    [request.args.get('latitude'),\
    request.args.get('longitude')])

    results = list(cursor.fetchall())
    cursor.close()
    connection.commit()
    connection.close()
    event_list = []
    for event in results:
        userCount = attendancetable.query.filter_by(eventID = event[3]).count()
        userAttending = attendancetable.query.filter_by(eventID = event[3],userID = userID_caller).first()
        if not userAttending:
            userAttending = 0
        else:
            userAttending = 1
        event_list.append(event_to_geojson(event[0],event[1],event[2],event[3],event[4],event[5],event[6],str(event[7]),str(event[8]),event[9],userCount,userAttending))
    return geojson.dumps(FeatureCollection(event_list),sort_keys=True)

#GET EVENTS
#Calling GetEvents Stored procedure
@app.route('/getnearevents_alt',methods=['GET'])
def getnearevents_alt():
    connection = engine.raw_connection()
    cursor = connection.cursor()

    userID_caller = 0 if not request.args.get('userID') else request.args.get('userID')

    cursor.callproc("GetEvents3",\
    [request.args.get('latitude'),\
    request.args.get('longitude')])

    results = list(cursor.fetchall())
    cursor.close()
    connection.commit()
    connection.close()
    event_list = []
    for event in results:
        userCount = attendancetable.query.filter_by(eventID = event[3]).count()
        userAttending = attendancetable.query.filter_by(eventID = event[3],userID = userID_caller).first()
        if not userAttending:
            userAttending = 0
        else:
            userAttending = 1
        event_list.append(event_to_geojson_alt(event[0],event[1],event[2],event[3],event[4],event[5],event[6],str(event[7]),str(event[8]),event[9],userCount,userAttending))
    return str(event_list)

#GET ATTENDANCE
@app.route('/getattendance',methods=['GET'])
def getattendance():
    eventID = request.args.get('eventID')
    attendees = list(attendancetable.query.filter_by(eventID = eventID))
    attendee_list = []

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

    def json_repr(self):
        return Feature(geometry=Point((self.latitude,self.longitude)),\
        properties={\
        "eventID":self.eventID,\
        "userID":self.userID,\
        "eventName":self.eventName,\
        "startTime":str(self.startTime),\
        "endTime":str(self.endTime),\
        "description":self.description})

    def json_repr_alt(self):
        return {"latitude": self.latitude,\
        "longitude": self.longitude,\
        "userID": int(self.userID),\
        "eventID": int(self.eventID),\
        "eventName": str(self.eventName),\
        "startTime": str(self.startTime),\
        "endTime": str(self.endTime),\
        "description": str(self.description)}

    def __repr__(self):
        geojson_rep = self.json_repr()
        return geojson.dumps(geojson_rep,sort_keys=True)

#Friendship table
#class friendshiptable(db.Model):
#    __tablename__ = 'friendshiptable'
#    userID_1 = db.Column('userID_1',db.BigInteger,primary_key=True,ForeignKey("users.userID",ondelete="CASCADE"),nullable=False)
#    userID_2 = db.Column('userID_2',db.BigInteger,primary_key=True,ForeignKey("users.userID",ondelete="CASCADE"),nullable=False)

#Request table
#class requesttable(db.Model):
#    __tablename__ = 'requesttable'
#    userID_sender = db.Column('userID_sender',db.BigInteger,primary_key=True,ForeignKey("users.userID",ondelete="CASCADE"),nullable=False)
#    userID_receiver = db.Column('userID_receiver',db.BigInteger,primary_key=True,ForeignKey("users.userID",ondelete="CASCADE"),nullable=False)
#    status = db.Column('status',db.Enum('accepted','rejected','pending'),primary_key=True,ForeignKey("users.userID",ondelete="CASCADE"),nullable=False)

#Attendance table
class attendancetable(db.Model):
    __tablename__ = 'attendancetable'
    userID = db.Column('userID',db.BigInteger,ForeignKey("users.userID",ondelete="CASCADE"),primary_key=True)
    eventID = db.Column('eventID',db.BigInteger,ForeignKey("eventtable.eventID",ondelete="CASCADE"),primary_key=True)

#Run App
if __name__ == "__main__":
    app.run(host='0.0.0.0')
