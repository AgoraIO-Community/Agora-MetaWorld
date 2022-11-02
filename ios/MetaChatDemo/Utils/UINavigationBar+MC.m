//
//  UINavigationBar+MC.m
//  MetaChatDemo
//
//  Created by FanPengpeng on 2022/8/9.
//

#import "UINavigationBar+MC.h"

@implementation UINavigationBar (MC)

+ (void)initialize {
    UINavigationBar *apprearance = [UINavigationBar appearance];
    [apprearance setBackgroundImage:[UIImage new] forBarMetrics:UIBarMetricsDefault];
    [apprearance setShadowImage:[UIImage new]];
    UIImage *backImge = [[UIImage imageNamed:@"navi_back"] imageWithRenderingMode:UIImageRenderingModeAlwaysOriginal];
    apprearance.backIndicatorTransitionMaskImage = backImge;
    apprearance.backIndicatorImage = backImge;
    apprearance.backItem.title = @"";
    
}

@end


@implementation UIBarButtonItem (MC)

+ (void)initialize {
    UIBarButtonItem *buttonItem = [UIBarButtonItem appearanceWhenContainedInInstancesOfClasses:@[UINavigationBar.class]];
    UIOffset offset;
    offset.horizontal = -500;
    offset.vertical = -200;
    [buttonItem setBackButtonTitlePositionAdjustment:offset forBarMetrics:UIBarMetricsDefault];
}

@end
