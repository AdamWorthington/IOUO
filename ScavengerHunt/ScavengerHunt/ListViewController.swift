
import UIKit
import LocalAuthentication

class ListViewController : UITableViewController, UIImagePickerControllerDelegate, UINavigationControllerDelegate, UIAlertViewDelegate {
    
    let clientTCP = ClientTCP()
    
    let owner = "Dan"
    let client1 = "Adam"
    let client2 = "Deegan"
    let client3 = "Raaghav"
    
    override func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view, typically from a nib.
        
        authenticateUser()
        populate()
        clientTCP.startStream()
        clientTCP.writeString(owner)
        //clientTCP.writeString("/kill")
    }
    
    var itemsManager = ItemsManager()
    var memberManager = MemberManager()
    override func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        
        return itemsManager.items.count
    }
    
    @IBAction func refresh(sender: AnyObject) {
        populate()
    }
    func tokenizer(string: String) -> [String] {
        let tokenizedString = string.componentsSeparatedByString("|")
        return tokenizedString
    }
    
    func populate() {
        clientTCP.startStream()
        clientTCP.writeString(owner)
        var groups = clientTCP.writeStringAndWaitForReturn("/groups")
        var groupArray = tokenizer(groups)
        
        var tempItems = itemsManager.items
        itemsManager.items.removeAll(keepCapacity: false)
        for var i = 0; i < groupArray.count; i++  {
            
            var nextGroup = ScavengerHuntItem(name: groupArray[i])
            var members = clientTCP.writeStringAndWaitForReturn("/members " + nextGroup.name)
            var memberArray = tokenizer(members)
            if i < tempItems.count {
                tempItems[i].members.removeAll(keepCapacity: false)
            }
            for var j = 0; j < memberArray.count; j++ {
                var memberValue = clientTCP.writeStringAndWaitForReturn("/update " + nextGroup.name + " " + memberArray[j])
                var string = NSString(string: memberValue)
                var memberValueDouble = string.doubleValue
                var nextMember = MemberItem(name: memberArray[j], money: memberValueDouble)
                nextGroup.members.append(nextMember)
            }
            
            itemsManager.items.append(nextGroup)
        }
        tableView.reloadData()
        
    }
    
    override func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        
            let cell = tableView.dequeueReusableCellWithIdentifier("ListViewCell") as UITableViewCell
            let item = itemsManager.items[indexPath.row]
            cell.textLabel!.text = item.name
            if item.isComplete {
                cell.accessoryType = .Checkmark
                cell.imageView!.image = item.photo
            } else {
                cell.accessoryType = .DisclosureIndicator
                cell.imageView?.image = nil
            }
            return cell
        }
    
    /*
    override func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {
        let imagePicker = UIImagePickerController()
        if UIImagePickerController.isSourceTypeAvailable(UIImagePickerControllerSourceType.Camera){
            imagePicker.sourceType = .Camera
        } else {
            imagePicker.sourceType = .PhotoLibrary
        }
        imagePicker.delegate = self
        
        presentViewController(imagePicker, animated: true, completion: nil)
        /*func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?) {
            if segue.identifier=="ClickItem"{
                
            }
        }*/
    }*/
    
    func imagePickerController(picker: UIImagePickerController, didFinishPickingMediaWithInfo info: [NSObject : AnyObject]) {
        let indexPath = tableView.indexPathForSelectedRow()!
        let selectedItem = itemsManager.items[indexPath.row]
        
        let photo = info[UIImagePickerControllerOriginalImage] as UIImage
        selectedItem.photo = photo
        itemsManager.save()
        
        dismissViewControllerAnimated(true, completion:{
            self.tableView.deselectRowAtIndexPath(indexPath, animated: true)
            self.tableView.reloadRowsAtIndexPaths([indexPath], withRowAnimation: .Automatic)
            
        })
    }
    override func tableView(tableView: UITableView, commitEditingStyle editingStyle: UITableViewCellEditingStyle, forRowAtIndexPath indexPath: NSIndexPath) {
        if editingStyle == UITableViewCellEditingStyle.Delete{
            itemsManager.items.removeAtIndex(indexPath.row)
            tableView.deleteRowsAtIndexPaths([indexPath], withRowAnimation: UITableViewRowAnimation.Automatic)
            itemsManager.save()
            memberManager.items.removeAll(keepCapacity: false)
            memberManager.save()
            
        }
    }
    
    @IBAction func unwindToList(segue: UIStoryboardSegue){
        if segue.identifier == "DoneItem" {
            let addItemController = segue.sourceViewController as ViewController
            if let newItem = addItemController.createdItem {
                itemsManager.items += [newItem]
                itemsManager.save()
                
                clientTCP.startStream()
                clientTCP.writeString(owner)
                
                let addGroup = "/addgroup " + newItem.name
                clientTCP.writeString(addGroup)
                
                let addMember = "/addmember " + newItem.name + " " + owner
                clientTCP.writeString(addMember)
                
                //clientTCP.writeString("/kill")
                
                let indexPath = NSIndexPath(forRow: itemsManager.items.count - 1, inSection: 0)
                tableView.insertRowsAtIndexPaths([indexPath], withRowAnimation: UITableViewRowAnimation.Automatic)
            }
        }
    }
    
    func authenticateUser() {
        // Get the local authentication context.
        let context = LAContext()
        
        // Declare a NSError variable.
        var error: NSError?
        
        // Set the reason string that will appear on the authentication alert.
        var reasonString = "Authentication is needed to access your notes."
        
        // Check if the device can evaluate the policy.
        if context.canEvaluatePolicy(LAPolicy.DeviceOwnerAuthenticationWithBiometrics, error: &error) {
            [context .evaluatePolicy(LAPolicy.DeviceOwnerAuthenticationWithBiometrics, localizedReason: reasonString, reply: { (success: Bool, evalPolicyError: NSError?) -> Void in
                
                if success {
                    
                }
                else{
                    // If authentication failed then show a message to the console with a short description.
                    // In case that the error is a user fallback, then show the password alert view.
                    println(evalPolicyError?.localizedDescription)
                    
                    switch evalPolicyError!.code {
                        
                    case LAError.SystemCancel.toRaw():
                        println("Authentication was cancelled by the system")
                        self.showPasswordAlert()
                        
                    case LAError.UserCancel.toRaw():
                        println("Authentication was cancelled by the user")
                        self.showPasswordAlert()
                        
                    case LAError.UserFallback.toRaw():
                        println("User selected to enter custom password")
                        self.showPasswordAlert()
                        
                    default:
                        println("Authentication failed")
                        self.showPasswordAlert()
                    }
                }
                
            })]
        }
        else{
            // If the security policy cannot be evaluated then show a short message depending on the error.
            switch error!.code{
                
            case LAError.TouchIDNotEnrolled.toRaw():
                println("TouchID is not enrolled")
                
            case LAError.PasscodeNotSet.toRaw():
                println("A passcode has not been set")
                
            default:
                // The LAError.TouchIDNotAvailable case.
                println("TouchID not available")
            }
            
            // Optionally the error description can be displayed on the console.
            println(error?.localizedDescription)
            
            // Show the custom alert view to allow users to enter the password.
            self.showPasswordAlert()
        }
    }
    
    func showPasswordAlert() {
        var passwordAlert : UIAlertView = UIAlertView(title: "TouchID", message: "Please type your password", delegate: self, cancelButtonTitle: "Cancel", otherButtonTitles: "Okay")
        passwordAlert.alertViewStyle = UIAlertViewStyle.SecureTextInput
        passwordAlert.show()
    }
    
    func alertView(alertView: UIAlertView!, clickedButtonAtIndex buttonIndex: Int) {
        if buttonIndex == 1 {
            if !alertView.textFieldAtIndex(0)!.text.isEmpty {
                if alertView.textFieldAtIndex(0)!.text == " " {
                    
                }
                else{
                    showPasswordAlert()
                }
            }
            else{
                showPasswordAlert()
            }
        }
        else {
            showPasswordAlert()
        }
    }
    
    override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?) {
        if let destination = segue.destinationViewController as? ListCoverController {
            let selectedItem = itemsManager.items[tableView.indexPathForSelectedRow()!.row]
            destination.scavengerHuntItem = selectedItem
        }
    }
    
    override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated)
        itemsManager.save()
    }
}

