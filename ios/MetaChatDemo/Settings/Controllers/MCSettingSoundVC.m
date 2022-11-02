//
//  MCSettingSoundVC.m
//  MetaChatDemo
//
//  Created by FanPengpeng on 2022/8/16.
//

#import "MCSettingSoundVC.h"
#import "MetaChatDemo-Swift.h"

static NSString *const kSettingTitleSpatial = @"Enable Spatial Audio Background";
static NSString *const kSettingTitleVocalDistorbn = @"Vocal Distortion";
static NSString *const kSettingTitleFactor = @"Vocal Air Attenuation Factor";

@interface MCSettingSoundVC ()

@end

@implementation MCSettingSoundVC

- (NSArray<id<MCSettingDetailModel>> *)settingItems {
    MCSettingDetailLabelModel *spatialModel = [MCSettingDetailLabelModel new];
    spatialModel.title = MCLocalizedString(kSettingTitleSpatial);
    spatialModel.info = MetaChatEngine.sharedEngine.spatialAudioOpen ? MCLocalizedString(@"Open") : MCLocalizedString(@"Close");
    
    MCSettingDetailLabelModel *distorbnModel = [MCSettingDetailLabelModel new];
    distorbnModel.title = MCLocalizedString(kSettingTitleVocalDistorbn);
    distorbnModel.info = MetaChatEngine.sharedEngine.audioBlurOpen ? MCLocalizedString(@"Open") : MCLocalizedString(@"Close");
    
    MCSettingDetailLabelModel *factorModel = [MCSettingDetailLabelModel new];
    factorModel.title = MCLocalizedString(kSettingTitleFactor);
    factorModel.info = MetaChatEngine.sharedEngine.audioAirAbsorbOpen ?MCLocalizedString(@"Open") : MCLocalizedString(@"Close");
    
    return @[spatialModel, distorbnModel, factorModel];
}

- (void)handleTextFiledCellWithTextFieldModel:(MCSettingDetailTextFieldModel *)model endedText:(NSString *)text {
  
}

- (void)handleClickRightViewWithImageModel:(MCSettingDetailImageModel *)model {
    
}

- (void)handleClickRightViewWithLabelModel:(MCSettingDetailLabelModel *)model {
    if ([model.title isEqualToString:MCLocalizedString(kSettingTitleSpatial)]) {
        [MetaChatEngine.sharedEngine enableSpatialAudio:!MetaChatEngine.sharedEngine.spatialAudioOpen];
//        self.sceneMgr.enableSpatialAudio = !MetaChatEngine.sharedEngine.spatialAudioOpen;
    }
    if ([model.title isEqualToString:MCLocalizedString(kSettingTitleVocalDistorbn)]) {
        [MetaChatEngine.sharedEngine setRemoteUserAudioEnableBlur:!MetaChatEngine.sharedEngine.audioBlurOpen];
    }
    if ([model.title isEqualToString:MCLocalizedString(kSettingTitleFactor)]) {
        [MetaChatEngine.sharedEngine setRemoteUserAudioEnableAirAbsorb:!MetaChatEngine.sharedEngine.audioAirAbsorbOpen];
    }
    [self reloadData];
}

@end
