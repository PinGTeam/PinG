//
//  MapViewController.swift
//  PinG
//
//  Created by Koji Tilley on 10/9/16.
//  Worked on by Koji Tilley, Jordan Harlow, Arthur Karapateas, and Monica Lombraño
//  Copyright © 2016 PinG Team. All rights reserved.
//

import UIKit
import MapKit
import CoreLocation

//constants to convert from metric to bad units
let METERS_MILE = 1609.344
let METERS_FEET = 3.28084

class MapViewController: UIViewController, MKMapViewDelegate, CLLocationManagerDelegate {
    
    // Interface variables
    @IBOutlet var mapView: MKMapView!
    @IBOutlet var pingButton: UIBarButtonItem!
    @IBOutlet weak var viewEventButton: UIBarButtonItem!
    
    // Variables
    var isTracking: Bool = false
    var user: String?
    var uID: Int = 0
    var locationManager: CLLocationManager!
    var currentCoordinate: CLLocationCoordinate2D!
    var firstCenter = false
    var pingMap: [Int:Ping] = [:]   //empty dictionary of Ping objects, with username as key

    override func viewDidLoad() {
        super.viewDidLoad()
        
        print("Map view did load..... xD")
        self.mapView.delegate = self
        
        //Set variables
        user = Shared.shared.username
        uID = Shared.shared.userID
        print(Shared.shared.userID)
        
        // Show User Location
        locationManager = CLLocationManager()
        locationManager.requestWhenInUseAuthorization()
        locationManager.desiredAccuracy = kCLLocationAccuracyBest
        locationManager.startUpdatingLocation()
        locationManager.delegate = self
        mapView.showsUserLocation = true
        refresh()
        // Do any additional setup after loading the view.
        
    }
    
