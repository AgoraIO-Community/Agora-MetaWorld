//
//  KTVAudioEffectModel.m
//  MetaChatDemo
//
//  Created by FanPengpeng on 2022/7/21.
//

#import "KTVAudioEffectModel.h"

@implementation KTVAudioEffectModel

- (BOOL)isEqual:(id)other
{
    if (other == self) {
        return YES;
    }
    if (![other isKindOfClass:KTVAudioEffectModel.class]) {
        return NO;
    }
    KTVAudioEffectModel *otherModel = (KTVAudioEffectModel *)other;
    return otherModel.preset == self.preset;
}

- (NSUInteger)hash
{
    return _preset ^ [_imageName hash] ^ [_title hash];
}

+ (instancetype)effectWithPreset:(AgoraAudioEffectPreset)preset title:(NSString *)title imageName:(NSString *)imageName {
    KTVAudioEffectModel *effect = [KTVAudioEffectModel new];
    effect.preset = preset;
    effect.title = title;
    effect.imageName = imageName;
    return effect;
}


@end
