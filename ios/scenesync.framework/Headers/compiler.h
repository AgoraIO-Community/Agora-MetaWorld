#ifndef __SYS_COMPILER_H__
#define __SYS_COMPILER_H__

#if defined(__GUNC__)
#   define COMPILER_IS_GCC
#   if defined(__MINGW32__) || defined(__MINGW64__)
#       define COMPILER_IS_MINGW
#   endif
#   if defined(__MSYS__)
#       define COMPILER_ON_MSYS
#   endif
#   if defined(__CYGWIN__) || defined(__CYGWIN32__)
#       define COMPILER_ON_CYGWIN
#   endif
#   if defined(__clang__)
#       define COMPILER_IS_CLANG
#   endif
#elif defined(_MSC_VER)
#   define COMPILER_IS_MSVC
#else
#   define COMPILER_IS_UNKNOWN
#endif



#endif