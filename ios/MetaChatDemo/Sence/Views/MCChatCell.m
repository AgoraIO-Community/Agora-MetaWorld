//
//  MCChatCell.m
//  MetaChatDemo
//
//  Created by FanPengpeng on 2022/8/19.
//

#import "MCChatCell.h"
#import "SDWebImage.h"

@interface MCChatCell()

@property (weak, nonatomic) IBOutlet UIImageView *headImgView;
@property (weak, nonatomic) IBOutlet UILabel *nameLabel;
@property (weak, nonatomic) IBOutlet UILabel *meLabel;
@property (weak, nonatomic) IBOutlet UILabel *timeLabel;
@property (weak, nonatomic) IBOutlet UILabel *msgLabel;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *timeLabelLeftCon;

@end

@implementation MCChatCell

- (void)awakeFromNib {
    [super awakeFromNib];
    self.contentView.backgroundColor = [UIColor clearColor];
    self.backgroundColor = [UIColor clearColor];
    self.meLabel.hidden = YES;
}

- (void)setNickname:(NSString *)nickname isMe:(BOOL)isMe time:(NSString *)time msg:(NSString *)msg img:(NSString *)img {
    self.nameLabel.text = nickname;
    self.meLabel.hidden = !isMe;
    self.timeLabel.text = time;
    self.msgLabel.text = msg;
    self.timeLabelLeftCon.constant = isMe ? 20 : 8;
    UIImage *image = [UIImage imageNamed:img];
    if (image) {
        self.headImgView.image = image;
    }else{
        [self.headImgView sd_setImageWithURL:[NSURL URLWithString:img]];        
    }
}

@end
