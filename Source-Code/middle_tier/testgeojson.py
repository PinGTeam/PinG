from geojson import Feature, Point, FeatureCollection

my_feature = Feature(geometry=Point((1.234,-12.23)),properties={"userID":1,"eventName":"FirstEvent","time":"2038-01-19 03:14:07","description":"the best party in the whole town"})
dump = geojson.dumps(my_feature)
print(geojson.loads(dump))
