//
//  MCMainSettingItem.h
//  MetaChatDemo
//
//  Created by FanPengpeng on 2022/8/15.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface MCSettingMainModel : NSObject

@property (copy, nonatomic) NSString *iconName;

@property (copy, nonatomic) NSString *title;

+ (NSArray<MCSettingMainModel *> *)itemsArrayWithIconNames:(NSArray<NSString *> *) iconNames titles:(NSArray<NSString *> *)titles;

@end

NS_ASSUME_NONNULL_END
