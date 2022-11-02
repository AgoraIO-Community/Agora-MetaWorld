//
//  MCDownloadViewController.h
//  MetaChatDemo
//
//  Created by FanPengpeng on 2022/8/23.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

typedef void(^MCDownloadCancelBlock)(void);

@interface MCDownloadViewController : UIViewController

@property (nonatomic, assign) CGFloat totalSize;

@property (nonatomic, assign) CGFloat progress;

@property (nonatomic, copy) MCDownloadCancelBlock cancelAction;

@end

NS_ASSUME_NONNULL_END
