//
//  MCSelectVCManager.h
//  MetaChatDemo
//
//  Created by FanPengpeng on 2022/8/23.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

typedef enum : NSUInteger {
    MCSelectProfileTypeCustom,
    MCSelectProfileTypeHeadImage,
    MCSelectProfileTypeBadge,
} MCSelectProfileType;

typedef void(^MCSelectVCManagerSelectedCompletion)(NSInteger selectedIndex, NSArray *imgArray);

@class MCSelectProfileViewController;

@interface MCSelectVCManager : NSObject

@property (nonatomic, strong, readonly) NSArray *headImageUrlArray;
@property (nonatomic, strong, readonly) NSArray *levelIconArray;
@property (strong, nonatomic, readonly) NSArray *namesArray;


- (MCSelectProfileViewController *)selectVCWithType:(MCSelectProfileType)type defaultSeletedIndex:(NSInteger)defaultSelectIndex didSelected:(MCSelectVCManagerSelectedCompletion) selectedCompletion;

- (MCSelectProfileViewController *)selectVCWithType:(MCSelectProfileType)type defaultValue:(id) defaultValue didSelected:(MCSelectVCManagerSelectedCompletion) selectedCompletion;


@end

NS_ASSUME_NONNULL_END
