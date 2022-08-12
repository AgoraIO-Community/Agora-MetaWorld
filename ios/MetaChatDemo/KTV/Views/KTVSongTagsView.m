//
//  KTVSongTagsView.m
//  MetaChatDemo
//
//  Created by FanPengpeng on 2022/7/15.
//

#import "KTVSongTagsView.h"
#import "MetaChatDemo-Swift.h"
#import "KTVSongTag.h"
#import "Masonry.h"

@interface KTVSongTagsView()

@property (nonatomic, strong) NSMutableArray <UILabel *> * labels;

@end

@implementation KTVSongTagsView

+ (instancetype )tagsViewWithTags:(NSArray<KTVSongTag *> *) tags {
    KTVSongTagsView *instance = [KTVSongTagsView new];
    instance.tags = tags;
    return instance;
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
    
}

- (void)setTags:(NSMutableArray<KTVSongTag *> *)tags {
    _tags = tags;
    for (UILabel *label in self.labels) {
        label.hidden = YES;
        [label removeFromSuperview];
    }
    UILabel *leftLabel = nil;
    NSInteger i = 0;
    for (KTVSongTag *obj in tags) {
        UILabel *label = [self nextHiddenLabel];
        label.textColor = [[UIColor alloc] initWithHexRGB:obj.colorValue alpha: obj.alpha];
        label.text = [NSString stringWithFormat:@"  %@  ",obj.tagName];
        label.font = [UIFont systemFontOfSize:9];
        label.layer.cornerRadius = 4;
        label.layer.borderColor = label.textColor.CGColor;
        label.layer.borderWidth = 1;
        [label setContentCompressionResistancePriority:UILayoutPriorityDefaultHigh forAxis:UILayoutConstraintAxisHorizontal];
        [self addSubview:label];
        [label mas_makeConstraints:^(MASConstraintMaker *make) {
            if (!leftLabel) {
                make.left.mas_equalTo(self);
            }else{
                make.left.mas_equalTo(leftLabel.mas_right).offset(3);
            }
            make.top.bottom.mas_equalTo(self);
            make.height.mas_equalTo(14);
            if (i == tags.count - 1) {
                make.right.mas_equalTo(self);
            }
        }];
        leftLabel = label;
        i ++;
    }
   
}

- (NSMutableArray<UILabel *> *)labels {
    if (!_labels) {
        _labels = [NSMutableArray arrayWithCapacity:3];
    }
    return _labels;
}

- (UILabel *)nextHiddenLabel {
    for (UILabel *label in self.labels) {
        if (label.isHidden) {
            label.hidden = NO;
            return label;
        }
    }
    UILabel *label = [UILabel new];
    label.hidden = NO;
    [self.labels addObject:label];
    return label;
}

@end
