//
//  MetaSceneView.swift
//  MetaDemo
//
//  Created by ZhouRui on 2023/8/21.
//

import Foundation
import UIKit

enum MetaSceneEvent: String {
    case addSceneView         = "addSceneView"
    case removeSceneView      = "removeSceneView"
    case leaveScene           = "leaveScene"
    case changeDress          = "changeDress"
    case pitchFace            = "pitchFace"
    case changeViewMode       = "changeViewMode"
}

protocol MetaSceneEventDelegate: NSObjectProtocol {
    func onMetaSceneEvent(event: MetaSceneEvent)
}

class MetaSceneView: UIView {
    @IBOutlet weak var avatarBackV: UIView!
    @IBOutlet weak var avatarImageV: UIImageView!
    @IBOutlet weak var nameL: UILabel!
    @IBOutlet weak var modeL: UILabel!
    @IBOutlet weak var visitorIcon: UIImageView!
    @IBOutlet weak var openMicB: UIButton!
    
    @IBOutlet weak var userListB: UIButton!
    
    @IBOutlet weak var userMicB: UIButton!
    
    @IBOutlet weak var userSpeakerB: UIButton!
    
    @IBOutlet weak var userListTableV: UITableView!
    
    @IBOutlet weak var visitorTipBack: UIView!
    
    @IBOutlet weak var tip1Label: UILabel!
    
    @IBOutlet weak var tip2Label: UILabel!
    
    @IBOutlet weak var tip3Label: UILabel!
    
    @IBOutlet weak var exitButton: UIButton!
    
    @IBOutlet weak var songListButton: UIButton!
    
    @IBOutlet weak var visitorTextView: UITextView!
        
    @IBOutlet weak var cancelBtn: UIButton!
    
    @IBOutlet weak var storeBtn: UIButton!
        
    @IBOutlet weak var chatTextField: UITextField!
    
    @IBOutlet weak var chatSendBtn: UIButton!
    
    @IBOutlet weak var shRemoteViewBtn: UIButton!
    @IBOutlet weak var shLocalViewBtn: UIButton!
    
    @IBOutlet weak var dressBtn: UIButton!
    @IBOutlet weak var faceBtn: UIButton!
    @IBOutlet weak var viewBtn: UIButton!
    public var dressView: DressView?
    
    // video collection view
    var remoteVideoCV: VideoCollectionView!
    var localVideoCV: VideoCollectionView!
    
    var avatarCV: VideoCollectionView!
    private var canAddSceneView = true
    private var canRemoveSceneView = true
    
    var previewView: UIView!
    
    public weak var delegate: MetaSceneEventDelegate?

    override class func awakeFromNib() {
        super.awakeFromNib()
    }
    
    /// 设置主界面UI
    func setupUI() {
        openMicB.setImage(UIImage.init(named: "onbtn"), for: .normal)
        openMicB.setImage(UIImage.init(named: "offbtn"), for: .selected)
        userMicB.setImage(UIImage.init(named: "microphone-on"), for: .normal)
        userMicB.setImage(UIImage.init(named: "microphone-off"), for: .selected)
        
        userSpeakerB.setImage(UIImage.init(named: "voice-on"), for: .normal)
        userSpeakerB.setImage(UIImage.init(named: "voice-off"), for: .selected)
        
        nameL.text = MetaServiceEngine.sharedEngine.userName
        modeL.text = "游客模式"
        
        visitorTipBack.layer.cornerRadius = 10.0
        visitorTipBack.layer.borderWidth = 4.0
        visitorTipBack.layer.borderColor = UIColor.init(red: 63.0/255.0, green: 69.0/255.0, blue: 83.0/255.0, alpha: 1.0).cgColor;
        
        tip1Label.layer.cornerRadius = 6.0
        tip1Label.layer.borderWidth = 1.0
        tip1Label.layer.borderColor = UIColor.init(red: 0xc3/255.0, green: 0xc0/255.0, blue: 0xc0/255.0, alpha: 1.0).cgColor;
        
        tip2Label.layer.cornerRadius = 6.0
        tip2Label.layer.borderWidth = 1.0
        tip2Label.layer.borderColor = UIColor.init(red: 0xc3/255.0, green: 0xc0/255.0, blue: 0xc0/255.0, alpha: 1.0).cgColor;
        
        tip3Label.layer.cornerRadius = 6.0
        tip3Label.layer.borderWidth = 1.0
        tip3Label.layer.borderColor = UIColor.init(red: 0xc3/255.0, green: 0xc0/255.0, blue: 0xc0/255.0, alpha: 1.0).cgColor;
        
        if let url = Bundle.main.url(forResource: "Novice guide", withExtension: "rtfd") {
            visitorTextView.attributedText = try? NSAttributedString(url: url, documentAttributes: nil)
        }
        
        setupDressView()
    }
    
