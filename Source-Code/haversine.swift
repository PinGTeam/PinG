func deg2rad(value: Double) -> Double {
        return value * M_PI / 180
    }
    
    func findDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double) -> Double {
        let earthRadius = 3959.0
        
        let distanceLat = deg2rad(value: (lat1 - lat2))
        let distanceLon = deg2rad(value: (lon1 - lon2))
        
        let a = sin(distanceLat/2) * sin(distanceLat/2) + cos(deg2rad(value: lat1)) * cos(deg2rad(value: lat2)) * sin(distanceLon/2) * sin(distanceLon/2)
        let c = 2 * asin(sqrt(a))
        let d = earthRadius * c
        
        return d
    }
