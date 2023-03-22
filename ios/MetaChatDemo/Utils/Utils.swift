//
//  Utils.swift
//  MetaChatDemo
//
//  Created by FanPengpeng on 2022/8/3.
//

import Foundation
import SwiftyMenu

func DLog(_ message: Any..., file: String = #file, function: String = #function, lineNumber: Int = #line) {
    #if DEBUG
    let fileName = (file as NSString).lastPathComponent
    print("MC ----- [文件:\(fileName)] [函数:\(function)] [行:\(lineNumber)] \(message) ----- MC")
    #endif
}

extension UserDefaults {
    func set<T:Encodable>(Array object: [T], key: String) {
        do {
            let data = try JSONEncoder().encode(object)
            self.set(data, forKey: key)
        } catch {
            print("[metachat] save user dress info error: ", error)
        }
    }
    
    func getObject<T:Decodable>(forKey key: String) -> [T] {
        guard let data = self.data(forKey: key) else { return [] }
        do {
            return try JSONDecoder().decode([T].self, from: data)
        } catch {
            print("[metachat] get user dress info error: ", error)
        }
        return []
    }
}

extension UIDevice {
    static func switchOrientation(_ interfaceOrientation: UIInterfaceOrientation) {
        let resetOrientationTarget = UIInterfaceOrientation.unknown
        UIDevice.current.setValue(NSNumber.init(value: resetOrientationTarget.rawValue), forKey: "orientation")
        let orientationTarget = interfaceOrientation
        UIDevice.current.setValue(NSNumber.init(value: orientationTarget.rawValue), forKey: "orientation")
    }
}

extension UIViewController {
    func switchOrientation(isPortrait: Bool, isFullScreen: Bool) {
        if #available(iOS 16.0, *) {
            let appDelegate = UIApplication.shared.delegate as! AppDelegate
            appDelegate.isPortrait = isPortrait
            appDelegate.isFullScreen = isFullScreen
            
            self.setNeedsUpdateOfSupportedInterfaceOrientations()
            
            guard let scene = UIApplication.shared.connectedScenes.first as? UIWindowScene else {
                return
            }
            
            let orientation: UIInterfaceOrientationMask = isPortrait ? .portrait : .landscapeRight
            let geometryPreferencesIOS = UIWindowScene.GeometryPreferences.iOS(interfaceOrientations: orientation)
            scene.requestGeometryUpdate(geometryPreferencesIOS) { error in
                print("[metachat] force \(isPortrait ? "portrait" : "landscapeRight" ) error: \(error)")
            }
        } else {
            let appDelegate = UIApplication.shared.delegate as! AppDelegate
            appDelegate.isPortrait = isPortrait
            appDelegate.isFullScreen = isFullScreen
            UIDevice.switchOrientation(isPortrait ? .portrait : .landscapeRight)
        }
    }
}

extension TimeInterval {
    func toCMTime() -> CMTime {
        let scale = CMTimeScale(NSEC_PER_SEC)
        let rt = CMTime(value: CMTimeValue(self * Double(scale)), timescale: scale)
        return rt
    }
}

extension String: SwiftyMenuDisplayable {
    public var retrievableValue: Any {
        self
    }
    
    public var displayableValue: String {
        return self
    }
}
