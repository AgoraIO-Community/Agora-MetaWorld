//
//  MCCreateRoomViewController.m
//  MetaChatDemo
//
//  Created by FanPengpeng on 2022/8/9.
//

#import "MCCreateRoomViewController.h"
#import "MCVerificationCodeView.h"
#import "MetaChatDemo-Swift.h"
//#import "IQKeyboardManager.h"
#import "MCLoginViewController.h"
#import "MBProgressHUD+Extension.h"

@interface MCCreateRoomViewController ()<UITextFieldDelegate>

{
    NSInteger roomImgID;
}

@property (weak, nonatomic) IBOutlet UIImageView *roomImageView;
@property (weak, nonatomic) IBOutlet UITextField *roomNameTextField;
@property (weak, nonatomic) IBOutlet MCVerificationCodeView *verificationCodeView;

@property (weak, nonatomic) IBOutlet UIButton *publicButton;
@property (weak, nonatomic) IBOutlet UIButton *entryptedButton;
/// 房间密码错误提示
@property (weak, nonatomic) IBOutlet UILabel *roomPwdTipsLabel;

@property (strong, nonatomic) NSArray *roomNamesArray;

@end

@implementation MCCreateRoomViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    roomImgID = arc4random() % 4;
    [self genRandomRoomInfo];
    [self setUpUI];
}

- (void)setUpUI{
//    [IQKeyboardManager sharedManager].enable = YES;
//    [IQKeyboardManager sharedManager].shouldResignOnTouchOutside = YES;
    self.navigationItem.title = NSLocalizedString(@"Create a room", @"");
    self.verificationCodeView.verificationCodeNum = 4;
    [self didSelectedModeButton:self.publicButton];
}

- (void)genRandomRoomInfo {
    roomImgID = (roomImgID + 1) % 4;
    self.roomImageView.image = [UIImage imageNamed:[NSString stringWithFormat:@"room_cover_%zd",roomImgID]];
    self.roomNameTextField.text = [NSString stringWithFormat:@"%@",self.roomNamesArray[arc4random() % self.roomNamesArray.count]];
}

- (BOOL)shouldAutorotate {
    return NO;
}

- (UIInterfaceOrientationMask)supportedInterfaceOrientations {
    return UIInterfaceOrientationMaskPortrait;
}

- (void)touchesEnded:(NSSet<UITouch *> *)touches withEvent:(UIEvent *)event {
    [self.view endEditing:YES];
}

/// 创建房间
- (void)createRoom {
    NSString *roomName = [self.roomNameTextField.text stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet]];
    if (roomName == nil || roomName.length == 0 || roomName.length > 12) {
        [MBProgressHUD showError:MCLocalizedString(@"Please input 1-12 characters") inView:self.view];
        return;
    }
    NSString *img = [NSString stringWithFormat:@"%zd",roomImgID];
    UIStoryboard *storyboard = [UIStoryboard storyboardWithName:@"Main" bundle:NSBundle.mainBundle];
    MCLoginViewController *loginVC = [storyboard instantiateViewControllerWithIdentifier:@"loginVC"];
    loginVC.roomName = roomName;
    loginVC.roomImg = img;
    loginVC.pwd = self.verificationCodeView.vertificationCode;
    loginVC.isRoomMaster = YES;
    loginVC.masterId = KeyCenter.RTM_UID;
    [self.navigationController pushViewController:loginVC animated:YES];
}

#pragma mark - actions

// 点击随机生成房间名按钮
- (IBAction)didClickRandomButton:(id)sender {
    [self genRandomRoomInfo];
}


- (IBAction)didSelectedModeButton:(UIButton *)sender {
    if (sender.isSelected) {
        return;
    }
    sender.selected = YES;
    if (sender == self.publicButton) {
        self.entryptedButton.selected = NO;
        self.verificationCodeView.canEdit = NO;
        self.roomPwdTipsLabel.hidden = YES;
        self.verificationCodeView.hidden = YES;
    }else{
        self.publicButton.selected = NO;
        self.verificationCodeView.hidden = NO;
        [self.verificationCodeView becomeFirstResponder];
    }
    
}

/// 点击创建房间按钮
- (IBAction)didClickCreateRoomButton:(id)sender {
    if (self.publicButton.isSelected) {
        [self createRoom];
        return;
    }
    if (self.verificationCodeView.vertificationCode.length < 4) {
        self.roomPwdTipsLabel.hidden = NO;
//        self.roomPwdTipsLabel.text = @"Please set the 4-digit room password";
        return;
    }
    [self createRoom];
}

#pragma mark - text field delegate

- (BOOL)textFieldShouldReturn:(UITextField *)textField {
    [textField resignFirstResponder];
    return YES;
}

#pragma mark - getters

- (NSArray *)roomNamesArray {
    if (!_roomNamesArray) {
#if Overseas
        _roomNamesArray = @[@"Jenoah",
                            @"York",
                            @"Keila",
                            @"Adelaide",
                            @"Kima",
                            @"Chyanne",
                            @"Reno",
                            @"Granville",
                            @"Cyrene",
                            @"Valencia",
                            @"Enna",
                            @"Nara",
                            @"Asgard",
                            @"Hogwarts",
                            @"Gryffindor",
                            @"Hufflepuff",
                            @"Ravenclaw",
                            @"Slytherin",
                            @"Shangri-La",
                            @"Alaska"];
#else
        NSString *langugeCode =  [NSLocale currentLocale].languageCode;
        DLog(@"language === %@",langugeCode);
        if ([langugeCode isEqualToString:@"zh"]) {
            _roomNamesArray = @[@"和你一起看月亮",
                                @"治愈",
                                @"一锤定音",
                                @"有酒吗",
                                @"早安序曲",
                                @"近在远方",
                                @"风中诗",
                                @"那年风月",
                                @"三万余年",
                                @"七十二街",
                                @"情怀如诗",
                                @"简遇而安",
                                @"十里笙歌",
                                @"回风舞雪",
                                @"梦初醒处",
                                @"别来无恙",
                                @"三里清风",
                                @"烟雨万重",
                                @"水洗晴空",
                                @"轻风淡月"];
        }else{
            _roomNamesArray = @[@"Jenoah",
                                @"York",
                                @"Keila",
                                @"Adelaide",
                                @"Kima",
                                @"Chyanne",
                                @"Reno",
                                @"Granville",
                                @"Cyrene",
                                @"Valencia",
                                @"Enna",
                                @"Nara",
                                @"Asgard",
                                @"Hogwarts",
                                @"Gryffindor",
                                @"Hufflepuff",
                                @"Ravenclaw",
                                @"Slytherin",
                                @"Shangri-La",
                                @"Alaska"];
        }
#endif
    }
    return _roomNamesArray;
}

@end
