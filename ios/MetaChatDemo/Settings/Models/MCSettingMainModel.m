//
//  MCMainSettingItem.m
//  MetaChatDemo
//
//  Created by FanPengpeng on 2022/8/15.
//

#import "MCSettingMainModel.h"

@implementation MCSettingMainModel

+ (NSArray<MCSettingMainModel *> *)itemsArrayWithIconNames:(NSArray<NSString *> *) iconNames titles:(NSArray<NSString *> *)titles{
    NSMutableArray *array = [NSMutableArray arrayWithCapacity:titles.count];
    for (NSInteger i = 0; i < titles.count; i ++) {
        MCSettingMainModel *item = [MCSettingMainModel new];
        if (i < iconNames.count) {
            NSString *iconname = iconNames[i];
            item.iconName = iconname;
        }
        item.title = titles[i];
        [array addObject:item];
    }
    return array;
}

@end
