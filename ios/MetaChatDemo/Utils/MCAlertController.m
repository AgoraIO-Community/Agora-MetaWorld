//
//  MCAlertControllerViewController.m
//  MetaChatDemo
//
//  Created by FanPengpeng on 2022/9/7.
//

#import "MCAlertController.h"

@interface MCAlertController ()

@property (weak, nonatomic) IBOutlet UILabel *titleLabel;

@property (weak, nonatomic) IBOutlet UILabel *messageLabel;

@property (weak, nonatomic) IBOutlet UIButton *cancelButton;

@property (weak, nonatomic) IBOutlet UIButton *confirmButton;

@property (copy, nonatomic) MCAlertHandler cancelHandler;

@property (copy, nonatomic) MCAlertHandler confirmHandler;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *msgTitleSpaceCon;

@property (weak, nonatomic) IBOutlet NSLayoutConstraint *cancelCenterCon;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *confirmCenterCon;

@end

@implementation MCAlertController

- (void)viewDidLoad {
    [super viewDidLoad];
    [self setUpUI];
}

- (BOOL)shouldAutorotate {
    return NO;
}

- (UIInterfaceOrientationMask)supportedInterfaceOrientations {
    return UIInterfaceOrientationMaskAll;
}

- (void)setUpUI{
    
    self.titleLabel.text = self.alertTitle;
    self.messageLabel.text = self.message;
    [self.cancelButton setTitle:self.cancelTitle forState:UIControlStateNormal];
    [self.confirmButton setTitle:self.confirmTitle forState:UIControlStateNormal];
    if (self.cancelTitle == nil || self.cancelTitle.length == 0) {
        self.cancelButton.hidden = YES;
        self.confirmCenterCon.constant = - 57.5;
    }
    if (self.confirmTitle == nil || self.confirmTitle.length == 0) {
        self.confirmButton.hidden = YES;
        self.cancelCenterCon.constant = 57.5;
    }
    if (self.message == nil || self.message.length == 0) {
        self.msgTitleSpaceCon.constant = 0;
    }
}

- (IBAction)didClickCacelButton:(UIButton *)sender {
    [self dismissViewControllerAnimated:NO completion:^{
        if (self.cancelHandler) {
            self.cancelHandler();
        }
    }];
}

- (IBAction)didClickConfirmButton:(UIButton *)sender {
    [self dismissViewControllerAnimated:NO completion:^{
        if (self.confirmHandler) {
            self.confirmHandler();
        }
    }];
}

- (void)signleButtonLayoutWithCancelTitle:(NSString * __nullable)cancelTitle confirmTitle:(NSString * __nullable)confirmTitle {
    if (cancelTitle == nil) {
        self.cancelButton.hidden = YES;
        self.confirmCenterCon.constant = 0;
    }
    if (confirmTitle == nil) {
        self.confirmButton.hidden = YES;
        self.cancelCenterCon.constant = 0;
    }
}

+ (instancetype)alertWithTitle:(NSString * __nullable)title message:(NSString * __nullable)message cancelTitle:(NSString * __nullable)cancelTitle confirmTitle:(NSString * __nullable)confirmTitle cancelHandler:(MCAlertHandler __nullable) cancelhandler confirmHandler: (MCAlertHandler __nullable) confirmhandler {
    UIStoryboard *sb = [UIStoryboard storyboardWithName:@"Main" bundle:[NSBundle mainBundle]];
    MCAlertController *vc = [sb instantiateViewControllerWithIdentifier:@"MCAlertVC"];
    vc.alertTitle = title;
    vc.message = message;
    vc.cancelTitle = cancelTitle;
    vc.confirmTitle = confirmTitle;
    vc.cancelHandler = cancelhandler;
    vc.confirmHandler = confirmhandler;
    [vc signleButtonLayoutWithCancelTitle:cancelTitle confirmTitle:confirmTitle];
    return vc;
}


@end
