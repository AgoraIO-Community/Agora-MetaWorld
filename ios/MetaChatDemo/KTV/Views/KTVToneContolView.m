//
//  KTVToneContolView.m
//  MetaChatDemo
//
//  Created by FanPengpeng on 2022/7/14.
//

#import "KTVToneContolView.h"
#import "Masonry.h"

static CGFloat const kValueViewWidth = 250;
static CGFloat const minHeight = 2;
static CGFloat const diff = 2;

@interface KTVToneContolView()

@property (nonatomic, strong) UIButton *reduceButton;
@property (nonatomic, strong) UIView *valueView;
@property (nonatomic, strong) UIButton *plusButton;
@property (nonatomic, strong) NSMutableArray<CALayer *> *valueViewArray;

@end

@implementation KTVToneContolView

+ (instancetype)toneControlViewWithMaxValue:(NSInteger)maxValue {
    KTVToneContolView *controlView = [KTVToneContolView new];
    controlView.maxValue = maxValue;
    return controlView;
}


- (instancetype)initWithFrame:(CGRect)frame
{
    self = [super initWithFrame:frame];
    if (self) {
        [self createSubviews];
    }
    return self;
}

- (void)createSubviews {
    [self addSubview:self.reduceButton];
    [self.reduceButton mas_makeConstraints:^(MASConstraintMaker *make) {
        make.width.height.mas_equalTo(40);
        make.left.mas_equalTo(0);
        make.top.mas_equalTo(0);
    }];
    
    CGFloat maxHeight = minHeight + diff * (_maxValue - 1);
    [self addSubview:self.valueView];
    [self.valueView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.mas_equalTo(self.reduceButton.mas_right).offset(6);
        make.top.mas_equalTo(self);
        make.width.mas_equalTo(kValueViewWidth);
        make.height.mas_equalTo(maxHeight);
    }];
    
    [self addSubview:self.plusButton];
    [self.plusButton mas_makeConstraints:^(MASConstraintMaker *make) {
        make.width.height.mas_equalTo(40);
        make.right.mas_equalTo(-20);
        make.top.mas_equalTo(0);
    }];
}

- (void)setMaxValue:(NSInteger)maxValue {
    _maxValue = MIN(maxValue, 13);
    _valueViewArray = [NSMutableArray arrayWithCapacity:_maxValue];
    CGFloat maxHeight = minHeight + diff * (_maxValue - 1);
    CGFloat itemWidth = 4.f;
    CGFloat horiSpace = (kValueViewWidth - itemWidth * _maxValue) / (_maxValue - 1);
    for (NSInteger i = 0; i < _maxValue; i ++) {
        CALayer *layer = [CALayer layer];
        layer.backgroundColor = [UIColor grayColor].CGColor;
        [_valueViewArray addObject:layer];
        CGFloat itemHeight = minHeight + i * diff;
        layer.frame = CGRectMake(horiSpace * i, maxHeight - itemHeight + 6, itemWidth, itemHeight);
        layer.cornerRadius = itemWidth * 0.5;
        layer.masksToBounds = true;
        [self.valueView.layer addSublayer:layer];
    }
}

- (void)setCurrentValue:(NSInteger)currentValue {
    [self updateUIWithCurrentValue:currentValue updateFloatValue:YES];
}

- (void)updateUIWithCurrentValue:(NSInteger)currentValue updateFloatValue:(BOOL)update {
    _currentValue = currentValue;
    [_valueViewArray enumerateObjectsUsingBlock:^(CALayer * _Nonnull obj, NSUInteger idx, BOOL * _Nonnull stop) {
        if (idx < currentValue) {
            obj.backgroundColor = [UIColor whiteColor].CGColor;
        }else{
            obj.backgroundColor = [UIColor grayColor].CGColor;
        }
    }];
    if (update) {
        _currentFloatValue = _minFloatValue + (double)(currentValue - 1) / (_maxValue - 1) * (_maxFloatValue - _minFloatValue);
    }
    if (self.valueChangedBlock) {
        self.valueChangedBlock(currentValue,_currentFloatValue);
    }
}

- (void)setCurrentFloatValue:(double)currentFloatValue {
    if (_maxFloatValue <= _minFloatValue) {
        return;
    }
    _currentFloatValue = currentFloatValue;
    NSInteger intValue = floor(((currentFloatValue - _minFloatValue) / (_maxFloatValue - _minFloatValue) * _maxValue) + 0.5);
    [self updateUIWithCurrentValue:intValue updateFloatValue:NO];
}

#pragma mark - actions

- (void)didClickReduceButton {
    if (self.currentValue <= 1) {
        return;
    }
    self.currentValue --;
}

- (void)didClickPlusButton {
    if (self.currentValue == _maxValue) {
        return;
    }
    self.currentValue ++;
}

#pragma mark - getter

- (UIButton *)reduceButton {
    if (!_reduceButton) {
        _reduceButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_reduceButton setImage:[UIImage imageNamed:@"console_reduce"] forState:UIControlStateNormal];
        [_reduceButton addTarget:self action:@selector(didClickReduceButton) forControlEvents:UIControlEventTouchUpInside];
    }
    return _reduceButton;
}

- (UIButton *)plusButton {
    if (!_plusButton) {
        _plusButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_plusButton setImage:[UIImage imageNamed:@"console_add"] forState:UIControlStateNormal];
        [_plusButton addTarget:self action:@selector(didClickPlusButton) forControlEvents:UIControlEventTouchUpInside];
    }
    return _plusButton;
}

- (UIView *)valueView {
    if (!_valueView) {
        _valueView = [UIView new];
        _valueView.backgroundColor = [UIColor redColor];
    }
    return _valueView;
}


@end
