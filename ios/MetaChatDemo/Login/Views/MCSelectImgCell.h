//
//  MCSelectProfileCell.h
//  MetaChatDemo
//
//  Created by FanPengpeng on 2022/8/11.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface MCSelectImgCell : UICollectionViewCell

@property (weak, nonatomic) IBOutlet UIImageView *imgView;

@property (nonatomic, strong)UIImage *indicatorImage;

@property (nonatomic, assign) UIEdgeInsets imageViewInsets;

@end

NS_ASSUME_NONNULL_END
