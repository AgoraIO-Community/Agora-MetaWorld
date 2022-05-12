#ifndef __SYS_KEYWORD_H__
#define __SYS_KEYWORD_H__

#include "compiler.h"

#ifdef __cplusplus
#   define EXTERN_C         extern "C"
#   define EXTERN_C_ENTER   extern "C" {
#   define EXTERN_C_LEAVE   }
#else
#   define EXTERN_C
#   define EXTERN_C_ENTER
#   define EXTERN_C_LEAVE
#endif

#if defined(COMPILER_IS_MSVC)
#   define AGORA_INLINE        __inline
#   define AGORA_CDECL         __cdecl
#   define AGORA_STDCALL       __stdcall
#   define AGORA_FASTCALL      __fastcall
#   define AGORA_THISCALL      __thiscall
#   define AGORA_ALIGNED(a)    __declspec(align(a))
#elif defined(COMPILER_IS_GCC)
#   define AGORA_INLINE        __inline__
#   define AGORA_ALIGNED(a)    __attribute__((aligned(a)))
#   if defined(__x86_64) \
    || defined(__amd64__) \
    || defined(__amd64) \
    || defined(_M_IA64) \
    || defined(_M_X64) \
    || defined(_M_IX64)
#       define AGORA_CDECL
#       define AGORA_STDCALL
#       define AGORA_FASTCALL
#       define AGORA_THISCALL
#   else
#       define AGORA_CDECL     __attribute__((__cdecl__))
#       define AGORA_STDCALL   __attribute__((__stdcall__))
#       define AGORA_FASTCALL  __attribute__((__fastcall__))
#       define AGORA_THISCALL  __attribute__((__thiscall__))
#   endif
#else
#   define AGORA_INLINE        inline
#   define AGORA_CDECL
#   define AGORA_STDCALL
#   define AGORA_FASTCALL
#   define AGORA_THISCALL
#   define AGORA_ALIGNED(a)
#endif

#define AGORA_CALL AGORA_CDECL


#if defined(COMPILER_IS_MSVC)
#   if defined(AGORA_EXPORT)
#       define AGORA_API EXTERN_C __declspec(dllexport)
#   else
#       define AGORA_API EXTERN_C __declspec(dllimport)
#   endif
#elif defined(COMPILER_IS_GCC) \
    && ((__GNUC__ >= 4) || (__GNUC__ == 3 && __GUNC_MINOR__ >= 3))
#   define AGORA_API EXTERN_C __attribute__((visibility("default")))
#else
#   define AGORA_API EXTERN_C
#endif


#endif