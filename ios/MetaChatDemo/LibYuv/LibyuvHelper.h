//
//  LibyuvHelper.h
//  Tutorial
//
//  Created by Ddread on 2022/2/26.
//

#import <Foundation/Foundation.h>
#import <AgoraRtcKit/AgoraRtcEngineKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface LibyuvHelper : NSObject

+ (NSData * __nullable)i420BufferOfPixelBuffer:(CVPixelBufferRef)pixelBuffer;
+ (NSData * __nullable)NV12ToI420:(CVPixelBufferRef)srcBuffer;

@end

NS_ASSUME_NONNULL_END
