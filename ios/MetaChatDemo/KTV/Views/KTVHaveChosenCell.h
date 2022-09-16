//
//  KTVHaveChosenCell.h
//  MetaChatDemo
//
//  Created by FanPengpeng on 2022/7/13.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

typedef void(^KTVHaveChosenCellClickButtonBlock)(void);

@interface KTVHaveChosenCell : UITableViewCell

@property (copy, nonatomic) KTVHaveChosenCellClickButtonBlock didClickSetTopButtonBlock;

@property (copy, nonatomic) KTVHaveChosenCellClickButtonBlock didClickDeleteButtonBlock;

- (void)setImage:(NSString *)imgUrl name:(NSString *)name author:(NSString *)author index:(NSInteger)index isTop:(BOOL)isTop;

@end

NS_ASSUME_NONNULL_END
