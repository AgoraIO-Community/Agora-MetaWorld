//
//  UIViewController+Extension.h
//  MetaDemo
//
//  Created by FanPengpeng on 2022/7/22.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface UIViewController (Extension)

- (void)ex_showAlertWithTitle:(NSString * __nullable)title message:(NSString * __nullable)message cancelHandler:(void (^ __nullable)(UIAlertAction *action)) cancelhandler comfirmHandler: (void (^ __nullable)(UIAlertAction *action)) comfirmhandler;

@end

NS_ASSUME_NONNULL_END
