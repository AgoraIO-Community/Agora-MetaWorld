//
//  MCRoomManager.swift
//  MetaChatDemo
//
//  Created by FanPengpeng on 2022/8/17.
//

import UIKit
import MJExtension
import HyphenateChat

private let kRegistered = "registered"
private let kJoinedGroupId = "kJoinedGroupId"
private let kMaxUserCount = 16

enum MCRoomRole: Int {
    case member
    case master
}

@objc enum MCRoomLeaveReason: Int {
    case beRemoved = 0
    case userLeave = 1
    case destroyed = 2
}

@objc enum MCJoinRoomErrorReason: Int {
    case notExit = 600 // 房间不存在
    case fullMember = 604 // 房间人数已满
    case unknown = 1000 // 未知
}

class MCRoom:NSObject {
    @objc var objectId:String?
    @objc var name:String?
    @objc var memCount:NSNumber?
    @objc var img: String?
    @objc var pwd: String?
    @objc var master: String?
}

@objc protocol MCRoomManagerDelegate: NSObjectProtocol {
    @objc optional
    func roomListDidUpdate(_ roomList: [String]?)
    @objc optional
    func didLeave(_ roomId: String, reason aReason: MCRoomLeaveReason)
}

class MCRoomManagerDelegateContainer: NSProxy {
    weak var delegate: MCRoomManagerDelegate?
}

class MCRoomManager: NSObject {
    
    @objc static let shared = MCRoomManager()
    
    
    private var currentGroup:EMGroup? {
        didSet{
            currentRoom = self.roomWithEMGroup(currentGroup)
            UserDefaults.standard.set(currentGroup?.groupId, forKey: kJoinedGroupId)
            UserDefaults.standard.synchronize()
        }
    }
    
    private (set) var currentRoom: MCRoom?
    
    @objc private var delegates = [MCRoomManagerDelegateContainer]()
    
    private func initManager(success:(()->())? = nil){
        
        
    }
    
    @objc func createRoom(name:String,img:String, pwd:String? = nil, success:((_ roomId:String)->())? = nil, fail:(()->())? = nil) {
        // 创建环信群组
        let options = EMGroupOptions()
        options.maxUsers = kMaxUserCount
        options.isInviteNeedConfirm = false
        options.style = .publicOpenJoin
        let dic = [
            "img":img,
            "pwd": pwd ?? ""
        ] as NSDictionary
        options.ext = dic.mj_JSONString()
        EMClient.shared().groupManager?.createGroup(withSubject: name, description: nil, invitees: nil, message: nil, setting: options, completion: {[weak self] group, error in
            self?.currentGroup = group
            if error == nil , group != nil {
#if USE_AgoraSyncManager
        let scene = Scene(id: group!.groupId, userId: KeyCenter.RTM_UID, property: ["name":name,"pwd":pwd ?? "","img":img,"master": KeyCenter.RTM_UID])
        self?.syncManager.createScene(scene: scene, success: { [weak self] in
            self?.syncManager.joinScene(sceneId: group!.groupId, success: { syncRef in
                self?.syncRef = syncRef
                self?.registerObserver()
                DLog("createRoom success")
                DispatchQueue.main.async {
                    success?(group!.groupId)
                }
            }, fail: { error in
                DispatchQueue.main.async {
                    fail?()
                }
            })
        }, fail: { error in
            DispatchQueue.main.async {
                fail?()
            }
        })
#else
                DispatchQueue.main.async {
                    success?(group!.groupId)
                }
#endif
            }else{
                print("创建群组失败 error = \(error!.code.rawValue)")
                DispatchQueue.main.async {
                    fail?()
                }
            }
        })
    }
    
