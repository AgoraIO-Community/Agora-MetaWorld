//
//  UIViewController+Extension.m
//  MetaChatDemo
//
//  Created by FanPengpeng on 2022/7/22.
//

#import "UIViewController+Extension.h"

@implementation UIViewController (Extension)

- (void)ex_showAlertWithTitle:(NSString * __nullable)title message:(NSString * __nullable)message cancelHandler:(void (^ __nullable)(UIAlertAction *action)) cancelhandler comfirmHandler: (void (^ __nullable)(UIAlertAction *action)) comfirmhandler {
    UIAlertController *alert = [UIAlertController alertControllerWithTitle:title message:message preferredStyle:UIAlertControllerStyleAlert];
    if (cancelhandler) {
        UIAlertAction *cancelAction = [UIAlertAction actionWithTitle:@"Cancel" style:UIAlertActionStyleCancel handler:^(UIAlertAction * _Nonnull action) {
            [alert dismissViewControllerAnimated:YES completion:nil];
            cancelhandler(action);
        }];
        [alert addAction:cancelAction];
    }
    if (comfirmhandler) {
        UIAlertAction *conmfirmAction = [UIAlertAction actionWithTitle:@"Comfirm" style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
            [alert dismissViewControllerAnimated:YES completion:nil];
            comfirmhandler(action);
        }];
        [alert addAction:conmfirmAction];
    }
    [self presentViewController:alert animated:YES completion:nil];
}


@end
