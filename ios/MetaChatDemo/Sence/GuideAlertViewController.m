//
//  GuideAlertViewController.m
//  MetaChatDemo
//
//  Created by FanPengpeng on 2022/7/25.
//

#import "GuideAlertViewController.h"
#import "Masonry.h"
#import "MetaChatDemo-Swift.h"

@interface GuideAlertViewController ()

@property (nonatomic, strong) UIImageView *shadowImgView;

@property (nonatomic, strong) UIImageView *bgImgView;

@property (nonatomic, strong) UILabel *titleLabel;

@property (nonatomic, strong) UIButton *closeButton;

@property (nonatomic, strong) UITextView *textView;

@end

@implementation GuideAlertViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    [self setUpUI];
}

- (void)setUpUI{
    self.view.backgroundColor = UIColor.clearColor;
    [self.view addSubview:self.shadowImgView];
    [self.view addSubview:self.bgImgView];
    [self.view addSubview:self.textView];
    [self.view addSubview:self.closeButton];
    [self.view addSubview:self.titleLabel];
    
    CGFloat shadowWidth = 490.f;
    [self.shadowImgView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.center.mas_equalTo(self.view);
        make.width.mas_equalTo(shadowWidth);
        make.height.mas_equalTo(shadowWidth / 220.f * 150.f);
    }];
    
    [self.bgImgView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.center.mas_equalTo(self.view);
        make.width.mas_equalTo(404);
        make.height.mas_equalTo(255);
    }];
    
    [self.textView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.mas_equalTo(self.bgImgView).offset(20);
        make.right.mas_equalTo(self.bgImgView).offset(-20);
        make.top.mas_equalTo(self.bgImgView).offset(54);
        make.bottom.mas_equalTo(self.bgImgView).offset(-29);
    }];
    
    [self.closeButton mas_makeConstraints:^(MASConstraintMaker *make) {
        make.top.mas_equalTo(self.bgImgView).offset(15);
        make.right.mas_equalTo(self.bgImgView).offset(-15);
        make.width.height.mas_equalTo(30);
    }];
    
    [self.titleLabel mas_makeConstraints:^(MASConstraintMaker *make) {
        make.centerX.mas_equalTo(self.bgImgView);
        make.top.mas_equalTo(self.bgImgView).offset(18);
        make.height.mas_equalTo(22);
    }];
    
}

- (void)didClickCloseButton {
    [self dismissViewControllerAnimated:YES completion:nil];
}

#pragma mark - getter

- (UIImageView *)shadowImgView {
    if (!_shadowImgView) {
        _shadowImgView = [UIImageView new];
        _shadowImgView.image = [UIImage imageNamed:@"guide_bg_shadow"];
    }
    return _shadowImgView;
}

- (UIImageView *)bgImgView {
    if (!_bgImgView) {
        _bgImgView = [UIImageView new];
        _bgImgView.image = [UIImage imageNamed:@"guide_bg"];
//        _bgImgView.backgroundColor = [UIColor whiteColor];
//        _bgImgView.layer.cornerRadius = 20;
//        _bgImgView.layer.borderColor = [[UIColor alloc] initWithHexString:@"#DCCCFF"].CGColor;
//        _bgImgView.layer.borderWidth = 2;
//        _bgImgView.layer.masksToBounds = YES;
        _bgImgView.contentMode = UIViewContentModeScaleAspectFill;
    }
    return _bgImgView;
}

- (UIButton *)closeButton {
    if (!_closeButton) {
        _closeButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_closeButton setImage:[UIImage imageNamed:@"guide_close"] forState:UIControlStateNormal];
        [_closeButton addTarget:self action:@selector(didClickCloseButton) forControlEvents:UIControlEventTouchUpInside];
    }
    return _closeButton;
}

- (UITextView *)textView {
    if (!_textView) {
        _textView = [UITextView new];
        _textView.backgroundColor = [UIColor clearColor];
        _textView.editable = NO;
//        _textView.userInteractionEnabled = NO;
        if (self.localFileName) {
            NSURL *url = [[NSBundle mainBundle] URLForResource: self.localFileName withExtension:@"rtfd"];
            _textView.attributedText = [[NSAttributedString alloc] initWithURL:url options:@{} documentAttributes:nil error:nil];
        }
    }
    return _textView;
}

- (UILabel *)titleLabel {
    if (!_titleLabel) {
        _titleLabel = [UILabel new];
        _titleLabel.text = self.title;
        _titleLabel.textColor = [[UIColor alloc] initWithHexRGB:0x000000 alpha:0.85];
        _titleLabel.font = [UIFont systemFontOfSize:16 weight:UIFontWeightMedium];
    }
    return _titleLabel;
}

@end
