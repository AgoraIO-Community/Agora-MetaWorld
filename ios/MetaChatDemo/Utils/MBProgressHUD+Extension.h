//
//  MBProgressHUD+Extension.h
//  MetaChatDemo
//
//  Created by FanPengpeng on 2022/9/7.
//

#import <MBProgressHUD/MBProgressHUD.h>

NS_ASSUME_NONNULL_BEGIN

@interface MBProgressHUD (Extension)

+ (void)showLoadingInView:(UIView *)view;

+ (void)dismissLoadingInView:(UIView *)view;

+ (void)showToast:(NSString *)info inView:(UIView *)view;

+ (void)showError:(NSString *)info inView:(UIView *)view;

+ (void)showInfo:(NSString *)info inView:(UIView *)view;

@end

NS_ASSUME_NONNULL_END
