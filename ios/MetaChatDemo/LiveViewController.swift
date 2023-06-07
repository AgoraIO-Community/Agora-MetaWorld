//
//  LiveViewController.swift
//  MianBU
//
//  Created by ZYP on 2023/3/8.
//

import UIKit
import AgoraRtcKit
import Network


@available(iOS 12.0, *)
class LiveViewController: UIViewController {
    private var agoraKit: AgoraRtcEngineKit?
    var host = "127.0.0.1"
    var port: UInt16 = 1006
    var rtmToken: String?
    var uid: String? = "123"
    var connection: NWConnection!
    var test:Int32 = 1;
    override func viewDidLoad() {
        if #available(iOS 12.0, *) {
            connection = NWConnection(host: .init(host), port: NWEndpoint.Port(rawValue: port)!, using: .udp)
        } else {
            // Fallback on earlier versions
        }
        connection = NWConnection(host: .init(host), port: NWEndpoint.Port(rawValue: port)!, using: .udp)
        view.backgroundColor = .white
        setupAgoraKit()
        commonInit()
    }
    
    deinit {
        AgoraRtcEngineKit.destroy()
    }
    
    private func setupAgoraKit() {
        agoraKit = createEngine()
        
        agoraKit?.enableExtension(withVendor: "agora_video_filters_face_capture", extension: "face_capture", enabled: true)
//        guard let token = rtmToken, let uid = uid else {
//            fatalError()
//        }
        
        let token = "006695752b975654e44bea00137d084c71cIAD6LGmiLYkLSix1qywcB+UjBRHWW4TBM6XbdQ9dq923b2HTcgkAAAAAEAD3b9wDxf4jZAEA6AMhUyRk"
        let uid = "123456"
        
        agoraKit?.setExtensionPropertyWithVendor("agora_video_filters_face_capture",
                                                extension: "face_capture",
                                                key:"face_capture_options", value:"{\"activationInfo\":{\"faceCapAppId\":\"0efd4ee02dd488c7c30cedd37b9b9b15\",\"faceCapAppKey\":\"e40886b37528408fe33b14871c516ed1\",\"agoraAppId\":\"695752b975654e44bea00137d084c71c\",\"agoraRtmToken\":\"\(token)\",\"agoraUid\":\"\(uid)\"},\"enable\":1}")
        
        let test3 = self.agoraKit?.enableExtension(withVendor: "agora_video_filters_metakit", extension: "metakit", enabled: true)
//
        print("token:\(token) uid:\(uid)")
        let canvas0 = AgoraRtcVideoCanvas()
        canvas0.uid = 0
        canvas0.renderMode = .hidden
        canvas0.view = view
        agoraKit?.setupLocalVideo(canvas0)
        agoraKit?.startPreview()
        agoraKit?.setVideoFrameDelegate(self)
    }
    
    func createEngine() -> AgoraRtcEngineKit {
        let config = AgoraRtcEngineConfig()
        config.appId = "aab8b8f5a8cd4469a63042fcfafe7063"
        config.channelProfile = .liveBroadcasting
        config.areaCode = .global
        config.eventDelegate = self;
        
        let agoraKit = AgoraRtcEngineKit.sharedEngine(with: config, delegate: nil)
        
        agoraKit.setClientRole(.broadcaster)
        
        let videoConfig = AgoraVideoEncoderConfiguration(size: .init(width: 1280, height: 720),
                                                         frameRate: .fps15,
                                                         bitrate: 100,
                                                         orientationMode: .fixedPortrait,
                                                         mirrorMode: .auto)
        agoraKit.setVideoEncoderConfiguration(videoConfig)
        agoraKit.enableVideo()
        agoraKit.disableAudio()
        
        return agoraKit
    }
    
    func commonInit() {
        connection.stateUpdateHandler = { (newState) in
            print("This is stateUpdateHandler:")
            switch (newState) {
            case .ready:
                print("State: Ready\n")
            case .setup:
                print("State: Setup\n")
            case .cancelled:
                print("State: Cancelled\n")
            case .preparing:
                print("State: Preparing\n")
            default:
                print("ERROR! State not defined!\n")
            }
        }
        
        connection.start(queue: .global())
    }
    
    func sendUDP(_ msg:String){
        let contentToSend=msg.data(using: String.Encoding.utf8)
        connection.send(content: contentToSend, completion: NWConnection.SendCompletion.contentProcessed({(NWError) in
            if NWError==nil{
                print("Data was sent to UDP")
            }else{
                print("ERROR! Error when data (Type: Data) sending. NWError: \n \(NWError!)")
            }
        }))
    }
}

