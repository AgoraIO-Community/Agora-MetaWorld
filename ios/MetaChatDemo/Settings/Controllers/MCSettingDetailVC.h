//
//  MCSettingDetailVC.h
//  MetaChatDemo
//
//  Created by FanPengpeng on 2022/8/15.
//

#import <UIKit/UIKit.h>
#import "MCSettingDetailModel.h"

NS_ASSUME_NONNULL_BEGIN

@interface MCSettingDetailVC : UIViewController

- (NSArray <id<MCSettingDetailModel>> *_Nonnull) settingItems;

- (void)handleTextFiledCellWithTextFieldModel:(MCSettingDetailTextFieldModel * _Nullable)model endedText:(NSString *_Nullable)text;

- (void)handleClickRightViewWithImageModel:(MCSettingDetailImageModel * _Nullable)model;

- (void)handleClickRightViewWithLabelModel:(MCSettingDetailLabelModel * _Nullable)model;

- (UIView *)tableFooterView;

- (void)reloadData;

@end

NS_ASSUME_NONNULL_END
