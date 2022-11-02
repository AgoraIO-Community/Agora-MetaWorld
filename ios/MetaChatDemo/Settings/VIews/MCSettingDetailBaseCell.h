//
//  MCSettingDetailBaseCell.h
//  MetaChatDemo
//
//  Created by FanPengpeng on 2022/8/15.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface MCSettingDetailBaseCell : UITableViewCell

@property (strong, nonatomic) UILabel *titleLabel;

@property (nonatomic, strong) UIImageView *iconImgView;

// rightViewClickEnable返回YES的时候生效
@property (nonatomic, copy)void(^rightViewClicked)(void);


- (void)createRightView;

- (UIImage *)rightIconImage;

- (BOOL)rightViewClickEnable;

@end

NS_ASSUME_NONNULL_END
