//
//  MCSettingMainVC.m
//  MetaChatDemo
//
//  Created by FanPengpeng on 2022/8/15.
//

#import "MCSettingMainVC.h"
#import "MCSettingMainModel.h"
#import "Masonry.h"
#import "MCSettingDetailVC.h"
#import "MCSettingGeneralVC.h"
#import "MCSettingOperationVC.h"
#import "MCSettingSoundVC.h"
#import "MCSettingRoomVC.h"
#import "MetaChatDemo-Swift.h"
#import "MCUserInfo.h"
#import "MCSettingNPCSoundVC.h"


static NSString *const kGeneralSettingTitle = @"General";
static NSString *const kOperationSettingTitle = @"Operation";
static NSString *const kSoundSettingTitle = @"Sound";
static NSString *const kRoomSettingTitle = @"Room";

@interface MCSettingMainVC ()<UITableViewDelegate, UITableViewDataSource>

@property (weak, nonatomic) IBOutlet UITableView *tableView;
@property (weak, nonatomic) IBOutlet UIView *detailView;

@property (strong, nonatomic) NSArray *dataArray;

@property (strong, nonatomic) MCSettingGeneralVC *generalSettingVC;
@property (strong, nonatomic) MCSettingOperationVC *operationSettingVC;
//@property (strong, nonatomic) MCSettingSoundVC *soundSettingVC;
@property (strong, nonatomic) MCSettingNPCSoundVC *soundSettingVC;
@property (strong, nonatomic) MCSettingRoomVC *roomSettingVC;

@property (strong, nonatomic) MCSettingDetailVC *currentSettingVC;


@end

@implementation MCSettingMainVC

- (void)dealloc {
    DLog(@"-%@--销毁了",[self class]);
}

- (void)viewDidLoad {
    [super viewDidLoad];
    [self setUpUI];
}

- (void)setUpUI {
    self.view.backgroundColor = [UIColor clearColor];
    self.tableView.backgroundColor = [UIColor clearColor];
    self.tableView.tableFooterView = [UIView new];
    
    // blur
    UIBlurEffect *blurEffect = [UIBlurEffect effectWithStyle:UIBlurEffectStyleLight];
    UIVisualEffectView *visualView = [[UIVisualEffectView alloc] initWithEffect:blurEffect];
    visualView.userInteractionEnabled = NO;
    [self.view addSubview:visualView];
    [visualView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.top.bottom.right.equalTo(self.view);
    }];
    
    // 半透明背景
    UIView *view = [UIView new];
    view.backgroundColor = [UIColor colorWithRed:0 green:0 blue:0 alpha:0.3];
    [self.view addSubview:view];
    [view mas_makeConstraints:^(MASConstraintMaker *make) {
        make.edges.equalTo(visualView);
    }];
    [self.view sendSubviewToBack:visualView];
    [self.view sendSubviewToBack:view];
    
    self.tableView.delegate = self;
    self.tableView.dataSource = self;
    self.tableView.separatorStyle = UITableViewCellSeparatorStyleNone;
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(0.1 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
        [self.tableView selectRowAtIndexPath:[NSIndexPath indexPathForRow:0 inSection:0] animated:NO scrollPosition:UITableViewScrollPositionNone];
        self.currentSettingVC = self.generalSettingVC;
    });
}

- (IBAction)didClickBackButton:(UIButton *)sender {
    [self dismissViewControllerAnimated:YES completion:^{
        if (self.dismissed) {
            self.dismissed();
        }
    }];
}


#pragma mark - Table view data source

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
    return 53;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {

    return self.dataArray.count;
}


- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:@"settingMainCell" forIndexPath:indexPath];
    MCSettingMainModel *item = self.dataArray[indexPath.row];
    cell.textLabel.text = item.title;
    cell.imageView.image = [UIImage imageNamed:item.iconName];
    DLog(@"=====cell=====");
    return cell;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    UITableViewCell *cell = [tableView cellForRowAtIndexPath:indexPath];
    NSString *title = cell.textLabel.text;
    if ([title isEqualToString:MCLocalizedString(kGeneralSettingTitle)]) {
        self.currentSettingVC = self.generalSettingVC;
    }
    
    if ([title isEqualToString:MCLocalizedString(kOperationSettingTitle)]) {
        self.currentSettingVC = self.operationSettingVC;
    }
    
    if ([title isEqualToString:MCLocalizedString(kSoundSettingTitle)]) {
        self.currentSettingVC = self.soundSettingVC;
    }
    
    if ([title isEqualToString:MCLocalizedString(kRoomSettingTitle)]) {
        self.currentSettingVC = self.roomSettingVC;
    }
    
}

