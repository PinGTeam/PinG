//
//  NewUserViewController.swift
//  PinG
//
//  Created by Koji Tilley on 10/27/16.
//  Copyright © 2016 PinG Team. All rights reserved.
//

import UIKit

class NewUserViewController: UIViewController {
    
    @IBOutlet weak var firstNameTextField: UITextField!
    @IBOutlet weak var lastNameTextField: UITextField!
    @IBOutlet weak var userNameTextField: UITextField!
    @IBOutlet weak var emailTextField: UITextField!
    @IBOutlet weak var passwordTextField: UITextField!
    @IBOutlet weak var confirmPassTextField: UITextField!
    @IBOutlet weak var signUpButton: UIButton!
    @IBOutlet weak var errorTextView: UITextView!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        // Do any additional setup after loading the view.
        errorTextView.text = ""
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    @IBAction func signUpPressed(sender: UIButton) {
        // Local var init
        func errorField(field: UITextField) {
            field.layer.borderColor = UIColor.red.cgColor
            field.layer.borderWidth = 0.8
            field.layer.cornerRadius = 4.0
            field.layer.masksToBounds = true
        }
        
        func isValidEmail(testStr:String) -> Bool {
            let emailRegEx = "[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}"
            
            let emailTest = NSPredicate(format:"SELF MATCHES %@", emailRegEx)
            return emailTest.evaluate(with: testStr)
        }
        
        // default colors for each field
        firstNameTextField.layer.borderColor = UIColor.clear.cgColor
        lastNameTextField.layer.borderColor = UIColor.clear.cgColor
        userNameTextField.layer.borderColor = UIColor.clear.cgColor
        emailTextField.layer.borderColor = UIColor.clear.cgColor
        passwordTextField.layer.borderColor = UIColor.clear.cgColor
        confirmPassTextField.layer.borderColor = UIColor.clear.cgColor
        errorTextView.text = ""
        
        // Client side checks before submitting form
        // Check for empty fields
        if firstNameTextField.text == "" || lastNameTextField.text == "" || userNameTextField.text == "" || emailTextField.text == "" || passwordTextField.text == "" || confirmPassTextField.text == "" {
            errorTextView.text = "Fields cannot be left blank."
            if firstNameTextField.text == "" {
                errorField(field: firstNameTextField)
            }
            if lastNameTextField.text == "" {
                errorField(field: lastNameTextField)
            }
            if userNameTextField.text == "" {
                errorField(field: userNameTextField)
            }
            if emailTextField.text == "" {
                errorField(field: emailTextField)
            }
            if passwordTextField.text == "" {
                errorField(field: passwordTextField)
            }
            if confirmPassTextField.text == "" {
                errorField(field: confirmPassTextField)
            }
            return  //exit out of function
        }
        
        // Check if valid email
        if !isValidEmail(testStr: emailTextField.text!) {
            errorTextView.text = "Invalid e-mail."
            errorField(field: emailTextField)
            return  //exit out of function
        }
        
        // Check if passwords match
        if passwordTextField.text != confirmPassTextField.text {
            errorTextView.text = "Passwords do not match."
            errorField(field: confirmPassTextField)
            return  //exit out of function
        }
        
        //Encode password into base64
        let utf8str = confirmPassTextField.text?.data(using: String.Encoding.utf8)
        let base64Encoded = utf8str?.base64EncodedString(options: NSData.Base64EncodingOptions(rawValue: 0))
        print("Encoded: \(base64Encoded!)")
        
        // Send account creation request to server
        var done = false
        var succ = true
        var resString:String?
        var request = URLRequest(url: URL(string: "http://162.243.15.139/adduser")!)
        request.httpMethod = "POST"
        //Create post string via string concatenation
        var postString = "userName=" + userNameTextField.text!
        postString += "&firstName=" + firstNameTextField.text!
        postString += "&lastName=" + lastNameTextField.text!
        postString += "&password=" + base64Encoded!
        postString += "&email=" + emailTextField.text!
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
            print("responseString = \(responseString!)")
            resString = responseString!
            
            done = true
        }
        task.resume()
        
        while !done {
            Thread.sleep(forTimeInterval: 0.1)
        }
        
        // Error depending on response string
        if !succ {
            errorTextView.text = "Unsuccessful connection to server."
            return  //exit out of function
        }
        
        if resString != "1" {
            if resString == "-1" {
                errorTextView.text = "The specified e-mail already exists for a user in the database."
                errorField(field: emailTextField)
                return
            }
            else if resString == "-2" {
                errorTextView.text = "The specified username is already taken."
                errorField(field: userNameTextField)
                return
            }
            else if resString == "-3" {
                errorTextView.text = "The username and e-mail both exist on the database."
                errorField(field: userNameTextField)
                errorField(field: emailTextField)
                return
            }
            else {
                errorTextView.text = "Unknown response from server. Blame Juan."
                return
            }
        }
        
        // Return to login view controller
        self.navigationController?.popViewController(animated: true)
        
        
    }
    /*
    // MARK: - Navigation

    // In a storyboard-based application, you will often want to do a little preparation before navigation
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        // Get the new view controller using segue.destinationViewController.
        // Pass the selected object to the new view controller.
    }
    */

    @IBAction func prepareForUnwind(sender: Any?) {
        
    }
}
