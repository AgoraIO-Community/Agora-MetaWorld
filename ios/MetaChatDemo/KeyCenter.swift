//
//  KeyCenter.swift
//  OpenLive
//
//  Created by GongYuhua on 6/25/16.
//  Copyright © 2016 Agora. All rights reserved.
//

import Darwin


@objc class KeyCenter: NSObject {
    
    @objc static let APP_ID: String = <#APP_ID#>
    @objc static let certificate: String = <#certificate#>
    @objc static let SCENE_ID = 1
    
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
        MetaChatTokenCreater.createRTMToken(withAppid: KeyCenter.APP_ID,
                                            certificate: KeyCenter.certificate,
                                            userid: KeyCenter.RTM_UID)
    }
    // 获取rtcToken
    static func rtcToken(channelID:String) ->String?{
        MetaChatTokenCreater.createRTCToken(withAppid: KeyCenter.APP_ID, certificate: KeyCenter.certificate, channelid: channelID, userid: UInt32(KeyCenter.RTC_UID))
    }
    
    // MARK: - KTV
    @objc static let kUserKey = <#kUserKey#>
    @objc static let kUserSecret = <#kUserSecret#>
    @objc static let kKTVServer = <#kKTVServer#>

    // MARK: - 环信
    @objc static let kEM_APPKEY = <#kEM_APPKEY#>
    @objc static let kEM_UserName = "\(RTC_UID)"
    @objc static let kEM_Password = "123456"
}
