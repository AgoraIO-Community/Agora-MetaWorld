//
//  MCSettingGeneralVC.h
//  MetaChatDemo
//
//  Created by FanPengpeng on 2022/8/16.
//

#import "MCSettingDetailVC.h"

NS_ASSUME_NONNULL_BEGIN

@class MCUserInfo;
@class AgoraMetachatSceneInfo;
@class AgoraMetachatAvatarInfo;

@interface MCSettingGeneralVC : MCSettingDetailVC

@property (nonatomic, strong) MCUserInfo *userInfo;

@property (nonatomic, copy) void(^didClickExitButtonAction)(void);

@end

NS_ASSUME_NONNULL_END
