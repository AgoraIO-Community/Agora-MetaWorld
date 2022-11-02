//
//  MCChatCell.h
//  MetaChatDemo
//
//  Created by FanPengpeng on 2022/8/19.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface MCChatCell : UITableViewCell

- (void)setNickname:(NSString *)nickname isMe:(BOOL)isMe time:(NSString *)time msg:(NSString *)msg img:(NSString *)img;
@end

NS_ASSUME_NONNULL_END
