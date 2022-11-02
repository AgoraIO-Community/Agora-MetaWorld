//
//  MCLoginViewController.m
//  MetaChatDemo
//
//  Created by FanPengpeng on 2022/8/10.
//

#import "MCLoginViewController.h"
#import "MCSelectProfileViewController.h"
#import <SDWebImage.h>
#import "MetaChatDemo-Swift.h"
#import "MCSelectAvatarViewController.h"
#import "MBProgressHUD+Extension.h"
#import "MCUserInfo.h"
#import "MCSelectVCManager.h"
#import "MCDownloadViewController.h"
#import "UIViewController+Extension.h"


@interface MCLoginViewController ()<MCResourceEventDelegate,UITextFieldDelegate>

@property (weak, nonatomic) IBOutlet UIImageView *headImgView;
@property (weak, nonatomic) IBOutlet UIButton *levelButton;
@property (weak, nonatomic) IBOutlet UITextField *nameTextField;
@property (weak, nonatomic) IBOutlet UILabel *errorTipsLabel;
@property (weak, nonatomic) IBOutlet UIButton *maleButton;
@property (weak, nonatomic) IBOutlet UIButton *femaleButton;

@property (assign, nonatomic) NSInteger headImgSelectedIndex;
@property (assign, nonatomic) NSInteger levelSelectedIndex;

@property (assign, nonatomic) NSUInteger currentSceneId;

@property (copy, nonatomic) NSString *name;

@property (strong, nonatomic) MCSelectVCManager *selectVCManager;

@property (weak, nonatomic) MCDownloadViewController *downloadVC;

@property (assign, nonatomic) double totalSize;

@property (strong, nonatomic) UIActivityIndicatorView *indicatorView;

@property (assign, nonatomic) AgoraMetachatGender gender;

@end

@implementation MCLoginViewController

- (void)dealloc {
   DLog("MCLoginViewController === 销毁了")
}

- (void)viewDidLoad {
    [super viewDidLoad];
    [self setupUI];
    [self selectedGenderButton:self.femaleButton];
    [self genRandomUserInfo];
}

- (void)setupUI{
    UILabel *titleLabel = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, 100, 30)];
    titleLabel.text = @"MetaChat";
    titleLabel.textColor = [UIColor whiteColor];
    titleLabel.font = [UIFont boldSystemFontOfSize:16];
    titleLabel.textAlignment = NSTextAlignmentCenter;
    self.navigationItem.titleView = titleLabel;
    
    UIBarButtonItem *item = [[UIBarButtonItem alloc] initWithImage:[[UIImage imageNamed:@"login_back"] imageWithRenderingMode:UIImageRenderingModeAlwaysOriginal] style:UIBarButtonItemStylePlain target:self action:@selector(didClickLeftBarButton)];
    self.navigationItem.leftBarButtonItem = item;
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    self.navigationItem.hidesBackButton = NO;
    self.navigationController.navigationBarHidden = NO;
}

- (void)viewDidDisappear:(BOOL)animated {
    [super viewDidDisappear:animated];
    [self dismissIndicatorView];
}

- (BOOL)shouldAutorotate {
    return NO;
}

- (UIInterfaceOrientationMask)supportedInterfaceOrientations {
    return UIInterfaceOrientationMaskPortrait;
}

#pragma mark - actions

- (void)didClickLeftBarButton {
    [self.navigationController popViewControllerAnimated:YES];
}

// 点击头像
- (IBAction)didTapedHeadImgView:(UITapGestureRecognizer *)sender {
    __weak typeof(self) wSelf = self;
    MCSelectProfileViewController *vc = [self.selectVCManager selectVCWithType:MCSelectProfileTypeHeadImage defaultSeletedIndex:self.headImgSelectedIndex didSelected:^(NSInteger selectedIndex, NSArray * _Nonnull imgArray) {
        wSelf.headImgSelectedIndex = selectedIndex;
    }];
    [self presentViewController:vc animated:NO completion:nil];
}

// 点击勋章按钮
- (IBAction)didClickLevelButton:(id)sender {
    __weak typeof(self) wSelf = self;
    MCSelectProfileViewController *vc = [self.selectVCManager selectVCWithType:MCSelectProfileTypeBadge defaultSeletedIndex:self.levelSelectedIndex didSelected:^(NSInteger selectedIndex, NSArray * _Nonnull imgArray) {
        wSelf.levelSelectedIndex = selectedIndex;
    }];
    [self presentViewController:vc animated:NO completion:nil];
}

