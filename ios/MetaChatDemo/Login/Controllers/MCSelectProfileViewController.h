//
//  MCSelectBaseViewController.h
//  MetaChatDemo
//
//  Created by FanPengpeng on 2022/8/11.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

typedef void(^SelectedCompletion)(NSUInteger index);

@interface MCSelectProfileViewController : UIViewController

@property (nonatomic, assign) UIInterfaceOrientationMask orientation;

@property (nonatomic, assign)CGFloat collectionViewHeight;
@property (nonatomic, assign)CGFloat minimumLineSpacing;
@property (nonatomic, assign)UIEdgeInsets imageViewInsets;
@property (nonatomic, assign)UIEdgeInsets insetForSection;
@property (nonatomic, assign)CGSize sizeForItem;
@property (nonatomic, strong)UIImage *indicatorImage;

@property (nonatomic, strong) NSArray<NSString *> *imgArray;

@property (nonatomic, assign) NSInteger defaultSelectIndex;

@property (nonatomic, copy)SelectedCompletion didSelected;


@end

NS_ASSUME_NONNULL_END
