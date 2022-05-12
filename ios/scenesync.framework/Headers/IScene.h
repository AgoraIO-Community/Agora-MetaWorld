#ifndef __ISCENE_H__
#define __ISCENE_H__

#include "prefix.h"
#include <cstdint>
#include <memory>

// native to scene
#define MSG_ID_BASE 1000
#define MSG_ID_LOAD_SCENE               14638
#define MSG_ID_UNLOAD_SCENE             22151
#define MSG_ID_LOAD_AVATAR              2939
#define MSG_ID_UNLOAD_AVATAR            39234
#define MSG_ID_TRANSFORM_INDICATION     10856
#define MSG_ID_STRUCTED_DATA_INDICATION 21308
#define MSG_ID_VIDEO_RENDERER           249
#define MSG_ID_SET_PROPERTY             31677
#define MSG_ID_CUSTOM                   18217

#define MSG_ID_LOCK                     24815
#define MSG_ID_UNLOCK                   36854
#define MSG_ID_SET_SCENE_LOCKS          6156
// unity to scene
#define MSG_ID_ENGINE_LOAD_STATUS       31937
#define MSG_ID_AVATAR_TRANSFORM         49266
#define MSG_ID_SCENE_LOAD_STATUS        20559
#define MSG_ID_AVATAR_LAOD_STATUS       59018
#define MSG_ID_PROPERTY_CHANGED         61172
#define MSG_ID_REQUEST_ROOM_PROPERTY    32788
#define MSG_ID_ERROR_NOTIFY             9963
#define MSG_ID_RECEIVED                 20860
#define MSG_ID_TRY_LOCK                 46765
#define MSG_ID_UN_LOCK                  52119
#define MSG_ID_REQUEST_LOCK             45794

      
//Agora.CustomMessage        18217 
//Agora.OnMessageReceived        20860

#define USE_STRING_UID

namespace agora {
namespace meta {

typedef float real;

#ifdef USE_STRING_UID
typedef char *uid_t;
#else
typedef std::uint32_t uid_t;
#endif

struct Vector3 {
  real pos[3];
};

struct Transform {
  Vector3 position;
  Vector3 axisForward;
  Vector3 axisRight;
  Vector3 axisUp;
};

#define MAX_PROPERTY_BUFFER 512
#define MAX_BUFFER 512

enum PROPERTY_TYPE {
  SCENE_PROPERTY,  // room property
  AVATAR_PROPERTY, // user property
};

/// Loading Loaded unLoading unLoaded error
enum LOAD_STATUS { UNKNOWN, LOADING, LOADED, UNLOADING, UNLOADED, LOAD_ERROR, UNLOAD_ERROR };

enum ERROR_CODE {
  ERR_OK,
  ERR_INPUT_PARAM,
  ERR_SEND_FAIL,
};

struct PropertyItem {
  uid_t uid;
  char key[MAX_PROPERTY_BUFFER];
  char value[MAX_PROPERTY_BUFFER];
};

struct AvatarInfo {
  uint32_t avatar_id;
  char avatar_path[MAX_BUFFER];
  char avatar_name[MAX_BUFFER];
};

struct SceneInfo {
  char scene_path[MAX_BUFFER];
  char scene_name[MAX_BUFFER];
  /// The json value of config file
  const char *config_json_value;
};

struct UserInfo {
  uid_t uid;
  char user_name[MAX_BUFFER];
  char user_icon_url[MAX_BUFFER];
};

struct VideoFrame {
  int width;
  int height;
  uint8_t *rgba_buffer;
};

enum MessageType {
  CUSTOM_MESSAGE,
  PARAMETER_MESSAGE,
};

struct RoomLockInfo {
  const char *ownerId;
  const char *key;
  const char *value;
  int64_t revision;
  int64_t updateTime;
  int64_t timeToLive;
};

class ISceneEventHandler {
public:
  virtual void onEngineLoadStatus(LOAD_STATUS status,
                                  const char *status_message) = 0;
  virtual void onAvatarEncodedData(const uint8_t *encoded_data,
                                   uint32_t size) = 0;
  virtual void onAvatarTransform(uid_t uid, const Transform &transform) = 0;
  virtual void onSceneLoadStatus(LOAD_STATUS status,
                                 const char *status_message) = 0;
  virtual void onAvatarLoadStatus(uid_t uid, LOAD_STATUS status,
                                  const char *status_message) = 0;
  virtual void onPropertyChanged(PROPERTY_TYPE type,
                                 const PropertyItem &item) = 0;
  virtual void onRequestProperty(PROPERTY_TYPE type, const char *key) = 0;
  virtual void onError(int error_code, const char *error_message) = 0;
  virtual void onMessageReceived(MessageType message_type,
                                 const uint8_t *message, size_t size) = 0;
  virtual void onTryLock(const char *key, const char *value, int64_t lockTimeMs) = 0;
  virtual void onUnLock(const char *key) = 0;
  virtual void onRequestLock() = 0;
};

class IScene {
public:
  static IScene *Instance();

