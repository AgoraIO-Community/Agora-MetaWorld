//
//  KTVConsoleView.m
//  MetaChatDemo
//
//  Created by FanPengpeng on 2022/7/13.
//

#import "KTVConsoleView.h"
#import "Masonry.h"
#import "MetaChatDemo-Swift.h"
#import "KTVCosoleStyleCell.h"
#import "KTVToneContolView.h"

static NSString * const kCellID = @"KTVCosoleStyleCell";

@interface KTVConsoleView()<UICollectionViewDelegateFlowLayout, UICollectionViewDataSource>


@property (nonatomic, strong) UISwitch *patternSwitch;  // 模式开关
@property (nonatomic, strong) UISwitch *ear2ReturnSwitch;
@property (nonatomic, strong) KTVToneContolView *toneContrlView;
@property (nonatomic, strong) UISlider *volumeSlider;
@property (nonatomic, strong) UISlider *accmpanySlider;
@property (nonatomic, strong) UICollectionView *collectionView;
@property (nonatomic, strong) NSArray<KTVAudioEffectModel *> *audioEffectArray;

@end

@implementation KTVConsoleView


- (instancetype)initWithFrame:(CGRect)frame
{
    self = [super initWithFrame:frame];
    if (self) {
        [self createSubviews];
    }
    return self;
}


- (void)createSubviews {
    CGFloat leftMargin = 44.f;
    
    // ear to return
    UILabel *originalLabel = [self addLabelWithTitle:MCLocalizedString(@"Original singing")];
    [originalLabel mas_makeConstraints:^(MASConstraintMaker *make) {
        make.top.mas_equalTo(self);
        make.left.mas_equalTo(leftMargin);
    }];
    
    [self addSubview:self.patternSwitch];
    [self.patternSwitch mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.mas_equalTo(originalLabel);
        make.top.mas_equalTo(originalLabel.mas_bottom).offset(6);
    }];
    
    UILabel *originalInfoLabel = [UILabel new];
    originalInfoLabel.text = MCLocalizedString(@"console_original_singing_tips");
    originalInfoLabel.textColor = [[UIColor alloc] initWithHexString:@"#B7B7B7"];
    originalInfoLabel.numberOfLines = 2;
    originalInfoLabel.font = [UIFont systemFontOfSize:10];
    [self addSubview:originalInfoLabel];
    [originalInfoLabel mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.mas_equalTo(self.patternSwitch.mas_right).offset(10);
        make.centerY.mas_equalTo(self.patternSwitch);
        make.width.mas_equalTo(250);
        make.height.mas_equalTo(30);
    }];
    
    // ear to return
    UILabel *ear2ReturnLabel = [self addLabelWithTitle:MCLocalizedString(@"Earphone Monitoring")];
    [ear2ReturnLabel mas_makeConstraints:^(MASConstraintMaker *make) {
        make.top.mas_equalTo(65);
        make.left.mas_equalTo(leftMargin);
    }];
    
    [self addSubview:self.ear2ReturnSwitch];
    [self.ear2ReturnSwitch mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.mas_equalTo(ear2ReturnLabel);
        make.top.mas_equalTo(ear2ReturnLabel.mas_bottom).offset(6);
    }];
    
    UILabel *earInfoLabel = [UILabel new];
    earInfoLabel.text = MCLocalizedString(@"console_earophone_monitoring_tips");
    earInfoLabel.textColor = [[UIColor alloc] initWithHexString:@"#B7B7B7"];
    earInfoLabel.numberOfLines = 2;
    earInfoLabel.font = [UIFont systemFontOfSize:10];
    [self addSubview:earInfoLabel];
    [earInfoLabel mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.mas_equalTo(_ear2ReturnSwitch.mas_right).offset(10);
        make.centerY.mas_equalTo(self.ear2ReturnSwitch);
        make.width.mas_equalTo(250);
        make.height.mas_equalTo(30);
    }];
    
    // Rising-falling tone
    UILabel *risingLabel = [self addLabelWithTitle:MCLocalizedString(@"Change Key of Song")];
    [risingLabel mas_makeConstraints:^(MASConstraintMaker *make) {
        make.top.mas_equalTo(self).offset(130);
        make.left.mas_equalTo(leftMargin);
    }];
    
    [self addSubview:self.toneContrlView];
    [self.toneContrlView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.mas_equalTo(leftMargin - 6);
        make.right.mas_equalTo(0);
        make.top.mas_equalTo(risingLabel.mas_bottom).offset(5);
        make.height.mas_equalTo(40);
    }];
    
    // volume
    UILabel *volumeLabel = [self addLabelWithTitle:MCLocalizedString(@"Volume")];
    [volumeLabel mas_makeConstraints:^(MASConstraintMaker *make) {
        make.top.mas_equalTo(self).offset(128 + 65);
        make.left.mas_equalTo(leftMargin);
    }];
    [self addSubview:self.volumeSlider];
    [self.volumeSlider mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.mas_equalTo(leftMargin);
        make.top.mas_equalTo(volumeLabel.mas_bottom).offset(5);
        make.width.mas_equalTo(295);
        make.height.mas_equalTo(30);
    }];
    
    // accompany
    UILabel *accompanyLabel = [self addLabelWithTitle:MCLocalizedString(@"Enable Accompaniment Music")];
    [accompanyLabel mas_makeConstraints:^(MASConstraintMaker *make) {
        make.top.mas_equalTo(self).offset(187 + 65);
        make.left.mas_equalTo(leftMargin);
    }];
    [self addSubview:self.accmpanySlider];
    [self.accmpanySlider mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.mas_equalTo(leftMargin);
        make.top.mas_equalTo(accompanyLabel.mas_bottom).offset(5);
        make.width.mas_equalTo(295);
        make.height.mas_equalTo(30);
    }];
    
    // 选项
    [self addSubview:self.collectionView];
    [self.collectionView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.mas_equalTo(leftMargin);
        make.top.mas_equalTo(accompanyLabel.mas_bottom).offset(42);
        make.right.mas_equalTo(0);
        make.height.mas_equalTo(54);
        make.bottom.mas_equalTo(self);
    }];
    
    [self setSelectedIndex:0];
}

