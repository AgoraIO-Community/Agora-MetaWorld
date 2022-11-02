//
//  MCSettingRoomVC.m
//  MetaChatDemo
//
//  Created by FanPengpeng on 2022/8/16.
//

#import "MCSettingRoomVC.h"
#import "MetaChatDemo-Swift.h"

static NSString *const kSettingTitleRoomName = @"Room name";
static NSString *const kSettingTitleEncrypt = @"Do you want to encrypt the room?";
static NSString *const kSettingTitlePWD = @"Password";

@interface MCSettingRoomVC ()

@property (nonatomic, assign) BOOL isEnrypt;

@end

@implementation MCSettingRoomVC

- (NSArray<id<MCSettingDetailModel>> *)settingItems {
    MCSettingDetailTextFieldModel *roomNameModel = [MCSettingDetailTextFieldModel new];
    roomNameModel.title = MCLocalizedString(kSettingTitleRoomName);
    roomNameModel.originalText = self.room.name;
    
    MCSettingDetailLabelModel *encryptModel = [MCSettingDetailLabelModel new];
    encryptModel.title = MCLocalizedString(kSettingTitleEncrypt);
    encryptModel.info = self.isEnrypt ? MCLocalizedString(@"Encrypted") : MCLocalizedString(@"Public");
    
    MCSettingDetailTextFieldModel *pwdModel = [MCSettingDetailTextFieldModel new];
    pwdModel.title = MCLocalizedString(kSettingTitlePWD);
    pwdModel.originalText = self.room.pwd;
    
    if (self.isEnrypt > 0) {
        return @[roomNameModel, encryptModel, pwdModel];
    }else{
        return @[roomNameModel, encryptModel];
    }
    
}

- (void)setRoom:(MCRoom *)room {
    _room = room;
    self.isEnrypt = self.room.pwd.length > 0;
}

- (void)handleTextFiledCellWithTextFieldModel:(MCSettingDetailTextFieldModel *)model endedText:(NSString *)text {
    if ([model.title isEqualToString:MCLocalizedString(kSettingTitleRoomName)]) {
        self.room.name = [text stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet]];
    }
    if ([model.title isEqualToString:MCLocalizedString(kSettingTitlePWD)]) {
        if (self.isEnrypt) {
            self.room.pwd = [text stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet]];
        }else{
            self.room.pwd = @"";
        }
    }
    [[MCRoomManager shared] updateRoomWithName:self.room.name];
    [[MCRoomManager shared] updateRoomWithPwd:self.isEnrypt ? self.room.pwd : @""];
    [self reloadData];
}

- (void)handleClickRightViewWithImageModel:(MCSettingDetailImageModel *)model {
    
}

- (void)handleClickRightViewWithLabelModel:(MCSettingDetailLabelModel *)model {
    if ([model.title isEqualToString:MCLocalizedString(kSettingTitleEncrypt)]) {
        self.isEnrypt = !self.isEnrypt;
        [[MCRoomManager shared] updateRoomWithPwd:self.isEnrypt ? self.room.pwd : @""];
    }
    [self reloadData];
}

@end
