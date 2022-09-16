//
//  LibyuvHelper.m
//  Tutorial
//
//  Created by Ddread on 2022/2/26.
//

#import "LibyuvHelper.h"
#include "libyuv.h"


uint8_t I420Buffer[1920 * 1080 * 3 / 2];
@implementation LibyuvHelper

+ (NSData * __nullable)i420BufferOfPixelBuffer:(CVPixelBufferRef)pixelBuffer {
    if (pixelBuffer == nil) {
        return nil;
    }
    CVPixelBufferLockBaseAddress(pixelBuffer, 0);
    
    int width = (int)CVPixelBufferGetWidth(pixelBuffer);
    int height = (int)CVPixelBufferGetHeight(pixelBuffer);
    const uint8 *frame_y = (uint8 *)CVPixelBufferGetBaseAddressOfPlane(pixelBuffer, 0);
    const uint8 *frame_uv = (uint8 *)CVPixelBufferGetBaseAddressOfPlane(pixelBuffer, 1);
    int stride_y = (int)CVPixelBufferGetBytesPerRowOfPlane (pixelBuffer, 0);
    int stride_uv = (int)CVPixelBufferGetBytesPerRowOfPlane (pixelBuffer, 1);
    
    
    uint8 *dst_y = I420Buffer;
    uint8 *dst_u = dst_y + width * height;
    uint8 *dst_v = dst_u + width * height / 4;
    
    int result = libyuv::NV12ToI420(frame_y, stride_y, frame_uv, stride_uv, dst_y, width, dst_u, width / 2, dst_v, width / 2, width, height);
    CVPixelBufferUnlockBaseAddress(pixelBuffer, 0);
    
    if (result != 0) {
        return nil;
    }
    
    NSData *data = [NSData dataWithBytes:dst_y length:width * height * 3/2];
    return data;
}

+ (NSData * __nullable)NV12ToI420:(CVPixelBufferRef)srcBuffer {
    int32_t width = (int32_t)CVPixelBufferGetWidth(srcBuffer);
    int32_t height = (int32_t)CVPixelBufferGetHeight(srcBuffer);
    int w = width;
    int h = height;
    NSDictionary *pixelAttributes = @{(NSString*)kCVPixelBufferIOSurfacePropertiesKey:@{}};
    CVPixelBufferRef dstBuffer = NULL;
    CVPixelBufferCreate(kCFAllocatorDefault,
                        w,
                        h,
                        kCVPixelFormatType_420YpCbCr8PlanarFullRange,
                        (__bridge CFDictionaryRef)(pixelAttributes),
                        &dstBuffer);
    
    if (!dstBuffer) {
        return nil;
    }
    CVPixelBufferLockBaseAddress(srcBuffer, 0);
    CVPixelBufferLockBaseAddress(dstBuffer, 0);
    
    uint8_t* srcY = (uint8_t*)CVPixelBufferGetBaseAddressOfPlane(srcBuffer, 0);
    uint8_t* srcUV = (uint8_t*)CVPixelBufferGetBaseAddressOfPlane(srcBuffer, 1);
    size_t srcStrideY = CVPixelBufferGetBytesPerRowOfPlane(srcBuffer, 0);
    size_t srcStrideUV = CVPixelBufferGetBytesPerRowOfPlane(srcBuffer, 1);
    
    uint8_t* dstY = (uint8_t*)CVPixelBufferGetBaseAddressOfPlane(dstBuffer, 0);
    uint8_t* dstU = (uint8_t*)CVPixelBufferGetBaseAddressOfPlane(dstBuffer, 1);
    uint8_t* dstV = (uint8_t*)CVPixelBufferGetBaseAddressOfPlane(dstBuffer, 2);
    size_t dstStrideY = CVPixelBufferGetBytesPerRowOfPlane(dstBuffer, 0);
    size_t dstStrideU = CVPixelBufferGetBytesPerRowOfPlane(dstBuffer, 1);
    size_t dstStrideV = CVPixelBufferGetBytesPerRowOfPlane(dstBuffer, 2);
    
    int result =  libyuv::NV12ToI420(srcY,
                       srcStrideY,
                       srcUV,
                       srcStrideUV,
                       dstY,
                       dstStrideY,
                       dstU,
                       dstStrideU,
                       dstV,
                       dstStrideV,
                       width, height);
    if (result != 0) {
        return nil;
    }
 
    uint8_t* dst_y = (uint8_t*)CVPixelBufferGetBaseAddressOfPlane(dstBuffer, 0);
    
    NSData *data = [NSData dataWithBytes:dst_y length:width * height * 3/2];
    CVPixelBufferUnlockBaseAddress(dstBuffer, 0);
    CVPixelBufferUnlockBaseAddress(srcBuffer, 0);
    
    CVPixelBufferRelease(dstBuffer);
    return data;
}


@end
