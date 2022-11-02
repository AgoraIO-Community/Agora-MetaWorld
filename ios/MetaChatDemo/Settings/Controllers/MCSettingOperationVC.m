//
//  MCSettingOperationVC.m
//  MetaChatDemo
//
//  Created by FanPengpeng on 2022/8/16.
//

#import "MCSettingOperationVC.h"


static NSString *const kUserViewSetting = @"User View Setting";
static NSString *const kDiscSetting = @"Disc setting";

@interface MCSettingOperationVC ()

@property (nonatomic, copy) NSString *userViewSetting;
@property (nonatomic, copy) NSString *discSetting;

@property (nonatomic, assign) BOOL isFirst;

@end

@implementation MCSettingOperationVC

- (NSArray<id<MCSettingDetailModel>> *)settingItems {
    MCSettingDetailLabelModel *userViewModel = [MCSettingDetailLabelModel new];
    userViewModel.title = MCLocalizedString(kUserViewSetting);
    userViewModel.info = _isFirst ? MCLocalizedString(@"1st person perspective view") : MCLocalizedString(@"3rd person perspective view");

    MCSettingDetailLabelModel *discModel = [MCSettingDetailLabelModel new];
    discModel.title = MCLocalizedString(kDiscSetting);
    discModel.info = @"";

    return @[userViewModel, discModel];
}

- (void)handleTextFiledCellWithTextFieldModel:(MCSettingDetailTextFieldModel *)model endedText:(NSString *)text {
    
}

- (void)handleClickRightViewWithImageModel:(MCSettingDetailImageModel *)model {
    
}

- (void)handleClickRightViewWithLabelModel:(MCSettingDetailLabelModel *)model {
    if ([model.title isEqualToString:MCLocalizedString(kUserViewSetting)]) {
        self.isFirst = !self.isFirst;
        // 设置人称
    }
    [self reloadData];
}


@end
