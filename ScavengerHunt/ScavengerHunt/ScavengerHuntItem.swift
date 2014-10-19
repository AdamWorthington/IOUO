
import UIKit

class ScavengerHuntItem: NSObject, NSCoding {
    
    let NameKey = "Name"
    let PhotoKey = "Photo"
    let MembersKey = "Members"
    var members: [MemberItem] = []
    //var memberManager = MemberManager()
    
    let name: String
    var photo: UIImage?
    //var mem: objc_object
    var isComplete: Bool {
        get {
            return photo != nil
        }
    }
    
    init(name: String){
        self.name = name 
    }
    
    func encodeWithCoder(coder: NSCoder) {
        coder.encodeObject(name, forKey: NameKey)
        //coder.encodeObject(members, forKey: MembersKey)
        if let thePhoto = photo {
            coder.encodeObject(thePhoto, forKey: PhotoKey)
        }
        
        coder.encodeObject(members, forKey: MembersKey)
    }
    
    required init(coder aDecoder: NSCoder) {
        name = aDecoder.decodeObjectForKey(NameKey) as String
        photo = aDecoder.decodeObjectForKey(PhotoKey) as? UIImage
        members = aDecoder.decodeObjectForKey(MembersKey) as [MemberItem]
        //mem = aDecoder.decodeObjectForKey(memberManager) as objc_object
    }
    
}
