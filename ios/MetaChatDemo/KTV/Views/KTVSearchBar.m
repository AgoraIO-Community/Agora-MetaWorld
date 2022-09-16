//
//  KTVSearchBar.m
//  MetaChatDemo
//
//  Created by FanPengpeng on 2022/7/12.
//

#import "KTVSearchBar.h"
#import "Masonry.h"
#import "MetaChatDemo-Swift.h"


@interface KTVSearchBar()

@property (nonatomic, strong) UITextField *textField;

@property (nonatomic, strong) UIButton *clearButton;

@property (nonatomic, strong) UIButton *searchButton;

@property (nonatomic, copy) KTVSearchBarClickedSearch searchBlock;



@end

@implementation KTVSearchBar

- (instancetype)initWithFrame:(CGRect)frame
{
    self = [super initWithFrame:frame];
    if (self) {
        [self createSubviews];
    }
    return self;
}

- (void)createSubviews {
    
    UIView *bgView = [UIView new];
    bgView.backgroundColor = [[UIColor alloc] initWithHexRGB:0x000000 alpha:0.3];
    bgView.layer.cornerRadius = 10;
    bgView.layer.masksToBounds = YES;
    [self addSubview:bgView];
    [bgView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.edges.mas_equalTo(UIEdgeInsetsMake(1, 1, 1, 1));
    }];
    
    [self addSubview:self.textField];
    [self.textField mas_makeConstraints:^(MASConstraintMaker *make) {
        make.bottom.top.mas_equalTo(bgView);
        make.left.mas_equalTo(15);
        make.right.mas_equalTo(-48);
    }];
    
    [self addSubview:self.clearButton];
    [self.clearButton mas_makeConstraints:^(MASConstraintMaker *make) {
        make.centerY.mas_equalTo(self);
        make.right.mas_equalTo(-44);
        make.width.height.mas_equalTo(28);
    }];

    [self addSubview:self.searchButton];
    [self.searchButton mas_makeConstraints:^(MASConstraintMaker *make) {
        make.height.mas_equalTo(32);
        make.width.mas_equalTo(38);
        make.centerY.equalTo(bgView);
        make.right.mas_equalTo(-2);
    }];
}

- (void)didClickSearchButton:(KTVSearchBarClickedSearch) block {
    [self.textField resignFirstResponder];
    self.searchBlock = block;
}

#pragma mark - actions

- (void)didClickClearButton {
    self.textField.text = nil;
}

- (void)didClickSearchButton {
    if (self.searchBlock) {
        self.searchBlock(self.textField.text);
    }
}

#pragma mark -  getter

- (UIButton *)searchButton {
    if (!_searchButton) {
        _searchButton = [UIButton buttonWithType:UIButtonTypeCustom];
        _searchButton.backgroundColor = [[UIColor alloc] initWithHexString:@"#6842F6"];
        _searchButton.layer.cornerRadius = 9;
        _searchButton.layer.masksToBounds = YES;
        [_searchButton setImage:[UIImage imageNamed:@"search"] forState:UIControlStateNormal];
        [_searchButton addTarget:self action:@selector(didClickSearchButton) forControlEvents:UIControlEventTouchUpInside];
    }
    return _searchButton;
}



- (UITextField *)textField {
    if (!_textField) {
        _textField = [UITextField new];
        _textField.textColor = [UIColor whiteColor];
        _textField.tintColor = [UIColor whiteColor];
    }
    return _textField;
}


- (UIButton *)clearButton {
    if (!_clearButton) {
        _clearButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_clearButton setImage:[UIImage imageNamed:@"clear"] forState:UIControlStateNormal];
        [_clearButton addTarget:self action:@selector(didClickClearButton) forControlEvents:UIControlEventTouchUpInside];
    }
    return _clearButton;
}


@end
