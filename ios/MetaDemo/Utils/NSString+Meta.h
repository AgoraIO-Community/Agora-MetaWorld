//
//  NSString+Meta.h
//  MetaDemo
//
//  Created by FanPengpeng on 2022/7/19.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface NSString (Meta)

- (NSString *)base64Encode;

- (NSString *)sha256Value;

@end

NS_ASSUME_NONNULL_END
