//
//  MCSettingDetailImageCell.m
//  MetaChatDemo
//
//  Created by FanPengpeng on 2022/8/16.
//

#import "MCSettingDetailImageCell.h"
#import "Masonry.h"
#import "SDWebImage.h"

@interface MCSettingDetailImageCell()

@property (nonatomic, strong) UIImageView *contentImgView;

@end

@implementation MCSettingDetailImageCell


- (void)createRightView {
    [self.contentView addSubview:self.contentImgView];
    [self.contentImgView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.right.mas_equalTo(-54);
        make.centerY.mas_equalTo(self.contentView);
        make.width.mas_equalTo(36);
        make.height.mas_equalTo(36);
    }];
}

- (UIImage *)rightIconImage {
    return [UIImage imageNamed:@"setting_detail_arrow"];
}

- (BOOL)rightViewClickEnable {
    return YES;
}



- (UIImageView *)contentImgView {
    if (!_contentImgView) {
        _contentImgView = [UIImageView new];
        _contentImgView.contentMode = UIViewContentModeScaleAspectFit;
    }
    return _contentImgView;
}

- (void)setTitle:(NSString *)title imgeUrl:(NSString *)imageUrl {
    self.titleLabel.text = title;
    if (imageUrl) {
        UIImage *image = [UIImage imageNamed:imageUrl];
        if (image) {
            self.contentImgView.image = image;
        }else{
            [self.contentImgView sd_setImageWithURL:[NSURL URLWithString:imageUrl] placeholderImage:[UIImage imageNamed:@""]];            
        }
    }
}



@end