    /// 显示chat UI
    func showChatUI() {
        avatarBackV.isHidden = false
        openMicB.isHidden = false
        userListB.isHidden = true
        userMicB.isHidden = true
        userSpeakerB.isHidden = false
        visitorTipBack.isHidden = true
        exitButton.isHidden = false
        cancelBtn.isHidden = true
        storeBtn.isHidden = true
        shLocalViewBtn.isHidden = false
        shRemoteViewBtn.isHidden = false
        dressBtn.isHidden = true
        faceBtn.isHidden = true
        dressView?.isHidden = true
    }
    
    /// 显示live UI
    func showLiveUI() {
        avatarBackV.isHidden = true
        openMicB.isHidden = true
        userListB.isHidden = true
        userMicB.isHidden = true
        userSpeakerB.isHidden = true
        visitorTipBack.isHidden = true
        exitButton.isHidden = true
        cancelBtn.isHidden = false
        storeBtn.isHidden = false
        shLocalViewBtn.isHidden = true
        shRemoteViewBtn.isHidden = true
        dressBtn.isHidden = false
        faceBtn.isHidden = false
        dressView?.isHidden = false
        viewBtn.isHidden = false
    }
    
    /// 显示chat room UI
    func showChatRoomUI() {
        avatarBackV.isHidden = true
        openMicB.isHidden = true
        userListB.isHidden = true
        userMicB.isHidden = true
        userSpeakerB.isHidden = true
        visitorTipBack.isHidden = true
        exitButton.isHidden = false
        cancelBtn.isHidden = true
        storeBtn.isHidden = true
        shLocalViewBtn.isHidden = true
        shRemoteViewBtn.isHidden = true
        dressBtn.isHidden = true
        faceBtn.isHidden = true
        dressView?.isHidden = true
        viewBtn.isHidden = false
    }
    
    /// 设置远端视频view
    func setupRemoteVideoCV() {
        if kSceneIndex == .chat {
            remoteVideoCV = VideoCollectionView.init(frame: CGRectMake((self.frame.size.width - 400) / 2 - 50, 10, 400, 130))
        } else {
            remoteVideoCV = VideoCollectionView.init(frame: CGRectMake(50, self.frame.size.height - 150, self.frame.size.width - 100, 130))
        }
        remoteVideoCV.isHidden = false
        remoteVideoCV.alpha = 0.9
        remoteVideoCV.needChangeLayout = true
        self.addSubview(remoteVideoCV)
    }
    
    /// 设置本地视频view
    func setupLocalVideoCV() {
        localVideoCV = VideoCollectionView.init(frame: CGRectMake((self.frame.size.width - 400) / 2 - 50, self.frame.size.height - 140, 400, 130))
        localVideoCV.isHidden = true
        localVideoCV.alpha = 0.9
        localVideoCV.needChangeLayout = true
        self.addSubview(localVideoCV)
    }
    
