//
//  PinGTests.swift
//  PinGTests
//
//  Created by Koji Tilley on 10/9/16.
//  Worked on by Koji Tilley and Jordan Harlow
//  Copyright © 2016 PinG Team. All rights reserved.
//

import XCTest
@testable import PinG

class PinGTests: XCTestCase {
    
    override func setUp() {
        super.setUp()
        // Put setup code here. This method is called before the invocation of each test method in the class.
    }
    
    override func tearDown() {
        // Put teardown code here. This method is called after the invocation of each test method in the class.
        super.tearDown()
    }
    
    func testExample() {
        // This is an example of a functional test case.
        // Use XCTAssert and related functions to verify your tests produce the correct results.
        print("Testing testing")
        XCTAssert(true)
    }
    
    /* Deprecated Method, testing the GET request is now performed in testAttendeesGet()
    func testGETRequest() {
        var succ = false
        print("Testing request")
        var request = URLRequest(url: URL(string: "http://162.243.15.139/getallevents")!)
        request.httpMethod = "GET"
        let task = URLSession.shared.dataTask(with: request) { data, response, error in guard let data = data, error == nil else {
                print("error=\(error)")
                XCTAssert(false)
                return
            
            }
            let httpStatus = response as! HTTPURLResponse
            print("In url session method")
            
            if httpStatus.statusCode != 200 {
                print("StatusCode should be 200, but it is \(httpStatus.statusCode)")
                print("response = \(response)")
                XCTAssert(false)
            }
            else {
                //Parse jason
                print("Begin retrieving json")
                do {
                    let json = try JSONSerialization.jsonObject(with: data, options: .allowFragments) as! [String:AnyObject]
                    
                    if let features = json["features"] as? [[String: AnyObject]] {
                        XCTAssert(!features.isEmpty)  //if data exists
                        for feature in features {
                            
                            print(feature)
                            
                        }
                        
                    }
                    
                } catch {
                    print("Error with JSON: \(error)")
                }
                
            }
            
            succ = true
            
        }
        task.resume()
        
        while !succ {
            Thread.sleep(forTimeInterval: 0.1)
        }
        
    }
    */
    
    func testLogin() {
        var done = false
        var succ = true
        let username = "username1"
        let password = "thepassword"
        
        print("Login button pressed")
        let utf8str = password.data(using: String.Encoding.utf8)
        let base64Encoded = utf8str?.base64EncodedString(options: NSData.Base64EncodingOptions(rawValue: 0))
        print("password: \(base64Encoded!)")
        
        //HTTP Post method
        var resString:String?
        var resData:Data?
        var request = URLRequest(url: URL(string: "http://162.243.15.139/login")!)
        request.httpMethod = "POST"
        //Create post string via string concatenation
        var postString = "userName=" + username
        postString += "&password=" + base64Encoded!
        request.httpBody = postString.data(using: .utf8)
        let task = URLSession.shared.dataTask(with: request) { data, response, error in guard let data = data, error == nil else {
            print("error=\(error)")
            done = true
            succ = false
            XCTAssert(false)
            return
            
            }
            
            if let httpStatus = response as? HTTPURLResponse, httpStatus.statusCode != 200 {
                print("StatusCode should be 200, but it is \(httpStatus.statusCode)")
                print("response = \(response)")
                done = true
                succ = false
                XCTAssert(false)
            }
            
            let responseString = String(data: data, encoding: .utf8)
            print("responseString = \(responseString)")
            resString = responseString!
            resData = data
            done = true
            
        }
        task.resume()
        
        //stay in function until async completion
        while !done {
            Thread.sleep(forTimeInterval: 0.25)
        }
        print("password: \(base64Encoded)")
        
        if succ {
            print("Begin retrieving json")
            if resString == "-1" {
                print("login failed xD")
                XCTAssert(false)
            }
            else {
                do {
                    let json = try JSONSerialization.jsonObject(with: resData!, options: .allowFragments) as! [String:AnyObject]
                    
                    print(json)
                    print("UserID = \(json["userID"]!)")
                    print("Username = \(json["userName"]!)")
                    print("First name = \(json["firstName"]!)")
                    print("Last name = \(json["lastName"]!)")
                    
                    //done
                    XCTAssert(true)
                    
                    
                } catch {
                    print("Error with JSON: \(error)")
                    XCTAssert(false)
                }
                
                
            }
            
        }
    }
    
