//
//  MCRoomItemCell.m
//  MetaChatDemo
//
//  Created by FanPengpeng on 2022/8/9.
//

#import "MCRoomItemCell.h"
#import "SDWebImage.h"
#import "MetaChatDemo-Swift.h"

@interface MCRoomItemCell()

@property (weak, nonatomic) IBOutlet UIImageView *roomImgView;
@property (weak, nonatomic) IBOutlet UILabel *nameLabel;
@property (weak, nonatomic) IBOutlet UILabel *idLabel;
@property (weak, nonatomic) IBOutlet UILabel *countLabel;
@property (weak, nonatomic) IBOutlet UIImageView *lockImgView;

@end

@implementation MCRoomItemCell

- (void)setRoomImg:(NSString *)img name:(NSString *)name roomId:(NSString *)roomId count:(NSInteger)count {
    self.roomImgView.image = [UIImage imageNamed:img];
    self.nameLabel.text = name;
    self.idLabel.text = [NSString stringWithFormat:@"%@: %@",NSLocalizedString(@"Room ID", @""),roomId];
    self.countLabel.text = [NSString stringWithFormat:@"%zd",count];
}

- (IBAction)didClickJoinButton:(UIButton *)sender {
    if (self.joinButtonCliced) {
        self.joinButtonCliced();
    }
}

- (void)setRoom:(MCRoom *)room {
    _room = room;
    self.nameLabel.text = room.name;
    self.idLabel.text = [NSString stringWithFormat:@"%@: %@",NSLocalizedString(@"Room ID", @""),room.objectId];
    self.countLabel.text = [NSString stringWithFormat:@"%zd", room.memCount.integerValue];
//    [self.roomImgView sd_setImageWithURL:[NSURL URLWithString:room.]];
    [self.roomImgView setImage:[UIImage imageNamed:[NSString stringWithFormat:@"room_cover_%@",room.img]]];
    self.lockImgView.hidden = room.pwd.length == 0;
}

@end
