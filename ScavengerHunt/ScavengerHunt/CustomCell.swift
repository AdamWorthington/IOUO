//
//  CustomCell.swift
//  ScavengerHunt
//
//  Created by Raaghav Karthik on 10/18/14.
//  Copyright (c) 2014 Raaghav. All rights reserved.
//

import UIKit

class CustomCell: UITableViewCell {

    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
    }

    override func setSelected(selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)

        // Configure the view for the selected state
    }
    
    @IBOutlet weak var leftLabel: UILabel!
    @IBOutlet weak var rightLabel: UILabel!
    
    
    
    
}
