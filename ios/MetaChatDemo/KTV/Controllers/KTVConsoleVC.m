//
//  KTVConsoleVC.m
//  MetaChatDemo
//
//  Created by FanPengpeng on 2022/7/12.
//

#import "KTVConsoleVC.h"
#import "UIViewController+KTVBackgroud.h"
#import "KTVConsoleView.h"
#import "Masonry.h"
#import "MetaChatDemo-Swift.h"
#import "KTVConsoleManager.h"
#import <AgoraRtcKit/AgoraRtcKit.h>


@interface KTVConsoleVC ()

{
    KTVConsoleManager *_console;
}

@end

@implementation KTVConsoleVC

- (void)viewDidLoad {
    [super viewDidLoad];
    [self setUpUI];
}

- (void)setUpUI{
    [self ktv_setBlurBackground];
    [self ktv_configCustomNaviBarWithTitle:NSLocalizedString(@"Console", @"")];
    
    UIScrollView *scrollView = [UIScrollView new];
    [self.view addSubview:scrollView];
    [scrollView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.top.mas_equalTo(60);
        make.left.bottom.mas_equalTo(self.view);
        make.width.mas_equalTo(self.blurWidth);
    }];
    
    KTVConsoleView *consoleView = [KTVConsoleView new];
    KTVConsoleManager *console = [KTVConsoleManager shared];
    _console = console;
    [consoleView setEarMoniting:console.inEarmonitoring localVoicePitch:console.localVoicePitch volume:console.recordingSignalVolume accompany:console.accompanyVolume audioEffectPreset:console.audioEffectPreset isOriginPattern:console.originalSong];
    
    [scrollView addSubview:consoleView];
    [consoleView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.edges.equalTo(scrollView);
        make.width.equalTo(scrollView);
    }];
    
    
    consoleView.patternValueChangedBlock = ^(BOOL isOriginal) {
        DLog(@"patternValueChanged --> %d",isOriginal);
        console.originalSong = isOriginal;
        [self broadcoastConsoleMessage];
    };
    
    consoleView.earReturnValueChangedBlock = ^(BOOL isOn) {
        DLog(@"earReturnValueChanged --> %d",isOn);
        console.inEarmonitoring = isOn;
    };
    
    consoleView.toneValueChangedBlock = ^(NSInteger value, double floatValue) {
        DLog(@"toneValueChanged --> %zd floatValue = %.2f",value, floatValue);
        console.localVoicePitch = (NSInteger)floatValue;
        [self broadcoastConsoleMessage];
    };
    
    consoleView.volumeChangedBlock = ^(double value) {
        DLog(@"volumeChanged --> %.2f",value);
        console.recordingSignalVolume = value;
    };
    
    consoleView.accompanyChangedBlock = ^(double value) {
        DLog(@"accompanyChanged --> %.2f",value);
        console.accompanyVolume = value;
        [self broadcoastConsoleMessage];
    };
    
    consoleView.styleChangedBlock = ^(KTVAudioEffectModel *effect) {
        DLog(@"styleChanged --> %@,%zd",effect.title,effect.preset);
        console.audioEffectPreset = effect;
        [self broadcoastConsoleMessage];
    };
}

- (void)broadcoastConsoleMessage {
//    KTVConsoleManager *console = _console;
//    [[MetaChatEngine sharedEngine] broadcastKTVConsoleMessageWithIsOriginal:console.originalSong localVoicepitch:console.localVoicePitch accompanyVolumn:console.accompanyVolume audioEffect:console.audioEffectPreset.preset];

}

@end
