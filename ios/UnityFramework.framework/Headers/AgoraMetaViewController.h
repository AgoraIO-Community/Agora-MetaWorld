#import <UIKit/UIKit.h>

#include <UnityFramework/UnityFramework.h>

__attribute__ ((visibility("default")))
@interface AgoraMetaViewController : UIViewController<UnityFrameworkListener, UINavigationControllerDelegate>

@property UnityFramework* ufw;
@property bool didQuit;

- (bool)unityIsInitialized;

+ (UnityFramework*)showUnity;

- (void)initUnity;

- (void)unloadUnity;

- (void)quitUnity;

- (void)unityDidLoaded:(UIView*)view;

- (void)unityDidUnload;

- (void)unityDidQuit;

@end
