//
//  MCChatModel.h
//  MetaChatDemo
//
//  Created by FanPengpeng on 2022/8/22.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface MCChatModel : NSObject

@property (nonatomic, copy) NSString *name;
@property (nonatomic, copy) NSString *text;
@property (nonatomic, copy) NSString *img;
@property (nonatomic, copy) NSString *timeStr;
@property (nonatomic, assign) BOOL isMe;

@end

NS_ASSUME_NONNULL_END
