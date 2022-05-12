#ifndef __RENDER_BASE_H__
#define __RENDER_BASE_H__

#include "prefix.h"
#include <cstdint>

typedef std::uint32_t GLTexureID;

struct ImageFrame {
  std::uint32_t width;     // 图片宽度
  std::uint32_t height;    // 图片高度
  std::uint32_t frameType; // 图片类型
  std::uint32_t dataSize;  // 数据大小
  std::uint8_t *data;      // 数据指针
};

enum ERR_TYPE {
  ERR_OK = 0,
  ERR_NULLPOINTER = -1,        // 空指针错误
  ERR_NOTFOUND_TEXTUREID = -2, // 没有发现纹理
  ERR_TEXTURESIZE_CHANGE = -3, // 纹理大小发生了变化
  ERR_CACHE_ISFULL = -4,       // 缓存队列已满
  ERR_CACHE_ISEMPTY = -5,      // 缓存队列为空
};

namespace agora_meta {

struct IRenderBase {
  virtual GLTexureID genTextureID(const char *key, int width, int height) = 0;
  virtual int renderPicture(const char *key, ImageFrame *frame) = 0;
  virtual void clearTextures() = 0;
};

struct CacheManager {
  virtual int pushQueue(const char *key, ImageFrame *frame) = 0;
  virtual int popQueue(const char *key, ImageFrame *&frame) = 0;
  virtual int getQueueSize(const char *key) = 0;
};

} // namespace agora_meta

// 基于业务功能的接口
/**
 * @brief 推送视频帧
 *
 * @param key 标识唯一的一路流
 * @param frame 图像数据
 */
AGORA_API void AGORA_CALL CacheManager_pushVideoFrame(const char *key,
                                                      ImageFrame *frame);

/**
 * @brief 是否允许缓存视频数据
 *
 * @param key 标识唯一的一路流
 * @param enable 为true表示允许缓存，为false则不允许缓存
 */
AGORA_API void AGORA_CALL CacheManager_enableVideoFrameCache(const char *key,
                                                             bool enable);

/**
 * @brief 生成纹理图片
 *
 * @param key 标识唯一的一路流
 * @param width 纹理宽度
 * @param height 纹理高度
 */
AGORA_API GLTexureID AGORA_CALL Render_genTextureID(const char *key, int width,
                                                    int height);

/**
 * @brief 渲染视频
 *
 * @param key 标识唯一的一路流
 * @param width 视频宽度，如果输入为0，则输出真实的视频宽度
 * @param height 视频高度，如果输入为0，则输出真实的视频高度
 * @return 为0表示成功，否则失败
 */
AGORA_API int AGORA_CALL Render_renderVideoFrame(const char *key, int &width,
                                                 int &height);

#endif
