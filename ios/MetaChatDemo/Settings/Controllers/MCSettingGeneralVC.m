//
//  MCSettingGeneralVC.m
//  MetaChatDemo
//
//  Created by FanPengpeng on 2022/8/16.
//

#import "MCSettingGeneralVC.h"
#import "MCUserInfo.h"
#import "MCSelectProfileViewController.h"
#import "MCSelectAvatarViewController.h"
#import "MetaChatDemo-Swift.h"
#import "MCSelectVCManager.h"
#import "MBProgressHUD+Extension.h"


static NSString *const kSettingTitleNickame = @"Name";
static NSString *const kSettingTitleHead = @"Profile";
static NSString *const kSettingTitleLevel = @"Badge";
static NSString *const kSettingTitleAvatar = @"Avatar";


@interface MCSettingGeneralVC ()

@property (strong, nonatomic) MCSelectVCManager *selectVCManager;

@end

@implementation MCSettingGeneralVC

- (NSArray<id<MCSettingDetailModel>> *)settingItems {
    MCSettingDetailTextFieldModel *nicknameModel = [MCSettingDetailTextFieldModel new];
    nicknameModel.title = MCLocalizedString(kSettingTitleNickame);
    nicknameModel.originalText = self.userInfo.nickname;
    
    MCSettingDetailImageModel *headModel = [MCSettingDetailImageModel new];
    headModel.title = MCLocalizedString(kSettingTitleHead);
    headModel.imageUrl = self.userInfo.headImg;
    
    MCSettingDetailImageModel *levelModel = [MCSettingDetailImageModel new];
    levelModel.title = MCLocalizedString(kSettingTitleLevel);
    levelModel.imageUrl = self.userInfo.badge;
    
    MCSettingDetailImageModel *avatarModel = [MCSettingDetailImageModel new];
    avatarModel.title = MCLocalizedString(kSettingTitleAvatar);
    avatarModel.imageUrl = MetaChatEngine.sharedEngine.currentAvatarInfo.thumbnailPath;
    
    return @[nicknameModel,headModel,levelModel,avatarModel];
}

- (void)handleTextFiledCellWithTextFieldModel:(MCSettingDetailTextFieldModel *)model endedText:(NSString *)text {
    if ([model.title isEqualToString:MCLocalizedString(kSettingTitleNickame)]) {
        NSString *newName = [text stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet]];
        if (newName.length < 2 || newName.length > 12) {
            [MBProgressHUD showError:MCLocalizedString(@"nickname length tips") inView:self.view];
            return;
        }
        self.userInfo.nickname = newName;
    }
}

- (void)handleClickRightViewWithImageModel:(MCSettingDetailImageModel *)model {
    if ([model.title isEqualToString:MCLocalizedString(kSettingTitleHead)]) {
        [self showSelectHeaderVC];
    }else if ([model.title isEqualToString:MCLocalizedString(kSettingTitleLevel)]){
        [self showSelectBadgeVC];
    }else if ([model.title isEqualToString:MCLocalizedString(kSettingTitleAvatar)]){
        [self showSelectAvatarVC];
    }
}

- (void)handleClickRightViewWithLabelModel:(MCSettingDetailLabelModel *)model {
    
}

// 点击头像
- (void)showSelectHeaderVC {
    __weak typeof(self) wSelf = self;
    MCSelectProfileViewController *vc = [self.selectVCManager selectVCWithType:MCSelectProfileTypeHeadImage defaultValue:self.userInfo.headImg didSelected:^(NSInteger selectedIndex, NSArray * _Nonnull imgArray) {
        wSelf.userInfo.headImg = imgArray[selectedIndex];
        [wSelf reloadData];
    }];
    [self presentViewController:vc animated:NO completion:nil];
}

// 点击勋章按钮
- (void)showSelectBadgeVC {
    __weak typeof(self) wSelf = self;
    MCSelectProfileViewController *vc = [self.selectVCManager selectVCWithType:MCSelectProfileTypeBadge defaultValue:self.userInfo.badge didSelected:^(NSInteger selectedIndex, NSArray * _Nonnull imgArray) {
        wSelf.userInfo.badge = imgArray[selectedIndex];
        [wSelf reloadData];
    }];
    [self presentViewController:vc animated:NO completion:nil];
}

- (void)showSelectAvatarVC {
    UIStoryboard *sb = [UIStoryboard storyboardWithName:@"Main" bundle:nil];
    MCSelectAvatarViewController *vc = [sb instantiateViewControllerWithIdentifier:@"selectAvatarVC"];
    vc.modalPresentationStyle = UIModalPresentationFullScreen;
    vc.sceneInfo = [MetaChatEngine.sharedEngine currentSceneInfo];
    vc.defaultAvatarInfo = [MetaChatEngine.sharedEngine currentAvatarInfo];
    vc.gender = self.userInfo.gender;
    vc.enterBtnTitle = MCLocalizedString(@"Confirm");
    __weak typeof(self) wSelf = self;
    vc.onSelectedAvatar = ^(AgoraMetachatAvatarInfo * _Nonnull avatarInfo) {
        [MetaChatEngine.sharedEngine updateAvatarInfo:avatarInfo userName:nil userIconUrl:nil];
        [wSelf reloadData];
    };
    [self presentViewController:vc animated:YES completion:nil];
}


- (UIView *)tableFooterView {
    UIButton *exitButton = [UIButton buttonWithType:UIButtonTypeCustom];
    [exitButton setTitle:MCLocalizedString(@"Quit room") forState:UIControlStateNormal];
    [exitButton setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
    exitButton.titleLabel.font = [UIFont systemFontOfSize:14];
    exitButton.titleEdgeInsets = UIEdgeInsetsMake(0, 5, 0, 0);
    exitButton.imageEdgeInsets = UIEdgeInsetsMake(0, 0, 0, 5);
    [exitButton setImage:[UIImage imageNamed:@"setting_exit"] forState:UIControlStateNormal];
    exitButton.frame = CGRectMake(0, 0, self.view.bounds.size.width, 59);
    [exitButton addTarget:self action:@selector(didClickExitButton) forControlEvents:UIControlEventTouchUpInside];
    return exitButton;
}

- (void)didClickExitButton {
    if (self.didClickExitButtonAction) {
        self.didClickExitButtonAction();
    }
}

#pragma mark - getter

- (MCSelectVCManager *)selectVCManager {
    if (!_selectVCManager) {
        _selectVCManager = [MCSelectVCManager new];
    }
    return _selectVCManager;
}

@end
