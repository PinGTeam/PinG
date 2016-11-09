//
//  LoginViewController.swift
//  PinG
//
//  Created by Koji Tilley on 10/9/16.
//  Worked on by Koji Tilley and Jordan Harlow
//  Copyright © 2016 PinG Team. All rights reserved.
//

import UIKit

class LoginViewController: UIViewController {

    @IBOutlet weak var usernameTextField: UITextField!
    @IBOutlet weak var passTextField: UITextField!
    @IBOutlet weak var firstnameTextField: UITextField!
    @IBOutlet weak var lastnameTextField: UITextField!
    @IBOutlet weak var activityIndicator: UIActivityIndicatorView!
    
    
    override func viewDidLoad() {
        super.viewDidLoad()

        // Do any additional setup after loading the view.
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    func switchView() {
        //Prepare for segue
        let view = self.storyboard?.instantiateViewController(withIdentifier: "nav")
        self.present(view as! UINavigationController, animated: true, completion: nil)
    }
    
    @IBAction func loginPressed(sender: UIButton) {
        //local vars
        var done = false
        var succ = true    //S U C C
        
        //Verify fields
        
        
        print("Login button pressed")
        let utf8str = passTextField.text?.data(using: String.Encoding.utf8)
        let base64Encoded = utf8str?.base64EncodedString(options: NSData.Base64EncodingOptions(rawValue: 0))
        print("password: \(base64Encoded!)")
        activityIndicator.startAnimating()
        
        //HTTP Post method
        var resString:String?
        var resData:Data?
        var request = URLRequest(url: URL(string: "http://162.243.15.139/login")!)
        request.httpMethod = "POST"
        //Create post string via string concatenation
        var postString = "userName=" + usernameTextField.text!
        postString += "&password=" + base64Encoded!
        request.httpBody = postString.data(using: .utf8)
        let task = URLSession.shared.dataTask(with: request) { data, response, error in guard let data = data, error == nil else {
                print("error=\(error)")
                done = true
                succ = false
                return
            
            }
        
            if let httpStatus = response as? HTTPURLResponse, httpStatus.statusCode != 200 {
                print("StatusCode should be 200, but it is \(httpStatus.statusCode)")
                print("response = \(response)")
                done = true
                succ = false
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
            }
            else {
                do {
                    let json = try JSONSerialization.jsonObject(with: resData!, options: .allowFragments) as! [String:AnyObject]
                    
                    print(json)
                    print("UserID = \(json["userID"]!)")
                    print("Username = \(json["userName"]!)")
                    print("First name = \(json["firstName"]!)")
                    print("Last name = \(json["lastName"]!)")
                    
                    Shared.shared.userID = json["userID"]! as! Int
                    Shared.shared.username = json["userName"]! as! String
                    Shared.shared.firstname = json["firstName"]! as! String
                    Shared.shared.lastname = json["lastName"]! as! String
                    
                    self.switchView()
                    
                    
                } catch {
                    print("Error with JSON: \(error)")
                }
                
                
            }
            
        }
 
        
        activityIndicator.stopAnimating()
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