- (void)setName:(NSString *)name {
    _name = name;
    self.nameTextField.text = name;
}

- (void)setLevelSelectedIndex:(NSInteger)levelSelectedIndex {
    _levelSelectedIndex = levelSelectedIndex;
    [self.levelButton sd_setImageWithURL:[NSURL URLWithString:self.selectVCManager.levelIconArray[levelSelectedIndex] ?: @""] forState:UIControlStateNormal];
}

- (void)setHeadImgSelectedIndex:(NSInteger)headImgSelectedIndex {
    _headImgSelectedIndex = headImgSelectedIndex;
    NSString *img = self.selectVCManager.headImageUrlArray[headImgSelectedIndex];
    UIImage *image = [UIImage imageNamed:img];
    if (image) {
        self.headImgView.image = image;
    }else{
        [self.headImgView sd_setImageWithURL:[NSURL URLWithString:img] placeholderImage:[UIImage imageNamed:@""]];
    }
}


- (void)genRandomUserInfo {
    self.name = self.selectVCManager.namesArray[arc4random() % self.selectVCManager.namesArray.count];
    self.levelSelectedIndex = arc4random() % self.selectVCManager.levelIconArray.count;
    self.headImgSelectedIndex = arc4random() % self.selectVCManager.headImageUrlArray.count;
}

- (void)selectedGenderButton:(UIButton *)genderButton {
    if (genderButton == self.maleButton) {
        self.femaleButton.selected = NO;
        self.femaleButton.layer.borderColor = [UIColor clearColor].CGColor;
        self.gender = AgoraMetachatGenderMale;
    }else{
        self.maleButton.selected = NO;
        self.maleButton.layer.borderColor = [UIColor clearColor].CGColor;
        self.gender = AgoraMetachatGenderFemale;
    }
    genderButton.selected = YES;
    genderButton.layer.borderColor = [[UIColor alloc] initWithHexString:@"#E65E75"].CGColor;
}

// 点击随机按钮
- (IBAction)didClickRandomButton:(id)sender {
    [self genRandomUserInfo];
}

// 点击性别按钮
- (IBAction)didClickGenderButton:(UIButton *)sender {
    [self selectedGenderButton:sender];
}

// 选择avatar
- (IBAction)didClickSelectAvatarButton:(id)sender {
    if ([self checkValid]) {
        
        [self showIndicatorView];
        NSString *name = self.nameTextField.text;
        NSString *levelIconUrl = self.selectVCManager.levelIconArray[self.levelSelectedIndex];
        [MetaChatEngine.sharedEngine createMetachatKitWithUserName:name avatarUrl:levelIconUrl];
        [MetaChatEngine.sharedEngine.metachatKit getScenes];
        MetaChatEngine.sharedEngine.resourceDelegate = self;
    }
}

#pragma mark - private

- (BOOL)checkValid {
    NSString *name = self.nameTextField.text;
    if (name.length < 2 || name.length > 12) {
        self.errorTipsLabel.hidden = NO;
        return NO;
    }
    return YES;
}

- (void)createOrJoinRoomWithScene:(AgoraMetachatSceneInfo *) sceneInfo avatarInfo:(AgoraMetachatAvatarInfo *)avatarInfo {
    __weak typeof(self) wSelf = self;
    if (self.isRoomMaster) {
        [[MCRoomManager shared] createRoomWithName:self.roomName img:self.roomImg  pwd:self.pwd success:^(NSString * _Nonnull roomId) {
            wSelf.roomId = roomId;
            [wSelf showScene:sceneInfo avatarInfo:avatarInfo];
        } fail:^{
            [MBProgressHUD showError:@"创建房间失败" inView:self.view];
        }];
    }else{
        [[MCRoomManager shared] joinRoomWithId:self.roomId success:^{
            [wSelf showScene:sceneInfo avatarInfo:avatarInfo];
        } fail:^(enum MCJoinRoomErrorReason reason) {
            switch (reason) {
                case MCJoinRoomErrorReasonNotExit:
                    [wSelf showErrorAlertWithTitle:MCLocalizedString(@"The room has been disbanded")];
                    break;
                case  MCJoinRoomErrorReasonFullMember:
                    [wSelf showErrorAlertWithTitle:MCLocalizedString(@"The number of people has exceeded the maximum limit")];
                    break;
                default:
                    [MBProgressHUD showError:@"加入房间失败" inView:self.view];
                    break;
            }
        }];
    }
}