    func setupAvatarCV() {
        avatarCV = VideoCollectionView.init(frame: CGRectMake(10, 120, self.frame.size.width - 20, 500))
        if let layout = avatarCV.collectionView.collectionViewLayout as? UICollectionViewFlowLayout {
            layout.scrollDirection = .vertical
        }
        avatarCV.alpha = 0.9
        avatarCV.cellSize = CGSize(width: avatarCellSize, height: avatarCellSize)
        avatarCV.collectionView.frame = avatarCV.frame
        self.addSubview(avatarCV)
    }
    
    func setupAvatarView() {
        guard let view = avatarCV.items.first as? UIView else { return }
        let label = UILabel(frame: CGRectMake(5, 5, 100, 20))
        label.text = "uid:" + String(KeyCenter.RTC_UID)
        label.textColor = .red
        view.addSubview(label)
        label.layer.zPosition = .greatestFiniteMagnitude
        
        view.layer.cornerRadius = 10
        view.layer.masksToBounds = true
        view.layer.borderWidth = 2.0
        view.layer.borderColor = UIColor.lightGray.cgColor
    }
    
    /// 显示或隐藏远端视图
    @IBAction func showOrHideRemoteView(_ sender: UIButton) {
        if sender.tag % 2 == 1 {
            remoteVideoCV.isHidden = false
            shRemoteViewBtn.setTitle("隐藏远端视角", for: .normal)
        } else {
            remoteVideoCV.isHidden = true
            shRemoteViewBtn.setTitle("显示远端视角", for: .normal)
        }
        sender.tag += 1
    }
    /// 显示或隐藏本地视图
    @IBAction func showOrHideLocalView(_ sender: UIButton) {
        if sender.tag % 2 == 1 {
            localVideoCV.isHidden = false
            shLocalViewBtn.setTitle("隐藏本地视角", for: .normal)
            MetaServiceEngine.sharedEngine.startPreview(previewView)
            addSceneView()
        } else {
            localVideoCV.isHidden = true
            shLocalViewBtn.setTitle("显示本地视角", for: .normal)
            MetaServiceEngine.sharedEngine.stopPreview()
            removeSceneView()
        }
        sender.tag += 1
    }
    
    /// 设置预览view
    func setupPreviewView() {
        previewView = UIView.init(frame: CGRectMake(0, 0, 120, 120))
        previewView.layer.cornerRadius = 10
        previewView.layer.masksToBounds = true
        localVideoCV.items.append(previewView!)
        let label = UILabel(frame: CGRectMake(5, 5, 100, 20))
        label.text = "uid:" + String(KeyCenter.RTC_UID)
        label.textColor = .red
        label.layer.zPosition = .greatestFiniteMagnitude
        previewView.addSubview(label)
    }
    
    /// 设置换装view
    func setupDressView() {
        dressView = DressView(frame: CGRectMake(0, self.frame.height - 300, self.frame.width, 300))
        dressView?.isHidden = true
        self.addSubview(dressView!)
    }
    
    
    // MARK: UI Event
    
    /// 添加场景view
    @IBAction func addSceneView() {
        self.delegate?.onMetaSceneEvent(event: .addSceneView)
    }
    
    /// 移除场景view
    @IBAction func removeSceneView() {
        self.delegate?.onMetaSceneEvent(event: .removeSceneView)
    }
    
    /// 离开场景
    @IBAction func exit(sender: UIButton) {
        self.delegate?.onMetaSceneEvent(event: .leaveScene)
    }
    
    /// 换装
    @IBAction func changeDress() {
        self.delegate?.onMetaSceneEvent(event: .changeDress)
    }
    
    /// 捏脸
    @IBAction func pitchFace() {
        self.delegate?.onMetaSceneEvent(event: .pitchFace)
    }
    
    /// 切换视角
    @IBAction func changeViewMode() {
        self.delegate?.onMetaSceneEvent(event: .changeViewMode)
    }
    
}
