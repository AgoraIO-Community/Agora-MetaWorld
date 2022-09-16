//
//  KTVChooseSongCell.h
//  MetaChatDemo
//
//  Created by FanPengpeng on 2022/7/12.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

typedef void(^KTVChooseSongCellClickButtonBlock)(void);

@interface KTVChooseSongCell : UITableViewCell

@property (copy, nonatomic) KTVChooseSongCellClickButtonBlock didClickAddButtonBlock;

@property (copy, nonatomic) KTVChooseSongCellClickButtonBlock didClickReduceButtonBlock;

- (void)setImage:(NSString *)imgUrl name:(NSString *)name author:(NSString *)author type:(NSInteger)type pitchType:(NSInteger)pitchType isAdded:(BOOL) isAdded;

@end

NS_ASSUME_NONNULL_END
