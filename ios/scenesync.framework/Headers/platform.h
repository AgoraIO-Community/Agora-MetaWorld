#ifndef __SYS_PLATFORM_H__
#define __SYS_PLATFORM_H__

#if defined(_WIN32) || defined(WIN32)
#   define OS_WIN
#elif defined(__APPLE__)
#   include <TargetConditionals.h>
#   if TARGET_IPHONE_SIMULATOR
#       define OS_IPHONE_SIMULATOR
#   elif TARGET_OS_MACCATALYST
#       define OS_MAC_CATALYST
#   elif TARGET_OS_IPHONE
#       define OS_IPHONE
#   elif TARGET_OS_MAC
#       define OS_MAC
#   else
#       error "unknown apple platform"
#   endif
#elif defined(__linux__) && defined(__ANDROID__)
#   define OS_ANDROID
#elif defined(__linux__)
#   define OS_LUNUX
#else
#   error "unknown compiler"
#endif



#endif