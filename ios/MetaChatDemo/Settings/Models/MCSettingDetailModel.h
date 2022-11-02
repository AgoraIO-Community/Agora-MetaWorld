//
//  MCSettingDetailModel.h
//  MetaChatDemo
//
//  Created by FanPengpeng on 2022/8/16.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@protocol MCSettingDetailModel <NSObject>

- (NSString *)title;

@end

@interface MCSettingDetailTextFieldModel : NSObject<MCSettingDetailModel>

@property (nonatomic, copy) NSString *title;
@property (nonatomic, copy) NSString *originalText;

@end

@interface MCSettingDetailImageModel : NSObject<MCSettingDetailModel>

@property (nonatomic, copy) NSString *title;
@property (nonatomic, copy) NSString *imageUrl;

@end

@interface MCSettingDetailLabelModel : NSObject<MCSettingDetailModel>

@property (nonatomic, copy) NSString *title;
@property (nonatomic, copy) NSString *info;

@end

NS_ASSUME_NONNULL_END
