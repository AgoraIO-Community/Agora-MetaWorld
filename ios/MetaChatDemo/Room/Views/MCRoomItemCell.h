//
//  MCRoomItemCell.h
//  MetaChatDemo
//
//  Created by FanPengpeng on 2022/8/9.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

typedef void(^MCRoomItemCellJoinButtonClicked)(void);

@class MCRoom;

@interface MCRoomItemCell : UICollectionViewCell

@property (nonatomic, copy)MCRoomItemCellJoinButtonClicked joinButtonCliced;

@property (nonatomic, strong)MCRoom *room;

@end

NS_ASSUME_NONNULL_END
