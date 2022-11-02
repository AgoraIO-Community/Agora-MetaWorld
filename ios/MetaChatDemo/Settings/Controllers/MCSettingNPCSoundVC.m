//
//  MCSettingNPCSoundVC.m
//  MetaChatDemo
//
//  Created by FanPengpeng on 2022/10/17.
//

#import "MCSettingNPCSoundVC.h"

@interface MCSettingNPCSoundVC ()
@property (weak, nonatomic) IBOutlet UISlider *tvSlider;
@property (weak, nonatomic) IBOutlet UISlider *tableNPCSlider;
@property (weak, nonatomic) IBOutlet UILabel *tvLabel;
@property (weak, nonatomic) IBOutlet UILabel *npcLabel;


@property (weak, nonatomic) IBOutlet UILabel *revRangeLabel;
@property (weak, nonatomic) IBOutlet UILabel *unitLabel;
@property (weak, nonatomic) IBOutlet UISlider *revRangeSlider;
@property (weak, nonatomic) IBOutlet UISlider *unitSlider;

@end

@implementation MCSettingNPCSoundVC

- (void)viewDidLoad {
    [super viewDidLoad];
    self.view.backgroundColor = [UIColor clearColor];
    _tvSlider.value = [self.sceneMgr.tvPlayerMgr.player getPlayoutVolume];
    _tvLabel.text = [NSString stringWithFormat:@"%d",[self.sceneMgr.tvPlayerMgr.player getPlayoutVolume]];
    _npcLabel.text = [NSString stringWithFormat:@"%d",[self.sceneMgr.tableNPCPlayerMgr.player getPlayoutVolume]];
    
    _tableNPCSlider.value = [self.sceneMgr.tableNPCPlayerMgr.player getPlayoutVolume];
    _revRangeLabel.text = [NSString stringWithFormat:@"%.1f",[MetaChatEngine sharedEngine].audioRecvRange];
    _unitLabel.text = [NSString stringWithFormat:@"%.1f",[MetaChatEngine sharedEngine].distanceUnit];
    _revRangeSlider.value = [MetaChatEngine sharedEngine].audioRecvRange;
    _unitSlider.value = [MetaChatEngine sharedEngine].distanceUnit;
    
}

- (IBAction)tvPlayerVolumeChanged:(UISlider *)sender {
    [self.sceneMgr.tvPlayerMgr.player adjustPlayoutVolume:sender.value];
    _tvLabel.text = [NSString stringWithFormat:@"%d",[self.sceneMgr.tvPlayerMgr.player getPlayoutVolume]];
}

- (IBAction)npcPlayerVolumeChanged:(UISlider *)sender {
    [self.sceneMgr.tableNPCPlayerMgr.player adjustPlayoutVolume:sender.value];
    _npcLabel.text = [NSString stringWithFormat:@"%d",[self.sceneMgr.tableNPCPlayerMgr.player getPlayoutVolume]];
}

- (IBAction)revRangeSliderValueChanged:(UISlider *)sender {
    [MetaChatEngine sharedEngine].audioRecvRange = sender.value;
    _revRangeLabel.text = [NSString stringWithFormat:@"%.1f",sender.value];
}

- (IBAction)unitSliderValueChanged:(UISlider *)sender {
    [MetaChatEngine sharedEngine].distanceUnit = sender.value;
    _unitLabel.text = [NSString stringWithFormat:@"%.1f",sender.value];
}



@end
