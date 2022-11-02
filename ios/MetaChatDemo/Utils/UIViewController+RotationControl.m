//
//  UIViewController+RotationControl.m
//  AgoraIoT
//
//  Created by FanPengpeng on 2022/5/8.
//

#import "UIViewController+RotationControl.h"

@implementation UIAlertController (RotationControl)
//- (BOOL)shouldAutorotate {
//    return YES;
//}
//
//- (UIInterfaceOrientationMask)supportedInterfaceOrientations {
//    return UIInterfaceOrientationMaskLandscapeRight;
//}

@end

@implementation UIViewController (RotationControl)
- (BOOL)shouldAutorotate {
    return YES;
}

- (UIInterfaceOrientationMask)supportedInterfaceOrientations {
//    NSString *version = [UIDevice currentDevice].systemVersion;
//    if (version.doubleValue >= 16.0) {
//        return  UIInterfaceOrientationMaskAll;
//    }
    return UIInterfaceOrientationMaskLandscapeRight;
}

@end


@implementation UITabBarController (RotationControl)
- (UIViewController *)sj_topViewController {
    if ( self.selectedIndex == NSNotFound )
        return self.viewControllers.firstObject;
    return self.selectedViewController;
}

- (BOOL)shouldAutorotate {
    return [[self sj_topViewController] shouldAutorotate];
}

- (UIInterfaceOrientationMask)supportedInterfaceOrientations {
    return [[self sj_topViewController] supportedInterfaceOrientations];
}

- (UIInterfaceOrientation)preferredInterfaceOrientationForPresentation {
    return [[self sj_topViewController] preferredInterfaceOrientationForPresentation];
}
@end

@implementation UINavigationController (RotationControl)
- (BOOL)shouldAutorotate {
    DLog(@"self.topViewController.shouldAutorotate  === %d",self.topViewController.shouldAutorotate)
    return self.topViewController.shouldAutorotate;
}

- (UIInterfaceOrientationMask)supportedInterfaceOrientations {
    DLog(@"self.topViewController.supportedInterfaceOrientations === %zd",self.topViewController.supportedInterfaceOrientations)

    return self.topViewController.supportedInterfaceOrientations;
}

- (UIInterfaceOrientation)preferredInterfaceOrientationForPresentation {
    DLog(@"self.topViewController.preferredInterfaceOrientationForPresentation == %zd",self.topViewController.preferredInterfaceOrientationForPresentation);
    return self.topViewController.preferredInterfaceOrientationForPresentation;
}

- (nullable UIViewController *)childViewControllerForStatusBarStyle {
    return self.topViewController;
}

- (nullable UIViewController *)childViewControllerForStatusBarHidden {
    return self.topViewController;
}

@end

