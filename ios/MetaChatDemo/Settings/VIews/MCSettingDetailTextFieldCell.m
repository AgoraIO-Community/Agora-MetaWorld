//
//  MCSettingDetailTextFieldCell.m
//  MetaChatDemo
//
//  Created by FanPengpeng on 2022/8/16.
//

#import "MCSettingDetailTextFieldCell.h"
#import "Masonry.h"

@interface MCSettingDetailTextFieldCell()<UITextFieldDelegate>

@property (nonatomic, strong) UITextField *textField;

@end

@implementation MCSettingDetailTextFieldCell


- (void)createRightView {
    [self.contentView addSubview:self.textField];
    [self.textField mas_makeConstraints:^(MASConstraintMaker *make) {
        make.right.mas_equalTo(-54);
        make.centerY.mas_equalTo(self.contentView);
        make.width.mas_equalTo(80);
        make.height.mas_equalTo(40);
    }];
    
}

- (UIImage *)rightIconImage {
    return [UIImage imageNamed:@"setting_detail_edit"];
}


- (UITextField *)textField {
    if (!_textField) {
        _textField = [UITextField new];
        _textField.font = [UIFont boldSystemFontOfSize:14];
        _textField.textColor = [UIColor whiteColor];
        _textField.delegate = self;
        _textField.textAlignment = NSTextAlignmentRight;
        _textField.returnKeyType = UIReturnKeyDone;
    }
    return _textField;
}


- (void)setTitle:(NSString *)title originalText:(NSString *)originalText {
    self.titleLabel.text = title;
    self.textField.text = originalText;
}

#pragma mark - text field delegate

- (BOOL)textFieldShouldReturn:(UITextField *)textField {
    [textField resignFirstResponder];
    return YES;
}

- (void)textFieldDidEndEditing:(UITextField *)textField {
    if (self.endEditingWithText) {
        self.endEditingWithText(textField.text);
    }
}

@end
