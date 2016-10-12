import geojson
import json
from geojson import Feature, Point, FeatureCollection
from flask import Flask, url_for, request
from flask_sqlalchemy import SQLAlchemy
from sqlalchemy import *
from datetime import datetime

#need run flask with following command
#flask run --host=0.0.0.0
#need to access http://162.243.15.139:5000/ to get response

app = Flask(__name__)
app.config['SQLALCHEMY_DATABASE_URI'] = 'mysql://juan:alfaro@localhost/ping'
app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False
db = SQLAlchemy(app)

engine = create_engine('mysql://juan:alfaro@localhost/ping')

#LIST OF USERS
#Quering list of users from the database
@app.route('/')
def getusers():
    str = ""
    results = users.query.all()
    for ev in results:
      str = str + ev.userName + " ,"
    return str

#ADDING USER
#Calling InsertUser Stored procedure
#Using POST method
@app.route('/adduser',methods=['POST'])
def insertuser():
    connection = engine.raw_connection()
    cursor = connection.cursor()
    username = request.form['UserName']
    name = request.form['Name']
    lname = request.form['LName']
    cursor.callproc("InsertUser", [username, name,lname])
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
    location = request.form['location']
    locfeat = json.loads(location)
    cursor.callproc("InsertEvent", [locfeat['properties']['userID'],locfeat['properties']['eventName'],locfeat['geometry']['coordinates'][0],locfeat['geometry']['coordinates'][1],locfeat['properties']['time'],locfeat['properties']['description']])
    results = list(cursor.fetchall())
    cursor.close()
    connection.commit()
    connection.close()
    return "hello"


#GET EVENTS
#Quering list of events from the database
@app.route('/getallevents')
def getevents():
    return geojson.dumps(allgeojson(eventtable),sort_keys=True)

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
        event_list.append(event_to_geojson(event[2],event[3],event[1],event[4],str(event[5]),evenT[6]))
    return geojson.dumps(FeatureCollection(event_list),sort_keys=True)

def allgeojson(eventtable):
    events = eventtable.query.all()
    event_list = []
    for event in events:
        event_list.append(event_to_geojson(event.latitude,event.longitude,event.userID,event.eventName,str(event.time),event.description))
    return FeatureCollection(event_list)

def event_to_geojson(x,y,userID,eventName,time,description):
    return Feature(geometry=Point((x,y)),properties={"userID":userID,"eventName":eventName,"time":time,"description":description})
'''
with app.test_request_context():
    print url_for('getusers')
    print url_for('insertuser', userName='John Doe', firstName='asd',lastName='asd')
    print url_for('getnearevents',topLatitude=8,topLongitude=-176,bottomLatitude=-8,bottomLongitude=170)
    print url_for('insertevent',userID=1,EventName="name",Latitude=12,Longitude=8,Datetime="2000-12-12 15:50:20",Description="reasdasd")
'''
class users(db.Model):
    __tablename__ = 'users'
    userID = db.Column('userid',db.Integer, primary_key=True)
    userName = db.Column('userName',db.Unicode, unique=False)
    firstName = db.Column('firstName',db.Unicode, unique=False)
    lastName = db.Column('lastName',db.Unicode, unique=False)

class eventtable(db.Model):
    __tablename__ = 'eventtable'
    eventID = db.Column('eventID',db.Integer,primary_key=True)
    userID = db.Column('userID',db.Integer,unique=False)
    latitude = db.Column('latitude',db.Float,unique=False)
    longitude = db.Column('longitude',db.Float,unique=False)
    eventName = db.Column('eventName',db.Unicode,unique=False)
    time = db.Column('time',db.DateTime,unique=False)
    description = db.Column('description',db.Unicode,unique=False)


if __name__ == "__main__":
    app.run(host='0.0.0.0')