  virtual ~IScene() = default;

  // initialize native bridge
  virtual int initialize() = 0;

  // listen callback from unity
  virtual int setEventHandler(ISceneEventHandler *event_handler) = 0;

  virtual ISceneEventHandler* eventHandler() = 0;

  // need listen onEngineLoadStatus LOADED status, then you can loadScene
  virtual int loadScene(const SceneInfo &scene_info,
                        const AvatarInfo *avatar_info, int avatar_count,
                        const UserInfo &local_user_info) = 0;

  // unload the scene which you load by api named loadScene
  virtual int unloadScene() = 0;

  // need listen onSceneLoadStatus LOADED status, then you can call this api to
  // generate a avatar in the scene. you can set your user_name and user_icon
  // upon the avatar by setting the user_info must have avatar_id and uid
  virtual int loadAvatar(const AvatarInfo &avatar_info,
                         const UserInfo &user_info) = 0;

  // unload the avatar which you load by api named loadAvatar
  virtual int unloadAvatar(const UserInfo &user_info) = 0;

  /// update Avatar status, animator and position.
  virtual int updateAvatarEncodedData(const uint8_t *encoded_data,
                                      uint32_t size) = 0;

  /// onAvatarTransform
  virtual int enableAvatarTransformIndication(bool enable) = 0;

  /// onAvatarEncodedData
  virtual int enableAvatarEncodedDataIndication(bool enable) = 0;

  virtual int enableVideoRenderer(uint32_t displayId, bool enable) = 0;

  virtual int pushVideoFrameToDisplay(uint32_t displayId,
                                      const VideoFrame *frame) = 0;

  /// onPropertyChanged
  virtual int setProperty(PROPERTY_TYPE type, const PropertyItem &item) = 0;

  virtual int release() = 0;

  /// Send custom message or set parameters （onMessageReceived）
  virtual int sendMessage(MessageType message_type, const uint8_t *message,
                           size_t size) = 0;
  virtual int lock(uid_t uid, const char *key, const char *value) = 0;
  virtual int unlock(uid_t uid, const char *key) = 0;
  virtual int setSceneLocks(const RoomLockInfo *locks, int locksCount) = 0;
};

/// int quality; 质量
/// int fps; 帧率
/// int anti_aliasing; 抗锯齿(0、2、4、8)
/// float resolution[2]; 分辨率
/// bool show_user_icon; 显示用户头像
/// bool show_user_name; 显示用户名
class ISceneParameter {
public:
  /**
   * release the resource
   */
  virtual void release() = 0;

  /**
   * set bool value of the json
   * @param [in] key
   *        the key name
   * @param [in] value
   *        the value
   * @return return 0 if success or an error code
   */
  virtual int setBool(const char *key, bool value) = 0;

  /**
   * set int value of the json
   * @param [in] key
   *        the key name
   * @param [in] value
   *        the value
   * @return return 0 if success or an error code
   */
  virtual int setInt(const char *key, int value) = 0;

  /**
   * set unsigned int value of the json
   * @param [in] key
   *        the key name
   * @param [in] value
   *        the value
   * @return return 0 if success or an error code
   */
  virtual int setUInt(const char *key, unsigned int value) = 0;

  /**
   * set double value of the json
   * @param [in] key
   *        the key name
   * @param [in] value
   *        the value
   * @return return 0 if success or an error code
   */
  virtual int setNumber(const char *key, double value) = 0;

  /**
   * set string value of the json
   * @param [in] key
   *        the key name
   * @param [in] value
   *        the value
   * @return return 0 if success or an error code
   */
  virtual int setString(const char *key, const char *value) = 0;

  /**
   * set object value of the json
   * @param [in] key
   *        the key name
   * @param [in] value
   *        the value
   * @return return 0 if success or an error code
   */
  virtual int setObject(const char *key, const char *value) = 0;

  /**
   * set array value of the json
   * @param [in] key
   *        the key name
   * @param [in] value
   *        the value
   * @return return 0 if success or an error code
   */
  virtual int setArray(const char *key, const char *value) = 0;
  /**
   * get bool value of the json
   * @param [in] key
   *        the key name
   * @param [in, out] value
   *        the value
   * @return return 0 if success or an error code
   */
  virtual int getBool(const char *key, bool &value) = 0;

  /**
   * get int value of the json
   * @param [in] key
   *        the key name
   * @param [in, out] value
   *        the value
   * @return return 0 if success or an error code
   */
  virtual int getInt(const char *key, int &value) = 0;

  /**
   * get unsigned int value of the json
   * @param [in] key
   *        the key name
   * @param [in, out] value
   *        the value
   * @return return 0 if success or an error code
   */
  virtual int getUInt(const char *key, unsigned int &value) = 0;

