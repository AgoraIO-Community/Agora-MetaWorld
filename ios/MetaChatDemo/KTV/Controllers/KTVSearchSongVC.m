//
//  KTVSearchSongVC.m
//  MetaChatDemo
//
//  Created by FanPengpeng on 2022/7/12.
//

#import "KTVSearchSongVC.h"
#import "KTVChooseSongCell.h"
#import "MetaChatDemo-Swift.h"
#import "Masonry.h"
#import "UIViewController+KTVBackgroud.h"
#import "KTVSearchBar.h"
//#import "IQKeyboardManager.h"
#import "KTVDataManager.h"
#import "KTVMusic.h"
#import "KTVNetworkHelper.h"

static NSString * const kCellID = @"KTVChooseSongCell";


@interface KTVSearchSongVC ()<UITableViewDelegate, UITableViewDataSource>

@property (nonatomic, strong) UITableView *tableView;

@property (nonatomic, strong) NSMutableArray *dataArray;

@end

@implementation KTVSearchSongVC

- (void)viewDidLoad {
    [super viewDidLoad];
    [self setUpUI];
    [self addObserver];
}

- (void)setUpUI{
    
    [self ktv_setBlurBackground];
//    __weak typeof(self) wSelf = self;
//    [self tapBlankAction:^{
//        [wSelf dismissViewControllerAnimated:YES completion:nil];
//    }];
    
    [self ktv_configCustomNaviBarWithTitle:MCLocalizedString(@"Search")];
    
    KTVSearchBar *searchBar = [KTVSearchBar new];
    __weak typeof(self) wSelf = self;
    [searchBar didClickSearchButton:^(NSString * _Nonnull text) {
        [wSelf searchMusicWithKey:text];
    }];
    [self.view addSubview:searchBar];
    [searchBar mas_makeConstraints:^(MASConstraintMaker *make) {
        make.top.mas_equalTo(60);
        make.left.mas_equalTo(44);
        make.width.mas_equalTo(300);
        make.height.mas_equalTo(36);
    }];
    
    [self.view addSubview:self.tableView];
    [self.tableView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.bottom.mas_equalTo(self.view);
        make.width.mas_equalTo(self.blurWidth);
        make.top.mas_equalTo(searchBar.mas_bottom).offset(20);
    }];
}

- (void)addObserver{
    [[NSNotificationCenter defaultCenter] addObserverForName:kLocalMusicListDidChangeNotificationName object:nil queue:nil usingBlock:^(NSNotification * _Nonnull note) {
        dispatch_async(dispatch_get_main_queue(), ^{
            [self.tableView reloadData];
        });
    }];
    [[NSNotificationCenter defaultCenter] addObserverForName:kPlayingMusicWillChangeNotificationName object:nil queue:nil usingBlock:^(NSNotification * _Nonnull note) {
        dispatch_async(dispatch_get_main_queue(), ^{
            [self.tableView reloadData];
        });
    }];
}

- (void)searchMusicWithKey:(NSString *)key {
    __weak typeof(self) wSelf = self;
    [KTVNetworkHelper searchSongsWithKey:key success:^(NSArray<KTVMusic *> * _Nullable songList) {
        [wSelf.dataArray addObjectsFromArray:songList];
        [wSelf.tableView reloadData];
    } fail:^(NSError * _Nullable err) {
        
    }];
}

- (BOOL)shouldAutorotate {
    return YES;
}

- (UIInterfaceOrientationMask)supportedInterfaceOrientations {
    return UIInterfaceOrientationMaskLandscapeRight;
}

- (UIInterfaceOrientation)preferredInterfaceOrientationForPresentation {
    return UIInterfaceOrientationLandscapeRight;
}

- (void)touchesEnded:(NSSet<UITouch *> *)touches withEvent:(UIEvent *)event {
    [self.view endEditing:YES];
}

#pragma mark - actions

- (void)didClickBackButton {
    [self.navigationController popViewControllerAnimated:NO];
}

#pragma mark - Table view data source

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {

    return self.dataArray.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    KTVChooseSongCell *cell = [tableView dequeueReusableCellWithIdentifier:kCellID forIndexPath:indexPath];
//    [cell setImage:@"" name:@"song name" isAdded:NO];
    KTVMusic *music = self.dataArray[indexPath.row];
    [cell setImage:music.poster name:music.name author:music.singer type:music.type pitchType:music.pitchType isAdded:music.isAdded];

    __weak typeof(self) wSelf = self;
    cell.didClickAddButtonBlock = ^{
        [[KTVDataManager shared] addToLocalMusicList:music];
        [wSelf.tableView reloadData];
    };
    cell.didClickReduceButtonBlock = ^{
        [[KTVDataManager shared] deleteLocalMusic:music];
        [wSelf.tableView reloadData];
    };
    return cell;
}

#pragma mark - getter

- (UITableView *)tableView {
    if (!_tableView) {
        _tableView = [[UITableView alloc] init];
        _tableView.delegate = self;
        _tableView.dataSource = self;
        _tableView.rowHeight = 66;
        _tableView.allowsSelection = NO;
        _tableView.tableFooterView = [UIView new];
        _tableView.backgroundColor = [UIColor clearColor];
        [_tableView registerClass:KTVChooseSongCell.class forCellReuseIdentifier:kCellID];
        
    }
    return _tableView;
}

- (NSMutableArray *)dataArray {
    if (!_dataArray) {
        _dataArray = [NSMutableArray array];
    }
    return _dataArray;
}

@end
