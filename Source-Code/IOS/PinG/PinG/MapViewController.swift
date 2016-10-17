//
//  MapViewController.swift
//  PinG
//
//  Created by Koji Tilley on 10/9/16.
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
    @IBOutlet var pingButton: UIButton!
    
    // Variables
    var user: String?
    var uID: Int = 0
    var locationManager: CLLocationManager!
    var currentCoordinate: CLLocationCoordinate2D!
    var firstCenter = false
    var pingMap: [Int:Ping] = [:]   //empty dictionary of Ping objects, with username as key

    override func viewDidLoad() {
        super.viewDidLoad()
        
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

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    func refresh() {
        print("Refresh method called")
        // Grab pings from database and populate map
        
        //Refresh map by removing all annotations but the user location
        self.mapView.annotations.forEach {
            if !($0 is MKUserLocation) {
                self.mapView.removeAnnotation($0)
            }
        }
        
        // show pings
        if !pingMap.isEmpty {
            for (ids, coordinates) in pingMap {
                if pingMap[ids]?.added == false {
                    pingMap[ids]?.added = true
                    let annotation = MKPointAnnotation()
                    annotation.coordinate = (pingMap[ids]?.location)!
                    annotation.title = pingMap[ids]?.title
                    annotation.subtitle = pingMap[ids]?.subtitle
                    mapView.addAnnotation(annotation)
                }
            }
        }
    }
    
    func centerView() {
        if currentCoordinate != nil {
            mapView.setCenter(currentCoordinate, animated: true)
        }
        let viewRegion: MKCoordinateRegion = MKCoordinateRegionMakeWithDistance(currentCoordinate, 2*METERS_MILE, 2*METERS_MILE)
        mapView.setRegion(viewRegion, animated: true)
        
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
            let ping = Ping(uid: uID, coord: currentCoordinate)
            ping.username = user
            ping.title = customView.eventName
            ping.subtitle = customView.eventDescription
            pingMap[uID] = ping
            print("Ping added")
            refresh()
        }
        
        let pingAction = UIAlertAction(title: "Ping", style: UIAlertActionStyle.default, handler: addHandler)
        let cancelAction = UIAlertAction(title: "Cancel", style: UIAlertActionStyle.cancel, handler: nil)
        
        alert.addAction(pingAction)
        alert.addAction(cancelAction)
        self.present(alert, animated: true, completion: nil)
        
    }
    
    //Interface actions
    @IBAction func refreshPressed(sender: UIBarButtonItem) {
        centerView()
        refresh()
    }
    
    @IBAction func pingButtonPressed(sender: UIButton) {
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
        if firstCenter == false {
            centerView()
            firstCenter = true
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
