//
//  RTCLiveDemoAgoraAVDecoder.m
//  RTCLiveDemo
//
//  Created by FanPengpeng on 2022/7/1.
//

#import "RTCLiveDemoAgoraAVDecoder.h"
#import "libyuv.h"


@implementation RTCLiveDemoAgoraAVDecoder

/// NV12转I420

+ (CVPixelBufferRef)I420PixelBufferWithNV12:(CVImageBufferRef)cvpixelBufferRef {
    
    CVPixelBufferLockBaseAddress(cvpixelBufferRef, 0);
    //图像宽度（像素）
    
    size_t pixelWidth = CVPixelBufferGetWidth(cvpixelBufferRef);
    
    //图像高度（像素）
    
    size_t pixelHeight = CVPixelBufferGetHeight(cvpixelBufferRef);
    
    //获取CVPixelBufferRef中的y数据
    
    const uint8_t * y_frame = (uint8_t *)CVPixelBufferGetBaseAddressOfPlane(cvpixelBufferRef,0);
    
    //获取CMVImageBufferRef中的uv数据
    
    const uint8_t* uv_frame = (uint8_t*)CVPixelBufferGetBaseAddressOfPlane(cvpixelBufferRef,1);
    
    //y stride
    
    size_t plane1_stride = CVPixelBufferGetBytesPerRowOfPlane (cvpixelBufferRef, 0);
    
    //uv stride
    
    size_t plane2_stride = CVPixelBufferGetBytesPerRowOfPlane (cvpixelBufferRef, 1);
    
    //yuv_size(内存空间)
    
    size_t frame_size = pixelWidth*pixelHeight*3/2;
    
    //开辟frame_size大小的内存空间用于存放转换好的i420数据
    
    uint8_t* buffer = (unsigned char *)malloc(frame_size);
    
    //buffer为这段内存的首地址,plane1_size代表这一帧中y数据的长度
    
    uint8_t* dst_u = buffer + pixelWidth*pixelHeight;
    
    //dst_u为u数据的首地,plane1_size/4为u数据的长度
    
    uint8_t* dst_v = dst_u + pixelWidth*pixelHeight/4;
    
    //libyuv转换
    
    int ret = NV12ToI420(y_frame,(int)plane1_stride,uv_frame,(int)plane2_stride,buffer,(int)pixelWidth,dst_u,(int)pixelWidth/2,dst_v,(int)pixelWidth/2,(int)pixelWidth,(int)pixelHeight);
    if (ret) {
        return NULL;
    }
    NSDictionary *pixelAttributes = @{(id)kCVPixelBufferIOSurfacePropertiesKey : @{}};
    CVPixelBufferRef pixelBuffer = NULL;
    CVReturn result = CVPixelBufferCreate(kCFAllocatorDefault,pixelWidth,pixelHeight,kCVPixelFormatType_420YpCbCr8Planar,(__bridge CFDictionaryRef)(pixelAttributes),&pixelBuffer);
    CVPixelBufferLockBaseAddress(pixelBuffer, 0);
    size_t d = CVPixelBufferGetBytesPerRowOfPlane(pixelBuffer, 0);
    size_t ud = CVPixelBufferGetBytesPerRowOfPlane(pixelBuffer, 1);
    size_t vd = CVPixelBufferGetBytesPerRowOfPlane(pixelBuffer, 2);
    unsigned char* dsty = (unsigned char *)CVPixelBufferGetBaseAddressOfPlane(pixelBuffer, 0);
    unsigned char* dstu = (unsigned char *)CVPixelBufferGetBaseAddressOfPlane(pixelBuffer, 1);
    unsigned char* dstv = (unsigned char *)CVPixelBufferGetBaseAddressOfPlane(pixelBuffer, 2);
    unsigned char* srcy = buffer;
    for (unsigned int rIdx = 0; rIdx < pixelHeight; ++rIdx, srcy += pixelWidth, dsty += d) {
        memcpy(dsty, srcy, pixelWidth);
    }
    unsigned char* srcu = buffer + pixelHeight*pixelWidth;
    for (unsigned int rIdx = 0; rIdx < pixelHeight/2; ++rIdx, srcu += pixelWidth/2, dstu += ud) {
        memcpy(dstu, srcu, pixelWidth/2);
    }
    unsigned char* srcv = buffer + pixelHeight*pixelWidth*5/4;
    for (unsigned int rIdx = 0; rIdx < pixelHeight/2; ++rIdx, srcv += pixelWidth/2, dstv += vd) {
        memcpy(dstv, srcv, pixelWidth/2);
    }
    CVPixelBufferUnlockBaseAddress(pixelBuffer, 0);
    if (result != kCVReturnSuccess) {
        NSLog(@"Unable to create cvpixelbuffer %d", result);
    }
    free(buffer);
    
    //    CVPixelBufferRelease(cvpixelBufferRef);
    return pixelBuffer;
}