- (void)showErrorAlertWithTitle:(NSString *)title {
    __weak typeof(self) wSelf = self;
    [self ex_showMCAlertWithTitle:title message:nil cancelTitle:nil confirmTitle:MCLocalizedString(@"Confirm") cancelHandler:nil confirmHandler:^{
        [wSelf.navigationController popViewControllerAnimated:YES];
    }];
}

- (void)showScene:(AgoraMetachatSceneInfo *) sceneInfo avatarInfo:(AgoraMetachatAvatarInfo *)avatarInfo{
    if (sceneInfo == nil || avatarInfo == nil || self.roomId == nil) {
        return;
    }
    self.navigationController.navigationBarHidden = YES;
    self.navigationItem.hidesBackButton = YES;
    UIStoryboard *sb = [UIStoryboard storyboardWithName:@"Main" bundle:nil];
    MetaChatSceneViewController *sceneViewController = [sb instantiateViewControllerWithIdentifier:@"SceneViewController"];
    sceneViewController.modalPresentationStyle = UIModalPresentationFullScreen;
    sceneViewController.avatarInfo = avatarInfo;
    
    MCRoom *room = [MCRoom new];
    room.name = self.roomName;
    room.img = self.roomImg;
    room.objectId = self.roomId;
    room.pwd = self.pwd;
    room.master = self.masterId;
    sceneViewController.room = room;
    
    MCUserInfo *userInfo = [MCUserInfo new];
    userInfo.headImg = self.selectVCManager.headImageUrlArray[self.headImgSelectedIndex];
    userInfo.nickname = self.name;
    userInfo.isRoomMaser = self.isRoomMaster;
    userInfo.badge = self.selectVCManager.levelIconArray[self.levelSelectedIndex];
    userInfo.gender = self.gender;
    sceneViewController.userInfo = userInfo;
    [MetaChatEngine.sharedEngine createScene:sceneInfo roomId:self.roomId];
    [self.navigationController presentViewController:sceneViewController animated:YES completion:nil];
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(0.3 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
        [self.navigationController popToRootViewControllerAnimated:NO];
    });
}


- (void)sceneReady:(AgoraMetachatSceneInfo *) sceneInfo{
    UIStoryboard *sb = [UIStoryboard storyboardWithName:@"Main" bundle:nil];
    MCSelectAvatarViewController *vc = [sb instantiateViewControllerWithIdentifier:@"selectAvatarVC"];
    vc.gender = self.gender;
    vc.modalPresentationStyle = UIModalPresentationFullScreen;
    vc.sceneInfo = sceneInfo;
    if (!self.isRoomMaster) {
        vc.enterBtnTitle = MCLocalizedString(@"Confirm");
    }
    __weak typeof(self) wSelf = self;
    vc.onSelectedAvatar = ^(AgoraMetachatAvatarInfo * _Nonnull avatarInfo) {
        [wSelf createOrJoinRoomWithScene:sceneInfo avatarInfo:avatarInfo];
    };
    [self presentViewController:vc animated:YES completion:nil];
}

- (void)showIndicatorView {
//    [SVProgressHUD show];
    [self.view addSubview:self.indicatorView];
    self.indicatorView.center = self.view.center;
    [self.indicatorView startAnimating];
}

- (void)dismissIndicatorView {
//    [SVProgressHUD dismiss];
    [self.indicatorView stopAnimating];
}

- (void)showAlertWithTitle:(NSString *)title message:(NSString *)message cancelTitle:(NSString *)cancelTitle sureTitle:(NSString *)sureTitle sureHandler:(void (^ __nullable)(UIAlertAction *action))handler {
    UIAlertController *alertController = [UIAlertController alertControllerWithTitle:title message:message preferredStyle:UIAlertControllerStyleAlert] ;
    [alertController addAction:[UIAlertAction actionWithTitle:cancelTitle style:UIAlertActionStyleCancel handler:nil]];
    if (sureTitle) {
        [alertController addAction:[UIAlertAction actionWithTitle:sureTitle style:UIAlertActionStyleDefault handler:handler]];
    }
    [self presentViewController:alertController animated:YES completion:nil];
}

#pragma mark - touch

- (void)touchesEnded:(NSSet<UITouch *> *)touches withEvent:(UIEvent *)event {
    [self.view endEditing:YES];
    [self dismissIndicatorView];
}

#pragma mark - private