    override func viewDidAppear(_ animated: Bool) {
        print("View did appear haha")
        if isTracking {
            //refresh map view upon seeing the view from a different view
            refresh()
        }
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    //refresh function that grabs pings from database
    func refresh() {
        if !isTracking{
            //don't refresh if user doesn't have a location
            return
        }
        print("Refresh method called")
        // Grab pings from database and populate dictionary
        var fin = false
        print("Testing request")
        var request = URLRequest(url: URL(string: "http://162.243.15.139/getnearevents?longitude=\(currentCoordinate.longitude)&latitude=\(currentCoordinate.latitude)&userID=\(Shared.shared.userID!)")!)
        request.httpMethod = "GET"
        let task = URLSession.shared.dataTask(with: request) { data, response, error in guard let data = data, error == nil else {
            print("error=\(error)")
            fin = true
            return
            
            }
            let httpStatus = response as! HTTPURLResponse
            print("In url session method")
            
            if httpStatus.statusCode != 200 {
                print("StatusCode should be 200, but it is \(httpStatus.statusCode)")
                print("response = \(response)")
                fin = true
            }
            else {
                //Parse jason from data object, stored in "data"
                print("Begin retrieving json")
                do {
                    //create json object from response string
                    let json = try JSONSerialization.jsonObject(with: data, options: .allowFragments) as! [String:AnyObject]
                    
                    if let features = json["features"] as? [[String: AnyObject]] {
                        //clear all pings
                        self.pingMap.removeAll()
                        
                        //add pings into pingmap for each geojson feature
                        for feature in features {
                            
                            let point = feature["geometry"]?["coordinates"] as? NSArray
                            let description = feature["properties"]?["description"] as! String
                            let eventName = feature["properties"]?["eventName"] as! String
                            let userID = feature["properties"]?["userID"] as! Int
                            let fname = feature["properties"]?["firstName"] as! String
                            let lname = feature["properties"]?["lastName"] as! String
                            let fromTime = feature["properties"]?["startTime"] as! String
                            let toTime = feature["properties"]?["endTime"] as! String
                            let eventID = feature["properties"]?["eventID"] as! Int
                            let attending = feature["properties"]?["attending"] as! Int
                            let attendance = feature["properties"]?["attendance"] as! Int
                            
                            let df = DateFormatter()
                            df.dateFormat = "yyyy-MM-dd HH:mm:ss"
                            df.timeZone = TimeZone(abbreviation: "UTC")
                            
                            print("\(eventName) at (\((point?[1])!),\((point?[0])!) for uID \(userID)")
                            //Populate local ping map from json
                            let ping = Ping(coordinate: CLLocationCoordinate2DMake(point?[1] as! CLLocationDegrees, point?[0] as! CLLocationDegrees))
                            ping.userID = userID
                            ping.eventID = eventID
                            ping.firstName = fname
                            ping.lastName = lname
                            ping.eventName = eventName
                            ping.eventDescription = description
                            ping.fromTime = df.date(from: fromTime)
                            ping.toTime = df.date(from: toTime)
                            ping.attending = attending
                            ping.attendance = attendance
                            if ping.toTime! > Date() {
                                //clientside check to only add pings if the event hasn't ended yet
                                self.pingMap[userID] = ping
                            }
                        }
                        
                    }
                    
                    fin = true
                    
                } catch {
                    print("Error with JSON: \(error)")
                }
                
            }
            
            
        }
        task.resume()
        //Bock main thread until ping refresh complete
        while !fin {
            Thread.sleep(forTimeInterval: 0.1)
        }
        
        //Refresh map by removing all annotations but the user location
        self.mapView.annotations.forEach {
            if !($0 is MKUserLocation) {
                self.mapView.removeAnnotation($0)
            }
        }
        
        // show pings
        if !self.pingMap.isEmpty {
            print("Number of pings in local storage: \(pingMap.count)")
            for (ids, coordinates) in self.pingMap {
                //deprecated check
                if self.pingMap[ids]?.added == false {
                    //pingMap[ids]?.added = true
                    /*
                    let annotation = Ping(coordinate: (self.pingMap[ids]?.coordinate)!)
                    annotation.coordinate = (self.pingMap[ids]?.coordinate)!
                    annotation.eventName = self.pingMap[ids]?.eventName
                    annotation.eventDescription = self.pingMap[ids]?.eventDescription
                    */
                    let annotation = self.pingMap[ids]!
                    //grab annotations from the pingmap and add them onto the mapview
                    self.mapView.addAnnotation(annotation)
                    print("Added ping at (\(annotation.coordinate.latitude), \(annotation.coordinate.longitude))")
                }
            }
        }
        for annotation in mapView.annotations {
            print("Ping: \(annotation.coordinate.longitude), \(annotation.coordinate.latitude)")
        }
        
    }
    
    //set view region to center around current location
    func centerView() {
        if isTracking {
            if currentCoordinate != nil {
                mapView.setCenter(currentCoordinate, animated: true)
            }
            let viewRegion: MKCoordinateRegion = MKCoordinateRegionMakeWithDistance(currentCoordinate, 1*METERS_MILE,1*METERS_MILE)
            mapView.setRegion(viewRegion, animated: true)
        }
        
    }
    
    //deprecated method used in older iterations
    //now handled in AddEventTableViewController.swift
    func addPing() {
        print("Ping about to be added")
        
        //Define alert view
        let alert = UIAlertController(title: "New Ping", message: "\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n", preferredStyle: UIAlertControllerStyle.actionSheet)
        
        let margin:CGFloat = 8.0
        let rect = CGRect(x: margin, y: margin, width: alert.view.bounds.size.width - margin * 4.0, height: 420)
        let customView = PingAddView(frame: rect)
        customView.backgroundColor = UIColor.clear
        
        alert.view.addSubview(customView)
        
        func addHandler(actionTarget: UIAlertAction) {
            //Set up date formatter for string date
            var fdate = customView.fromDate
            var tdate = customView.toDate
            
            if fdate < Date() {
                fdate = Calendar.current.date(byAdding: .day, value: 1, to: fdate)!
            }
            while tdate < fdate {
                tdate = Calendar.current.date(byAdding: .day, value: 1, to: tdate)!
            }
            
            let df = DateFormatter()
            df.dateFormat = "yyyy-MM-dd HH:mm:ss"
            df.timeZone = TimeZone(abbreviation: "UTC")
            let fdateString = df.string(from: fdate)
            let tdateString = df.string(from: tdate)
            
            //Create json string from dictionary
            let geojson = ["geometry": ["coordinates": [currentCoordinate.latitude, currentCoordinate.longitude], "type": "Point"], "properties": ["description": customView.eventDescription!, "eventName": customView.eventName!, "startTime": fdateString, "endTime": tdateString, "userID": uID], "type": "Feature"] as [String : Any]
            
            do {
                let jsonData = try JSONSerialization.data(withJSONObject: geojson, options: .init(rawValue: 0))
                let jsonText = String(data: jsonData, encoding: String.Encoding.ascii)
                var succ = false
                var err = false
                var msg = ""
                
                //Prepare to use http post method to add the jsonText on the server
                var request = URLRequest(url: URL(string: "http://162.243.15.139/addevent")!)
                request.httpMethod = "POST"
                //Create post string
                let postString = "event=" + jsonText!
                print("\(postString)")
                request.httpBody = postString.data(using: .utf8)
                print("Begin post request")
                let task = URLSession.shared.dataTask(with: request) { data, response, error in guard let data = data, error == nil else {
                    print("error=\(error)")
                    err = true
                    msg = "Unable to connect to the internet."
                    succ = true
                    return
                    
                    }
                    
                    if let httpStatus = response as? HTTPURLResponse, httpStatus.statusCode != 200 {
                        print("StatusCode should be 200, but it is \(httpStatus.statusCode)")
                        msg = "HTTP status code \(httpStatus.statusCode)"
                        print("response = \(response)")
                        succ = true
                        err = true
                    }
                    
                    let responseString = String(data: data, encoding: .utf8)
                    print("responseString = \(responseString)")
                    
                    succ = true
                }
                task.resume()
                
                while !succ {
                    Thread.sleep(forTimeInterval: 0.1)
                }
                
                if err {
                    //Create alert
                    
                    let Alert = UIAlertController(title: "Error connecting to server", message: msg, preferredStyle: UIAlertControllerStyle.alert)
                    
                    let OkButton = UIAlertAction(title: "OK", style: UIAlertActionStyle.default, handler: nil)
                    
                    Alert.addAction(OkButton)
                    
                    self.present(Alert, animated: true, completion: nil)
                }
                
            } catch {
                print(error.localizedDescription)
            }
            refresh()
        }
        
        let pingAction = UIAlertAction(title: "Ping", style: UIAlertActionStyle.default, handler: addHandler)
        let cancelAction = UIAlertAction(title: "Cancel", style: UIAlertActionStyle.cancel, handler: nil)
        
        alert.addAction(pingAction)
        alert.addAction(cancelAction)
        self.present(alert, animated: true, completion: nil)
        
    }
    
    //returns currentCoordinate
    func getCurrentLocation() -> CLLocationCoordinate2D {
        return currentCoordinate
    }
    
    //Interface actions
    @IBAction func refreshPressed(sender: UIBarButtonItem) {
        centerView()
        refresh()
    }
    
    //old method no longer used; handled in another class
    @IBAction func pingButtonPressed(sender: UIBarButtonItem) {
        print("Ping add function entered")
        if pingMap[uID] == nil {
            // New ping for user
            addPing()
        }
        else {
            // Notify user that their old ping will be removed
            let Alert = UIAlertController(title: "Are you sure?", message: "Are you sure you would like to replace your current Ping with a new Ping?", preferredStyle: UIAlertControllerStyle.alert)
            func okHandler(actionTarget: UIAlertAction) {
                addPing()
            }
            let OkButton = UIAlertAction(title: "OK", style: UIAlertActionStyle.default, handler: okHandler)
            let CancelButton = UIAlertAction(title: "Cancel", style: UIAlertActionStyle.cancel, handler: nil)
            
            Alert.addAction(OkButton)
            Alert.addAction(CancelButton)
            
            self.present(Alert, animated: true, completion: nil)
        }
        
    }
    
    //push ViewEventTableViewController onto navigation stack
    @IBAction func viewButtonPressed(sender: UIBarButtonItem) {
        self.performSegue(withIdentifier: "viewPing", sender: sender)
    }

    //Delegate methods
    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        //Grab current location and store it in currentCoordinate
        let location: CLLocation = locations.last!
        currentCoordinate = location.coordinate
        Shared.shared.sharedLocation = currentCoordinate
        if firstCenter == false {
            isTracking = true
            centerView()
            refresh()
            firstCenter = true
        }
    }
    
