//
//  ListCoverControllerViewController.swift
//  ScavengerHunt
//
//  Created by Raaghav Karthik on 10/18/14.
//  Copyright (c) 2014 Raaghav. All rights reserved.
//

import UIKit
class ListCoverController : UITableViewController, UIImagePickerControllerDelegate, UINavigationControllerDelegate {
    
    @IBOutlet weak var leftLabel: UILabel!
    @IBOutlet weak var rightLabel: UILabel!
    @IBOutlet var coverTableView: UITableView!
    
    let clientTCP = ClientTCP()
    var lastClicked: Int = -1
    
    let owner = "Dan"
    let client1 = "Adam"
    let client2 = "Deegan"
    let client3 = "Raaghav"
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        // Do any additional setup after loading the view.
    }
    
    var scavengerHuntItem: ScavengerHuntItem!
    
//    var memberManager = MemberManager()
    
    override func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        
        return scavengerHuntItem.members.count
    }
    
    override func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        
        let cell = tableView.dequeueReusableCellWithIdentifier("MemberRowCell") as CustomCell
        let item = scavengerHuntItem.members[indexPath.row]
        lastClicked = indexPath.row
        
        //clientTCP.startStream()
        //clientTCP.writeString(owner)
        
        
        
        let a = item.name
        let b = String(format:"%.2f", item.money)
        
        if item.money > 0 {
            cell.rightLabel.textColor = UIColor.greenColor()
        }
        else if item.money < 0 {
            cell.rightLabel.textColor = UIColor.redColor()
        }
        
        cell.leftLabel!.text = a
        cell.rightLabel!.text = b
        
        if item.isComplete{
            cell.accessoryType = .Checkmark
            cell.imageView!.image = item.photo
        } else {
            cell.accessoryType = .None
            cell.imageView?.image = nil
        }
        return cell
    }
    
    override func tableView(tableView: UITableView, commitEditingStyle editingStyle: UITableViewCellEditingStyle, forRowAtIndexPath indexPath: NSIndexPath) {
        if editingStyle == UITableViewCellEditingStyle.Delete{
            scavengerHuntItem.members.removeAtIndex(indexPath.row)
            tableView.deleteRowsAtIndexPaths([indexPath], withRowAnimation: UITableViewRowAnimation.Automatic)

            
        }
    }
    
    @IBAction func unwindToList(segue: UIStoryboardSegue){
        if segue.identifier == "DoneItem" {
            let addItemController = segue.sourceViewController as MoneyController
            if let newItem = addItemController.createdItem {
                scavengerHuntItem.members += [newItem]
                
                clientTCP.startStream()
                clientTCP.writeString(owner)
                
                let addMember = "/addmember " + scavengerHuntItem.name + " " + newItem.name
                clientTCP.writeString(addMember)
                let moneyAdd = "/alterValues " + scavengerHuntItem.name + " " + owner + " " + newItem.name + " " + String(format:"%.2f", newItem.money)
                clientTCP.writeString(moneyAdd)
                
                let indexPath = NSIndexPath(forRow: scavengerHuntItem.members.count - 1, inSection: 0)
                tableView.insertRowsAtIndexPaths([indexPath], withRowAnimation: UITableViewRowAnimation.Automatic)
            }
        }
        else if segue.identifier == "DoneItem1" {
            let addItemController = segue.sourceViewController as UpdateMoneyController
//            let indexPath = NSIndexPath(forRow: scavengerHuntItem.members.count - 1, inSection: 0)
            let newItem = scavengerHuntItem.members[tableView.indexPathForSelectedRow()!.row]
            
            clientTCP.startStream()
            clientTCP.writeString(owner)
            
            let moneyAdd = String(format:"%.2f", newItem.money)
            let alterValues = "/alterValues" + " " + scavengerHuntItem.name + " " + owner + " " + newItem.name + " " + moneyAdd
            clientTCP.writeString(alterValues)
            
            let update = "/update" + " "  + scavengerHuntItem.name + " " + newItem.name
            let retVal = clientTCP.writeStringAndWaitForReturn(update)
            newItem.money = (retVal as NSString).doubleValue
            
            tableView.reloadRowsAtIndexPaths([tableView.indexPathForSelectedRow()!], withRowAnimation: .Automatic)
            println("I GOT SOMETHING WTF: \(retVal)")
        }
    }
    
    override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?) {
        if let destination = segue.destinationViewController as? UpdateMoneyController {
            let newItem = scavengerHuntItem.members[tableView.indexPathForSelectedRow()!.row]
            destination.createdItem = newItem
        }
    }
    
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    
    
}
