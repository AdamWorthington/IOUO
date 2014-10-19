
import Foundation

class ItemsManager {
    var items = [ScavengerHuntItem]()
    
    lazy private var archivePath: String = {
        let fileManager = NSFileManager.defaultManager()
        let documentsDirectoryURLs = fileManager.URLsForDirectory(.DocumentDirectory, inDomains: .UserDomainMask)
            as [NSURL]
        
        let archiveURL = documentsDirectoryURLs.first!.URLByAppendingPathComponent("ScavengerHuntItems", isDirectory: false)
        return archiveURL.path!
    }()
    func save() {
        NSKeyedArchiver.archiveRootObject(items, toFile: archivePath)
    }
    private func unarchiveSavedItems() {
        if NSFileManager.defaultManager().fileExistsAtPath(archivePath){
            let saveItems = NSKeyedUnarchiver.unarchiveObjectWithFile(archivePath) as [ScavengerHuntItem]
            items = saveItems
        }
        
    }
    
    init() {
        unarchiveSavedItems()
    }
    
}


