//
//  DressInfo.swift
//  MetaDemo
//
//  Created by ZhouRui on 2023/5/11.
//

import Foundation

class DressInfoModel: Codable {
    var dressResources: [DressResources]?
    var faceParameters: [FaceParameters]?
    
    class DressResources: Codable {
        var avatar: String = ""
        var resources: [Resources]?
        
        class Resources: Codable {
            var id: Int?
            var name: String?
            var assets: [Int]?
        }
    }
    
    class FaceParameters: Codable {
        var avatar: String = ""
        var blendshape: [BlendShape]?
        
        class BlendShape: Codable {
            var type: String?
            var shapes: [Shapes]?
            
            class Shapes: Codable {
                var key: String?
                var ch: String?
            }
        }
    }
}
