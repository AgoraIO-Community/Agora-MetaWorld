//
//  KeyCenter.swift
//  OpenLive
//
//  Created by GongYuhua on 6/25/16.
//  Copyright © 2016 Agora. All rights reserved.
//

import Darwin


@objc class KeyCenter: NSObject {
    
    #if DEBUG
    @objc static let CHANNEL_ID: String = <#Channel Id#>
    @objc static let APP_ID: String = <#AppId#>
    @objc static let certificate: String = <#certificate#>
    #elseif TEST
    @objc static let CHANNEL_ID: String = <#Channel Id#>
    @objc static let APP_ID: String = <#AppId#>
    @objc static let certificate: String = <#certificate#>
    #else
    @objc static let CHANNEL_ID: String = <#Channel Id#>
    @objc static let APP_ID: String = <#AppId#>
    @objc static let certificate: String = <#certificate#>
    #endif
    @objc static let RTC_UID: UInt = UInt(arc4random() % 100000) // 请修改uid 不要设置成0
    @objc static var RTC_TOKEN: String? {
        MetaChatTokenCreater.createRTCToken(withAppid: KeyCenter.APP_ID, certificate: KeyCenter.certificate, channelid: KeyCenter.CHANNEL_ID, userid: UInt32(KeyCenter.RTC_UID))
    }
    @objc static let RTM_UID: String = "\(RTC_UID)"
    @objc static var RTM_TOKEN: String? {
        MetaChatTokenCreater.createRTMToken(withAppid: KeyCenter.APP_ID,
                                            certificate: KeyCenter.certificate,
                                            userid: KeyCenter.RTM_UID)
    }
    @objc static let kUserKey = <#kUserKey#>
    @objc static let kUserSecret = <#kUserSecret#>
}
