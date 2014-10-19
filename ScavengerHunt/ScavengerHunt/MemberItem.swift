
import UIKit

class MemberItem: NSObject, NSCoding {
    
    let NameKey = "Name"
    let PhotoKey = "Photo"
    let MoneyKey = "Money"
    
    
    let name: String
    var photo: UIImage?
    var money: Double
    var isComplete: Bool {
        get {
            return photo != nil
        }
    }
    
    init(name: String, money: Double){
        self.name = name
        self.money = money
    }
    
    func encodeWithCoder(coder: NSCoder) {
        coder.encodeObject(name, forKey: NameKey)
        coder.encodeObject(money, forKey: MoneyKey)

        /*if let theMoney = money {
            coder.encodeObject(theMoney, forKey: MoneyKey)
        }*/
        
        if let thePhoto = photo {
            coder.encodeObject(thePhoto, forKey: PhotoKey)
        }
    }
    
    required init(coder aDecoder: NSCoder) {
        name = aDecoder.decodeObjectForKey(NameKey) as String
        photo = aDecoder.decodeObjectForKey(PhotoKey) as? UIImage
        money = aDecoder.decodeObjectForKey(MoneyKey) as Double
    }
    
}
