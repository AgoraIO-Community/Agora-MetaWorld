//
//  MCSelectVCManager.m
//  MetaChatDemo
//
//  Created by FanPengpeng on 2022/8/23.
//

#import "MCSelectVCManager.h"
#import "MCSelectProfileViewController.h"

@interface MCSelectVCManager()

@property (nonatomic, strong) NSArray *headImageUrlArray;
@property (nonatomic, strong) NSArray *levelIconArray;
@property (strong, nonatomic) NSArray *namesArray;

@end

@implementation MCSelectVCManager

- (MCSelectProfileViewController *)selectVCWithType:(MCSelectProfileType)type defaultSeletedIndex:(NSInteger)defaultSelectIndex didSelected:(MCSelectVCManagerSelectedCompletion) selectedCompletion{
    UIStoryboard *sb = [UIStoryboard storyboardWithName:@"Main" bundle:[NSBundle mainBundle]];
    MCSelectProfileViewController *vc = [sb instantiateViewControllerWithIdentifier:@"SelectBaseVC"];
    if (type == MCSelectProfileTypeHeadImage) {
        NSArray *imgArray = self.headImageUrlArray;
        vc.imgArray = imgArray;
        vc.title = MCLocalizedString(@"Select your profile picture");
        vc.defaultSelectIndex = defaultSelectIndex;
        vc.collectionViewHeight = 230;
        vc.imageViewInsets = UIEdgeInsetsMake(2, 2, 12, 2);
        vc.indicatorImage = [UIImage imageNamed:@"login_select_profile"];
        vc.sizeForItem = CGSizeMake(60, 74);
        vc.insetForSection = UIEdgeInsetsMake(30, 0, 40, 0);
        vc.minimumLineSpacing = 29;
        vc.modalPresentationStyle = UIModalPresentationOverCurrentContext;
        vc.didSelected = ^(NSUInteger index) {
            if (selectedCompletion) {
                selectedCompletion(index, imgArray);
            }
        };
    }else if (type == MCSelectProfileTypeBadge) {
        NSArray *imgArray = self.levelIconArray;
        vc.defaultSelectIndex = defaultSelectIndex;
        vc.title = MCLocalizedString(@"Select your badge");
        vc.imgArray = imgArray;
        vc.collectionViewHeight = 148;
        vc.imageViewInsets = UIEdgeInsetsMake(7, 7, 7, 7);
        vc.indicatorImage = [UIImage imageNamed:@"login_select_badge"];
        vc.sizeForItem = CGSizeMake(72, 72);
        vc.insetForSection = UIEdgeInsetsMake(30, 0, 40, 0);
        vc.modalPresentationStyle = UIModalPresentationOverCurrentContext;
        vc.didSelected = ^(NSUInteger index) {
            if (selectedCompletion) {
                selectedCompletion(index, imgArray);
            }
        };
    }
    return vc;
}

- (MCSelectProfileViewController *)selectVCWithType:(MCSelectProfileType)type defaultValue:(id) defaultValue didSelected:(MCSelectVCManagerSelectedCompletion) selectedCompletion {
    NSArray *imgArray;
    NSInteger defaultIndex = 0;
    if (type == MCSelectProfileTypeHeadImage) {
        imgArray = self.headImageUrlArray;
    }else if (type == MCSelectProfileTypeBadge) {
        imgArray = self.levelIconArray;
    }
    defaultIndex = [imgArray indexOfObject:defaultValue];
    if (defaultIndex == NSNotFound) {
        defaultIndex = 0;
    }
    return [self selectVCWithType:type defaultSeletedIndex:defaultIndex didSelected:selectedCompletion];
}


- (NSArray *)levelIconArray {
    if (!_levelIconArray) {
//        _levelIconArray = @[@"login_badge_0",@"login_badge_1",@"login_badge_2"];
        _levelIconArray = @[
            @"http://accktvpic.oss-cn-beijing.aliyuncs.com/pic/meta/demo/metaChat/login_badge_0.png",
            @"http://accktvpic.oss-cn-beijing.aliyuncs.com/pic/meta/demo/metaChat/login_badge_1.png",
            @"http://accktvpic.oss-cn-beijing.aliyuncs.com/pic/meta/demo/metaChat/login_badge_2.png"
        ];
    }
    return _levelIconArray;
}

- (NSArray *)headImageUrlArray {
    if (!_headImageUrlArray) {
        NSInteger count = 18;
        NSMutableArray *array = [NSMutableArray arrayWithCapacity:count];
        for (int i = 1; i <= count; i ++) {
            [array addObject:[NSString stringWithFormat:@"avatar%d",i]];
        }
        _headImageUrlArray = array;
    }
    return _headImageUrlArray;
}

- (NSArray *)namesArray {
    if (!_namesArray) {
        _namesArray = @[@"James",@"William",@"Lucas",@"Henry",@"Jack",@"Daniel",@"Michael",@"Logan",@"Owen",@"Ashley",@"Aaron",@"Cooper",@"Alex",@"Wesley",@"Adam",@"Bryson",@"Jasper",@"Jason",@"Cole",@"Ace",@"Ivan",@"Leon",@"Brandon",@"Joe",@"Jenny",@"Simon",@"Kylie",@"Kobe",@"Jay",@"Travis",@"Jared",@"Jefferey",@"Hassan",@"Dash",@"Mia",@"Isabella",@"Emily",@"Layla",@"Nora",@"Lily",@"Zoe",@"Stella",@"Elena",@"Claire",@"Alice",@"Bella",@"Cora",@"Eva",@"Iris",@"Maria",@"Lucia",@"Jasmine",@"Olive",@"Blake",@"Aspen",@"Myla",@"Hanna",@"Julie",@"Eve"];
    }
    return _namesArray;
}

@end
