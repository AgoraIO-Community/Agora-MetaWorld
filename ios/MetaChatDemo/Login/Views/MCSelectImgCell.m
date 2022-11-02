//
//  MCSelectProfileCell.m
//  MetaChatDemo
//
//  Created by FanPengpeng on 2022/8/11.
//

#import "MCSelectImgCell.h"

@interface MCSelectImgCell()

@property (weak, nonatomic) IBOutlet UIImageView *selectIndicatorImgView;

@property (weak, nonatomic) IBOutlet NSLayoutConstraint *rightCon;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *leftCon;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *topCon;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *bottomCon;

@end

@implementation MCSelectImgCell

- (void)awakeFromNib {
    [super awakeFromNib];
}

- (void)setIndicatorImage:(UIImage *)indicatorImage {
    _indicatorImage = indicatorImage;
    self.selectIndicatorImgView.image = self.indicatorImage;
}

- (void)setImageViewInsets:(UIEdgeInsets)imageViewInsets {
    _imageViewInsets = imageViewInsets;
    self.topCon.constant = self.imageViewInsets.top;
    self.leftCon.constant = self.imageViewInsets.left;
    self.bottomCon.constant = self.imageViewInsets.bottom;
    self.rightCon.constant = self.imageViewInsets.right;
}

- (void)setSelected:(BOOL)selected {
    [super setSelected:selected];
    self.selectIndicatorImgView.hidden = !selected;
}

@end