- (void)showDownlaodVC{
    UIStoryboard *sb = [UIStoryboard storyboardWithName:@"Main" bundle:nil];
    MCDownloadViewController *vc = [sb instantiateViewControllerWithIdentifier:@"downloadVC"];
    vc.modalPresentationStyle = UIModalPresentationOverFullScreen;
    __weak typeof(self) wSelf = self;
    vc.cancelAction = ^{
        [[MetaChatEngine sharedEngine].metachatKit cancelDownloadScene:wSelf.currentSceneId];
    };
    vc.totalSize = self.totalSize;
    self.downloadVC = vc;
    [self presentViewController:vc animated:NO completion:nil];
}

#pragma mark - text field delegate

- (BOOL)textFieldShouldReturn:(UITextField *)textField {
    [textField resignFirstResponder];
    return YES;
}

#pragma mark - MCResourceEventDelegate

- (void)onConnectionStateChanged:(AgoraMetachatConnectionStateType)state reason:(AgoraMetachatConnectionChangedReasonType)reason {
    if (state == AgoraMetachatConnectionStateTypeDisconnected) {
        [self dismissIndicatorView];
    } 
}

- (void)onGetScenesResult:(NSMutableArray * _Nonnull)scenes errorCode:(NSInteger)errorCode {
    
    [self dismissIndicatorView];
    
    if (errorCode != 0) {
        NSString *title = [NSString stringWithFormat:@"get Scenes failed:errorcode:%zd",errorCode];
        [self ex_showMCAlertWithTitle:title message:nil cancelTitle:nil confirmTitle:@"OK" cancelHandler:nil confirmHandler:nil];
        return;
    }
    
    if (scenes.count == 0) {
        return;
    }
    AgoraMetachatSceneInfo *firstScene = scenes.firstObject;
    if (firstScene == nil || firstScene.sceneId != self.currentSceneId) {
        return;
    }
   
    AgoraMetachatKit *metachatKit = MetaChatEngine.sharedEngine.metachatKit;
    double totalSize = firstScene.totalSize / 1024.f / 1024.f;
    self.totalSize = totalSize;
    NSString *localizedMsg1 = MCLocalizedString(@"download_scene_title_1");
    NSString *localizedMsg2 = MCLocalizedString(@"download_scene_title_2");
    NSString *message = [NSString stringWithFormat:@"%@%.fMB%@",localizedMsg1,totalSize,localizedMsg2];
    if ([metachatKit isSceneDownloaded:self.currentSceneId] != 1) {
        __weak typeof(self) weakSelf = self;
        [weakSelf ex_showMCAlertWithTitle:MCLocalizedString(@"Download tips") message:message cancelTitle:MCLocalizedString(@"Next time") confirmTitle:MCLocalizedString(@"Download now")  cancelHandler:nil confirmHandler:^{
            [metachatKit downloadScene:weakSelf.currentSceneId];
            [weakSelf showDownlaodVC];
        }];
    }else{
        [self sceneReady:firstScene];
    }
}

- (void)onDownloadSceneProgress:(AgoraMetachatSceneInfo * _Nullable)sceneInfo progress:(NSInteger)progress state:(AgoraMetachatDownloadStateType)state {
    
    self.downloadVC.progress = progress/100.0;
    if (state == AgoraMetachatDownloadStateTypeDownloaded && sceneInfo != nil) {
        [self.downloadVC dismissViewControllerAnimated:NO completion:^{
            [self sceneReady:sceneInfo];
        }];
    }
}

#pragma mark - getter

- (MCSelectVCManager *)selectVCManager {
    if (!_selectVCManager) {
        _selectVCManager = [MCSelectVCManager new];
    }
    return _selectVCManager;
}

- (NSUInteger)currentSceneId {
    if (!_currentSceneId) {
        return [KeyCenter SCENE_ID];
    }
    return _currentSceneId;
}

- (UIActivityIndicatorView *)indicatorView {
    if (!_indicatorView) {
        _indicatorView = [[UIActivityIndicatorView alloc] initWithActivityIndicatorStyle:UIActivityIndicatorViewStyleWhite];
        _indicatorView.bounds = CGRectMake(0, 0, 60, 60);
        _indicatorView.layer.cornerRadius = 6;
        _indicatorView.layer.masksToBounds = YES;
        _indicatorView.backgroundColor = [[UIColor alloc] initWithHexRGB:0x000000 alpha:0.3];
    }
    return _indicatorView;
}


@end
