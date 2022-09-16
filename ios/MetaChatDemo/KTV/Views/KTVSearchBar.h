//
//  KTVSearchBar.h
//  MetaChatDemo
//
//  Created by FanPengpeng on 2022/7/12.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

typedef void(^KTVSearchBarClickedSearch)(NSString *text);

@interface KTVSearchBar : UIView

- (void)didClickSearchButton:(KTVSearchBarClickedSearch) block;

@end

NS_ASSUME_NONNULL_END
