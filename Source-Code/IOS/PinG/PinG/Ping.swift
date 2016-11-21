//
//  Ping.swift
//  PinG
//
//  Ping object that stores information about the ping
//
//  Created by Koji Tilley on 10/11/16.
//  Worked on by Koji Tilley and Jordan Harlow
//  Copyright © 2016 PinG Team. All rights reserved.
//

import MapKit

class Ping: NSObject, MKAnnotation {
    var username: String?
    var firstName: String?
    var lastName: String?
    var userID: Int?
    var coordinate: CLLocationCoordinate2D
    var eventName: String?
    var eventDescription: String?
    var fromTime: Date?
    var toTime: Date?
    var image: UIImage!
    var eventID: Int?
    var attending: Int?
    var attendance: Int?
    var title: String?
    var added = false
    
    init(coordinate: CLLocationCoordinate2D) {
        self.coordinate = coordinate
    }}
