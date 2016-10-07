
from flask import Flask
from flask_sqlalchemy import SQLAlchemy

app = Flask(__name__)
app.config['SQLALCHEMY_DATABASE_URI'] = 'mysql://juan:alfaro@localhost/ping'
app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False
# app.config['SQLALCHEMY_DATABASE_URI'] = 'mysql://alforo:Be+HQ2Nj@dbsrv2.cs.fsu.edu:3306/alforo_db'
#app.config['MYSQL_DATABASE_USER'] = 'alforo'
#app.config['MYSQL_DATABASE_PASSWORD'] = 'Be+HQ2Nj'
#app.config['MYSQL_DATABASE_DB'] = 'alforo_db'
#app.config['MYSQL_DATABASE_HOST'] = 'dbsrv2.cs.fsu.edu:3306'
#app.config['SQLALCHEMY_DATABASE_URI'] = 'mysql://alforo:Be+HQ2Nj@anywheredb.clt1czfvlzlc.us-east-1.rds.amazonaws.com/alforo_db'
db = SQLAlchemy(app)

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
