//
//  Shared.swift
//  PinG
//
//  Created by Koji Tilley on 10/13/16.
//  Worked on by Koji Tilley and Jordan Harlow
//  Copyright © 2016 PinG Team. All rights reserved.
//

import UIKit
import CoreLocation

final class Shared: NSObject {
    static let shared = Shared()
    
    //User data
    var username: String!
    var firstname: String!
    var lastname: String!
    var userID: Int!
    
    //Location data
    var sharedLocation: CLLocationCoordinate2D!
    
    //shared ping info
    
}
