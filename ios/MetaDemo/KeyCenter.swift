//
//  KeyCenter.swift
//  MetaDemo
//
//  Created by ZhouRui on 2023/8/18.
//

import Foundation

@objc class KeyCenter: NSObject {
    
    @objc static let APP_ID: String = <#APP_ID#>
    @objc static let certificate: String = <#certificate#>
    @objc static let SCENE_ID = 1
    @objc static var CHANNEL_ID: String = "MetaDemoTest"
    
    @objc static var RTC_UID: UInt {
        let key = "RTC_UID"
        var uid = UserDefaults.standard.integer(forKey: key)
        if uid == 0 {
            uid = Int(arc4random()) % 100000
            UserDefaults.standard.set(uid, forKey: key)
        }
        return UInt(uid)
    }

    @objc static let RTM_UID: String = "\(RTC_UID)"
    @objc static var RTM_TOKEN: String? {
        MetaTokenCreater.createRTMToken(withAppid: KeyCenter.APP_ID,
                                            certificate: KeyCenter.certificate,
                                            userid: KeyCenter.RTM_UID)
    }
    // 获取rtcToken
    static func rtcToken(channelID:String) ->String?{
        MetaTokenCreater.createRTCToken(withAppid: KeyCenter.APP_ID, certificate: KeyCenter.certificate, channelid: channelID, userid: UInt32(KeyCenter.RTC_UID))
    }
}
