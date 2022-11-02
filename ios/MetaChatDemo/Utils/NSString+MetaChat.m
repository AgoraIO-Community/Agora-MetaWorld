//
//  NSString+MetaChat.m
//  MetaChatDemo
//
//  Created by FanPengpeng on 2022/7/19.
//

#import "NSString+MetaChat.h"
#import <Foundation/Foundation.h>
#import <CommonCrypto/CommonDigest.h>

@implementation NSString (MetaChat)

- (NSString *)base64Encode {
    NSData *data = [self dataUsingEncoding:NSUTF8StringEncoding];
    return  [data base64EncodedStringWithOptions:NSDataBase64EncodingEndLineWithCarriageReturn];
}

- (NSString *)sha256Value {
    
    const char *string = [self UTF8String];
    unsigned char result[CC_SHA256_DIGEST_LENGTH];
    CC_SHA256(string, (CC_LONG)strlen(string), result);

    NSMutableString *hashed = [NSMutableString stringWithCapacity:CC_SHA256_DIGEST_LENGTH * 2];
    for (NSInteger i = 0; i < CC_SHA256_DIGEST_LENGTH; i++) {
      [hashed appendFormat:@"%02x", result[i]];
    }
    return hashed;

}

- (NSString *)md5 {
    const char *cStr = [self UTF8String];
    unsigned char result[CC_MD5_DIGEST_LENGTH] = {0};
    CC_MD5(cStr, (CC_LONG)strlen(cStr), result);
    
    NSMutableString *ret = [NSMutableString stringWithCapacity:CC_MD5_DIGEST_LENGTH*2];
    for (int i=0; i<CC_MD5_DIGEST_LENGTH; i++) {
        [ret appendFormat:@"%02X", result[i]];
    }
    return ret;
}

@end
