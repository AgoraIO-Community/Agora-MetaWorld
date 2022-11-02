//
//  AppDelegate.swift
//  MetaChatDemo
//
//  Created by 胡润辰 on 2022/4/21.
//

import UIKit
import AFNetworking

@main
class AppDelegate: UIResponder, UIApplicationDelegate {

    var window: UIWindow?

    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
        // Override point for customization after application launch.
        initialThirdPartySDK()
        return true
    }

//    func application(_ application: UIApplication, supportedInterfaceOrientationsFor window: UIWindow?) -> UIInterfaceOrientationMask {
//        return .landscapeRight
//    }
    
    func applicationDidEnterBackground(_ application: UIApplication) {
        DLog("AFNetworkReachabilityManager.shared().isReachable ==== \(AFNetworkReachabilityManager.shared().networkReachabilityStatus.rawValue)")
    }
    
    func applicationWillTerminate(_ application: UIApplication) {
        MCRoomManager.shared.leaveRoom()
    }
}

extension AppDelegate {
    func initialThirdPartySDK(){
        MCRoomManager.shared.initialSDK()
    }
}
