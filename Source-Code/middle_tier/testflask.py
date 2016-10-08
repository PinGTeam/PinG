#need run flask with following command
#flask run --host=0.0.0.0
#need to access http://162.243.15.139:5000/ to get response
 
from flask import Flask
app = Flask(__name__)

@app.route('/')
def hello_world():
	return 'Hello, World!'
