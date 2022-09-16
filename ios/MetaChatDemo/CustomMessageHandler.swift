//
//  File.swift
//  MetaChatDemo
//
//  Created by FanPengpeng on 2022/7/8.
//

import Foundation


/// 物体的id
enum ObjectID: Int {
    case tv = 1 // 电视
    case npcTable = 2 // NPC桌子
    case npcMove1 = 3 // 移动NPC1
    case npcMove2 = 4 // 移动NPC2
}

/// 消息类型
enum MessageType: Int {
    // 获取物体位置
    case postion = 1
    // 点击k歌按钮
    case ktvBtnClicked = 2
    // k歌结束按钮
    case ktvFinishBtnClicked = 3
}

/// 返回结果
enum MessageResult {
    case objectPosition(id:ObjectID, position:[NSNumber], forward:[NSNumber] = [1,0,0])
    case didClickKTVBtn
    case didClickFinishKTVBtn
}

class CustomMessageHandler {
    
    static let shared = CustomMessageHandler()
    
    func handleMessage(_ msgStr: String, callback:((_ ret: MessageResult)->Void)) {
        guard let jsonData:Data = msgStr.data(using: .utf8) else {return}
        guard let json: [String: Any] = try? JSONSerialization.jsonObject(with: jsonData) as? [String: Any] else {
            return
        }
        guard let type:Int = json["type"] as? Int, let data = json["data"] as? [String: Any] else {
            return
        }
        
        if let msgType: MessageType = MessageType(rawValue: type) {
            switch msgType {
            // 如果是获取物体位置信息
            case .postion:
                guard let id = data["id"] as? Int, let positionDic = data["position"] as? [String : NSNumber] else {
                    return
                }
                guard let postionArr = arrayFormDic(positionDic) else {return}
                if let objID = ObjectID(rawValue: id) {
                    var forwardArr: [NSNumber] = [1,0,0]
                    if let forwardDic = data["forward"] as? [String: NSNumber] {
                        forwardArr = arrayFormDic(forwardDic) ?? [1,0,0]
                    }
                    callback(.objectPosition(id: objID, position: postionArr, forward: forwardArr))
                }
            case .ktvBtnClicked:
                callback(.didClickKTVBtn)
            case .ktvFinishBtnClicked:
                callback(.didClickFinishKTVBtn)
            }
        }
    }
    
    private func arrayFormDic(_ dic: [String: NSNumber]) -> [NSNumber]? {
        guard let x = dic["x"], let y = dic["y"] ,let z = dic["z"] else { return nil}
        return [x,y,z]
    }
}
