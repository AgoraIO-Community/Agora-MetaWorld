//
//  MCSelectAvatarViewController.m
//  MetaChatDemo
//
//  Created by FanPengpeng on 2022/8/12.
//

#import "MCSelectAvatarViewController.h"
#import "MetaChatDemo-Swift.h"
#import "MBProgressHUD+Extension.h"
#import "MCSelectAvatarCell.h"

@interface MCSelectAvatarViewController () <UICollectionViewDelegateFlowLayout,UICollectionViewDataSource>

@property (weak, nonatomic) IBOutlet UICollectionView *collectionView;
@property (weak, nonatomic) IBOutlet UIImageView *avatarImageView;
@property (weak, nonatomic) IBOutlet UIButton *enterButton;

@property (strong, nonatomic) NSIndexPath *selectedIndexPath;

@property (strong, nonatomic) NSMutableArray *dataArray;

@end

@implementation MCSelectAvatarViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    if (self.enterBtnTitle) {
        [self.enterButton setTitle:self.enterBtnTitle forState:UIControlStateNormal];
    }
    if (_gender == AgoraMetachatGenderMale) {
        [self.sceneInfo.avatars enumerateObjectsUsingBlock:^(AgoraMetachatAvatarInfo * _Nonnull obj, NSUInteger idx, BOOL * _Nonnull stop) {
            if ([obj.avatarName containsString:@"boy"]) {
                [self.dataArray addObject:obj];
            }
        }];
    }else{
        [self.sceneInfo.avatars enumerateObjectsUsingBlock:^(AgoraMetachatAvatarInfo * _Nonnull obj, NSUInteger idx, BOOL * _Nonnull stop) {
            if (![obj.avatarName containsString:@"boy"]) {
                [self.dataArray addObject:obj];
            }
        }];
    }
    if (self.dataArray.count <= 0) {
        [MBProgressHUD showToast:@"所选性别不存在" inView:self.view];
//        dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(1 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
//            [self dismissViewControllerAnimated:YES completion:nil];
//        });
        return;
    }
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(0.1 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
        NSUInteger index = [self.dataArray indexOfObject:self.defaultAvatarInfo];
        if (index == NSNotFound) {
            index = 0;
        }
        NSIndexPath *defaultIndexPath = [NSIndexPath indexPathForItem:index inSection:0];
        [self.collectionView selectItemAtIndexPath:defaultIndexPath animated:NO scrollPosition:UICollectionViewScrollPositionNone];
        self.selectedIndexPath = defaultIndexPath;
    });
}

- (BOOL)shouldAutorotate {
    return NO;
}

- (UIInterfaceOrientationMask)supportedInterfaceOrientations {
    return UIInterfaceOrientationMaskLandscapeRight;
}

- (IBAction)didClickBackButton:(id)sender {
    [self dismissViewControllerAnimated:YES completion:nil];
}

- (IBAction)didClickEnterButton:(id)sender {
    if (self.dataArray.count == 0) {
        [MBProgressHUD showToast:@"所选性别不存在" inView:self.view];
        return;
    }
    [self dismissViewControllerAnimated:YES completion:nil];
    if (self.onSelectedAvatar) {
        AgoraMetachatAvatarInfo *info = self.dataArray[self.selectedIndexPath.row];
        self.onSelectedAvatar(info);
    }
}

- (void)setSelectedIndexPath:(NSIndexPath *)selectedIndexPath {
    _selectedIndexPath = selectedIndexPath;
    AgoraMetachatAvatarInfo *info = self.dataArray[selectedIndexPath.row];
    self.avatarImageView.image = [[UIImage alloc] initWithContentsOfFile:info.thumbnailPath];
}

- (NSInteger)collectionView:(UICollectionView *)collectionView numberOfItemsInSection:(NSInteger)section {
    return self.dataArray.count;
}

- (__kindof UICollectionViewCell *)collectionView:(UICollectionView *)collectionView cellForItemAtIndexPath:(NSIndexPath *)indexPath {
    MCSelectAvatarCell *cell = [collectionView dequeueReusableCellWithReuseIdentifier:@"selectAvatarCell" forIndexPath:indexPath];
    AgoraMetachatAvatarInfo *info = self.dataArray[indexPath.row];
    cell.imgPath = info.thumbnailPath;
    return cell;
}

- (CGSize)collectionView:(UICollectionView *)collectionView layout:(UICollectionViewLayout *)collectionViewLayout sizeForItemAtIndexPath:(NSIndexPath *)indexPath {
    return CGSizeMake(64, 64);
}

- (void)collectionView:(UICollectionView *)collectionView didSelectItemAtIndexPath:(NSIndexPath *)indexPath {
    self.selectedIndexPath = indexPath;
}

#pragma mark - getter
 
- (NSMutableArray *)dataArray {
    if (!_dataArray) {
        _dataArray = [NSMutableArray array];
    }
    return _dataArray;
}

@end
