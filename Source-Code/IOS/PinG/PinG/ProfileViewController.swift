//
//  ProfileViewController.swift
//  PinG
//
//  Created by Arthur Xenophon Karapateas on 11/8/16.
//  Copyright © 2016 PinG Team. All rights reserved.
//

import UIKit

class ProfileViewController: UIViewController {
    
    @IBOutlet weak var nameLabel: UILabel!
    @IBOutlet weak var usernameLabel: UILabel!
    @IBOutlet weak var logoutButton: UIButton!

    override func viewDidLoad() {
        super.viewDidLoad()

        nameLabel.text = Shared.shared.firstname + " " + Shared.shared.lastname
        usernameLabel.text = Shared.shared.username
        
        // Do any additional setup after loading the view.
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    @IBAction func logoutPressed(sender: UIButton) {
        print("xd")
        //Clear shared data
        Shared.shared.username = nil
        Shared.shared.firstname = nil
        Shared.shared.lastname = nil
        Shared.shared.userID = nil
        //Present login view controller
        let view = self.storyboard?.instantiateViewController(withIdentifier: "loginNav")
        self.present(view as! UINavigationController, animated: true, completion: nil)
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
