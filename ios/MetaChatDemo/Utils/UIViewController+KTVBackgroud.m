//
//  UIViewController+KTVBackgroud.m
//  MetaChatDemo
//
//  Created by FanPengpeng on 2022/7/13.
//

#import "UIViewController+KTVBackgroud.h"
#import "Masonry.h"
#import "KTVNaviBar.h"
#import <objc/runtime.h>

static CGFloat const kContainerWidth = 360;
static CGFloat const kNaviBarHeight = 60;

typedef void(^TapBlankAction)(void);

@interface UIViewController()

@property (nonatomic, copy) TapBlankAction tapAction;

@end

@implementation UIViewController (KTVBackgroud)

- (CGFloat)blurWidth {
    return kContainerWidth;
}

- (void)ktv_setBlurBackground {
    // blur
    UIBlurEffect *blurEffect = [UIBlurEffect effectWithStyle:UIBlurEffectStyleLight];
    UIVisualEffectView *visualView = [[UIVisualEffectView alloc] initWithEffect:blurEffect];
    visualView.userInteractionEnabled = NO;
    [self.view addSubview:visualView];
    [visualView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.top.bottom.equalTo(self.view);
        make.width.mas_equalTo(kContainerWidth);
    }];
    
    // 半透明背景
    UIView *view = [UIView new];
    view.backgroundColor = [UIColor colorWithRed:0 green:0 blue:0 alpha:0.3];
    [self.view addSubview:view];
    [view mas_makeConstraints:^(MASConstraintMaker *make) {
        make.edges.equalTo(visualView);
    }];
    
}

- (void)ktv_tapBlankAction:(void(^)(void))action {
    // 点击背景
    UIView *bgView = [UIView new];
    [self.view addSubview:bgView];
    [bgView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.edges.mas_equalTo(self.view);
    }];
    
    UITapGestureRecognizer *tap = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(ktvbg_didTapedBgView)];
    [bgView addGestureRecognizer:tap];
    self.tapAction = action;
}

- (void)ktv_configCustomNaviBarWithTitle:(NSString *)title {
    self.title = title;
    KTVNaviBar *naviBar = [KTVNaviBar new];
    naviBar.titleLabel.text = title;
    [naviBar.backButton addTarget:self action:@selector(ktvbg_didClickBackButton) forControlEvents:UIControlEventTouchUpInside];
    [self.view addSubview:naviBar];
    [naviBar mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.top.mas_equalTo(self.view);
        make.height.mas_equalTo(kNaviBarHeight);
        make.width.mas_equalTo(kContainerWidth);
    }];
}

// 点击背景
-(void)ktvbg_didTapedBgView {
    if (self.tapAction) {
        self.tapAction();
    }
}


- (TapBlankAction)tapAction {
    TapBlankAction _tapAction = objc_getAssociatedObject(self, _cmd);
    return _tapAction;
}

- (void)setTapAction:(TapBlankAction)tapAction {
    objc_setAssociatedObject(self, @selector(tapAction), tapAction, OBJC_ASSOCIATION_COPY);
}

- (void)ktvbg_didClickBackButton {
    [self.navigationController popViewControllerAnimated:NO];
}

@end