  /**
   * get double value of the json
   * @param [in] key
   *        the key name
   * @param [in, out] value
   *        the value
   * @return return 0 if success or an error code
   */
  virtual int getNumber(const char *key, double &value) = 0;

  //  /**
  //   * get string value of the json
  //   * @param [in] key
  //   *        the key name
  //   * @param [in, out] value
  //   *        the value
  //   * @return return 0 if success or an error code
  //   */
  //  virtual int getString(const char* key, agora::util::AString& value) = 0;
  //
  //  /**
  //   * get a child object value of the json
  //   * @param [in] key
  //   *        the key name
  //   * @param [in, out] value
  //   *        the value
  //   * @return return 0 if success or an error code
  //   */
  //  virtual int getObject(const char* key, agora::util::AString& value) = 0;
  //
  //  /**
  //   * get array value of the json
  //   * @param [in] key
  //   *        the key name
  //   * @param [in, out] value
  //   *        the value
  //   * @return return 0 if success or an error code
  //   */
  //  virtual int getArray(const char* key, const char* args,
  //  agora::util::AString& value) = 0;

  /**
   * set parameters of the sdk or engine
   * @param [in] parameters
   *        the parameters
   * @return return 0 if success or an error code
   */
  virtual int setParameters(const char *parameters) = 0;

  virtual ~ISceneParameter() {}
};

} // namespace meta
} // namespace agora


AGORA_API agora::meta::IScene* AGORA_CALL createSceneInstance();

AGORA_API int AGORA_CALL IScene_initialize(agora::meta::IScene* scene);

  // listen callback from unity
AGORA_API int AGORA_CALL IScene_setEventHandler(agora::meta::IScene* scene, agora::meta::ISceneEventHandler *event_handler);

AGORA_API agora::meta::ISceneEventHandler* AGORA_CALL IScene_eventHandler(agora::meta::IScene* scene);

  // need listen onEngineLoadStatus LOADED status, then you can loadScene
AGORA_API int AGORA_CALL IScene_loadScene(agora::meta::IScene* scene, const agora::meta::SceneInfo &scene_info,
                        const agora::meta::AvatarInfo *avatar_info, int avatar_count,
                        const agora::meta::UserInfo &local_user_info);

  // unload the scene which you load by api named loadScene
AGORA_API int AGORA_CALL IScene_unloadScene(agora::meta::IScene* scene);

  // need listen onSceneLoadStatus LOADED status, then you can call this api to
  // generate a avatar in the scene. you can set your user_name and user_icon
  // upon the avatar by setting the user_info must have avatar_id and uid
AGORA_API int AGORA_CALL IScene_loadAvatar(agora::meta::IScene* scene, const agora::meta::AvatarInfo &avatar_info,
                         const agora::meta::UserInfo &user_info);

  // unload the avatar which you load by api named loadAvatar
AGORA_API int AGORA_CALL IScene_unloadAvatar(agora::meta::IScene* scene, const agora::meta::UserInfo &user_info);

  /// update Avatar status, animator and position.
AGORA_API int AGORA_CALL IScene_updateAvatarEncodedData(agora::meta::IScene* scene, const uint8_t *encoded_data,
                                      uint32_t size);

  /// onAvatarTransform
AGORA_API int AGORA_CALL IScene_enableAvatarTransformIndication(agora::meta::IScene* scene, bool enable);

  /// onAvatarEncodedData
AGORA_API int AGORA_CALL IScene_enableAvatarEncodedDataIndication(agora::meta::IScene* scene, bool enable);

AGORA_API int AGORA_CALL IScene_enableVideoRenderer(agora::meta::IScene* scene, uint32_t displayId, bool enable);

AGORA_API int AGORA_CALL IScene_pushVideoFrameToDisplay(agora::meta::IScene* scene, uint32_t displayId,
                                      const agora::meta::VideoFrame *frame);

  /// onPropertyChanged
AGORA_API int AGORA_CALL IScene_setProperty(agora::meta::IScene* scene, agora::meta::PROPERTY_TYPE type, const agora::meta::PropertyItem &item);

AGORA_API int AGORA_CALL IScene_release(agora::meta::IScene* scene);

  /// Send custom message or set parameters （onMessageReceived）
AGORA_API int AGORA_CALL IScene_sendMessage(agora::meta::IScene* scene, agora::meta::MessageType message_type, const uint8_t *message,
                           size_t size);

AGORA_API int AGORA_CALL IScene_lock(agora::meta::IScene* scene, agora::meta::uid_t uid, const char *key, const char *value);
AGORA_API int AGORA_CALL IScene_unlock(agora::meta::IScene* scene, agora::meta::uid_t uid, const char *key);
AGORA_API int AGORA_CALL IScene_setSceneLocks(agora::meta::IScene* scene, const agora::meta::RoomLockInfo *locks, int locksCount);


#endif