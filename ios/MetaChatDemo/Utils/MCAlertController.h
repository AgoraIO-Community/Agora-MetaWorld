//
//  MCAlertControllerViewController.h
//  MetaChatDemo
//
//  Created by FanPengpeng on 2022/9/7.
//

#import <UIKit/UIKit.h>

typedef void(^MCAlertHandler)(void);

NS_ASSUME_NONNULL_BEGIN

@interface MCAlertController : UIViewController

@property (copy, nonatomic) NSString *alertTitle;
@property (copy, nonatomic) NSString *message;
@property (copy, nonatomic) NSString *cancelTitle;
@property (copy, nonatomic) NSString *confirmTitle;


+ (instancetype)alertWithTitle:(NSString * __nullable)title message:(NSString * __nullable)message cancelTitle:(NSString * __nullable)cancelTitle confirmTitle:(NSString * __nullable)confirmTitle cancelHandler:(MCAlertHandler __nullable) cancelhandler confirmHandler: (MCAlertHandler __nullable) comfirmhandler;

@end

NS_ASSUME_NONNULL_END
