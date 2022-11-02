//
//  KTVSingingCell.m
//  MetaChatDemo
//
//  Created by FanPengpeng on 2022/7/26.
//

#import "KTVSingingCell.h"
#import "Masonry.h"
#import <SDWebImage.h>
#import "MetaChatDemo-Swift.h"


@interface KTVSingingCell()

/// 歌曲名称
@property (nonatomic, strong) UILabel *indexLabel;

/// 头像
@property (nonatomic, strong) UIImageView *imgView;

/// 歌曲名称
@property (nonatomic, strong) UILabel *nameLabel;

/// 作者名称
@property (nonatomic, strong) UILabel *authorLabel;

/// 删除按钮
@property (nonatomic, strong) UIButton *nextButton;

/// 正在唱的按钮
@property (nonatomic, strong) UIButton *singingButton;



@end

@implementation KTVSingingCell

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
    [self.contentView addSubview:self.nextButton];
    [self.contentView addSubview:self.singingButton];
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
    
    [self.nextButton mas_makeConstraints:^(MASConstraintMaker *make) {
        make.right.mas_equalTo(-6);
        make.centerY.mas_equalTo(self.contentView);
        make.width.mas_equalTo(40);
        make.height.mas_equalTo(40);
    }];
    
    [self.singingButton mas_makeConstraints:^(MASConstraintMaker *make) {
        make.right.mas_equalTo(-54);
        make.width.mas_equalTo(75);
        make.centerY.mas_equalTo(self.contentView);
    }];
}

#pragma mark - public

- (void)setImage:(NSString *)imgUrl name:(NSString *)name author:(NSString *)author index:(NSInteger)index {
    self.indexLabel.text = [NSString stringWithFormat:@"%zd",index + 1];
    UIImage *image = [UIImage imageNamed:imgUrl];
    if (image) {
        self.imgView.image = image;
    }else{
        [self.imgView sd_setImageWithURL:[NSURL URLWithString:imgUrl] placeholderImage:[UIImage imageNamed:@"avatar6"]];
    }
    self.nameLabel.text = name;
    self.authorLabel.text = author;
}

#pragma mark - actions

- (void)didClickNextButton:(UIButton *)button {
    if (self.didClickNextButtonBlock) {
        self.didClickNextButtonBlock();
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


- (UIButton *)nextButton {
    if (!_nextButton) {
        _nextButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_nextButton setImage:[UIImage imageNamed:@"next_song"] forState:UIControlStateNormal];
        [_nextButton addTarget:self action:@selector(didClickNextButton:) forControlEvents:UIControlEventTouchUpInside];
    }
    return _nextButton;
}

- (UIButton *)singingButton {
    if (!_singingButton) {
        _singingButton = [UIButton buttonWithType:UIButtonTypeCustom];
        _singingButton.titleLabel.font = [UIFont systemFontOfSize:14];
        [_singingButton setImage:[UIImage imageNamed:@"singing"] forState:UIControlStateNormal];
        [_singingButton setTitle:MCLocalizedString(@"singing") forState:UIControlStateNormal];
        _singingButton.titleEdgeInsets = UIEdgeInsetsMake(0, 5, 0, 5);
    }
    return _singingButton;
}

@end