    //define custom view for each annotation by replacing default view with AnnotationView.xib
    func mapView(_ mapView: MKMapView, viewFor annotation: MKAnnotation) -> MKAnnotationView? {
        if annotation is MKUserLocation
        {
            return nil
        }
        var annotationView = self.mapView.dequeueReusableAnnotationView(withIdentifier: "Pin")
        if annotationView == nil{
            annotationView = AnnotationView(annotation: annotation, reuseIdentifier: "Pin") as MKAnnotationView
            annotationView?.canShowCallout = false  ////disable default annotation view
            
        }else{
            annotationView?.annotation = annotation
        }
        return annotationView
    }
    
    // Custom callout info grabbed from each ping
    func mapView(_ mapView: MKMapView, didSelect view: MKAnnotationView)
    {
        if view.annotation is MKUserLocation
        {
            // Don't proceed with custom callout
            return
        }
        let df = DateFormatter()
        df.dateFormat = "h:mm a"
        let pingAnnotation = view.annotation as! Ping
        let views = Bundle.main.loadNibNamed("AnnotationView", owner: nil, options: nil)
        let calloutView = views?[0] as! AnnotationViewClass
        // set interface variables in the custom callout
        calloutView.eventNameLabel.text = pingAnnotation.eventName
        calloutView.nameLabel.text = "\(pingAnnotation.firstName!) \(pingAnnotation.lastName!)"
        calloutView.descriptionLabel.text = pingAnnotation.eventDescription
        calloutView.fromLabel.text = "From \(df.string(from: pingAnnotation.fromTime!))"
        calloutView.toLabel.text = "To \(df.string(from: pingAnnotation.toTime!))"
        
        // center callout view on map
        calloutView.center = CGPoint(x: view.bounds.size.width / 2 - 8, y: -calloutView.bounds.size.height*0.52)
        view.addSubview(calloutView)
        let centerRegion = MKCoordinateRegionMake((view.annotation?.coordinate)!, mapView.region.span)
        var newView = mapView.convertRegion(centerRegion, toRectTo: nil)
        newView = newView.offsetBy(dx: 0, dy: -140)
        let newRegion = mapView.convert(newView, toRegionFrom: nil)
        mapView.setRegion(newRegion, animated: true)
        
        // show if attending
        if pingAnnotation.attending != 1 {
            calloutView.checkImage.isHidden = true
        }
        
        //Prepare for possible view switch
        //Info is stored in shared class per ping to populate table view upon view switch
        viewEventButton.isEnabled = true
        Shared.shared.eventName = pingAnnotation.eventName
        Shared.shared.eventDescription = pingAnnotation.eventDescription
        Shared.shared.eventFullName = "\(pingAnnotation.firstName!) \(pingAnnotation.lastName!)"
        Shared.shared.fromTime = pingAnnotation.fromTime
        Shared.shared.toTime = pingAnnotation.toTime
        Shared.shared.eventUserID = pingAnnotation.userID
        Shared.shared.eventID = pingAnnotation.eventID
        Shared.shared.eventLocation = pingAnnotation.coordinate
        Shared.shared.attending = pingAnnotation.attending
        Shared.shared.eventAttendance = pingAnnotation.attendance
    }
    
    //remove custom callout views upon deselected
    func mapView(_ mapView: MKMapView, didDeselect view: MKAnnotationView) {
        if view.isKind(of: AnnotationView.self)
        {
            for subview in view.subviews
            {
                subview.removeFromSuperview()
            }
            viewEventButton.isEnabled = false
        }
    }
    
    //method that never worked xD
    func mapView(_ mapView: MKMapView, annotationView view: MKAnnotationView, calloutAccessoryControlTapped control: UIControl) {
        let pingAnnotation = view.annotation as! Ping
        let views = Bundle.main.loadNibNamed("AnnotationView", owner: nil, options: nil)
        let calloutView = views?[0] as! AnnotationViewClass
        
        print(control)
    }
    
    /*
    // MARK: - Navigation

    // In a storyboard-based application, you will often want to do a little preparation before navigation
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        // Get the new view controller using segue.destinationViewController.
        // Pass the selected object to the new view controller.
    }
    */

}
