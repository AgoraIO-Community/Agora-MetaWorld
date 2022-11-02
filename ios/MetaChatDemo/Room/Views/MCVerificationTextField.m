//
//  MCTextField.m
//  MetaChatDemo
//
//  Created by FanPengpeng on 2022/8/9.
//

#import "MCVerificationTextField.h"

@implementation MCVerificationTextField

-(void)deleteBackward{
    
    [super deleteBackward];
    
    if ([self.mc_delegate respondsToSelector:@selector(textFieldDeleteBackward:)]) {
        
        [self.mc_delegate textFieldDeleteBackward:self];
    }
}

@end
