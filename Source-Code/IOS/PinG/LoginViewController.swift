//
//  LoginViewController.swift
//  PinG
//
//  Created by Koji Tilley on 10/9/16.
//  Copyright © 2016 PinG Team. All rights reserved.
//

import UIKit

class LoginViewController: UIViewController {

    @IBOutlet weak var usernameTextField: UITextField!
    @IBOutlet weak var passTextField: UITextField!
    @IBOutlet weak var firstnameTextField: UITextField!
    @IBOutlet weak var lastnameTextField: UITextField!
    
    
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
        var succ = false    //S U C C
        
        //Verify fields
        
        
        print("Login button pressed")
        print("Username: %s", usernameTextField.text)
        print("Password: %s", passTextField.text)
        
        //HTTP Post method
        var request = URLRequest(url: URL(string: "http://162.243.15.139/adduser")!)
        request.httpMethod = "POST"
        //Create post string via string concatenation
        var postString = "UserName=" + usernameTextField.text!
        postString += "&Name=" + firstnameTextField.text!
        postString += "&LName=" + lastnameTextField.text!
        request.httpBody = postString.data(using: .utf8)
        let task = URLSession.shared.dataTask(with: request) { data, response, error in guard let data = data, error == nil else {
                print("error=\(error)")
                done = true
                return
            
            }
        
            if let httpStatus = response as? HTTPURLResponse, httpStatus.statusCode != 200 {
                print("StatusCode should be 200, but it is \(httpStatus.statusCode)")
                print("response = \(response)")
                done = true
            }
            
            let responseString = String(data: data, encoding: .utf8)
            print("responseString = \(responseString)")
            done = true
            succ = true     //good succ is always a success
            Shared.shared.userID = Int(responseString!)
        }
        task.resume()
            
        //stay in function until async completion
        while !done {
            Thread.sleep(forTimeInterval: 0.25)
        }
        
        if succ {
            self.switchView()
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