    @objc func joinRoom(id: String, success:(()->())? = nil,fail:((_ reason:MCJoinRoomErrorReason)->())? = nil) {
        EMClient.shared().groupManager?.joinPublicGroup(id, completion: {[weak self] group, error in
            if error == nil || error?.code == EMErrorCode.groupAlreadyJoined {
                self?.currentGroup = group
#if USE_AgoraSyncManager
                self?.syncManager.joinScene(sceneId: id) { syncRef in
                    self?.syncRef = syncRef
                    self?.registerObserver()
                    DLog("joinScene success \(syncRef.id)")
                    DispatchQueue.main.async {
                        success?()
                    }
                } fail: { error in
                    DLog("joinScene fail")
                    DispatchQueue.main.async {
                        fail?(error.code)
                    }
                }
#else
                DispatchQueue.main.async {
                    success?()
                }
#endif
            }else{
                print("环信加入群组失败 error = \(error!.code.rawValue)")
                DispatchQueue.main.async {
                    var reason = MCJoinRoomErrorReason.unknown
                    if error!.code == EMErrorCode.groupMembersFull {
                        reason = .fullMember
                    }else if error!.code == EMErrorCode.groupInvalidId {
                        reason = .notExit
                    }else{
                        reason = .unknown
                    }
                    fail?(reason)
                }
            }
        })
    }
    
    @objc func leaveRoom(_ id:String? = nil) {
        if self.currentGroup?.owner == KeyCenter.RTM_UID {
            self.deleteRoom()
            return
        }
        guard let roomId = id ?? self.currentGroup?.groupId else { return  }
        EMClient.shared().groupManager?.leaveGroup(roomId, completion: { error in
            self.currentGroup = nil
            DLog("-------离开了房间------\(roomId)")
        })
    }
    
    @objc func deleteRoom(_ id:String? = nil) {
        guard let roomId = id ?? self.currentGroup?.groupId else { return  }
        EMClient.shared().groupManager?.destroyGroup(roomId, finishCompletion: { error in
            self.currentGroup = nil
            DLog("-------销毁了房间------\(roomId)")
        })
#if USE_AgoraSyncManager
        self.syncRef.delete { objs in
            print("deleteRoom success")
        } fail: { error in
            print("deleteRoom error = \(error.message) code = \(error.code)")
        }
#endif
    }
    
    @objc func updateRoom(name:String) {
        guard let group = currentGroup else { return }
        EMClient.shared().groupManager?.updateGroupSubject(name, forGroup: group.groupId)
    }
    
    @objc func updateRoom(pwd:String) {
        guard let room = currentRoom else { return }
        let dic = [
            "img":room.img ?? "",
            "pwd": pwd
        ] as NSDictionary
        let ext = dic.mj_JSONString()
        EMClient.shared().groupManager?.updateGroupExt(withId: room.objectId ?? "", ext: ext)
    }
    
    private func roomWithEMGroup(_ group: EMGroup?) -> MCRoom? {
        guard let group = group else {
            return nil
        }
        let room = MCRoom()
        room.objectId = group.groupId
        let groupDetail = EMClient.shared().groupManager?.getGroupSpecificationFromServer(withId: group.groupId, error: nil)
        room.name = groupDetail?.groupName
        if let settings = groupDetail?.settings, let ext = settings.ext as? NSString, let dic = ext.mj_JSONObject() as? NSDictionary {
            room.pwd = dic["pwd"] as? String
            room.img = dic["img"] as? String
        }
        room.master = groupDetail?.owner
        room.memCount = (group.occupantsCount) as NSNumber
        return room
    }
    
    private func registerObserver(){
        
    }
    
    @objc func leaveAllJoinedGroups(completion:(()->())?){
        DispatchQueue.global().async {
            guard let joinedGroupId = UserDefaults.standard.value(forKey: kJoinedGroupId) as? String else {
                DLog("没有已加入的房间")
                DispatchQueue.main.async {
                    completion?()
                }
                return
            }
            if let groupDetail = EMClient.shared().groupManager?.getGroupSpecificationFromServer(withId: joinedGroupId, error: nil) {
                if groupDetail.owner == KeyCenter.RTM_UID {
                    EMClient.shared().groupManager?.destroyGroup(joinedGroupId)
                    DLog("销毁了房间----\(joinedGroupId)")
                }else{
                    EMClient.shared().groupManager?.leaveGroup(joinedGroupId)
                    DLog("离开了房间----\(joinedGroupId)")
                }
                DispatchQueue.main.async {
                    completion?()
                }
            }
        }
    }
}


