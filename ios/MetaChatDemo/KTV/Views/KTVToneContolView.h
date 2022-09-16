//
//  KTVToneContolView.h
//  MetaChatDemo
//
//  Created by FanPengpeng on 2022/7/14.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

typedef void(^KTVToneContolViewValueChangedBlock)(NSInteger value, double floatValue);

@interface KTVToneContolView : UIView

@property (nonatomic, assign) double minFloatValue;
@property (nonatomic, assign) double maxFloatValue;
@property (nonatomic, assign) double currentFloatValue;

@property (nonatomic, assign) NSInteger maxValue;

@property (nonatomic, assign) NSInteger currentValue;

@property (copy, nonatomic) KTVToneContolViewValueChangedBlock valueChangedBlock;

@end

NS_ASSUME_NONNULL_END
