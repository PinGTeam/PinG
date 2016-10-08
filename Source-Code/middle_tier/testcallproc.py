#need run flask with following command
#flask run --host=0.0.0.0
#need to access http://162.243.15.139:5000/ to get response

from flask import Flask
from flask_sqlalchemy import SQLAlchemy
from sqlalchemy import *

app = Flask(__name__)
app.config['SQLALCHEMY_DATABASE_URI'] = 'mysql://juan:alfaro@localhost/ping'
app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False
# app.config['SQLALCHEMY_DATABASE_URI'] = 'mysql://alforo:Be+HQ2Nj@dbsrv2.cs.fsu.edu:3$
#app.config['MYSQL_DATABASE_USER'] = 'alforo'
#app.config['MYSQL_DATABASE_PASSWORD'] = 'Be+HQ2Nj'
#app.config['MYSQL_DATABASE_DB'] = 'alforo_db'
#app.config['MYSQL_DATABASE_HOST'] = 'dbsrv2.cs.fsu.edu:3306'
#app.config['SQLALCHEMY_DATABASE_URI'] = 'mysql://alforo:Be+HQ2Nj@anywheredb.clt1czfvl$
db = SQLAlchemy(app)



@app.route('/work')
def tryout():
    engine = create_engine('mysql://juan:alfaro@localhost/ping')
    connection = engine.raw_connection()
#try:
    cursor = connection.cursor()
    cursor.callproc("InsertUser", ["John123", "John", "Doe"])
    result = list(cursor.fetchall())
    cursor.close()
    connection.commit()
#finally:
    connection.close()



@app.route('/')
def hello_world():
    str = ""
    results = users.query.all()
    for ev in results:
      str = str + ev.userName + " ,"
    return str

#@app.route('/workbitch')
#def ejemplo():
#	stri = ""
#	for row in resultado:
#		stri = stri + row.userName + "," + row.firstName + "," + row.lastName + "\n"
#	return resultado



class users(db.Model):
    __tablename__ = 'users'
    userID = db.Column('userid',db.Integer, primary_key=True)
    userName = db.Column('userName',db.Unicode, unique=False)
    firstName = db.Column('firstName',db.Unicode, unique=False)
    lastName = db.Column('lastName',db.Unicode, unique=False)


    #def __init__(self, userName):
        #self.job_name = job_name

    #def __repr__(self):
        #return '<Job %r>' % self.job_name


#args = (123, 'Miami Game', 1.2, 1.3, '23:59:59', 'Watch game')

#queryx = 'call  InsertEvent(123, %s, 1.2, 1.3, %s, %s)', 'Miami Game', '23:59:59','Watch Game'


#return "it is printing\n"

#cursor = connection.cursor()
#cursor.callproc('InsertEvent', args)
#results = list(cursor.fetchall())
#cursor.close()
#print results
