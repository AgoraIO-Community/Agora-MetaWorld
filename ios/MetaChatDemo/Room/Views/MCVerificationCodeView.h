//
//  MCVerificationCodeView.h
//  MetaChatDemo
//
//  Created by FanPengpeng on 2022/8/9.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface MCVerificationCodeView : UIView

@property(nonatomic,assign)NSInteger verificationCodeNum;//验证码位数

@property(nonatomic,assign)BOOL isSecure;//是否密文显示

@property (nonatomic, strong,readonly) NSString *vertificationCode;//验证码内容

@property (nonatomic, assign) BOOL canEdit;

@end

NS_ASSUME_NONNULL_END