- (void)setCurrentSettingVC:(MCSettingDetailVC *)currentSettingVC {
    if (_currentSettingVC) {
        [_currentSettingVC willMoveToParentViewController:self];
        [_currentSettingVC removeFromParentViewController];
        [_currentSettingVC.view removeFromSuperview];
    }
    _currentSettingVC = currentSettingVC;
    
    [self addChildViewController:_currentSettingVC];
    [self.detailView addSubview:_currentSettingVC.view];
    [_currentSettingVC.view mas_makeConstraints:^(MASConstraintMaker *make) {
        make.edges.mas_equalTo(self.detailView);
    }];
    [_currentSettingVC didMoveToParentViewController:self];
}

#pragma mark - getters

- (NSArray *)dataArray {
    if (!_dataArray) {
        NSString *general = MCLocalizedString(kGeneralSettingTitle);
//        NSString *operation = MCLocalizedString(kOperationSettingTitle);
        NSString *sound = MCLocalizedString(kSoundSettingTitle);
//        NSString *room = MCLocalizedString(kRoomSettingTitle);
//        if (self.userInfo.isRoomMaser) {
//            _dataArray = [MCSettingMainModel itemsArrayWithIconNames:@[@"setting_general",@"setting_operation",@"setting_sound",@"setting_room"] titles:@[general,sound]];
//        }else{
//            _dataArray = [MCSettingMainModel itemsArrayWithIconNames:@[@"setting_general",@"setting_operation",@"setting_sound"] titles:@[general,sound]];
//        }
        _dataArray = [MCSettingMainModel itemsArrayWithIconNames:@[@"setting_general",@"setting_sound"] titles:@[general,sound]];
    }
    return _dataArray;
}

- (MCSettingGeneralVC *)generalSettingVC {
    if (!_generalSettingVC) {
        _generalSettingVC = [MCSettingGeneralVC new];
        _generalSettingVC.title = MCLocalizedString(kGeneralSettingTitle);
        _generalSettingVC.userInfo = self.userInfo;
        __weak typeof(self) weakSelf = self;
        _generalSettingVC.didClickExitButtonAction = ^{
            [weakSelf dismissViewControllerAnimated:nil completion:nil];
            if (weakSelf.exitRoomBlock) {
                weakSelf.exitRoomBlock();
            }
        };
    }
    return _generalSettingVC;
}

- (MCSettingOperationVC *)operationSettingVC {
    if (!_operationSettingVC) {
        _operationSettingVC = [MCSettingOperationVC new];
        _operationSettingVC.title = MCLocalizedString(kOperationSettingTitle);
    }
    return _operationSettingVC;
}

- (MCSettingNPCSoundVC *)soundSettingVC {
    if (!_soundSettingVC) {
        _soundSettingVC = [MCSettingNPCSoundVC new];
        UIStoryboard *sb = [UIStoryboard storyboardWithName:@"Main" bundle:nil];
        _soundSettingVC = [sb instantiateViewControllerWithIdentifier:@"npcSound"];
        _soundSettingVC.title = MCLocalizedString(kSoundSettingTitle);
        _soundSettingVC.sceneMgr = self.sceneMgr;
    }
    return _soundSettingVC;
}

/*
- (MCSettingSoundVC *)soundSettingVC {
    if (!_soundSettingVC) {
        _soundSettingVC = [MCSettingSoundVC new];
        _soundSettingVC.title = MCLocalizedString(kSoundSettingTitle);
        _soundSettingVC.sceneMgr = self.sceneMgr;
    }
    return _soundSettingVC;
}
 */

- (MCSettingRoomVC *)roomSettingVC {
    if (!_roomSettingVC) {
        _roomSettingVC = [MCSettingRoomVC new];
        _roomSettingVC.title = MCLocalizedString(kRoomSettingTitle);
        _roomSettingVC.room = self.room;
    }
    return _roomSettingVC;
}

@end
