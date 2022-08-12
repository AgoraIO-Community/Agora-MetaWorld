//
//  KTVMusic.m
//  MetaChatDemo
//
//  Created by FanPengpeng on 2022/7/18.
//

#import "KTVMusic.h"

@implementation KTVMusicMV


@end

@implementation KTVMusic

- (BOOL)isEqual:(id)other
{
    if (other == self) {
        return YES;
    }
    if (![other isKindOfClass:KTVMusic.class]) {
        return NO;
    }
    KTVMusic *otherMusic = (KTVMusic *)other;
    return otherMusic.songCode == self.songCode;
}

- (NSUInteger)hash
{
    return _songCode ^ [_name hash] ^ [_singer hash] ^ [_poster hash] ^[_song hash];
}



@end
