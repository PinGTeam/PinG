//
//  EditEventTableViewController.swift
//  PinG
//
//  Created by Arthur Xenophon Karapateas on 11/14/16.
//  Copyright © 2016 PinG Team. All rights reserved.
//

import UIKit
import CoreLocation

class EditEventTableViewController: UITableViewController, UITextViewDelegate, CLLocationManagerDelegate {
    
    @IBOutlet weak var nameTextField: UITextField!
    @IBOutlet weak var descriptionTextField: UITextView!
    @IBOutlet weak var fromDetailLabel: UILabel!
    @IBOutlet weak var toDetailLabel: UILabel!
    @IBOutlet weak var fromDatePicker: UIDatePicker!
    @IBOutlet weak var toDatePicker: UIDatePicker!
    
    var timeStamp = Shared.shared.fromTime
    var placeholderLabel: UILabel!
    var fromDate = Shared.shared.fromTime
    var toDate = Shared.shared.toTime
    var fromDatePickerHidden = true
    var toDatePickerHidden = true
    var currentCoordinate = Shared.shared.eventLocation

    override func viewDidLoad() {
        super.viewDidLoad()
        timeStamp = Calendar.current.date(byAdding: .second, value: -1, to: Date())!

        // Uncomment the following line to preserve selection between presentations
        // self.clearsSelectionOnViewWillAppear = false

        // Uncomment the following line to display an Edit button in the navigation bar for this view controller.
        // self.navigationItem.rightBarButtonItem = self.editButtonItem()
        self.tableView.contentInset = UIEdgeInsetsMake(-36, 0, -36, 0);
        // Delegate text view
        descriptionTextField.clipsToBounds = true
        descriptionTextField.delegate = self
        
        // Create placeholder label for text view
        placeholderLabel = UILabel()
        placeholderLabel.text = "Event Description"
        placeholderLabel.font = UIFont.systemFont(ofSize: (descriptionTextField.font?.pointSize)!)
        placeholderLabel.sizeToFit()
        descriptionTextField.addSubview(placeholderLabel)
        placeholderLabel.frame.origin = CGPoint(x: 5, y: (descriptionTextField.font?.pointSize)!/2)
        placeholderLabel.textColor = UIColor(white: 0, alpha: 0.2)
        placeholderLabel.isHidden = !descriptionTextField.text.isEmpty
        
        // Init labels
        let df = DateFormatter()
        df.dateFormat = "h:mm a"
        fromDetailLabel.text = df.string(from: fromDate!)
        toDetailLabel.text = df.string(from: toDate!)
        fromDatePicker.setDate(fromDate!, animated: false)
        toDatePicker.setDate(toDate!, animated: false)
        datePickerChanged(fromDatePicker)
        datePickerChanged(toDatePicker)
        
        nameTextField.text = Shared.shared.eventName
        descriptionTextField.text = Shared.shared.eventDescription
        placeholderLabel.isHidden = !descriptionTextField.text.isEmpty
        
    }
    
    

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    @IBAction func datePickerUsed(sender: UIDatePicker) {
        datePickerChanged(sender)
    }
    
    func datePickerChanged(_ picker: UIDatePicker) {
        let df = DateFormatter()
        df.dateFormat = "h:mm a"
        
        if picker == fromDatePicker {
            fromDetailLabel.text = df.string(from: fromDatePicker.date)
        }
        else if picker == toDatePicker {
            toDetailLabel.text = df.string(from: toDatePicker.date)
        }
    }
    
    func toggleDatePicker(_ picker: UIDatePicker) {
        if picker == fromDatePicker {
            fromDatePickerHidden = !fromDatePickerHidden
        }
        else if picker == toDatePicker {
            toDatePickerHidden = !toDatePickerHidden
        }
        
    }

    // MARK: - Table view data source
/*
    override func numberOfSections(in tableView: UITableView) -> Int {
        // #warning Incomplete implementation, return the number of sections
        return 0
    }

    override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        // #warning Incomplete implementation, return the number of rows
        return 0
    }
*/
    /*
    override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "reuseIdentifier", for: indexPath)

        // Configure the cell...

        return cell
    }
    */

    /*
    // Override to support conditional editing of the table view.
    override func tableView(_ tableView: UITableView, canEditRowAt indexPath: IndexPath) -> Bool {
        // Return false if you do not want the specified item to be editable.
        return true
    }
    */

    /*
    // Override to support editing the table view.
    override func tableView(_ tableView: UITableView, commit editingStyle: UITableViewCellEditingStyle, forRowAt indexPath: IndexPath) {
        if editingStyle == .delete {
            // Delete the row from the data source
            tableView.deleteRows(at: [indexPath], with: .fade)
        } else if editingStyle == .insert {
            // Create a new instance of the appropriate class, insert it into the array, and add a new row to the table view
        }    
    }
    */

    /*
    // Override to support rearranging the table view.
    override func tableView(_ tableView: UITableView, moveRowAt fromIndexPath: IndexPath, to: IndexPath) {

    }
    */

