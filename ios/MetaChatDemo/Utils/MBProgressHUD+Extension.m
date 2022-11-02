//
//  MBProgressHUD+Extension.m
//  MetaChatDemo
//
//  Created by FanPengpeng on 2022/9/7.
//

#import "MBProgressHUD+Extension.h"

@implementation MBProgressHUD (Extension)

+ (void)showLoadingInView:(UIView *)view {
    MBProgressHUD *hud = [[MBProgressHUD alloc] initWithView:view];
    [view addSubview:hud];
    [hud showAnimated:YES];
}

+ (void)dismissLoadingInView:(UIView *)view {
    [MBProgressHUD hideHUDForView:view animated:YES];
}

+ (void)showToast:(NSString *)info inView:(UIView *)view {
    MBProgressHUD *hud = [[MBProgressHUD alloc] initWithView:view];
    [view addSubview:hud];
    hud.mode = MBProgressHUDModeText;
    hud.label.text = info;
    [hud defalutConfig];
    [hud showAnimated:YES];
    [hud hideAnimated:YES afterDelay:2];
}

+ (void)showError:(NSString *)info inView:(UIView *)view {
    MBProgressHUD *hud = [[MBProgressHUD alloc] initWithView:view];
    [view addSubview:hud];
    hud.mode = MBProgressHUDModeCustomView;
    UIImageView *imageView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"hud_error"]];
    hud.customView = imageView;
    hud.label.text = info;
    [hud defalutConfig];
    [hud showAnimated:YES];
    [hud hideAnimated:YES afterDelay:2];
}

+ (void)showInfo:(NSString *)info inView:(UIView *)view {
    MBProgressHUD *hud = [[MBProgressHUD alloc] initWithView:view];
    [view addSubview:hud];
    hud.mode = MBProgressHUDModeCustomView;
    UIImageView *imageView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"hud_info"]];
    hud.customView = imageView;
    hud.label.text = info;
    [hud defalutConfig];
    [hud showAnimated:YES];
    [hud hideAnimated:YES afterDelay:2];
}

- (void)defalutConfig {
    self.label.textColor = [UIColor whiteColor];
    self.label.font = [UIFont systemFontOfSize:14];
    self.bezelView.style = MBProgressHUDBackgroundStyleSolidColor;
    self.removeFromSuperViewOnHide = YES;
    self.bezelView.color = [UIColor colorWithRed:0 green:0 blue:0 alpha:0.8];
}

@end
