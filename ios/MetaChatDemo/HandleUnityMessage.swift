//
//  HandleUnityMessage.swift
//  MetaChatDemo
//
//  Created by FanPengpeng on 2022/7/8.
//

import Foundation

struct UnityObjMsg: Codable {
    var id:Int = 0
    var position:[NSNumber] = [NSNumber]()
    var forward:[NSNumber] = [NSNumber]()
    var right:[NSNumber] = [NSNumber]()
    var up:[NSNumber] = [NSNumber]()
    
    init(from decoder: Decoder) throws {
        
    }
    
    func encode(to encoder: Encoder) throws {
        
    }
}
