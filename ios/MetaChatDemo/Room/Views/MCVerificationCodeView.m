//
//  MCVerificationCodeView.m
//  MetaChatDemo
//
//  Created by FanPengpeng on 2022/8/9.
//

#import "MCVerificationCodeView.h"
#import "MCVerificationTextField.h"
#import "Masonry.h"
#import "MetaChatDemo-Swift.h"

@interface MCVerificationCodeView()<UITextFieldDelegate,MCTextFieldDelegate>

@property (nonatomic, strong,readwrite) NSString *vertificationCode;//验证码内容

@property (nonatomic, strong)NSMutableArray *textFieldArray;//放textField的array用于在外面好取消键盘

@end

@implementation MCVerificationCodeView

-(void)setSecure {
    for (UITextField *tf in _textFieldArray) {
        tf.secureTextEntry = _isSecure;
    }
    
}
-(void)setView{
    
    self.textFieldArray = [NSMutableArray array];
    NSArray *views = [self subviews];
    for (UITextField *tf in views) {
        [tf removeFromSuperview];
    }
    
    for (int i = 0 ; i < self.verificationCodeNum; i++) {
        MCVerificationTextField *tf = [[MCVerificationTextField alloc] init];
        tf.backgroundColor = [UIColor whiteColor];
        tf.mc_delegate = self;
        tf.keyboardType = UIKeyboardTypeNumberPad;
        tf.textColor = [UIColor blackColor];
        tf.inputView =nil;
        tf.tintColor = [[UIColor alloc] initWithHexRGB:0x009FFF alpha:1];
        
        //圆弧度
        tf.layer.cornerRadius = 8;
        tf.delegate = self;
        tf.tag = 100 + i;
        tf.textAlignment = NSTextAlignmentCenter;
        tf.secureTextEntry = self.isSecure;
        [self addSubview:tf];
        [self.textFieldArray addObject:tf];
    }
    
    [self.textFieldArray mas_makeConstraints:^(MASConstraintMaker *make) {
        make.height.mas_equalTo(48.f);
        make.centerY.mas_equalTo(self);
    }];
    [self.textFieldArray mas_distributeViewsAlongAxis:MASAxisTypeHorizontal withFixedItemLength:62.f leadSpacing:40.f tailSpacing:40.f];
}

//点击退格键的代理
#pragma mark - MCTextFieldDelegate
-(void)textFieldDeleteBackward:(MCVerificationTextField *)textField{
    
    MCVerificationTextField *tf = [_textFieldArray firstObject];
    if (textField.tag > tf.tag) {
        UITextField *newTF =  (UITextField *)[self viewWithTag:textField.tag - 1];
        newTF.text = @"";
        [newTF becomeFirstResponder];
    }
}

#pragma mark - UITextFieldDelegate

//代理（里面有自己的密码线）
-(BOOL)textField:(UITextField *)textField shouldChangeCharactersInRange:(NSRange)range replacementString:(NSString *)string{
    
    textField.text = string;
    UITextField *tf = [_textFieldArray lastObject];
    if (textField.text.length > 0) {//防止退格第一个的时候往后跳一格
        if (textField.tag<  tf.tag) {
            UITextField *newTF =  (UITextField *)[self viewWithTag:textField.tag + 1];
            [newTF becomeFirstResponder];
        }
    }
    [self getVertificationCode];
    return NO;
}

//在里面改变选中状态以及获取验证码
-(void)textFieldDidBeginEditing:(UITextField *)textField{
    [self getVertificationCode];
}

-(void)textFieldDidEndEditing:(UITextField *)textField{
    [self getVertificationCode];
}

-(void)getVertificationCode{ //获取验证码方法
    NSString *str = [NSString string];
    for (int i = 0; i<_textFieldArray.count; i ++) {
        str = [str stringByAppendingString:[NSString stringWithFormat:@"%@",(UITextField *)[_textFieldArray[i] text]]];
    }
    _vertificationCode = str;
    
}
#pragma mark - set方法
-(void)setVerificationCodeNum:(NSInteger)verificationCodeNum{
    _verificationCodeNum = verificationCodeNum;
    [self setView];
}

-(void)setIsSecure:(BOOL)isSecure{
    _isSecure = isSecure;
    [self setSecure];
}

- (void)setCanEdit:(BOOL)canEdit {
    _canEdit = canEdit;
    for (UITextField *tf in self.textFieldArray) {
        tf.enabled = canEdit;
    }
}

//点击回收键盘
-(void)touchesBegan:(NSSet<UITouch *> *)touches withEvent:(UIEvent *)event{
    for (UITextField *tf in self.textFieldArray) {
        [tf resignFirstResponder];
    }
}

- (BOOL)becomeFirstResponder {
    self.canEdit = YES;
    UITextField *tf =  self.textFieldArray.firstObject;
    [tf becomeFirstResponder];
    return [super becomeFirstResponder];
}

@end
