//
//  MapViewController.swift
//  PinG
//
//  Created by Koji Tilley on 10/9/16.
//  Worked on by Koji Tilley and Jordan Harlow
//  Copyright © 2016 PinG Team. All rights reserved.
//

import UIKit
import MapKit
import CoreLocation

let METERS_MILE = 1609.344
let METERS_FEET = 3.28084

class MapViewController: UIViewController, MKMapViewDelegate, CLLocationManagerDelegate {
    
    // Interface variables
    @IBOutlet var mapView: MKMapView!
    @IBOutlet var pingButton: UIBarButtonItem!
    
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
            refresh()
        }
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    func refresh() {
        if !isTracking{
            return
        }
        print("Refresh method called")
        // Grab pings from database and populate dictionary
        var fin = false
        print("Testing request")
        var request = URLRequest(url: URL(string: "http://162.243.15.139/getnearevents?longitude=\(currentCoordinate.longitude)&latitude=\(currentCoordinate.latitude)")!)
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
                //Parse jason
                print("Begin retrieving json")
                do {
                    let json = try JSONSerialization.jsonObject(with: data, options: .allowFragments) as! [String:AnyObject]
                    
                    if let features = json["features"] as? [[String: AnyObject]] {
                        self.pingMap.removeAll()
                        
                        for feature in features {
                            
                            let point = feature["geometry"]?["coordinates"] as? NSArray
                            let description = feature["properties"]?["description"] as! String
                            let eventName = feature["properties"]?["eventName"] as! String
                            let userID = feature["properties"]?["userID"] as! Int
                            let fname = feature["properties"]?["firstName"] as! String
                            let lname = feature["properties"]?["lastName"] as! String
                            let fromTime = feature["properties"]?["startTime"] as! String
                            let toTime = feature["properties"]?["endTime"] as! String
                            
                            let df = DateFormatter()
                            df.dateFormat = "yyyy-MM-dd HH:mm:ss"
                            df.timeZone = TimeZone(abbreviation: "UTC")
                            
                            print("\(eventName) at (\((point?[1])!),\((point?[0])!) for uID \(userID)")
                            //Populate local ping map
                            let ping = Ping(coordinate: CLLocationCoordinate2DMake(point?[1] as! CLLocationDegrees, point?[0] as! CLLocationDegrees))
                            ping.userID = userID
                            ping.firstName = fname
                            ping.lastName = lname
                            ping.eventName = eventName
                            ping.eventDescription = description
                            ping.fromTime = df.date(from: fromTime)
                            ping.toTime = df.date(from: toTime)
                            if ping.toTime! > Date() {
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
                if self.pingMap[ids]?.added == false {
                    //pingMap[ids]?.added = true
                    /*
                    let annotation = Ping(coordinate: (self.pingMap[ids]?.coordinate)!)
                    annotation.coordinate = (self.pingMap[ids]?.coordinate)!
                    annotation.eventName = self.pingMap[ids]?.eventName
                    annotation.eventDescription = self.pingMap[ids]?.eventDescription
                    */
                    let annotation = self.pingMap[ids]!
                    self.mapView.addAnnotation(annotation)
                    print("Added ping at (\(annotation.coordinate.latitude), \(annotation.coordinate.longitude))")
                }
            }
        }
        for annotation in mapView.annotations {
            print("Ping: \(annotation.coordinate.longitude), \(annotation.coordinate.latitude)")
        }
        
    }
    
    func centerView() {
        if isTracking {
            if currentCoordinate != nil {
                mapView.setCenter(currentCoordinate, animated: true)
            }
            let viewRegion: MKCoordinateRegion = MKCoordinateRegionMakeWithDistance(currentCoordinate, 30*METERS_MILE, 30*METERS_MILE)
            mapView.setRegion(viewRegion, animated: true)
        }
        
    }
    
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
    
    func getCurrentLocation() -> CLLocationCoordinate2D {
        return currentCoordinate
    }
    
    //Interface actions
    @IBAction func refreshPressed(sender: UIBarButtonItem) {
        centerView()
        refresh()
    }
    
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

    //Delegate methods
    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        //Grab current location
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
    
    func mapView(_ mapView: MKMapView, viewFor annotation: MKAnnotation) -> MKAnnotationView? {
        if annotation is MKUserLocation
        {
            return nil
        }
        var annotationView = self.mapView.dequeueReusableAnnotationView(withIdentifier: "Pin")
        if annotationView == nil{
            annotationView = AnnotationView(annotation: annotation, reuseIdentifier: "Pin")
            annotationView?.canShowCallout = false  //disable default annotation view
        }else{
            annotationView?.annotation = annotation
        }
        annotationView?.image = UIImage(named: "pingPin")
        return annotationView
    }
    
    // Custom callout
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
        calloutView.eventNameLabel.text = pingAnnotation.eventName
        calloutView.nameLabel.text = "\(pingAnnotation.firstName!) \(pingAnnotation.lastName!)"
        calloutView.descriptionLabel.text = pingAnnotation.eventDescription
        calloutView.fromLabel.text = "From \(df.string(from: pingAnnotation.fromTime!))"
        calloutView.toLabel.text = "To \(df.string(from: pingAnnotation.toTime!))"
        
        
        calloutView.center = CGPoint(x: view.bounds.size.width / 2, y: -calloutView.bounds.size.height*0.52)
        view.addSubview(calloutView)
        let centerRegion = MKCoordinateRegionMake((view.annotation?.coordinate)!, mapView.region.span)
        var newView = mapView.convertRegion(centerRegion, toRectTo: nil)
        newView = newView.offsetBy(dx: 0, dy: -150)
        let newRegion = mapView.convert(newView, toRegionFrom: nil)
        mapView.setRegion(newRegion, animated: true)
    }
    
    func mapView(_ mapView: MKMapView, didDeselect view: MKAnnotationView) {
        if view.isKind(of: AnnotationView.self)
        {
            for subview in view.subviews
            {
                subview.removeFromSuperview()
            }
        }
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
