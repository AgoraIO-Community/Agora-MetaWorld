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
