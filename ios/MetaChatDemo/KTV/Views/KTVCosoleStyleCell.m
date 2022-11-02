//
//  KTVCosoleStyleCell.m
//  MetaChatDemo
//
//  Created by FanPengpeng on 2022/7/14.
//

#import "KTVCosoleStyleCell.h"
#import "Masonry.h"
#import "MetaChatDemo-Swift.h"

@interface KTVCosoleStyleCell()

@property (nonatomic, strong) UILabel *titleLabel;
@property (nonatomic, strong) UIImageView *bgImageView;
@property (nonatomic, strong) UIImageView *selectIndicatorView;


@end

@implementation KTVCosoleStyleCell

- (instancetype)initWithFrame:(CGRect)frame
{
    self = [super initWithFrame:frame];
    if (self) {
        [self createSubviews];
    }
    return self;
}

- (void)createSubviews {
    
    [self.contentView addSubview:self.selectIndicatorView];
    [self.selectIndicatorView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.center.mas_equalTo(self.contentView);
        make.width.height.mas_equalTo(self.contentView).offset(-2);
    }];
    
   
    [self.contentView addSubview:self.bgImageView];
    [self.bgImageView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.center.mas_equalTo(self.contentView);
        make.width.height.mas_equalTo(self.contentView).offset(-6);
    }];
    

    [self.contentView addSubview:self.titleLabel];
    [self.titleLabel mas_makeConstraints:^(MASConstraintMaker *make) {
        make.center.mas_equalTo(self.contentView);
        make.width.mas_equalTo(self.contentView).offset(-6);
    }];
}

- (void)setSelected:(BOOL)selected {
    _selectIndicatorView.hidden = !selected;
}

- (void)setTitle:(NSString *)title bgImage:(UIImage *)image {
    self.titleLabel.text = title;
    self.bgImageView.image = image;
}


#pragma makr getter

- (UIImageView *)selectIndicatorView {
    if (!_selectIndicatorView) {
        _selectIndicatorView = [[UIImageView alloc] init];
        _selectIndicatorView.backgroundColor = [[UIColor alloc] initWithHexString:@"#7A51FF"];
        _selectIndicatorView.layer.cornerRadius = 6;
        _selectIndicatorView.layer.masksToBounds = YES;
        _selectIndicatorView.hidden = YES;
    }
    return _selectIndicatorView;
}

- (UIImageView *)bgImageView {
    if (!_bgImageView) {
        _bgImageView = [[UIImageView alloc] init];
        _bgImageView.contentMode = UIViewContentModeScaleAspectFill;
        _bgImageView.layer.cornerRadius = 6;
        _bgImageView.layer.masksToBounds = YES;
        _bgImageView.backgroundColor = [UIColor grayColor];
    }
    return _bgImageView;
}

- (UILabel *)titleLabel {
    if (!_titleLabel) {
        _titleLabel = [UILabel new];
        _titleLabel.text = @"";
        _titleLabel.numberOfLines = 2;
        _titleLabel.textAlignment = NSTextAlignmentCenter;
        _titleLabel.font = [UIFont boldSystemFontOfSize:13];
        _titleLabel.textColor = [UIColor whiteColor];
    }
    return _titleLabel;
}


@end
