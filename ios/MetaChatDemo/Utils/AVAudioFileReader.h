//
//  AVAudioFileReader.h
//  Wayang
//
//  Created by LLF on 2020/6/3.
//  Copyright © 2020 Agora. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <AVFoundation/AVFoundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface AVAudioFileReader : NSObject

- (BOOL)audioFileOpen:(NSString *)file;
- (NSData *)audioFileRead;
- (CMSampleBufferRef _Nullable)audioFileReadSampleBuffer;
- (BOOL)audioFileClose;
- (uint32_t)audioFileChannels;
- (uint32_t)audioFileSampleRate;
- (uint32_t)audioFile40msSize;
- (uint32_t)audioFileBytesPerSample;

@end

NS_ASSUME_NONNULL_END
