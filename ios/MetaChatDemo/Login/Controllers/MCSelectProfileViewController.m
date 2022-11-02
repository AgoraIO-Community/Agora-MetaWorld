//
//  MCSelectBaseViewController.m
//  MetaChatDemo
//
//  Created by FanPengpeng on 2022/8/11.
//

#import "MCSelectProfileViewController.h"
#import "MCSelectImgCell.h"
#import <SDWebImage.h>

@interface MCSelectProfileViewController ()

@property (weak, nonatomic) IBOutlet UILabel *titleLabel;

@property (weak, nonatomic) IBOutlet UICollectionView *collectionView;

@property (weak, nonatomic) IBOutlet NSLayoutConstraint *collectionViewHeightCon;

@end

@implementation MCSelectProfileViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    [self setUpUI];
    dispatch_after(0.1, dispatch_get_main_queue(), ^{
        NSIndexPath *indexPath = [NSIndexPath indexPathForItem:self.defaultSelectIndex inSection:0];
        if ([self.collectionView numberOfItemsInSection:0] <= self.defaultSelectIndex) {
            return;
        }
        [self.collectionView selectItemAtIndexPath:indexPath animated:NO scrollPosition:UICollectionViewScrollPositionTop];
    });
    self.collectionView.backgroundColor = [UIColor whiteColor];
}

- (void)viewDidAppear:(BOOL)animated {
    [super viewDidAppear:animated];
}

- (BOOL)shouldAutorotate {
    return YES;
}

- (UIInterfaceOrientationMask)supportedInterfaceOrientations {
    return UIInterfaceOrientationMaskAll;
}

- (void)setUpUI{
    self.titleLabel.text = self.title;
    self.collectionViewHeightCon.constant = self.collectionViewHeight;
}

- (IBAction)didClickCancelButton:(UIButton *)sender {
    [self dismissViewControllerAnimated:NO completion:nil];
}

- (IBAction)didClickConfirmButton:(UIButton *)sender {
    if (self.didSelected) {
        NSIndexPath *indexPath = [self.collectionView indexPathsForSelectedItems].firstObject;
        if (indexPath) {
            self.didSelected(indexPath.item);
        }
    }
    [self dismissViewControllerAnimated:NO completion:nil];
}

- (nonnull NSString *)reuseIdentifier {
    return @"selectCell";
}

- (NSInteger)collectionView:(UICollectionView *)collectionView numberOfItemsInSection:(NSInteger)section {
    return self.imgArray.count;
}

- (__kindof UICollectionViewCell *)collectionView:(UICollectionView *)collectionView cellForItemAtIndexPath:(NSIndexPath *)indexPath {
    MCSelectImgCell *cell = [collectionView dequeueReusableCellWithReuseIdentifier:[self reuseIdentifier] forIndexPath:indexPath];
    cell.imageViewInsets = self.imageViewInsets;
    cell.indicatorImage = self.indicatorImage;
    id item = self.imgArray[indexPath.item];
    if ([item isKindOfClass:[NSString class]]) {
        UIImage *image = [UIImage imageNamed:item];
        if (image) {
            cell.imgView.image = image;
        }else{
            NSURL *url = [NSURL URLWithString:self.imgArray[indexPath.item]];
//            UIImage *placeholderImg = [UIImage imageNamed:[NSString stringWithFormat:@"avatar%zd",indexPath.item + 1]];
            UIImage *placeholderImg = nil;
            [cell.imgView sd_setImageWithURL:url placeholderImage:placeholderImg];
        }
    }
    return cell;
}

- (CGSize)collectionView:(UICollectionView *)collectionView layout:(UICollectionViewLayout *)collectionViewLayout sizeForItemAtIndexPath:(NSIndexPath *)indexPath {
    return self.sizeForItem;
}

- (UIEdgeInsets)collectionView:(UICollectionView *)collectionView layout:(UICollectionViewLayout *)collectionViewLayout insetForSectionAtIndex:(NSInteger)section {
    return self.insetForSection;
}

- (CGFloat)collectionView:(UICollectionView *)collectionView layout:(UICollectionViewLayout *)collectionViewLayout minimumLineSpacingForSectionAtIndex:(NSInteger)section {
    return self.minimumLineSpacing;
}

@end
