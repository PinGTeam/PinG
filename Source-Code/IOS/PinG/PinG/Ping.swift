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

import UIKit
import MapKit

class Ping: NSObject {
    var username: String?
    var userID: Int?
    var location: CLLocationCoordinate2D?
    var title: String?
    var subtitle: String?
    var added = false
    
    init(uid: Int, coord: CLLocationCoordinate2D) {
        userID = uid
        location = coord
    }
}
