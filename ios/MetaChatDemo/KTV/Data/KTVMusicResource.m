//
//  KTVMusicResourse.m
//  MetaChatDemo
//
//  Created by FanPengpeng on 2022/7/19.
//

#import "KTVMusicResource.h"
#import "MJExtension.h"

@implementation KTVMusicMVInfo

@end

@implementation KTVMusicResource

+ (NSDictionary *)mj_objectClassInArray {
    return @{@"mvList": KTVMusicMVInfo.class};
}

@end
