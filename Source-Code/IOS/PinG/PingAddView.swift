//
//  PingAddView.swift
//  PinG
//
//  Created by Koji Tilley on 10/16/16.
//  Worked on by Koji Tilley and Jordan Harlow
//  Copyright © 2016 PinG Team. All rights reserved.
//

// This code is legacy code used on an old iteration. This code is no longer used, but is there as a reference.

import UIKit

class PingAddView: UIView, UITextViewDelegate {

    @IBOutlet var contentView: UIView!
    @IBOutlet weak var eventNameTextField: UITextField!
    @IBOutlet weak var eventDescriptionTextView: UITextView!
    @IBOutlet weak var fromDatePicker: UIDatePicker!
    @IBOutlet weak var toDatePicker: UIDatePicker!
    var placeholderLabel: UILabel!
    
    //Initialization code
    required init(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)!
        initSubviews()
    }
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        initSubviews()
    }
    
    func initSubviews() {
        // standard initialization logic
        let nib = UINib(nibName: "PingAddView", bundle: nil)
        nib.instantiate(withOwner: self, options: nil)
        contentView.frame = bounds
        addSubview(contentView)
            
        // custom initialization logic
        // init textview
        eventDescriptionTextView.layer.cornerRadius = 5
        eventDescriptionTextView.layer.borderColor = UIColor.gray.withAlphaComponent(0.5).cgColor
        eventDescriptionTextView.layer.borderWidth = 0.5
        eventDescriptionTextView.clipsToBounds = true
        eventDescriptionTextView.delegate = self
        // create custom placeholder text for textview
        placeholderLabel = UILabel()
        placeholderLabel.text = "Event Description"
        placeholderLabel.font = UIFont.italicSystemFont(ofSize: (eventDescriptionTextView.font?.pointSize)!)
        placeholderLabel.sizeToFit()
        eventDescriptionTextView.addSubview(placeholderLabel)
        placeholderLabel.frame.origin = CGPoint(x: 5, y: (eventDescriptionTextView.font?.pointSize)!/2)
        placeholderLabel.textColor = UIColor(white: 0, alpha: 0.3)
        placeholderLabel.isHidden = !eventDescriptionTextView.text.isEmpty
    }
    
    //Configuration code
    var eventName: String? {
        get { return eventNameTextField.text }
        set { eventNameTextField.text = newValue }
    }
    
    var eventDescription: String? {
        get { return eventDescriptionTextView.text }
        set { eventNameTextField.text = newValue }
    }
    
    var fromDate: Date {
        get { return fromDatePicker.date }
        set { fromDatePicker.date = newValue }
    }
    
    var toDate: Date {
        get { return toDatePicker.date }
        set { toDatePicker.date = newValue }
    }
    
    //Delegate methods
    func textViewDidChange(_ textView: UITextView) {
        placeholderLabel.isHidden = !textView.text.isEmpty
    }
    /*
    // Only override draw() if you perform custom drawing.
    // An empty implementation adversely affects performance during animation.
    override func draw(_ rect: CGRect) {
        // Drawing code
    }
    */

}
