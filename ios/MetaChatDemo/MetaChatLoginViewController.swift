//
//  ViewController.swift
//  MetaChatDemo
//
//  Created by 胡润辰 on 2022/4/21.
//
/*
import UIKit
import AgoraRtcKit
import Zip

let kOnConnectionStateChangedNotifyName = NSNotification.Name(rawValue: "onConnectionStateChanged")

class SelSexCell: UIView {
    @IBOutlet weak var selectedBack: UIView!
    @IBOutlet weak var selectedButton: UIButton!
}

protocol SelSexAlertDelegate: NSObjectProtocol {
    func onSelectSex(index: Int)
    
    func onSelectCancel()
}

class SelSexAlert: UIView {
    @IBOutlet weak var cancelButton: UIButton!
    @IBOutlet weak var selManCell: SelSexCell!
    @IBOutlet weak var selWomanCell: SelSexCell!
    
    public var selIndex: Int = 0
    
    weak var delegate: SelSexAlertDelegate?
        
    @IBAction func selectedAction(sender: UIButton) {
        if sender == selManCell.selectedButton {
            selIndex = 0
            selManCell.selectedBack.isHidden = false
            selWomanCell.selectedBack.isHidden = true
        }else if sender == selWomanCell.selectedButton {
            selIndex = 1
            selManCell.selectedBack.isHidden = true
            selWomanCell.selectedBack.isHidden = false
        }
        
        delegate?.onSelectSex(index: selIndex)
        
        isHidden = true
    }
    
    @IBAction func cancelAction(sender: UIButton) {
        delegate?.onSelectCancel()
        
        isHidden = true
    }
}

class SelAvatarCell: UIView {
    @IBOutlet weak var selectedIcon: UIImageView!
    @IBOutlet weak var selectedButton: UIButton!
}

protocol SelAvatarAlertDelegate: NSObjectProtocol {
    func onSelectAvatar(index: Int)
}

class SelAvatarAlert: UIView {
    @IBOutlet weak var blankButton: UIButton!

    @IBOutlet weak var avatarBoardView: UIView!
    @IBOutlet weak var cancelButton: UIButton!
    @IBOutlet weak var ensureButton: UIButton!
    
    public var selIndex: Int = 0

    weak var delegate: SelAvatarAlertDelegate?
    
    func setUI() {
        avatarBoardView.layer.borderWidth = 1.0
        avatarBoardView.layer.borderColor = UIColor.init(red: 224.0/255.0, green: 216.0/255.0, blue: 203.0/255.0, alpha: 1.0).cgColor
        avatarBoardView.layer.cornerRadius = 4.0
        
        cancelButton.layer.borderWidth = 1.0
        cancelButton.layer.borderColor = UIColor.init(red: 111/255.0, green: 87/255.0, blue: 235/255.0, alpha: 1.0).cgColor
        cancelButton.layer.cornerRadius = 20.0

    }

    
    @IBAction func cancelAction(sender: UIButton) {
        isHidden = true
    }

    @IBAction func selectedAction(sender: UIButton) {
        selIndex = sender.superview?.tag ?? 0;
        
        for subView in avatarBoardView.subviews {
            let avatarCell = subView as! SelAvatarCell
            
            if avatarCell == sender.superview {
                avatarCell.selectedIcon.isHidden = false
            }else {
                avatarCell.selectedIcon.isHidden = true
            }
        }
    }
    
    @IBAction func ensureAction(sender: UIButton) {
        delegate?.onSelectAvatar(index: selIndex)
        
        isHidden = true
    }
}

class MetaChatLoginViewController: UIViewController {
    @IBOutlet weak var selSexAlert: SelSexAlert!
    @IBOutlet weak var selAvatarAlert: SelAvatarAlert!
    
    @IBOutlet weak var selSexLabel: UILabel!
    @IBOutlet weak var selSexIcon: UIImageView!
    
    @IBOutlet weak var avatarImageView: UIImageView!
    @IBOutlet weak var userNameTF: UITextField!
    @IBOutlet weak var errorLabel: UILabel!
    
    @IBOutlet weak var downloadingBack: UIView!
    @IBOutlet weak var downloadingProgress: UIProgressView!
    
    @IBOutlet weak var cancelDownloadButton: UIButton!
    
    #if DEBUG
    private var currentSceneId: Int = 4
    #elseif TEST
    private var currentSceneId: Int = 4
    #else
    private var currentSceneId: Int = 1
    #endif
    
    private let libraryPath = NSHomeDirectory() + "/Library/Caches/"
    
    var selSex: Int = 0    //0未选择，1男，2女
    
    var roomId: String!
    
    var selAvatarIndex: Int = 0
    
    var avatarUrlArray = ["https://accpic.sd-rtn.com/pic/test/png/2.png", "https://accpic.sd-rtn.com/pic/test/png/4.png", "https://accpic.sd-rtn.com/pic/test/png/1.png", "https://accpic.sd-rtn.com/pic/test/png/3.png", "https://accpic.sd-rtn.com/pic/test/png/6.png", "https://accpic.sd-rtn.com/pic/test/png/5.png"]
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        userNameTF.attributedPlaceholder = NSAttributedString.init(string: "请输入2-10个字符", attributes: [NSAttributedString.Key.foregroundColor : UIColor.init(red: 161.0/255.0, green: 139.0/255.0, blue: 176/255.0, alpha: 1.0)])
        
        selSexAlert.delegate = self
        selAvatarAlert.setUI()
        
        selAvatarAlert.delegate = self
        
        view.addGestureRecognizer(UITapGestureRecognizer.init(target: self, action: #selector(hideKeyboard)))
    }

    private func moveFileHandler() {
        if FileManager.default.fileExists(atPath: libraryPath + "3", isDirectory: nil) {
            return
        }
        FileManager.default.createFile(atPath: libraryPath, contents: nil, attributes: nil)
        let path = Bundle.main.path(forResource: "3", ofType: "zip")
        try? Zip.unzipFile(URL(fileURLWithPath: path ?? ""), destination: URL(fileURLWithPath: libraryPath), overwrite: true, password: nil) { progress in
            DLog("zip progress = \(progress)")
        } fileOutputHandler: { unzippedFile in
            DLog(unzippedFile.path)
        }
    }
    
    override var shouldAutorotate: Bool {
        return true
    }
    
    override var supportedInterfaceOrientations: UIInterfaceOrientationMask {
        return .portrait
    }
    
    override var prefersStatusBarHidden: Bool {
        false
    }

    @objc func hideKeyboard() {
        view.endEditing(true)
    }
    
    @IBAction func selectedSexAction(sender: UIButton) {
        selSexAlert.isHidden = false
        
        selSexIcon.image = UIImage.init(named: "arrow-up")
        view.endEditing(true)
    }
    
    @IBAction func selectedAvatarAction(sender: UIButton) {
        selAvatarAlert.isHidden = false
    }
    
    @IBAction func cancelDownloadHandler(_ sender: Any) {
        downloadingBack.isHidden = true
        MetaChatEngine.sharedEngine.metachatKit?.cancelDownloadScene(currentSceneId)
    }
    
    func checkValid() -> Bool {
        let nameCount = userNameTF.text?.count ?? 0
        if (nameCount < 2) || (nameCount > 10) {
            errorLabel.text = "姓名必须包含2-10个字符"
            return false
        }
        
        if selSex == 0 {
            errorLabel.text = "请选择性别"
            return false
        }
        
        errorLabel.text = nil
        return true
    }
        
    var indicatorView: UIActivityIndicatorView?
    
    @IBAction func enterScene(sender: UIButton) {
        if !checkValid() {
            return
        }
        
        indicatorView = UIActivityIndicatorView.init(frame: view.frame)
        if #available(iOS 13.0, *) {
            indicatorView?.style = UIActivityIndicatorView.Style.large
        } else {
            // Fallback on earlier versions
        }
        indicatorView?.color = UIColor.white
        view.addSubview(indicatorView!)
        indicatorView?.startAnimating()
        
        MetaChatEngine.sharedEngine.createMetachatKit(userName: userNameTF.text!, avatarUrl: avatarUrlArray[selAvatarIndex])
                        
        MetaChatEngine.sharedEngine.metachatKit?.getScenes()
    }
    
    func onSceneReady(_ sceneInfo: AgoraMetachatSceneInfo) {
        let storyBoard: UIStoryboard = UIStoryboard(name: "Main", bundle: nil)
        
        DispatchQueue.main.async {
            self.downloadingBack.isHidden = true
            
            guard let sceneViewController = storyBoard.instantiateViewController(withIdentifier: "SceneViewController") as? MetaChatSceneViewController else { return }
            sceneViewController.modalPresentationStyle = .fullScreen
//            MetaChatEngine.sharedEngine.createScene(sceneInfo, roomId: self.roomId)

            self.present(sceneViewController, animated: true)
        }
        

    }
}

extension MetaChatLoginViewController: SelSexAlertDelegate {
    func onSelectCancel() {
        selSexIcon.image = UIImage.init(named: "arrow-down")
    }
    
    func onSelectSex(index: Int) {
        selSex = index + 1
        
        if selSex == 1 {
            selSexLabel.text = "男"
        }else if selSex == 2 {
            selSexLabel.text = "女"
        }
        
        selSexIcon.image = UIImage.init(named: "arrow-down")
    }
}


extension MetaChatLoginViewController: SelAvatarAlertDelegate {
    func onSelectAvatar(index: Int) {
        selAvatarIndex = index
        
        let localImageName = "avatar\(index+1)"
        avatarImageView.image = UIImage.init(named: localImageName)
    }
}

extension MetaChatLoginViewController: AgoraMetachatEventDelegate {
    func onConnectionStateChanged(_ state: AgoraMetachatConnectionStateType, reason: AgoraMetachatConnectionChangedReasonType) {
        if state == .disconnected {
            DispatchQueue.main.async {
                self.indicatorView?.stopAnimating()
                self.indicatorView?.removeFromSuperview()
                self.indicatorView = nil
            }
        } else if state == .reconnecting || state == .aborted {
            MetaChatEngine.sharedEngine.leaveScene()
            DispatchQueue.main.async {
                DLog("state == \(state.rawValue), reason == \(reason.rawValue)")
                NotificationCenter.default.post(name: kOnConnectionStateChangedNotifyName, object: nil, userInfo: ["state":state.rawValue,"reason":reason.rawValue])
            }
        }
    }
    
    func onRequestToken() {
        
    }
    
    func onGetScenesResult(_ scenes: NSMutableArray, errorCode: Int) {
        DispatchQueue.main.async {
            self.indicatorView?.stopAnimating()
            self.indicatorView?.removeFromSuperview()
            self.indicatorView = nil
        }
        
        if errorCode != 0 {
            let alertController = UIAlertController.init(title: "get Scenes failed:errorcode:\(errorCode)", message:nil , preferredStyle:.alert)
            
            alertController.addAction(UIAlertAction.init(title: "确定", style: .cancel, handler: nil))

            DispatchQueue.main.async {
                self.present(alertController, animated: true)
            }
            return
        }
        
        if scenes.count == 0 {
            return
        }
        
        guard let firstScene = scenes.compactMap({ $0 as? AgoraMetachatSceneInfo }).first(where: { $0.sceneId == currentSceneId }) else {
            return
        }
        let metachatKit = MetaChatEngine.sharedEngine.metachatKit
        let totalSize = firstScene.totalSize / 1024
        if metachatKit?.isSceneDownloaded(currentSceneId) != 1 {
            let alertController = UIAlertController.init(title: "下载提示", message: "首次进入MetaChat场景需下载\(totalSize)M数据包", preferredStyle:.alert)
            
            alertController.addAction(UIAlertAction.init(title: "下次再说", style: .cancel, handler: nil))
            alertController.addAction(UIAlertAction.init(title: "立即下载", style: .default, handler: { UIAlertAction in
                metachatKit?.downloadScene(self.currentSceneId)
                self.downloadingBack.isHidden = false
            }))
            
            DispatchQueue.main.async {
                self.present(alertController, animated: true)
            }
        }else {
            onSceneReady(firstScene)
        }
    }
    
    func onDownloadSceneProgress(_ sceneInfo: AgoraMetachatSceneInfo?, progress: Int, state: AgoraMetachatDownloadStateType) {
        DispatchQueue.main.async {
            self.downloadingProgress.progress = Float(progress)/100.0
        }
        
        if state == .downloaded && sceneInfo != nil {
            onSceneReady(sceneInfo!)
        }
    }
}
*/
