//
//  KTVNetworkHelper.m
//  MetaChatDemo
//
//  Created by FanPengpeng on 2022/7/19.
//

#import "KTVNetworkHelper.h"
#import <UIKit/UIKit.h>
#import "AFNetworking.h"
#import "MetaChatDemo-Swift.h"
#import "NSString+MetaChat.h"
#import "MJExtension.h"
#import "KTVMusic.h"
#import "KTVMusicResource.h"

#ifdef DEBUG
//#define kKTVServer @"https://api.agora.io"
#define kKTVServer @"https://api-test.agora.io"
#elifdef TEST
//#define kKTVServer @"https://api.agora.io"
#define kKTVServer @"https://api-test.agora.io"
#else
#define kKTVServer @"https://api.agora.io"
#endif


@interface KTVNetworkHelper()

@end

@implementation KTVNetworkHelper

static NSString *_requestId = nil;

+ (NSString *)requestId {
    if (_requestId == nil) {
        _requestId = [UIDevice currentDevice].identifierForVendor.UUIDString;
    }
    return _requestId;
}

+ (void)requestWithURI:(NSString *)URI params:(NSDictionary *)params success:(void(^)(_Nullable id responseObj)) success fail:(void(^)( NSError * _Nullable err)) fail{
    AFHTTPSessionManager *manager = [AFHTTPSessionManager manager];
    NSString *path = [NSString stringWithFormat:@"/cn/v1.1/projects/%@/ktv-service/sdk/v1/%@",[KeyCenter APP_ID],URI];
    NSString *url = [NSString stringWithFormat:@"%@%@",kKTVServer, path];
    NSString *author = [NSString stringWithFormat:@"%@:%@",[KeyCenter kUserKey],[KeyCenter kUserSecret]];
    NSString *authorization = [NSString stringWithFormat:@"Basic %@",[author base64Encode]];
    NSDictionary *header = @{
        @"Authorization": authorization,
        @"x-agora-token": [KeyCenter RTM_TOKEN],
        @"x-agora-uid": [KeyCenter RTM_UID],
    };
    DLog(@"url == %@, parameters = %@, headers = %@",url, params, header);
    [manager POST:url parameters:params headers:header progress:nil success:^(NSURLSessionDataTask * _Nonnull task, id  _Nullable responseObject) {
        DLog(@"responseObject ==== %@",responseObject);
        if (success) {
            success(responseObject);
        }
    } failure:^(NSURLSessionDataTask * _Nullable task, NSError * _Nonnull error) {
        DLog(@"error ==== %@",error);
        if (fail) {
            fail(error);
        }
    }];
}



+ (void)songsForHotType:(NSNumber *)hotType Success:(void(^)( NSArray<KTVMusic *> * _Nullable songList)) success fail:(void(^)( NSError * _Nullable err)) fail{
    NSMutableDictionary *dic = [NSMutableDictionary dictionary];
    NSString *hotTypeStr = [NSString stringWithFormat:@"%@",hotType];
    NSString *requestId = self.requestId;
    long requestTime = (long)[[NSDate date] timeIntervalSince1970] * 1000;
    NSInteger page = 1;
    NSInteger size = 10;
    
    NSString *option = @"{\"songType\":[4,5]}";
    dic[@"hotType"] = hotTypeStr;
    dic[@"requestId"] = requestId;
    dic[@"requestTime"] = @(requestTime);
    dic[@"page"] = @(page);
    dic[@"size"] = @(size);
    dic[@"option"] = option;
    
    NSString *params = [NSString stringWithFormat:@"%@%zd%zd%zd%@%@",requestId,requestTime,page,size,hotTypeStr,option];
    NSString *sign = [params sha256Value];
    dic[@"sign"] = sign;
    
    DLog(@"sign === %@",sign);
    
    [self requestWithURI:@"song-hot" params:dic success:^(id  _Nullable responseObj) {
        NSDictionary *data = responseObj[@"data"];
        if ([data isKindOfClass:[NSDictionary class]]) {
            NSDictionary *list = data[@"list"];
            DLog(@"list == %@",list)
            NSArray<KTVMusic *> *array = [KTVMusic mj_objectArrayWithKeyValuesArray:list];
            if (success) {
                success(array);
            }
        }else{
            if (success) {
                success(nil);
            }
        }
    } fail:^(NSError * _Nullable err) {
        
    }];
}

+ (void)searchSongsWithKey:(NSString *)key success:(void(^)( NSArray<KTVMusic *> * _Nullable songList)) success fail:(void(^)( NSError * _Nullable err)) fail {
    
    NSMutableDictionary *dic = [NSMutableDictionary dictionary];
    NSString *requestId = self.requestId;
    long requestTime = (long)[[NSDate date] timeIntervalSince1970] * 1000;
    NSString *name = [NSString stringWithFormat:@"%@",key];
    NSInteger page = 1;
    NSInteger size = 10;
    NSString *option = @"{\"songType\":[4,5]}";
    
    dic[@"requestId"] = requestId;
    dic[@"requestTime"] = @(requestTime);
    dic[@"name"] = name;
    dic[@"page"] = @(page);
    dic[@"size"] = @(size);
    dic[@"option"] = option;
    
    NSString *params = [NSString stringWithFormat:@"%@%zd%@%zd%zd%@",requestId,requestTime,name,page,size,option];
    NSString *sign = [params sha256Value];
    dic[@"sign"] = sign;
    
    DLog(@"sign === %@",sign);
    
    [self requestWithURI:@"search" params:dic success:^(id  _Nullable responseObj) {
        NSDictionary *data = responseObj[@"data"];
        if ([data isKindOfClass:[NSDictionary class]]) {
            NSDictionary *list = data[@"list"];
            NSArray<KTVMusic *> *array = [KTVMusic mj_objectArrayWithKeyValuesArray:list];
            if (success) {
                success(array);
            }
        }else{
            if (success) {
                success(nil);
            }
        }
    } fail:^(NSError * _Nullable err) {
        
    }];
}

+ (void)mvWithSongCode:(NSInteger)songCode Success:(void(^)(KTVMusicResource * _Nullable resouce)) success fail:(void(^)( NSError * _Nullable err)) fail{
    NSMutableDictionary *dic = [NSMutableDictionary dictionary];
    NSString *requestId = self.requestId;
    long requestTime = (long)[[NSDate date] timeIntervalSince1970] * 1000;
    NSInteger lyricType = 0;
    dic[@"requestId"] = requestId;
    dic[@"requestTime"] = @(requestTime);
    dic[@"songCode"] = @(songCode);
    dic[@"resolution"] = @"";
    dic[@"lyricType"] = @(lyricType);
    
    NSString *params = [NSString stringWithFormat:@"%@%zd%zd%@%zd",self.requestId,requestTime,songCode,@"",lyricType];
    NSString *sign = [params sha256Value];
    dic[@"sign"] = sign;
    
    DLog(@"sign === %@",sign);
    
    [self requestWithURI:@"mv-url" params:dic success:^(id  _Nullable responseObj) {
        NSDictionary *data = responseObj[@"data"];
        if ([data isKindOfClass:[NSDictionary class]]) {
            KTVMusicResource *resouce = [KTVMusicResource mj_objectWithKeyValues:data];
            if (success) {
                success(resouce);
            }
        }else{
            if (success) {
                success(nil);
            }
        }
    } fail:^(NSError * _Nullable err) {
        
    }];
}

@end
