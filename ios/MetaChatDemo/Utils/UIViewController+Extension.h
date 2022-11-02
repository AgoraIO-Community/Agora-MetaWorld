//
//  UIViewController+Extension.h
//  MetaChatDemo
//
//  Created by FanPengpeng on 2022/7/22.
//

#import <UIKit/UIKit.h>
#import "MCAlertController.h"

NS_ASSUME_NONNULL_BEGIN

@interface UIViewController (Extension)

- (void)ex_showAlertWithTitle:(NSString * __nullable)title message:(NSString * __nullable)message cancelHandler:(void (^ __nullable)(UIAlertAction *action)) cancelhandler comfirmHandler: (void (^ __nullable)(UIAlertAction *action)) comfirmhandler;

- (void)ex_showAlertWithTitle:(NSString * __nullable)title message:(NSString * __nullable)message cancelTitle:(NSString * __nullable)cancelTitle confirmTitle:(NSString * __nullable)confirmTitle cancelHandler:(void (^ __nullable)(UIAlertAction *action)) cancelhandler comfirmHandler: (void (^ __nullable)(UIAlertAction *action)) comfirmhandler;

//- (void)ex_showMCAlertWithTitle:(NSString * __nullable)title message:(NSString * __nullable)message cancelHandler:(MCAlertHandler __nullable) cancelhandler confirmHandler: (MCAlertHandler __nullable) confirmhandler;

- (void)ex_showMCAlertWithTitle:(NSString * __nullable)title message:(NSString * __nullable)message cancelTitle:(NSString * __nullable)cancelTitle confirmTitle:(NSString * __nullable)confirmTitle cancelHandler:(MCAlertHandler __nullable) cancelhandler confirmHandler: (MCAlertHandler __nullable) confirmhandler;

@end

NS_ASSUME_NONNULL_END
