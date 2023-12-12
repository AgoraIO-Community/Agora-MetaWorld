//
//  MetaTokenCreater.m
//  MetaDemo
//
//  Created by ZhouRui on 2023/8/18.
//


#import "MetaTokenCreater.h"
#include "RtmTokenBuilder.h"
#include "RtcTokenBuilder.h"
#include <iostream>
#include <cstdint>

using namespace agora::tools;

@implementation MetaTokenCreater

+ (NSString *)createRTMTokenWithAppid:(NSString *)appid certificate:(NSString *)certificate userid:(NSString *)userid {
    std::string stdAppID = std::string([appid cStringUsingEncoding:NSUTF8StringEncoding]);
    std::string stdCertificate = std::string([certificate cStringUsingEncoding:NSUTF8StringEncoding]);
    std::string stdUser = std::string([userid cStringUsingEncoding:NSUTF8StringEncoding]);
    uint32_t expirationTimeInSeconds = 3600;
    uint32_t currentTimeStamp = (uint32_t)time(NULL);
    uint32_t privilegeExpiredTs = currentTimeStamp + expirationTimeInSeconds;
    std::string result =
      RtmTokenBuilder::buildToken(stdAppID, stdCertificate, stdUser,
          RtmUserRole::Rtm_User, privilegeExpiredTs);
    return [NSString stringWithCString:result.c_str() encoding:NSUTF8StringEncoding];
}

+ (NSString *)createRTCTokenWithAppid:(NSString *)appid certificate:(NSString *)certificate channelid:(NSString *)cid userid:(uint32_t)userid {
    std::string stdAppID = std::string([appid cStringUsingEncoding:NSUTF8StringEncoding]);
    std::string stdCertificate = std::string([certificate cStringUsingEncoding:NSUTF8StringEncoding]);
    std::string stdCid = std::string([cid cStringUsingEncoding:NSUTF8StringEncoding]);
    uint32_t expirationTimeInSeconds = 3600;
    uint32_t currentTimeStamp = (uint32_t)time(NULL);
    uint32_t privilegeExpiredTs = currentTimeStamp + expirationTimeInSeconds;
    std::string result =
      RtcTokenBuilder::buildTokenWithUid(stdAppID, stdCertificate, stdCid, userid,
                                         UserRole::Role_Publisher, privilegeExpiredTs);
    return [NSString stringWithCString:result.c_str() encoding:NSUTF8StringEncoding];
}

@end
