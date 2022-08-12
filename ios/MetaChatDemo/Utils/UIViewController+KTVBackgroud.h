//
//  UIViewController+KTVBackgroud.h
//  MetaChatDemo
//
//  Created by FanPengpeng on 2022/7/13.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface UIViewController (KTVBackgroud)

@property (nonatomic, assign, readonly) CGFloat blurWidth;

- (void)ktv_setBlurBackground;

- (void)ktv_tapBlankAction:(void(^)(void))action;

- (void)ktv_configCustomNaviBarWithTitle:(NSString *)title;

@end

NS_ASSUME_NONNULL_END
