//
//  KTVSingingCell.h
//  MetaChatDemo
//
//  Created by FanPengpeng on 2022/7/26.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

typedef void(^KTVSingingCellClickButtonBlock)(void);

@interface KTVSingingCell : UITableViewCell

@property (copy, nonatomic) KTVSingingCellClickButtonBlock didClickNextButtonBlock;


- (void)setImage:(NSString *)imgUrl name:(NSString *)name author:(NSString *)author index:(NSInteger)index;

@end

NS_ASSUME_NONNULL_END