-(void)setSelectedIndex:(NSInteger)index {
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(0.1 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
        [self.collectionView selectItemAtIndexPath:[NSIndexPath indexPathForItem:index inSection:0] animated:NO scrollPosition:UICollectionViewScrollPositionRight];
    });
}


- (void)setEarMoniting:(BOOL)enable localVoicePitch:(double)voicePitchValue volume:(NSInteger)volume accompany:(NSInteger)accompany audioEffectPreset:(nonnull KTVAudioEffectModel *)effect isOriginPattern:(BOOL)isOrigin{
    self.patternSwitch.on = isOrigin;
    self.ear2ReturnSwitch.on = enable;
    self.toneContrlView.currentFloatValue = voicePitchValue;
    self.volumeSlider.value = volume;
    self.accmpanySlider.value = accompany;
    
    NSUInteger index = [self.audioEffectArray indexOfObject:effect];
    if (index != NSNotFound) {
        [self setSelectedIndex:index];
    }
}

#pragma mark - private

- (UILabel *)addLabelWithTitle:(NSString *)title {
    UILabel *label = [UILabel new];
    label.textColor = [UIColor whiteColor];
    label.font = [UIFont systemFontOfSize:12];
    label.text = title;
    [self addSubview:label];
    return  label;
}

- (UISlider *)createCustomSlider {
    UISlider *slider = [[UISlider alloc] init];
    [slider setThumbImage:[UIImage imageNamed:@"console_slider"] forState:UIControlStateNormal];
    [slider setMinimumTrackTintColor:[UIColor whiteColor]];
    [slider setMaximumTrackTintColor:[[UIColor alloc] initWithHexRGB:0xffffff alpha:0.12]];
    return slider;
}

#pragma mark - actions

- (void)patternSwitchValueChanged:(UISwitch *)aSwitch {
    if (self.patternValueChangedBlock) {
        self.patternValueChangedBlock(aSwitch.isOn);
    }
}

// 耳返开关
- (void)earReturnSwitchValueChanged:(UISwitch *)aSwitch {
    if (self.earReturnValueChangedBlock) {
        self.earReturnValueChangedBlock(aSwitch.isOn);
    }
}

// 滑块变化
- (void)sliderValueChanged:(UISlider *)slider {
    if (slider == self.volumeSlider) {
        if (self.volumeChangedBlock) {
            self.volumeChangedBlock(slider.value);
        }
    }
    if (slider == self.accmpanySlider) {
        if (self.accompanyChangedBlock) {
            self.accompanyChangedBlock(slider.value);
        }
    }
}

#pragma mark - collection view delegate

- (NSInteger)collectionView:(UICollectionView *)collectionView numberOfItemsInSection:(NSInteger)section {
    return self.audioEffectArray.count;
}

- (__kindof UICollectionViewCell *)collectionView:(UICollectionView *)collectionView cellForItemAtIndexPath:(NSIndexPath *)indexPath {
    KTVAudioEffectModel *model = self.audioEffectArray[indexPath.row];
    KTVCosoleStyleCell *cell = [collectionView dequeueReusableCellWithReuseIdentifier:kCellID forIndexPath:indexPath];
    [cell setTitle:model.title bgImage:[UIImage imageNamed:model.imageName]];
    return  cell;
}

