//
//  KTVConsoleView.h
//  MetaChatDemo
//
//  Created by FanPengpeng on 2022/7/13.
//

#import <UIKit/UIKit.h>
#import "KTVAudioEffectModel.h"

NS_ASSUME_NONNULL_BEGIN

typedef void(^KTVConsoleViewOriginalPatternBlock)(BOOL isOriginal);

typedef void(^KTVConsoleViewEarReturnBlock)(BOOL isOn);

typedef void(^KTVConsoleViewToneChangedBlock)(NSInteger value, double floatValue);

typedef void(^KTVConsoleViewSliderValueChangedBlock)(double value);

typedef void(^KTVConsoleViewStyleChangedBlock)(KTVAudioEffectModel *effect);


@interface KTVConsoleView : UIView

@property (copy, nonatomic) KTVConsoleViewOriginalPatternBlock patternValueChangedBlock;

@property (copy, nonatomic) KTVConsoleViewEarReturnBlock earReturnValueChangedBlock;

@property (copy, nonatomic) KTVConsoleViewToneChangedBlock toneValueChangedBlock;

@property (copy, nonatomic) KTVConsoleViewSliderValueChangedBlock volumeChangedBlock;

@property (copy, nonatomic) KTVConsoleViewSliderValueChangedBlock accompanyChangedBlock;

@property (copy, nonatomic) KTVConsoleViewStyleChangedBlock styleChangedBlock;



- (void)setEarMoniting:(BOOL)enable localVoicePitch:(double)voicePitchValue volume:(NSInteger)volume accompany:(NSInteger)accompany audioEffectPreset:(KTVAudioEffectModel *) effect isOriginPattern:(BOOL) isOrigin;

@end

NS_ASSUME_NONNULL_END
