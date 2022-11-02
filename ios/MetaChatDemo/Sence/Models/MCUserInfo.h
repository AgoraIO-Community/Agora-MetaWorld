//
//  MCUserInfo.h
//  MetaChatDemo
//
//  Created by FanPengpeng on 2022/8/22.
//

#import <Foundation/Foundation.h>
NS_ASSUME_NONNULL_BEGIN

typedef enum : NSUInteger {
    AgoraMetachatGenderMale,
    AgoraMetachatGenderFemale
} AgoraMetachatGender;

@interface MCUserInfo : NSObject

@property (nonatomic, copy) NSString *headImg;
@property (nonatomic, copy) NSString *nickname;
@property (nonatomic, strong) NSString *badge;
@property (nonatomic, copy) NSString *avatar;
@property (nonatomic, assign) BOOL isRoomMaser;
@property (nonatomic, assign) AgoraMetachatGender gender;

@end

NS_ASSUME_NONNULL_END
