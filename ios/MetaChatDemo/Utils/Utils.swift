//
//  Utils.swift
//  MetaChatDemo
//
//  Created by FanPengpeng on 2022/8/3.
//

import Foundation

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
