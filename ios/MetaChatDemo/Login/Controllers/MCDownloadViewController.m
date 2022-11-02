//
//  MCDownloadViewController.m
//  MetaChatDemo
//
//  Created by FanPengpeng on 2022/8/23.
//

#import "MCDownloadViewController.h"

@interface MCDownloadViewController ()

@property (weak, nonatomic) IBOutlet UIProgressView *progressView;
@property (weak, nonatomic) IBOutlet UILabel *sizeLabel;

@end

@implementation MCDownloadViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    self.sizeLabel.text = [NSString stringWithFormat:@"0.0MB / %.fMB", _totalSize];
}

- (BOOL)shouldAutorotate {
    return YES;
}

- (UIInterfaceOrientationMask)supportedInterfaceOrientations {
    return UIInterfaceOrientationMaskPortrait;
}

- (void)setProgress:(CGFloat)progress {
    DLog(@"%.2f",progress);
    self.progressView.progress = progress;
    if (_totalSize > 0) {
        self.sizeLabel.text = [NSString stringWithFormat:@"%.1fMB / %.fMB",_totalSize * progress, _totalSize];
    }
}

- (IBAction)didClickCancelButton:(id)sender {
    [self dismissViewControllerAnimated:NO completion:nil];
    if (self.cancelAction) {
        self.cancelAction();
    }
}

@end
