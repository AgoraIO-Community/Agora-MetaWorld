//
//  UIViewController+Extension.m
//  MetaChatDemo
//
//  Created by FanPengpeng on 2022/7/22.
//

#import "UIViewController+Extension.h"

@implementation UIViewController (Extension)

- (void)ex_showAlertWithTitle:(NSString * __nullable)title message:(NSString * __nullable)message cancelTitle:(NSString *)cancelTitle confirmTitle:(NSString *)confirmTitle cancelHandler:(void (^ __nullable)(UIAlertAction *action)) cancelhandler comfirmHandler: (void (^ __nullable)(UIAlertAction *action)) comfirmhandler {
    UIAlertController *alert = [UIAlertController alertControllerWithTitle:title message:message preferredStyle:UIAlertControllerStyleAlert];
    if (cancelhandler) {
        UIAlertAction *cancelAction = [UIAlertAction actionWithTitle:cancelTitle style:UIAlertActionStyleCancel handler:^(UIAlertAction * _Nonnull action) {
            [alert dismissViewControllerAnimated:YES completion:nil];
            cancelhandler(action);
        }];
        [alert addAction:cancelAction];
    }
    if (comfirmhandler) {
        UIAlertAction *conmfirmAction = [UIAlertAction actionWithTitle:confirmTitle style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
            [alert dismissViewControllerAnimated:YES completion:nil];
            comfirmhandler(action);
        }];
        [alert addAction:conmfirmAction];
    }
    [self presentViewController:alert animated:YES completion:nil];
}

- (void)ex_showAlertWithTitle:(NSString * __nullable)title message:(NSString * __nullable)message cancelHandler:(void (^ __nullable)(UIAlertAction *action)) cancelhandler comfirmHandler: (void (^ __nullable)(UIAlertAction *action)) comfirmhandler {
    [self ex_showAlertWithTitle:title message:message cancelTitle:MCLocalizedString(@"Cancel") confirmTitle:MCLocalizedString(@"Confirm")  cancelHandler:cancelhandler comfirmHandler:comfirmhandler];
}

- (void)ex_showMCAlertWithTitle:(NSString * __nullable)title message:(NSString * __nullable)message cancelHandler:(MCAlertHandler __nullable) cancelhandler confirmHandler: (MCAlertHandler __nullable) confirmhandler {
    [self ex_showMCAlertWithTitle:title message:message cancelTitle:MCLocalizedString(@"Cancel") confirmTitle:MCLocalizedString(@"Confirm")  cancelHandler:cancelhandler confirmHandler:confirmhandler];
}

- (void)ex_showMCAlertWithTitle:(NSString * __nullable)title message:(NSString * __nullable)message cancelTitle:(NSString * __nullable)cancelTitle confirmTitle:(NSString * __nullable)confirmTitle cancelHandler:(MCAlertHandler __nullable) cancelhandler confirmHandler: (MCAlertHandler __nullable) confirmhandler {
    MCAlertController *vc = [MCAlertController alertWithTitle:title message:message cancelTitle:cancelTitle confirmTitle:confirmTitle cancelHandler:cancelhandler confirmHandler:confirmhandler];
    vc.modalPresentationStyle = UIModalPresentationOverCurrentContext;
    [self presentViewController:vc animated:NO completion:nil];
}


@end
