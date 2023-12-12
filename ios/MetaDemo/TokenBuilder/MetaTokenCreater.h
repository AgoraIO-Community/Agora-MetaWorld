//
//  MetaTokenCreater.h
//  MetaDemo
//
//  Created by ZhouRui on 2023/8/18.
//


#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface MetaTokenCreater : NSObject

+ (NSString *)createRTMTokenWithAppid:(NSString *)appid certificate:(NSString *)certificate userid:(NSString *)userid;

+ (NSString *)createRTCTokenWithAppid:(NSString *)appid certificate:(NSString *)certificate channelid:(NSString *)cid userid:(uint32_t)userid;

@end

NS_ASSUME_NONNULL_END
