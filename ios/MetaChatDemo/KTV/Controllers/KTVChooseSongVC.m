//
//  KTVChooseSong\VC.m
//  MetaChatDemo
//
//  Created by FanPengpeng on 2022/7/12.
//

#import "KTVChooseSongVC.h"
#import "JXSegmentedView-Swift.h"
#import "KTVChooseSongCell.h"
#import "KTVDataManager.h"
#import "KTVMusic.h"
#import "UIViewController+Extension.h"
#import "KTVNetworkHelper.h"

static NSString * const kCellID = @"KTVChooseSongCell";

@interface KTVChooseSongVC ()<JXSegmentedListContainerViewListDelegate>

@property (nonatomic, strong) NSArray *dataArray;

@end

@implementation KTVChooseSongVC

- (void)dealloc {
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

- (void)viewDidLoad {
    [super viewDidLoad];
    
    [self.tableView registerClass:KTVChooseSongCell.class forCellReuseIdentifier:kCellID];
    self.tableView.backgroundColor = [UIColor clearColor];
    self.tableView.tableFooterView = [UIView new];

    self.tableView.rowHeight = 66;
    self.tableView.allowsSelection = NO;
    self.clearsSelectionOnViewWillAppear = YES;
    [self loadData];
    [self addObserver];
}

- (void)loadData {
    NSArray *cachedList = [KTVDataManager shared].cachedHotSongs[self.title];
    if (cachedList.count > 0) {
        self.dataArray = cachedList;
        [self.tableView reloadData];
        return;
    }
    
    NSInteger hotType = [[KTVDataManager shared] hotTypeForTitle:self.title];
    [KTVNetworkHelper songsForHotType:@(hotType) Success:^(NSArray<KTVMusic *> * _Nullable songList) {
        self.dataArray = songList;
        [KTVDataManager shared].cachedHotSongs[self.title] = songList;
        [self.tableView reloadData];
    } fail:^(NSError * _Nullable err) {
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
        if (music.isPlaying) {
            [self ex_showMCAlertWithTitle:@"This song is playing!!!" message:nil cancelTitle:MCLocalizedString(@"Cancel") confirmTitle:MCLocalizedString(@"Confirm") cancelHandler:nil confirmHandler:^{
                [[KTVDataManager shared] makeNextAsPlaying];
            }];
        }else{
            [[KTVDataManager shared] deleteLocalMusic:music];
        }
    };
    return cell;
}


- (NSArray *)dataArray {
    if (!_dataArray) {
        _dataArray = [NSArray array];
    }
    return _dataArray;
}


- (UIView *)listView {
    return  self.view;
}


@end
