//
//  MCLoginViewController.h
//  MetaChatDemo
//
//  Created by FanPengpeng on 2022/8/10.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface MCLoginViewController : UIViewController

@property (copy, nonatomic) NSString *roomId;
@property (copy, nonatomic) NSString *roomName;
@property (copy, nonatomic) NSString *roomImg;
@property (copy, nonatomic) NSString *pwd;
@property (assign, nonatomic) BOOL isRoomMaster;
@property (copy, nonatomic) NSString *masterId;

@end

NS_ASSUME_NONNULL_END