extension MCRoomManager {
    @objc func getRooms(result: ((_ list:Array<MCRoom>)->())?) {
        
#if USE_AgoraSyncManager
        initManager {
            self.syncManager.getScenes { (objs) in
                DLog("success")
                let strs = objs.compactMap({ $0.toJson() })
                let list = MCRoom.mj_objectArray(withKeyValuesArray: strs)
                result?(list as! Array<MCRoom>)
                DLog(strs)
            } fail: { (error) in
                DLog("fail: " + error.description)
            }
        }
#else
        EMClient.shared().groupManager?.getPublicGroupsFromServer(withCursor: nil, pageSize: 100, completion: { ret, error in
            guard let groups = ret?.list else {
                DispatchQueue.main.async {
                    result?([])
                }
                return
            }
            DispatchQueue.global().async { [weak self] in
                var list = [MCRoom]()
                for group in groups {
                    if let room = self?.roomWithEMGroup(group) {
                        list.append(room)
                    }
                }
                DispatchQueue.main.async {
                    result?(list)
                }
            }
        })
#endif
    }
}


extension MCRoomManager {
    
    private func addObserverEM(){
#if USE_AgoraSyncManager
#else
        EMClient.shared().groupManager?.add(self, delegateQueue: nil)
#endif
    }
    
    @objc func initialSDK(){
        // 初始化环信
        let options = EMOptions(appkey: KeyCenter.kEM_APPKEY);
        
        if let err = EMClient.shared().initializeSDK(with: options) {
            DLog("error == \(err.code.rawValue)")
        }
    }
    
    @objc func registerEM(success:(()->())? = nil) {
        // 注册环信
        EMClient.shared().register(withUsername: KeyCenter.kEM_UserName, password: KeyCenter.kEM_Password) {[weak self] name, error in
            if (error == nil || error?.code == EMErrorCode.userAlreadyExist){
                DLog("注册成功")
                UserDefaults.standard.setValue(true, forKey: kRegistered)
                UserDefaults.standard.synchronize()
                self?.loginEM(success: success)
            }else{
                DLog("name == \(name), error = \(error!.code.rawValue)")
                // 网络失败 重试
                if error!.code == .networkUnavailable {
                    DispatchQueue.main.asyncAfter(deadline: .now() + 2) {
                        self?.registerEM(success: success)
                    }
                }
            }
        }
    }
    
    @objc func loginEM(success:(()->())? = nil){
        // 如果没有注册，先注册
        guard let isRegistered = UserDefaults.standard.value(forKey: kRegistered) as? Bool, isRegistered == true else {
            registerEM(success: success)
            return
        }
        // 环信登录
        EMClient.shared().login(withUsername: KeyCenter.kEM_UserName, password: KeyCenter.kEM_Password) {[weak self] name, error in
            if (error != nil){
                DLog("name == \(name), error = \(error!.code.rawValue)")
                // code == 200是已经登录
                if error?.code == EMErrorCode.userAlreadyLoginSame {
                    success?()
                    self?.addObserverEM()
                }
            }else{
                DLog("登录成功")
                success?()
                self?.addObserverEM()
            }
        }
    }
}

extension MCRoomManager {
    @objc func addDelegate(_ delegate: MCRoomManagerDelegate) {
        let container = MCRoomManagerDelegateContainer.alloc()
        container.delegate = delegate
        delegates.append(container)
    }
    
    @objc func remoteDelegate(_ delegate: MCRoomManagerDelegate) {
        delegates.removeAll { target in
            let obj1 = target.delegate as? NSObject
            let obj2 = delegate as? NSObject
            return obj1?.isEqual(obj2) ?? false
        }
    }
}

extension MCRoomManager: EMGroupManagerDelegate {
    func groupListDidUpdate(_ aGroupList: [EMGroup]) {
        var roomIds = [String]()
        for list in aGroupList {
            roomIds.append(list.groupId)
        }
        for delegate in delegates {
            delegate.delegate?.roomListDidUpdate?(roomIds)
        }
    }
    
    func didLeave(_ aGroup: EMGroup, reason aReason: EMGroupLeaveReason) {
        for delegate in delegates {
            delegate.delegate?.didLeave?(aGroup.groupId, reason: MCRoomLeaveReason(rawValue: aReason.rawValue) ?? .userLeave)
        }
    }
}
