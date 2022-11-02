//
//  KTVChooseSongCell.m
//  MetaChatDemo
//
//  Created by FanPengpeng on 2022/7/12.
//

#import "KTVChooseSongCell.h"
#import "Masonry.h"
#import <SDWebImage.h>
#import "KTVSongTagsView.h"
#import "KTVSongTag.h"
#import "MetaChatDemo-Swift.h"


@interface KTVChooseSongCell()

/// 头像
@property (nonatomic, strong) UIImageView *imgView;
/// 名称
@property (nonatomic, strong) UILabel *nameLabel;
/// 添加按钮
@property (nonatomic, strong) UIButton *addButton;
/// 标签
@property (nonatomic, strong) KTVSongTagsView *tagsView;
/// 作者名称
@property (nonatomic, strong) UILabel *authorLabel;

@property (nonatomic, strong) KTVSongTag *accompanyTag; // 伴奏
@property (nonatomic, strong) KTVSongTag *originalTag; // 原唱
@property (nonatomic, strong) KTVSongTag *scoreTag; // 伴奏

@end

@implementation KTVChooseSongCell

- (instancetype)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier {
    self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
    if (self) {
        [self createSubviews];
        self.backgroundColor = [UIColor clearColor];
    }
    return self;
}

- (void)createSubviews {
    [self.contentView addSubview:self.imgView];
    [self.contentView addSubview:self.nameLabel];
    [self.contentView addSubview:self.addButton];
    [self.contentView addSubview:self.tagsView];
    [self.contentView addSubview:self.authorLabel];
    
    [self.imgView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.centerY.mas_equalTo(self.contentView);
        make.width.height.mas_equalTo(40);
        make.left.mas_equalTo(self).offset(44);
    }];
    
    [self.nameLabel mas_makeConstraints:^(MASConstraintMaker *make) {
        make.top.mas_equalTo(self.imgView);
        make.left.equalTo(self.imgView.mas_right).offset(10);
        make.width.mas_equalTo(150);
        make.height.mas_equalTo(20);
    }];
    
    [self.addButton mas_makeConstraints:^(MASConstraintMaker *make) {
        make.right.mas_equalTo(-6);
        make.centerY.mas_equalTo(self.contentView);
        make.width.mas_equalTo(40);
        make.height.mas_equalTo(40);
    }];
    
    [self.tagsView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.mas_equalTo(self.imgView.mas_right).offset(10);
        make.bottom.mas_equalTo(-10);
    }];
    
    [self.authorLabel mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.mas_equalTo(self.tagsView.mas_right).offset(3);
        make.bottom.mas_equalTo(-10);
    }];
    
}

#pragma mark - public

- (void)setImage:(NSString *)imgUrl name:(NSString *)name author:(NSString *)author type:(NSInteger)type pitchType:(NSInteger)pitchType isAdded:(BOOL) isAdded {
    UIImage *image = [UIImage imageNamed:imgUrl];
    if (image) {
        self.imgView.image = image;
    }else{
        [self.imgView sd_setImageWithURL:[NSURL URLWithString:imgUrl] placeholderImage:[UIImage imageNamed:@"avatar6"]];
    }
    self.nameLabel.text = name;
    self.addButton.selected = isAdded;
    
    NSMutableArray *tags = [NSMutableArray array];
    switch (type) {
        case 1:
        case 4:
        case 5:
            [tags addObject:self.accompanyTag];
            [tags addObject:self.originalTag];
            break;
        case 2:
            [tags addObject:self.accompanyTag];
            break;
        case 3:
            [tags addObject:self.originalTag];
            break;
        default:
            break;
    }
    if (pitchType == 1) {
        [tags addObject:self.scoreTag];
    }
    self.tagsView.tags = tags;
    self.authorLabel.text = author;
    
}

#pragma mark - actions

- (void)didClickAddButton:(UIButton *)button {
    if (button.selected) {
        if (self.didClickReduceButtonBlock) {
            self.didClickReduceButtonBlock();
        }
    }else{
        if (self.didClickAddButtonBlock) {
            self.didClickAddButtonBlock();
        }
    }
}

#pragma mark - getter

- (KTVSongTag *)accompanyTag {
    if (!_accompanyTag) {
        _accompanyTag = [KTVSongTag new];
        _accompanyTag.tagName = @"Accompany";
        _accompanyTag.colorValue = 0xDEA960;
        _accompanyTag.alpha = 1;
    }
    return _accompanyTag;
}

- (KTVSongTag *)originalTag {
    if (!_originalTag) {
        _originalTag = [KTVSongTag new];
        _originalTag.tagName = @"Original";
        _originalTag.colorValue = 0x35C67A;
        _originalTag.alpha = 1;
    }
    return _originalTag;
}

- (KTVSongTag *)scoreTag {
    if (!_scoreTag) {
        _scoreTag = [KTVSongTag new];
        _scoreTag.tagName = @"Score";
        _scoreTag.colorValue = 0x00A1FF;
        _scoreTag.alpha = 1;
    }
    return _scoreTag;
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

- (UIButton *)addButton {
    if (!_addButton) {
        _addButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_addButton setImage:[UIImage imageNamed:@"add"] forState:UIControlStateNormal];
        [_addButton setImage:[UIImage imageNamed:@"reduce"] forState:UIControlStateSelected];
        [_addButton addTarget:self action:@selector(didClickAddButton:) forControlEvents:UIControlEventTouchUpInside];
    }
    return _addButton;
}

- (KTVSongTagsView *)tagsView {
    if (!_tagsView) {
        _tagsView = [KTVSongTagsView new];
    }
    return _tagsView;
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

@end
