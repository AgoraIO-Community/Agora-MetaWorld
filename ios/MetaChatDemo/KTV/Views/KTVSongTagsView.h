//
//  KTVSongTagsView.h
//  MetaChatDemo
//
//  Created by FanPengpeng on 2022/7/15.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@class  KTVSongTag;

@interface KTVSongTagsView : UIView

@property (nonatomic, strong) NSArray <KTVSongTag *> * tags;

@end

NS_ASSUME_NONNULL_END
