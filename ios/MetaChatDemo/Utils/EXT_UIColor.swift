//
//  EXT-UIColor.swift
//  TDStarMall
//
//  Created by s on 2019/4/30.
//  Copyright © 2019 tiens. All rights reserved.
//

import UIKit

@objc extension UIColor{
    
    public convenience init(hexString: String) {
        
        let hexString = hexString.trimmingCharacters(in: .whitespacesAndNewlines)
        let scanner = Scanner(string: hexString)
        
        if hexString.hasPrefix("#") {
            scanner.scanLocation = 1
        }
                
        var color: UInt32 = 0
        scanner.scanHexInt32(&color)
        
        let mask = 0x000000FF
        let r = Int(color >> 16) & mask
        let g = Int(color >> 8) & mask
        let b = Int(color) & mask
        
        let red   = CGFloat(r) / 255.0
        let green = CGFloat(g) / 255.0
        let blue  = CGFloat(b) / 255.0
        
        self.init(red: red, green: green, blue: blue, alpha: 1)
    }
    
    public convenience init(hexadecimal:String) {
        
        let cstr = hexadecimal.trimmingCharacters(in:  CharacterSet.whitespacesAndNewlines).uppercased() as NSString;
        var range = NSRange.init()
        range.location = 0
        range.length = 2
        //r
        let rStr = cstr.substring(with: range);
        //g
        range.location = 2;
        let gStr = cstr.substring(with: range)
        //b
        range.location = 4;
        let bStr = cstr.substring(with: range)
        var r :UInt32 = 0x0;
        var g :UInt32 = 0x0;
        var b :UInt32 = 0x0;
        Scanner.init(string: rStr).scanHexInt32(&r);
        Scanner.init(string: gStr).scanHexInt32(&g);
        Scanner.init(string: bStr).scanHexInt32(&b);
        self.init(red: CGFloat(r)/255.0, green: CGFloat(g)/255.0, blue: CGFloat(b)/255.0, alpha: 1);
    }
    
    // UIColor -> Hex String
    public var hexString: String? {
        var red: CGFloat = 0
        var green: CGFloat = 0
        var blue: CGFloat = 0
        var alpha: CGFloat = 0
        
        let multiplier = CGFloat(255.999999)
        
        guard self.getRed(&red, green: &green, blue: &blue, alpha: &alpha) else {
            return nil
        }
        
        if alpha == 1.0 {
            return String(
                format: "#%02lX%02lX%02lX",
                Int(red * multiplier),
                Int(green * multiplier),
                Int(blue * multiplier)
            )
        }
        else {
            return String(
                format: "#%02lX%02lX%02lX%02lX",
                Int(red * multiplier),
                Int(green * multiplier),
                Int(blue * multiplier),
                Int(alpha * multiplier)
            )
        }
    }
    
    //用数值初始化颜色，便于生成设计图上标明的十六进制颜色 如valueRGB = 0x23c4f6
    @objc convenience init(hexRGB: UInt,alpha: CGFloat = 1.0) {
           self.init(
               red: CGFloat((hexRGB & 0xFF0000) >> 16) / 255.0,green: CGFloat((hexRGB & 0x00FF00) >> 8) / 255.0,blue: CGFloat(hexRGB & 0x0000FF) / 255.0,alpha: alpha
           )
       }
    
}
