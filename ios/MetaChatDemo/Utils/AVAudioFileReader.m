//
//  AVAudioFileReader.m
//  Wayang
//
//  Created by LLF on 2020/6/3.
//  Copyright © 2020 Agora. All rights reserved.
//

#import "AVAudioFileReader.h"
#import <UIKit/UIKit.h>


@interface AVAudioFileReader()

@property (assign, nonatomic) BOOL mVerGTE9; // Whether iOS version is greater than or equal to 9.0
@property (assign, nonatomic) BOOL mVerAbove10_10;
@property (assign, nonatomic) uint32_t mChannels;
@property (assign, nonatomic) uint32_t mSampleRate;
@property (assign, nonatomic) uint32_t mBytesPerSample;

@property (assign, nonatomic) uint32_t mDataSize40ms;
@property (strong, nonatomic) AVAudioFile *mAudioFile;
@property (strong, nonatomic) AVAudioPCMBuffer *mDataBuffer;

@end

@implementation AVAudioFileReader

- (instancetype)init {
    if (self = [super init]) {
        self.mVerGTE9 = [[[UIDevice currentDevice] systemVersion] compare:@"9.0" options:NSNumericSearch] != NSOrderedAscending;
    }
    return self;
}

- (BOOL)audioFileOpen:(NSString *)file {
    NSURL *url = [NSURL fileURLWithPath:file];
    NSError *error = nil;
    AVAudioCommonFormat pcmFormat;
    
    if (self.mVerGTE9) {
        pcmFormat = AVAudioPCMFormatInt16;
    } else {
        pcmFormat = AVAudioPCMFormatFloat32;
    }
    
    self.mAudioFile = [[AVAudioFile alloc] initForReading:url
                                             commonFormat:pcmFormat
                                              interleaved:NO
                                                    error:&error];
    
    if (error != nil) {
        error = nil;
        // Will try again for cases such as: ipod-library://item/item.mp3?id=3736932668014684581
        NSURL* url1 = [NSURL URLWithString:[file stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding]];
        self.mAudioFile = [[AVAudioFile alloc] initForReading:url1
                                                 commonFormat:pcmFormat
                                                  interleaved:NO
                                                        error:&error];
        if (error != nil) {
            return NO;
        }
    }
    
    AVAudioFormat *pFormat = [self.mAudioFile processingFormat];
    self.mSampleRate = (uint32_t) pFormat.sampleRate;
    self.mChannels = (uint32_t) pFormat.channelCount;
    const AudioStreamBasicDescription* const asbd = [pFormat streamDescription];
    self.mBytesPerSample = asbd->mBytesPerPacket;
    self.mDataSize40ms = self.mSampleRate * self.mChannels * 40 / 1000;
    self.mDataBuffer = [[AVAudioPCMBuffer alloc] initWithPCMFormat:pFormat frameCapacity:self.mSampleRate * 40 / 1000];
    return true;
}

- (CMSampleBufferRef _Nullable)audioFileReadSampleBuffer {
  if (!self.mAudioFile || !self.mDataBuffer) return nil;
  NSError* err = nil;
  [self.mAudioFile readIntoBuffer:self.mDataBuffer error:&err];
  if (err != nil) {
    return nil;
  }
  
  AudioBufferList *audioBufferList = [self.mDataBuffer mutableAudioBufferList];
  AudioStreamBasicDescription asbd = *self.mDataBuffer.format.streamDescription;
  
  CMSampleBufferRef sampleBuffer = NULL;
  CMFormatDescriptionRef format = NULL;
  
  OSStatus error = CMAudioFormatDescriptionCreate(kCFAllocatorDefault, &asbd, 0, NULL, 0, NULL, NULL, &format);
  if (error != noErr) {
    return nil;
  }
  
  CMSampleTimingInfo timing = { CMTimeMake(1, asbd.mSampleRate), kCMTimeZero, kCMTimeInvalid };
  error = CMSampleBufferCreate(kCFAllocatorDefault,
                               NULL, false, NULL, NULL, format,
                               self.mDataBuffer.frameLength,
                               1, &timing, 0, NULL, &sampleBuffer);
  if (error != noErr) {
    CFRelease(format);
    return nil;
  }
  
  error = CMSampleBufferSetDataBufferFromAudioBufferList(sampleBuffer, kCFAllocatorDefault, kCFAllocatorDefault, 0, audioBufferList);
  if (error != noErr) {
    CFRelease(format);
    return nil;
  }
  
  CFRelease(format);
  return sampleBuffer;
}

- (NSData *)audioFileRead {
    int16_t data[8192] = {0};
    if (!self.mAudioFile || !self.mDataBuffer) {
        memset(data, 0, sizeof(int16_t) * self.mDataSize40ms);
        return nil;
    }
    
    NSError* err = nil;
    [self.mAudioFile readIntoBuffer:self.mDataBuffer error:&err];
    if (err != nil) {
        memset(data, 0, sizeof(int16_t) * self.mDataSize40ms);
        return nil;
    }
    
    if (self.mVerGTE9) {
        for (AVAudioChannelCount channelIndex = 0; channelIndex < self.mDataBuffer.format.channelCount; ++channelIndex) {
            int16_t *channelData = self.mDataBuffer.int16ChannelData[channelIndex];
            if (channelData) {
                for (AVAudioFrameCount frameIndex = 0; frameIndex < self.mDataBuffer.frameLength; ++frameIndex) {
                    data[self.mDataBuffer.format.channelCount * frameIndex + channelIndex] = channelData[frameIndex];
                }
            }
        }
    } else {
        for (AVAudioChannelCount channelIndex = 0; channelIndex < self.mDataBuffer.format.channelCount; ++channelIndex) {
            if (self.mDataBuffer.floatChannelData != nil) {
                float *channelData = self.mDataBuffer.floatChannelData[channelIndex];
                if (channelData) {
                    for (AVAudioFrameCount frameIndex = 0; frameIndex < self.mDataBuffer.frameLength; ++frameIndex) {
                        data[self.mDataBuffer.format.channelCount * frameIndex+channelIndex] = (int16_t)(channelData[frameIndex] * 32767.0f);
                    }
                }
            } else {
                memset(data, 0, self.mDataSize40ms * sizeof(int16_t));
                return nil;
            }
        }
    }
    
    BOOL retval = self.mDataBuffer.frameLength == self.mSampleRate * 40 / 1000;
    if (retval) {
      NSData *rawData = [[NSData alloc] initWithBytes:(void *)data length:sizeof(data)];
      return rawData;
    }
    return nil;
}

- (BOOL)audioFileClose {
    if (self.mAudioFile) {
        self.mAudioFile = nil;
    }
    if (self.mDataBuffer) {
        self.mDataBuffer = nil;
    }
    return YES;
}

- (uint32_t)audioFileBytesPerSample {
    return self.mBytesPerSample;
}

- (uint32_t)audioFileChannels {
    return self.mChannels;
}

- (uint32_t)audioFileSampleRate {
    return self.mSampleRate;
}

- (uint32_t)audioFile10msSize {
    return self.mDataSize40ms;
}

@end
