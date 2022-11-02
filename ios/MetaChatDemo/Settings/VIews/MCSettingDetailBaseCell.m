//
//  MCSettingDetailBaseCell.m
//  MetaChatDemo
//
//  Created by FanPengpeng on 2022/8/15.
//

#import "MCSettingDetailBaseCell.h"
#import "Masonry.h"

@interface MCSettingDetailBaseCell()

@end

@implementation MCSettingDetailBaseCell

- (instancetype)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier {
    self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
    if (self) {
        [self createSubviews];
    }
    return self;
}

- (void)createSubviews{
    self.backgroundColor = [UIColor clearColor];
    self.contentView.backgroundColor = [UIColor clearColor];
    
    [self.contentView addSubview:self.titleLabel];
    [self.titleLabel mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.mas_equalTo(30);
        make.centerY.mas_equalTo(self.contentView);
    }];
    [self.contentView addSubview:self.iconImgView];
    [self.iconImgView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.right.mas_equalTo(-30);
        make.centerY.mas_equalTo(self.contentView);
    }];
    
    [self createRightView];
    
    if ([self rightViewClickEnable]) {
        UIButton *rightButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [rightButton addTarget:self action:@selector(didClickRightButton) forControlEvents:UIControlEventTouchUpInside];
        [self.contentView addSubview:rightButton];
        [rightButton mas_makeConstraints:^(MASConstraintMaker *make) {
            make.top.bottom.mas_equalTo(0);
            make.right.mas_equalTo(-20);
            make.width.mas_equalTo(80);
        }];
    }
}

- (void)didClickRightButton {
    if (self.rightViewClicked) {
        self.rightViewClicked();
    }
}

- (void)createRightView {
}

- (UILabel *)titleLabel {
    if (!_titleLabel) {
        _titleLabel = [UILabel new];
        _titleLabel.font = [UIFont boldSystemFontOfSize:12];
        _titleLabel.textColor = [UIColor whiteColor];
    }
    return _titleLabel;
}

- (UIImageView *)iconImgView {
    if (!_iconImgView) {
        _iconImgView = [UIImageView new];
        _iconImgView.image = [self rightIconImage];
    }
    return _iconImgView;
}

- (UIImage *)rightIconImage {
    return [UIImage new];
}

- (BOOL)rightViewClickEnable {
    return NO;
}

@end