// MARK: - AgoraVideoDataFrameProtocol
@available(iOS 12.0, *)
extension LiveViewController: AgoraVideoFrameDelegate {
    func onCapture(_ videoFrame: AgoraOutputVideoFrame) -> Bool {
//        if let string = videoFrame.metaInfo["KEY_FACE_CAPTURE"] as? String {
//            sendUDP(string)
//        }
        print(videoFrame.metaInfo)
        return true
    }
}


@available(iOS 12.0, *)
extension LiveViewController: AgoraMediaFilterEventDelegate {
   
        
    func onEvent(_ provider: String?,  extension: String?, key: String?, value: String?) {
        
        if key == "initializeFinish" {
            let metakit = MetaKitEngine.sharedInstance()
            DispatchQueue.main.async{
                guard let viewTest = metakit?.createRenderView(CGRect(x: 0,y: 0,width: 300,height: 300), viewId: 1) else { return  }
                self.view.addSubview(viewTest)
            }
            
        } else if key == "unityLoadFinish" {
            
            agoraKit?.setExtensionPropertyWithVendor("agora_video_filters_metakit",
                                                    extension: "metakit",
                                                    key:"checkUpdate", value:"{\"appid\":\"4d4bf997732c4309911147503e91e338\",\"userid\":\"123456\",\"token\":\"0064d4bf997732c4309911147503e91e338IAD8YNxEcPA/puiqEr88bfUNTf71aoIUyzZEPyimbiTLKWHTcgkAAAAAEACe/1oDf/4jZAEA6APTUiRk\",\"os\":\"1\",\"gameid\":\"18\"}")
            
        } else if key == "cb_checkUpdate" {
            agoraKit?.setExtensionPropertyWithVendor("agora_video_filters_metakit",
                                                    extension: "metakit",
                                                    key:"loadScene", value:"{}")
        } else if key == "cb_loadScene" {
            agoraKit?.setExtensionPropertyWithVendor("agora_video_filters_metakit",
                                                    extension: "metakit",
                                                    key:"setAvatar", value:"{\"name\":\"girl\"}")
        } else if key == "cb_setAvatar" && value == "success" {
            agoraKit?.setExtensionPropertyWithVendor("agora_video_filters_metakit",
                                                    extension: "metakit",
                                                    key:"setCam", value:"{\"position\":[0.0,1.0,2.0],\"euler\":[0.0,180,0.0]}")
            agoraKit?.setExtensionPropertyWithVendor("agora_video_filters_metakit",
                                                    extension: "metakit",
                                                    key:"mirrorAvatar", value:"true")
            //[_shareEngine sendMsgToMetaKit:@"showFacePt" jsonMessage:@"face"];
        }
        
        print("onEvent");
        //reportSDKEvent("onEvent", info: ["provider":provider, "extension": `extension`, "key":key, "value":value])
    }

    func onExtensionStarted(_ provider: String?, extension: String?) {
        print("onExtensionStarted");
       
        //reportSDKEvent("onExtensionStarted", info: ["provider":provider, "extension":`extension`])
    }

    func onExtensionStopped(_ provider: String?, extension: String?) {
        print("onExtensionStopped");
        //reportSDKEvent("onExtensionStopped", info: ["provider":provider, "extension":`extension`])
    }

    func onExtensionError(_ provider: String, extension: String, error: Int, message: String) {
        print("onExtensionError");
            //reportSDKEvent("onExtensionError", info: ["provider":provider, "extension":`extension`, "error":error, "message":message])
    }

}