- (void)collectionView:(UICollectionView *)collectionView didSelectItemAtIndexPath:(NSIndexPath *)indexPath {
    KTVAudioEffectModel *model = self.audioEffectArray[indexPath.row];
    if (self.styleChangedBlock) {
        self.styleChangedBlock(model);
    }
}

#pragma mark - getter

- (UISwitch *)patternSwitch {
    if (!_patternSwitch) {
        _patternSwitch = [UISwitch new];
        _patternSwitch.onTintColor = [[UIColor alloc] initWithHexString:@"#7A51FF"];
        [_patternSwitch addTarget:self action:@selector(patternSwitchValueChanged:) forControlEvents:UIControlEventValueChanged];
    }
    return _patternSwitch;
}

- (UISwitch *)ear2ReturnSwitch {
    if (!_ear2ReturnSwitch) {
        _ear2ReturnSwitch = [UISwitch new];
        _ear2ReturnSwitch.onTintColor = [[UIColor alloc] initWithHexString:@"#7A51FF"];
        [_ear2ReturnSwitch addTarget:self action:@selector(earReturnSwitchValueChanged:) forControlEvents:UIControlEventValueChanged];
    }
    return _ear2ReturnSwitch;
}

- (KTVToneContolView *)toneContrlView {
    if (!_toneContrlView) {
        _toneContrlView = [KTVToneContolView new];
        _toneContrlView.maxValue = 13;
        _toneContrlView.minFloatValue = -12;
        _toneContrlView.maxFloatValue = 12;
        __weak typeof(self) wSelf = self;
        _toneContrlView.valueChangedBlock = ^(NSInteger value, double floatValue) {
            if (wSelf.toneValueChangedBlock) {
                wSelf.toneValueChangedBlock(value, floatValue);
            }
        };
    }
    return _toneContrlView;
}

- (UISlider *)volumeSlider {
    if (!_volumeSlider) {
        _volumeSlider = [self createCustomSlider];
        _volumeSlider.minimumValue = 0;
        _volumeSlider.maximumValue = 100;
        [_volumeSlider addTarget:self action:@selector(sliderValueChanged:) forControlEvents:UIControlEventValueChanged];
    }
    return _volumeSlider;
}

- (UISlider *)accmpanySlider {
    if (!_accmpanySlider) {
        _accmpanySlider = [self createCustomSlider];
        _accmpanySlider.minimumValue = 0;
        _accmpanySlider.maximumValue = 100;
        [_accmpanySlider addTarget:self action:@selector(sliderValueChanged:) forControlEvents:UIControlEventValueChanged];
    }
    return _accmpanySlider;
}

- (UICollectionView *)collectionView {
    if (!_collectionView) {
        UICollectionViewFlowLayout *flowlayout = [[UICollectionViewFlowLayout alloc] init];
        flowlayout.itemSize = CGSizeMake(75, 52);
        flowlayout.minimumInteritemSpacing = 12;
        flowlayout.sectionInset = UIEdgeInsetsMake(0, 0, 0, 0);
        flowlayout.scrollDirection = UICollectionViewScrollDirectionHorizontal;
        _collectionView = [[UICollectionView alloc] initWithFrame:CGRectZero collectionViewLayout:flowlayout];
        [_collectionView registerClass:KTVCosoleStyleCell.class forCellWithReuseIdentifier:kCellID];
        _collectionView.showsHorizontalScrollIndicator = NO;
        _collectionView.backgroundColor = [UIColor clearColor];
        _collectionView.delegate = self;
        _collectionView.dataSource = self;
    }
    return _collectionView;
}

- (NSArray<KTVAudioEffectModel *> *)audioEffectArray {
    if (!_audioEffectArray) {
        KTVAudioEffectModel *recording = [KTVAudioEffectModel effectWithPreset:AgoraAudioEffectPresetRoomAcousStudio title:MCLocalizedString(@"Recording Studio") imageName:@"audio_effect_recording_studio"];
        KTVAudioEffectModel *concert = [KTVAudioEffectModel effectWithPreset:AgoraAudioEffectPresetRoomAcousVocalConcer title:MCLocalizedString(@"Concert") imageName:@"audio_effect_concert"];
        KTVAudioEffectModel *ktv = [KTVAudioEffectModel effectWithPreset:AgoraAudioEffectPresetRoomAcousticsKTV title:@"KTV" imageName:@"audio_effect_KTV"];
        KTVAudioEffectModel *hollow = [KTVAudioEffectModel effectWithPreset:AgoraAudioEffectPresetRoomAcousSpatial title:MCLocalizedString(@"Hollow Sound") imageName:@"audio_effect_hollow"];
        
        _audioEffectArray = @[recording,concert, ktv, hollow];
    }
    return _audioEffectArray;
}

@end
