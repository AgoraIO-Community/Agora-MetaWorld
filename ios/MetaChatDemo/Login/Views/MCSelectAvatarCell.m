//
//  MCSelectAvatarCell.m
//  MetaChatDemo
//
//  Created by FanPengpeng on 2022/8/18.
//

#import "MCSelectAvatarCell.h"

@interface MCSelectAvatarCell()

@property (weak, nonatomic) IBOutlet UIImageView *iconImageView;

@property (weak, nonatomic) IBOutlet UIImageView *selectIndicator;

@end

@implementation MCSelectAvatarCell

- (void)setSelected:(BOOL)selected {
    [super setSelected:selected];
    self.selectIndicator.hidden = !selected;
}

- (void)setImgPath:(NSString *)imgPath {
    _imgPath = imgPath;
    self.iconImageView.image = [[UIImage alloc] initWithContentsOfFile:imgPath];
}


@end
