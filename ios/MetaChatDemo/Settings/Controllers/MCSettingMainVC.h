//
//  MCSettingMainVC.h
//  MetaChatDemo
//
//  Created by FanPengpeng on 2022/8/15.
//

#import <UIKit/UIKit.h>

@class MCUserInfo;
@class MCRoom;
@class MCChatSceneManager;

NS_ASSUME_NONNULL_BEGIN

@interface MCSettingMainVC : UIViewController

@property (nonatomic, strong) MCChatSceneManager *sceneMgr;
@property (nonatomic, strong) MCUserInfo *userInfo;
@property (nonatomic, strong) MCRoom *room;

@property (nonatomic, copy) void(^exitRoomBlock)(void);

@property (nonatomic, copy) void(^dismissed)(void);

@end

NS_ASSUME_NONNULL_END
