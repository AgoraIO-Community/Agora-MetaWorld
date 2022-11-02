//
//  MCSelectAvatarViewController.h
//  MetaChatDemo
//
//  Created by FanPengpeng on 2022/8/12.
//

#import <UIKit/UIKit.h>
#import "MCUserInfo.h"

NS_ASSUME_NONNULL_BEGIN

@class AgoraMetachatAvatarInfo;
@class AgoraMetachatSceneInfo;

typedef void(^SelectedAvatarFinishedBlock)(AgoraMetachatAvatarInfo *avatarInfo);

@interface MCSelectAvatarViewController : UIViewController

@property (nonatomic, copy) SelectedAvatarFinishedBlock onSelectedAvatar;

@property (nonatomic, strong) AgoraMetachatAvatarInfo *defaultAvatarInfo;

@property (nonatomic, strong) AgoraMetachatSceneInfo *sceneInfo;

@property (nonatomic, assign) AgoraMetachatGender gender;

@property (nonatomic, copy) NSString *enterBtnTitle;

@end

NS_ASSUME_NONNULL_END
