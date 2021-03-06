//
//  ViewEventTableViewController.swift
//  PinG
//
//  Created by Arthur Xenophon Karapateas on 11/14/16.
//  Copyright © 2016 PinG Team. All rights reserved.
//

import UIKit

class ViewEventTableViewController: UITableViewController, UITextViewDelegate {
    
    @IBOutlet weak var eventNameLabel: UILabel!
    @IBOutlet weak var eventHostnameLabel: UILabel!
    @IBOutlet weak var descriptionTextField: UITextView!
    @IBOutlet weak var fromDetailLabel: UILabel!
    @IBOutlet weak var toDetailLabel: UILabel!
    @IBOutlet weak var attendingDetailLabel: UILabel!
    @IBOutlet weak var attendingTableCell: UITableViewCell!
    @IBOutlet weak var attendingSwitch: UISwitch!
    
    var placeholderLabel: UILabel!
    var fromDate: Date!
    var toDate: Date!
    var editButton: UIBarButtonItem!
    var numAttendees: Int!

    override func viewDidLoad() {
        super.viewDidLoad()

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
        
        // Set up switch cell
        attendingTableCell.accessoryView = attendingSwitch
        if Shared.shared.attending == 1 {
            attendingSwitch.isOn = true
        }
        else {
            attendingSwitch.isOn = false
        }
        
        // Init labels
        eventNameLabel.text = Shared.shared.eventName
        eventHostnameLabel.text = Shared.shared.eventFullName
        descriptionTextField.text = Shared.shared.eventDescription
        fromDate = Shared.shared.fromTime
        toDate = Shared.shared.toTime
        let df = DateFormatter()
        df.dateFormat = "h:mm a"
        fromDetailLabel.text = df.string(from: fromDate)
        toDetailLabel.text = df.string(from: toDate)
        numAttendees = Shared.shared.eventAttendance!
        attendingDetailLabel.text = "\(numAttendees!) attendee"
        if numAttendees != 1 {
            let newLabel = attendingDetailLabel.text! + "s"
            attendingDetailLabel.text = newLabel
        }
        
        placeholderLabel.isHidden = !descriptionTextField.text.isEmpty
        
        //Add edit button if you are the one who made the ping
        if Shared.shared.userID == Shared.shared.eventUserID {
            print("xd")
            editButton = UIBarButtonItem(barButtonSystemItem: UIBarButtonSystemItem.edit, target: self, action: #selector(editButtonPressed))
            self.navigationItem.rightBarButtonItem = editButton
        }
    }
    
    override func viewDidAppear(_ animated: Bool) {
        eventNameLabel.text = Shared.shared.eventName
        eventNameLabel.text = Shared.shared.eventName
        eventHostnameLabel.text = Shared.shared.eventFullName
        descriptionTextField.text = Shared.shared.eventDescription
        fromDate = Shared.shared.fromTime
        toDate = Shared.shared.toTime
        let df = DateFormatter()
        df.dateFormat = "h:mm a"
        fromDetailLabel.text = df.string(from: fromDate)
        toDetailLabel.text = df.string(from: toDate)
        attendingDetailLabel.text = "\(numAttendees!) attendee"
        if numAttendees != 1 {
            let newLabel = attendingDetailLabel.text! + "s"
            attendingDetailLabel.text = newLabel
        }
        placeholderLabel.isHidden = !descriptionTextField.text.isEmpty
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    func editButtonPressed(sender: UIBarButtonItem) {
        self.performSegue(withIdentifier: "editEvent", sender: sender)
    }
    
    @IBAction func attendSwitchPressed(sender: UISwitch) {
        if attendingSwitch.isOn == true {
            attendingDetailLabel.text = "\(numAttendees! + 1) attendee"
            if (numAttendees + 1) != 1 {
                let newLabel = attendingDetailLabel.text! + "s"
                attendingDetailLabel.text = newLabel
            }
        }
        else {
            attendingDetailLabel.text = "\(numAttendees! - 1) attendee"
            if (numAttendees - 1) != 1 {
                let newLabel = attendingDetailLabel.text! + "s"
                attendingDetailLabel.text = newLabel
            }
        }
        var request = URLRequest(url: URL(string: "http://162.243.15.139/attend")!)
        request.httpMethod = "POST"
        //Create post string via string concatenation
        let postString = "userID=\(Shared.shared.userID!)&eventID=\(Shared.shared.eventID!)"
        print(postString)
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
            
            if responseString == "attending" {
                self.attendingSwitch.setOn(true, animated: true)
                self.numAttendees = self.numAttendees + 1
            }
            else if responseString == "not attending" {
                self.attendingSwitch.setOn(false, animated: true)
                self.numAttendees = self.numAttendees - 1
            }
            
            
            self.tableView.beginUpdates()
            self.tableView.endUpdates()
            
        }
        task.resume()
        
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
        
        if indexPath.section == 2 && indexPath.row == 1 {
            tableView.deselectRow(at: indexPath, animated: true)
            //view attendees
            var succ = false
            print("Testing request")
            var request = URLRequest(url: URL(string: "http://162.243.15.139/getattendance?eventID=\(Shared.shared.eventID!)")!)
            request.httpMethod = "GET"
            let task = URLSession.shared.dataTask(with: request) { data, response, error in guard let data = data, error == nil else {
                print("error=\(error)")
                succ = true
                return
                
                }
                let httpStatus = response as! HTTPURLResponse
                print("In url session method")
                
                if httpStatus.statusCode != 200 {
                    print("StatusCode should be 200, but it is \(httpStatus.statusCode)")
                    print("response = \(response)")
                }
                else {
                    //Parse jason
                    print("Begin retrieving json")
                    do {
                        let json = try JSONSerialization.jsonObject(with: data, options: .allowFragments) as! [AnyObject]
                        
                        print(json)
                        print(json.count)
                        for attendee in json {
                            print("\(attendee["firstName"]!) \(attendee["lastName"]!)")
                        }
                        Shared.shared.eventAttendees = json
                        
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
            
            //prepare for segue
            self.performSegue(withIdentifier: "viewAttendees", sender: self)
            
        }
    }
    
}
