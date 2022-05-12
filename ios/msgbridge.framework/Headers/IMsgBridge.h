#ifndef __BRIDGE_BASE_H__
#define __BRIDGE_BASE_H__

#include "prefix.h"
#include "protocol.h"
#include <string>

namespace agora_meta {

#define MAX_BUFFER_LEN 1024

enum ModuleType {
  Module_Unknown = 0,
  Module_Native = 1,
  Module_Unity = 2,
  Module_Sync = 3,
};

typedef struct IMessageHandlerVtbl {
  /**
   * @brief 所有的消息都通过这个接口回调
   *
   * @param head 消息头部
   * @param body 消息体
   */
  void(AGORA_CALL *OnMessage)(MessageHead *head, void *body);
  /**
   * @brief 错误消息回调
   *
   * @param err 错误代码
   * @param msg 错误消息
   */
  void(AGORA_CALL *OnError)(std::uint16_t module, char msg[MAX_BUFFER_LEN]);
} IMessageHandlerVtbl;

struct IMsgBridge {
public:
  /**
   * @brief 注册消息通知对象
   *
   * @param from 是那个模块在注册，标明使用的模块
   * @param observer 回调处理对象
   */
  virtual void registerMessageObserver(ModuleType from,
                                       IMessageHandlerVtbl *observer) = 0;

  /**
   * @brief 注销消息通知对象
   *
   * @param from 是那个模块在注销
   * @param observer 回调处理对象
   */
  virtual void unregisterMessageObserver(ModuleType from,
                                         IMessageHandlerVtbl *observer) = 0;

  /**
   * @brief 发送消息
   *
   * @param to 发送到那个模块
   * @param msgType 消息类型，自行定义
   * @param data 数据缓存指针
   * @param dataSize 数据大小
   * @return int 返回0表示成功，否则为失败
   */
  virtual int sendMessage(ModuleType from, ModuleType to, std::uint16_t version,
                          std::uint16_t msgType, void *data,
                          std::uint32_t dataSize) = 0;

  /**
   * @brief 获取已经注册的观察者数量
   *
   * @param module 模块类型
   * @return int 返回已经注册的观察者数量
   */
  virtual int getModuleObserverCount(ModuleType module) = 0;
};

} // namespace agora_meta

// 返回bridge的实例对象，不同模块调用之后返回的都是同一个指针
AGORA_API agora_meta::IMsgBridge *AGORA_CALL createMsgBridgeInstance();

/**
 * @brief 注册消息通知对象的c接口
 *
 * @param bridge 调用createMsgBridgeInstance生成的bridge
 * @param from 当前属于那个模块
 * @param observer 回调处理对象
 */
AGORA_API void AGORA_CALL MsgBridge_RegisterMessageObserver(
    agora_meta::IMsgBridge *bridge, agora_meta::ModuleType from,
    agora_meta::IMessageHandlerVtbl *observer);

/**
 * @brief 注销消息通知对象的c接口
 *
 * @param bridge 调用createMsgBridgeInstance生成的bridge
 * @param from 当前属于那个模块
 * @param observer 回调处理对象
 */
AGORA_API void AGORA_CALL MsgBridge_UnregisterMessageObserver(
    agora_meta::IMsgBridge *bridge, agora_meta::ModuleType from,
    agora_meta::IMessageHandlerVtbl *observer);

/**
 * @brief 模块间发送消息的c接口
 *
 * @param bridge 调用createMsgBridgeInstance生成的bridge
 * @param to 消息即将发送到那个模块
 * @param msgType 消息类型，自行定义
 * @param data 数据缓存指针
 * @param dataSize 数据大小
 * @return int 返回0表示成功，否则为失败
 */
AGORA_API int AGORA_CALL MsgBridge_SendMessage(agora_meta::IMsgBridge *bridge,
                                               agora_meta::ModuleType from,
                                               agora_meta::ModuleType to,
                                               std::uint16_t msgType,
                                               void *data,
                                               std::uint32_t dataSize);

AGORA_API int AGORA_CALL MsgBridge_SendMessageWithHeader(
    agora_meta::IMsgBridge *bridge, MessageHead *head, void *data);

AGORA_API int AGORA_CALL MsgBridge_getModuleObserverCount(
    agora_meta::IMsgBridge *bridge, agora_meta::ModuleType module);

#endif