#pragma mark - NV12转BGRA

+ (CVPixelBufferRef)RGBAPixelBufferWithNV12:(CVImageBufferRef)pixelBufferNV12{
    CVPixelBufferLockBaseAddress(pixelBufferNV12, 0);
    //图像宽度（像素）
    size_t pixelWidth = CVPixelBufferGetWidth(pixelBufferNV12);
    //图像高度（像素）
    size_t pixelHeight = CVPixelBufferGetHeight(pixelBufferNV12);
    //y_stride
    size_t src_stride_y = CVPixelBufferGetBytesPerRowOfPlane(pixelBufferNV12, 0);
    //uv_stride
    size_t src_stride_uv = CVPixelBufferGetBytesPerRowOfPlane(pixelBufferNV12,1);
    //获取CVImageBufferRef中的y数据
    uint8_t *src_y = (unsigned char *)CVPixelBufferGetBaseAddressOfPlane(pixelBufferNV12, 0);
    //获取CMVImageBufferRef中的uv数据
    uint8_t *src_uv =(unsigned char *) CVPixelBufferGetBaseAddressOfPlane(pixelBufferNV12, 1);
    // 创建一个空的32BGRA格式的CVPixelBufferRef
    NSDictionary *pixelAttributes = @{(id)kCVPixelBufferIOSurfacePropertiesKey : @{}};
    CVPixelBufferRef pixelBufferRGBA = NULL;
    CVReturn result = CVPixelBufferCreate(kCFAllocatorDefault,pixelWidth,pixelHeight,kCVPixelFormatType_32BGRA,(__bridge CFDictionaryRef)pixelAttributes,&pixelBufferRGBA);//kCVPixelFormatType_32BGRA
    if (result != kCVReturnSuccess) {
        NSLog(@"Unable to create cvpixelbuffer %d", result);
        return NULL;
    }
    result = CVPixelBufferLockBaseAddress(pixelBufferRGBA, 0);
    if (result != kCVReturnSuccess) {
        CFRelease(pixelBufferRGBA);
        NSLog(@"Failed to lock base address: %d", result);
        return NULL;
        
    }
    // 得到新创建的CVPixelBufferRef中 rgb数据的首地址
    uint8_t *rgb_data = (uint8_t*)CVPixelBufferGetBaseAddress(pixelBufferRGBA);
    // 使用libyuv为rgb_data写入数据，将NV12转换为BGRA
    size_t bgraStride = CVPixelBufferGetBytesPerRowOfPlane(pixelBufferRGBA,0);
    int ret = NV12ToARGB(src_y, (int)src_stride_y, src_uv, (int)src_stride_uv, rgb_data,(int)bgraStride, (int)pixelWidth, (int)pixelHeight);
    if (ret) {
        NSLog(@"Error converting NV12 VideoFrame to BGRA: %d", result);
        CFRelease(pixelBufferRGBA);
        return NULL;
    }
    CVPixelBufferUnlockBaseAddress(pixelBufferRGBA, 0);
    CVPixelBufferUnlockBaseAddress(pixelBufferNV12, 0);
    return pixelBufferRGBA;
}


@end
