//
//  MCTextField.h
//  MetaChatDemo
//
//  Created by FanPengpeng on 2022/8/9.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@class MCVerificationTextField;//要class一下不然代理里面无法识别

@protocol MCTextFieldDelegate <NSObject>

- (void)textFieldDeleteBackward:(MCVerificationTextField *)textField;


@end

@interface MCVerificationTextField : UITextField

@property (nonatomic, assign)id<MCTextFieldDelegate> mc_delegate;

@end

NS_ASSUME_NONNULL_END
