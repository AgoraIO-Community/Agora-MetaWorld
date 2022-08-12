//
//  RTCLiveDemoAgoraAVDecoder.h
//  RTCLiveDemo
//
//  Created by FanPengpeng on 2022/7/1.
//

#import <Foundation/Foundation.h>
#import <CoreVideo/CoreVideo.h>

NS_ASSUME_NONNULL_BEGIN

@interface RTCLiveDemoAgoraAVDecoder : NSObject

+ (CVPixelBufferRef)I420PixelBufferWithNV12:(CVImageBufferRef)cvpixelBufferRef;

@end

NS_ASSUME_NONNULL_END
