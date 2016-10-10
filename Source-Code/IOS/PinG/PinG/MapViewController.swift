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
    var locationManager: CLLocationManager!
    var currentCoordinate: CLLocationCoordinate2D!
    var firstCenter = false

    override func viewDidLoad() {
        super.viewDidLoad()
        
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
        // Grab pings from database
    }
    
    func centerView() {
        if currentCoordinate != nil {
            mapView.setCenter(currentCoordinate, animated: true)
        }
        let viewRegion: MKCoordinateRegion = MKCoordinateRegionMakeWithDistance(currentCoordinate, 2*METERS_MILE, 2*METERS_MILE)
        mapView.setRegion(viewRegion, animated: true)
        
    }
    
    //Interface actions
    @IBAction func refreshPressed(sender: UIBarButtonItem) {
        centerView()
        refresh()
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
