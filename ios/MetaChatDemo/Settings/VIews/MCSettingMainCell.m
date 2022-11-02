//
//  MCSettingMainCell.m
//  MetaChatDemo
//
//  Created by FanPengpeng on 2022/8/15.
//

#import "MCSettingMainCell.h"
#import "MetaChatDemo-Swift.h"

@implementation MCSettingMainCell

- (void)awakeFromNib {
    [super awakeFromNib];
    // Initialization code
    self.backgroundColor = [UIColor clearColor];
    self.contentView.backgroundColor = [UIColor clearColor];
    self.contentView.layer.cornerRadius = 9;
    self.contentView.layer.masksToBounds = YES;
    self.textLabel.textColor = [UIColor whiteColor];
    self.textLabel.font = [UIFont systemFontOfSize:12];
}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated {
    [super setSelected:selected animated:animated];
    self.contentView.backgroundColor = selected ? [[UIColor alloc] initWithHexString:@"#6842F6"] : [UIColor clearColor];
    self.selectionStyle = UITableViewCellSelectionStyleNone;
}

@end
