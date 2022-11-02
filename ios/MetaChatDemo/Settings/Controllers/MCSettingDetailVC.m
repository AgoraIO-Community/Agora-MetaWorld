//
//  MCSettingDetailVC.m
//  MetaChatDemo
//
//  Created by FanPengpeng on 2022/8/15.
//

#import "MCSettingDetailVC.h"
#import "Masonry.h"
#import "MCSettingDetailTextFieldCell.h"
#import "MCSettingDetailImageCell.h"
#import "MCSettingDetailLabelCell.h"
#import "MCSettingDetailModel.h"

static NSString * const kTextFieldCellId = @"kTextFieldCellId";
static NSString * const kImageCellId = @"kImageCellId";
static NSString * const kLabelCellId = @"kLabelCellId";

@interface MCSettingDetailVC ()<UITableViewDelegate, UITableViewDataSource>

@property (strong, nonatomic) UILabel *titleLabel;

@property (strong, nonatomic) UITableView *tableView;

@property (strong, nonatomic) NSArray <id<MCSettingDetailModel>> * dataArray;

@end

@implementation MCSettingDetailVC

- (void)dealloc {
    DLog(@"-%@--销毁了",[self class]);
}

- (void)viewDidLoad {
    [super viewDidLoad];
    [self setUpUI];
}

- (void)setUpUI{
    self.dataArray = [self settingItems];
    
    self.view.backgroundColor = [UIColor clearColor];
    [self.view addSubview:self.titleLabel];
    self.titleLabel.text = self.title;
    [self.titleLabel mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.mas_equalTo(30);
        make.top.mas_equalTo(20);
    }];
    
    [self.view addSubview:self.tableView];
    [self.tableView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.top.mas_equalTo(40);
        make.left.bottom.right.mas_equalTo(0);
    }];
}

- (void)reloadData {
    self.dataArray = [self settingItems];
    [self.tableView reloadData];
}

#pragma mark - Table view data source

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return self.dataArray.count;
}


- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    
    id<MCSettingDetailModel> model = [self.dataArray objectAtIndex:indexPath.row];
    __weak typeof(self) weakSelf = self;
    if ([model isKindOfClass:[MCSettingDetailTextFieldModel class]]) {
        MCSettingDetailTextFieldModel *realModel = (MCSettingDetailTextFieldModel *)model;
        MCSettingDetailTextFieldCell *textFieldCell = [tableView dequeueReusableCellWithIdentifier:kTextFieldCellId forIndexPath:indexPath];
        [textFieldCell setTitle:realModel.title originalText:realModel.originalText];
        textFieldCell.endEditingWithText = ^(NSString * _Nonnull text) {
            [weakSelf handleTextFiledCellWithTextFieldModel:realModel endedText:text];
        };
        return textFieldCell;
    }
    
    if ([model isKindOfClass:[MCSettingDetailImageModel class]]) {
        MCSettingDetailImageModel *realModel = (MCSettingDetailImageModel *)model;
        MCSettingDetailImageCell *imageCell = [tableView dequeueReusableCellWithIdentifier:kImageCellId forIndexPath:indexPath];
        [imageCell setTitle:realModel.title imgeUrl:realModel.imageUrl];
        imageCell.rightViewClicked = ^{
            [weakSelf handleClickRightViewWithImageModel:realModel];
        };
        return imageCell;
    }
    
    MCSettingDetailLabelModel *realModel = (MCSettingDetailLabelModel *)model;
    MCSettingDetailLabelCell *labelCell = [tableView dequeueReusableCellWithIdentifier:kLabelCellId forIndexPath:indexPath];
    [labelCell setTitle:realModel.title info:realModel.info];
    labelCell.rightViewClicked = ^{
        [weakSelf handleClickRightViewWithLabelModel:realModel];
    };
    return labelCell;
}

#pragma mark - settting

- (NSArray<id<MCSettingDetailModel>> *)settingItems {
    return nil;
}

- (void)handleTextFiledCellWithTextFieldModel:(MCSettingDetailTextFieldModel *)model endedText:(NSString *)text {
  
}

- (void)handleClickRightViewWithImageModel:(MCSettingDetailImageModel *)model {
    
}

- (void)handleClickRightViewWithLabelModel:(MCSettingDetailLabelModel *)model {
    
}

- (UIView *)tableFooterView {
    
    return [UIView new];
}
#pragma mark - getters

- (UILabel *)titleLabel {
    if (!_titleLabel) {
        _titleLabel = [UILabel new];
        _titleLabel.textColor = [UIColor whiteColor];
        _titleLabel.font = [UIFont boldSystemFontOfSize:14];
    }
    return _titleLabel;
}

- (UITableView *)tableView {
    if (!_tableView) {
        _tableView = [[UITableView alloc] initWithFrame:CGRectZero style:UITableViewStylePlain];
        _tableView.delegate = self;
        _tableView.dataSource = self;
        _tableView.backgroundColor = [UIColor clearColor];
        _tableView.tableFooterView = [self tableFooterView];
        _tableView.rowHeight = 53;
        _tableView.allowsSelection = NO;
        [_tableView registerClass:[MCSettingDetailTextFieldCell class] forCellReuseIdentifier:kTextFieldCellId];
        [_tableView registerClass:[MCSettingDetailImageCell class] forCellReuseIdentifier:kImageCellId];
        [_tableView registerClass:[MCSettingDetailLabelCell class] forCellReuseIdentifier:kLabelCellId];
    }
    return _tableView;
}

- (void)touchesEnded:(NSSet<UITouch *> *)touches withEvent:(UIEvent *)event {
    [self.view endEditing:YES];
}

@end
