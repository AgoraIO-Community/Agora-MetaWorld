//
//  KTVHaveChosenCell.m
//  MetaChatDemo
//
//  Created by FanPengpeng on 2022/7/13.
//

#import "KTVHaveChosenCell.h"
#import "Masonry.h"
#import <SDWebImage.h>
#import "MetaChatDemo-Swift.h"

@interface KTVHaveChosenCell()

/// 歌曲名称
@property (nonatomic, strong) UILabel *indexLabel;

/// 头像
@property (nonatomic, strong) UIImageView *imgView;

/// 歌曲名称
@property (nonatomic, strong) UILabel *nameLabel;

/// 作者名称
@property (nonatomic, strong) UILabel *authorLabel;

/// 置顶按钮
@property (nonatomic, strong) UIButton *setTopButton;

/// 删除按钮
@property (nonatomic, strong) UIButton *deleteButton;



@end

@implementation KTVHaveChosenCell

- (instancetype)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier {
    self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
    if (self) {
        [self createSubviews];
        self.backgroundColor = [UIColor clearColor];
        self.contentView.backgroundColor = [UIColor clearColor];
    }
    return self;
}

- (void)createSubviews {
    [self.contentView addSubview:self.indexLabel];
    [self.contentView addSubview:self.imgView];
    [self.contentView addSubview:self.nameLabel];
    [self.contentView addSubview:self.deleteButton];
    [self.contentView addSubview:self.setTopButton];
    [self.contentView addSubview:self.authorLabel];
    
    [self.indexLabel mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.mas_equalTo(self).offset(44);
        make.centerY.mas_equalTo(self.contentView);
    }];
    
    [self.imgView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.centerY.mas_equalTo(self.contentView);
        make.width.height.mas_equalTo(40);
        make.left.mas_equalTo(self).offset(66);
    }];
    
    [self.nameLabel mas_makeConstraints:^(MASConstraintMaker *make) {
        make.top.mas_equalTo(self.imgView);
        make.left.equalTo(self.imgView.mas_right).offset(10);
        make.width.mas_equalTo(150);
        make.height.mas_equalTo(20);
    }];
    
    [self.authorLabel mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.right.equalTo(self.nameLabel);
        make.top.mas_equalTo(self.imgView).offset(23);
    }];
    
    [self.deleteButton mas_makeConstraints:^(MASConstraintMaker *make) {
        make.right.mas_equalTo(-6);
        make.centerY.mas_equalTo(self.contentView);
        make.width.mas_equalTo(40);
        make.height.mas_equalTo(40);
    }];
    
    [self.setTopButton mas_makeConstraints:^(MASConstraintMaker *make) {
        make.right.mas_equalTo(-44);
        make.centerY.mas_equalTo(self.contentView);
        make.width.mas_equalTo(40);
        make.height.mas_equalTo(40);
    }];
}

#pragma mark - public

- (void)setImage:(NSString *)imgUrl name:(NSString *)name author:(NSString *)author index:(NSInteger)index isTop:(BOOL)isTop {
    if (isTop) {
        self.setTopButton.hidden = YES;
    }else{
        self.setTopButton.hidden = NO;
    }
    self.indexLabel.text = [NSString stringWithFormat:@"%zd",index + 1];
    [self.imgView sd_setImageWithURL:[NSURL URLWithString:imgUrl] placeholderImage:[UIImage imageNamed:@"avatar6"]];
    self.nameLabel.text = name;
    self.authorLabel.text = author;
}

#pragma mark - actions

// 点击置顶按钮
- (void)didClickSetTopButton:(UIButton *)button {
    if (self.didClickSetTopButtonBlock) {
        self.didClickSetTopButtonBlock();
    }
}

- (void)didClickDeleteButton:(UIButton *)button {
    if (self.didClickDeleteButtonBlock) {
        self.didClickDeleteButtonBlock();
    }
}

#pragma mark - getter

- (UILabel *)indexLabel {
    if (!_indexLabel) {
        _indexLabel = [UILabel new];
        _indexLabel.font = [UIFont systemFontOfSize:12];
        _indexLabel.textColor = [UIColor whiteColor];
        _indexLabel.text = @"1";
    }
    return _indexLabel;
}

- (UIImageView *)imgView {
    if (!_imgView) {
        _imgView = [UIImageView new];
        _imgView.contentMode = UIViewContentModeScaleAspectFill;
        _imgView.clipsToBounds = YES;
        _imgView.layer.cornerRadius = 8;
    }
    return _imgView;
}

- (UILabel *)nameLabel {
    if (!_nameLabel) {
        _nameLabel = [UILabel new];
        _nameLabel.font = [UIFont systemFontOfSize:12];
        _nameLabel.textColor = [UIColor whiteColor];
        _nameLabel.text = @"song name";
    }
    return _nameLabel;
}

- (UILabel *)authorLabel {
    if (!_authorLabel) {
        _authorLabel = [UILabel new];
        _authorLabel.font = [UIFont systemFontOfSize:12];
        _authorLabel.textColor = [[UIColor alloc] initWithHexRGB:0x999999 alpha:1];
        _authorLabel.text = @"Jay Chou";
    }
    return _authorLabel;
}



- (UIButton *)setTopButton {
    if (!_setTopButton) {
        _setTopButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_setTopButton setImage:[UIImage imageNamed:@"setTop"] forState:UIControlStateNormal];
        [_setTopButton addTarget:self action:@selector(didClickSetTopButton:) forControlEvents:UIControlEventTouchUpInside];
    }
    return _setTopButton;
}

- (UIButton *)deleteButton {
    if (!_deleteButton) {
        _deleteButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_deleteButton setImage:[UIImage imageNamed:@"ktv_delete"] forState:UIControlStateNormal];
        [_deleteButton addTarget:self action:@selector(didClickDeleteButton:) forControlEvents:UIControlEventTouchUpInside];
    }
    return _deleteButton;
}

@end
