//
//  MCSettingRoomVC.h
//  MetaChatDemo
//
//  Created by FanPengpeng on 2022/8/16.
//

#import "MCSettingDetailVC.h"

NS_ASSUME_NONNULL_BEGIN

@class MCRoom;

@interface MCSettingRoomVC : MCSettingDetailVC

@property (nonatomic, strong) MCRoom *room;

@end

NS_ASSUME_NONNULL_END
