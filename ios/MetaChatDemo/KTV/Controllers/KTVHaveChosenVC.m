//
//  KTVHaveChosenVC.m
//  MetaChatDemo
//
//  Created by FanPengpeng on 2022/7/12.
//

#import "KTVHaveChosenVC.h"
#import "JXSegmentedView-Swift.h"
#import "KTVHaveChosenCell.h"
#import "KTVDataManager.h"
#import "KTVSingingCell.h"
#import "KTVMusic.h"


static NSString * const kWaitingCellID = @"KTVHaveChosenCell";
static NSString * const kSingingCellID = @"kSingingCellID";

@interface KTVHaveChosenVC ()<JXSegmentedListContainerViewListDelegate>

@end

@implementation KTVHaveChosenVC

- (void)viewDidLoad {
    [super viewDidLoad];
    
    [self setUpUI];
    // 添加监听
    [self addObserver];
}

- (void)setUpUI{
    [self.tableView registerClass:KTVHaveChosenCell.class forCellReuseIdentifier:kWaitingCellID];
    [self.tableView registerClass:KTVSingingCell.class forCellReuseIdentifier:kSingingCellID];
    self.tableView.backgroundColor = [UIColor clearColor];
    self.tableView.tableFooterView = [UIView new];
    self.tableView.rowHeight = 66;
    self.tableView.allowsSelection = NO;
    self.clearsSelectionOnViewWillAppear = YES;
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

#pragma mark - Table view data source


- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return [KTVDataManager shared].localMusicList.count;
}


- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    __weak typeof(self) wSelf = self;
    KTVMusic *music = [KTVDataManager shared].localMusicList[indexPath.row];
    if (indexPath.row == 0) {
        KTVSingingCell *singingCell = [tableView dequeueReusableCellWithIdentifier:kSingingCellID forIndexPath:indexPath];
        [singingCell setImage:music.poster name:music.name author:music.singer index:indexPath.row];
        singingCell.didClickNextButtonBlock = ^{
            [[KTVDataManager shared] makeNextAsPlaying];
            [wSelf.tableView reloadData];
        };
        return singingCell;
    }
    KTVHaveChosenCell *cell = [tableView dequeueReusableCellWithIdentifier:kWaitingCellID forIndexPath:indexPath];
    [cell setImage:music.poster name:music.name author:music.singer index:indexPath.row isTop:indexPath.row == 1];
    cell.didClickSetTopButtonBlock = ^{
        DLog(@"didClickSetTopButtonBlock --> %zd",indexPath.row);
        [[KTVDataManager shared] bringMusicToTop:music];
        [wSelf.tableView reloadData];
    };
    cell.didClickDeleteButtonBlock = ^{
        DLog(@"didClickDeleteButtonBlock --> %zd",indexPath.row);
        [[KTVDataManager shared] deleteLocalMusic:music];
        [wSelf.tableView reloadData];
    };
    return cell;
}


- (UIView *)listView {
    
    return  self.view;
}

@end
