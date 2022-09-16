//
//  KTVAudioEffectModel.h
//  MetaChatDemo
//
//  Created by FanPengpeng on 2022/7/21.
//

#import <Foundation/Foundation.h>
#import <AgoraRtcKit/AgoraRtcKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface KTVAudioEffectModel : NSObject

@property (assign, nonatomic)AgoraAudioEffectPreset preset;
@property (copy, nonatomic) NSString *title;
@property (copy, nonatomic) NSString *imageName;

+ (instancetype)effectWithPreset:(AgoraAudioEffectPreset)preset title:(NSString *)title imageName:(NSString *)imageName;

@end

NS_ASSUME_NONNULL_END
