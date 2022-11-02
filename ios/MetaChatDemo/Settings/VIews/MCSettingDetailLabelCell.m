//
//  MCSettingDetailLabelCell.m
//  MetaChatDemo
//
//  Created by FanPengpeng on 2022/8/16.
//

#import "MCSettingDetailLabelCell.h"
#import "Masonry.h"

@interface MCSettingDetailLabelCell()

@property (nonatomic, strong) UILabel *infoLabel;

@end

@implementation MCSettingDetailLabelCell

- (void)createRightView {
    [self.contentView addSubview:self.infoLabel];
    [self.infoLabel mas_makeConstraints:^(MASConstraintMaker *make) {
        make.right.mas_equalTo(-54);
        make.centerY.mas_equalTo(self.contentView);
    }];
}

- (UIImage *)rightIconImage {
    return [UIImage imageNamed:@"setting_detail_arrow"];
}

- (BOOL)rightViewClickEnable {
    return YES;
}

- (UILabel *)infoLabel {
    if (!_infoLabel) {
        _infoLabel = [UILabel new];
        _infoLabel.textColor = [UIColor whiteColor];
        _infoLabel.font = [UIFont systemFontOfSize:12];
    }
    return _infoLabel;
}

- (void)setTitle:(NSString *)title info:(NSString *)info {
    self.titleLabel.text = title;
    self.infoLabel.text = info;
}

@end