    /* Deprecated method
    func testPOSTRequest() {
        var succ = false
        var request = URLRequest(url: URL(string: "http://162.243.15.139/adduser")!)
        request.httpMethod = "POST"
        //Create post string via string concatenation
        var postString = "UserName=" + "BigNice"
        postString += "&Name=" + "Koji"
        postString += "&LName=" + "Harlow"
        request.httpBody = postString.data(using: .utf8)
        let task = URLSession.shared.dataTask(with: request) { data, response, error in guard let data = data, error == nil else {
            print("error=\(error)")
            return
            
            }
            
            if let httpStatus = response as? HTTPURLResponse, httpStatus.statusCode != 200 {
                print("StatusCode should be 200, but it is \(httpStatus.statusCode)")
                print("response = \(response)")
            }
            
            let responseString = String(data: data, encoding: .utf8)
            print("responseString = \(responseString)")
            
            XCTAssert(responseString == "5")
            succ = true
        }
        task.resume()
        
        while !succ {
            Thread.sleep(forTimeInterval: 0.1)
        }
    }
    
    func testPOSTRequestFail() {
        var succ = false
        var request = URLRequest(url: URL(string: "http://162.243.15.139/adduser")!)
        request.httpMethod = "POST"
        //Create post string via string concatenation
        var postString = "UserName=" + "BigNice"
        postString += "&Name=" + "Koji"
        postString += "&LName=" + "Harlow"
        request.httpBody = postString.data(using: .utf8)
        let task = URLSession.shared.dataTask(with: request) { data, response, error in guard let data = data, error == nil else {
            print("error=\(error)")
            return
            
            }
            
            if let httpStatus = response as? HTTPURLResponse, httpStatus.statusCode != 200 {
                print("StatusCode should be 200, but it is \(httpStatus.statusCode)")
                print("response = \(response)")
            }
            
            let responseString = String(data: data, encoding: .utf8)
            print("responseString = \(responseString)")
            
            XCTAssert(responseString == "6")
            succ = true
        }
        task.resume()
        
        while !succ {
            Thread.sleep(forTimeInterval: 0.1)
        }
    }
 
 */
    
    func testDateFormat() {
        let date = Date(timeIntervalSince1970: 0)
        let df = DateFormatter()
        df.dateFormat = "yyyy-MM-dd HH:mm:ss"
        let timestamp = df.string(from: date)
        print(timestamp)
        XCTAssert(timestamp == "1969-12-31 19:00:00")
    }
    
    func testJSONCreate() {
        let date = Date()
        let df = DateFormatter()
        df.dateFormat = "yyyy-MM-dd HH:mm:ss"
        let dateString = df.string(from: date)
        
        let geojson = ["geometry": ["coordinates": [-3.5123, 175.5], "type": "Point"], "properties": ["description": "This is a test event", "eventName": "Party_at_Juans_House", "time": dateString, "userID": 1], "type": "Feature"] as [String : Any]
        do {
            let jsonData = try JSONSerialization.data(withJSONObject: geojson, options: .init(rawValue: 0))
            let jsonText = String(data: jsonData, encoding: String.Encoding.ascii)!
            print("\(jsonText)")
            XCTAssert(jsonText != "")
        } catch {
            print(error.localizedDescription)
            XCTAssert(false)
        }
    }
    
    func testAttendeesGet() {
        var succ = false
        print("Testing request")
        var request = URLRequest(url: URL(string: "http://162.243.15.139/getattendance?eventID=1")!)
        request.httpMethod = "GET"
        let task = URLSession.shared.dataTask(with: request) { data, response, error in guard let data = data, error == nil else {
            print("error=\(error)")
            XCTAssert(false)
            succ = true
            return
            
            }
            let httpStatus = response as! HTTPURLResponse
            print("In url session method")
            
            if httpStatus.statusCode != 200 {
                print("StatusCode should be 200, but it is \(httpStatus.statusCode)")
                print("response = \(response)")
                XCTAssert(false)
            }
            else {
                //Parse jason
                print("Begin retrieving json")
                do {
                    let json = try JSONSerialization.jsonObject(with: data, options: .allowFragments) as! [AnyObject]
                    
                    print(json)
                    print(json.count)
                    XCTAssert(json.count > 0)
                    for attendee in json {
                        print("\(attendee["firstName"]!) \(attendee["lastName"]!)")
                    }
                    
                } catch {
                    print("Error with JSON: \(error)")
                    XCTAssert(false)
                }
                
            }
            
            succ = true
            
        }
        task.resume()
        
        while !succ {
            Thread.sleep(forTimeInterval: 0.1)
        }
    }
    
    func testPerformanceExample() {
        // This is an example of a performance test case.
        self.measure {
            // Put the code you want to measure the time of here.
        }
    }
    
}
