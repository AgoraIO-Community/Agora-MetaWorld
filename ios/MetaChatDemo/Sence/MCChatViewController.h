//
//  MCChatViewController.h
//  MetaChatDemo
//
//  Created by FanPengpeng on 2022/8/19.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@class MCChatViewController;
@class MCUserInfo;

@protocol MCChatViewControllerDelegate <NSObject>

- (void)chatVC:(MCChatViewController *)chatVC didSendMessageContent:(NSString *)content;

- (void)chatVC:(MCChatViewController *)chatVC didReceiveMessageContent:(NSString *)content fromUserId:(NSString *)fromUserId;

@end

@interface MCChatViewController : UIViewController

@property (nonatomic, copy) NSString * roomId;
@property (nonatomic, strong)MCUserInfo *userInfo;
@property (nonatomic, weak) id<MCChatViewControllerDelegate> delegate;

@end

NS_ASSUME_NONNULL_END