    /*
    // Override to support conditional rearranging of the table view.
    override func tableView(_ tableView: UITableView, canMoveRowAt indexPath: IndexPath) -> Bool {
        // Return false if you do not want the item to be re-orderable.
        return true
    }
    */

    /*
    // MARK: - Navigation

    // In a storyboard-based application, you will often want to do a little preparation before navigation
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        // Get the new view controller using segue.destinationViewController.
        // Pass the selected object to the new view controller.
    }
    */
    
    // MARK: - Delegate methods
    func textViewDidChange(_ textView: UITextView) {
        placeholderLabel.isHidden = !textView.text.isEmpty
    }
    
    override func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        //print(indexPath.section)
        //print(indexPath.row)
        
        if indexPath.section == 1 && indexPath.row == 0 {
            //print("From time edit")
            toggleDatePicker(fromDatePicker)
            toDatePickerHidden = true
            tableView.beginUpdates()
            tableView.endUpdates()
            tableView.deselectRow(at: indexPath, animated: true)
        }
        else if indexPath.section == 1 && indexPath.row == 2 {
            //print("To time edit xD")
            toggleDatePicker(toDatePicker)
            fromDatePickerHidden = true
            tableView.beginUpdates()
            tableView.endUpdates()
            tableView.deselectRow(at: indexPath, animated: true)
        }
        else if indexPath.section == 2 && indexPath.row == 0 {
            
            //add ping
            
            var fdate = fromDatePicker.date
            var tdate = toDatePicker.date
            
            if fdate < timeStamp! {
                fdate = Calendar.current.date(byAdding: .day, value: 1, to: fdate)!
            }
            while tdate < fdate {
                tdate = Calendar.current.date(byAdding: .day, value: 1, to: tdate)!
            }
            
            let df = DateFormatter()
            df.dateFormat = "yyyy-MM-dd HH:mm:ss"
            df.timeZone = TimeZone(abbreviation: "UTC")
            let fdateString = df.string(from: fdate)
            let tdateString = df.string(from: tdate)
            let eventName = nameTextField.text!
            let eventDescription = descriptionTextField.text!
            print(eventName)
            print(eventDescription)
            
            //Create json string from dictionary
            let geojson = ["geometry": ["coordinates": [currentCoordinate!.latitude, currentCoordinate!.longitude], "type": "Point"], "properties": ["description": eventDescription, "eventName": eventName, "startTime": fdateString, "endTime": tdateString, "eventID": Shared.shared.eventID!], "type": "Feature"] as [String : Any]
            
            do {
                let jsonData = try JSONSerialization.data(withJSONObject: geojson, options: .init(rawValue: 0))
                let jsonText = String(data: jsonData, encoding: String.Encoding.ascii)
                var succ = false
                var err = false
                var msg = ""
                
                //Prepare to use http post method to add the jsonText on the server
                var request = URLRequest(url: URL(string: "http://162.243.15.139/editevent")!)
                request.httpMethod = "POST"
                //Create post string
                let postString = "event=" + jsonText!
                print("\(postString)")
                request.httpBody = postString.data(using: .utf8)
                print("Begin post request")
                let task = URLSession.shared.dataTask(with: request) { data, response, error in guard let data = data, error == nil else {
                    print("error=\(error)")
                    err = true
                    msg = "Unable to connect to the internet."
                    succ = true
                    return
                    
                    }
                    
                    if let httpStatus = response as? HTTPURLResponse, httpStatus.statusCode != 200 {
                        print("StatusCode should be 200, but it is \(httpStatus.statusCode)")
                        msg = "HTTP status code \(httpStatus.statusCode)"
                        print("response = \(response)")
                        succ = true
                        err = true
                    }
                    
                    let responseString = String(data: data, encoding: .utf8)
                    print("responseString = \(responseString)")
                    
                    succ = true
                }
                task.resume()
                
                while !succ {
                    Thread.sleep(forTimeInterval: 0.1)
                }
                
                if err {
                    //Create alert
                    
                    let Alert = UIAlertController(title: "Error connecting to server", message: msg, preferredStyle: UIAlertControllerStyle.alert)
                    
                    let OkButton = UIAlertAction(title: "OK", style: UIAlertActionStyle.default, handler: nil)
                    
                    Alert.addAction(OkButton)
                    
                    self.present(Alert, animated: true, completion: nil)
                }
                
                else {
                    //Edit shared class
                    Shared.shared.eventName = nameTextField.text!
                    Shared.shared.eventDescription = descriptionTextField.text!
                    Shared.shared.fromTime = fromDatePicker.date
                    Shared.shared.toTime = toDatePicker.date
                    self.navigationController?.popViewController(animated: true)
                }
                
            } catch {
                print(error.localizedDescription)
            }
        }
    }
    
    override func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        if fromDatePickerHidden && indexPath.section == 1 && indexPath.row == 1 {
            return 0
        }
        else if toDatePickerHidden && indexPath.section == 1 && indexPath.row == 3 {
            return 0
        }
        else {
            return super.tableView(tableView, heightForRowAt: indexPath)
        }
    }
    
    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        //Grab current location
        let location: CLLocation = locations.last!
        currentCoordinate = location.coordinate
        
    }
}
