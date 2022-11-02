//
//  MCSettingDetailTextFieldCell.h
//  MetaChatDemo
//
//  Created by FanPengpeng on 2022/8/16.
//

#import "MCSettingDetailBaseCell.h"

NS_ASSUME_NONNULL_BEGIN

@interface MCSettingDetailTextFieldCell : MCSettingDetailBaseCell

@property(nonatomic,copy) void(^endEditingWithText)(NSString *text);

- (void)setTitle:(NSString *)title originalText:(NSString *)originalText;

@end

NS_ASSUME_NONNULL_END
