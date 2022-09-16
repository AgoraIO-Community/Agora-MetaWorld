//
//  NSString+MetaChat.h
//  MetaChatDemo
//
//  Created by FanPengpeng on 2022/7/19.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface NSString (MetaChat)

- (NSString *)base64Encode;

- (NSString *)sha256Value;

@end

NS_ASSUME_NONNULL_END
