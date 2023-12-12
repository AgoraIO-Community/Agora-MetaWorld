//
//  MetaInputView.swift
//  MetaDemo
//
//  Created by ZhouRui on 2023/8/21.
//

import Foundation
import UIKit

protocol MetaLoginViewDelegate: NSObjectProtocol {
    func enterScene(sceneIndex: MetaSceneIndex, role: String, userName: String, avatarIndex: Int)
    func cancelDownload()
}

class MetaLoginView: UIView {
    @IBOutlet weak var selSexAlert: SelSexAlert!
    @IBOutlet weak var selSceneAlert: SelSceneAlert!
    @IBOutlet weak var selAvatarAlert: SelAvatarAlert!
    
    @IBOutlet weak var selRoleLabel: UILabel!
    @IBOutlet weak var selRoleIcon: UIImageView!
    @IBOutlet weak var selSceneLable: UILabel!
    @IBOutlet weak var selSceneIcon: UIImageView!
    
    @IBOutlet weak var avatarImageView: UIImageView!
    @IBOutlet weak var userNameTF: UITextField!
    
    @IBOutlet weak var roomNameTF: UITextField!
    
    @IBOutlet weak var downloadingBack: UIView!
    @IBOutlet weak var downloadingProgress: UIProgressView!
    
    @IBOutlet weak var cancelDownloadButton: UIButton!
    
    @IBOutlet weak var enterSceneButton: UIButton!
    
    public weak var loginViewDelegate: MetaLoginViewDelegate?
    
    var selAvatarIndex: Int = 0
    var currentSelBtnTag: Int = 0
    var selCellIndex: Int = 0
    
    override class func awakeFromNib() {
        super.awakeFromNib()
    }
    
    func setupUI() {
        userNameTF.attributedPlaceholder = NSAttributedString.init(string: "请输入2-10个字符", attributes: [NSAttributedString.Key.foregroundColor: UIColor.init(red: 161.0/255.0, green: 139.0/255.0, blue: 176/255.0, alpha: 1.0)])
        userNameTF.text = "metaUser" + String(Int.random(in: 0...100))
        
        selSexAlert.delegate = self
        selSceneAlert.delegate = self
    }
    
    @IBAction func selectedAlertAction(sender: UIButton) {
        var frame = selSexAlert.frame
        frame.origin.y = 0
        
        if sender.tag == 1001 {
            
            selRoleIcon.image = UIImage.init(named: "arrow-up")
            selSexAlert.selManCell.selectedLabel.text = "男"
            selSexAlert.selWomanCell.selectedLabel.text = "女"
            selSexAlert.selKdaCell.selectedLabel.text = "Kda"
            selSexAlert.selMinaCell.selectedLabel.text = "米娜"
            selSexAlert.selMulanCell.selectedLabel.text = "花木兰"
        } else if sender.tag == 1002 {
            selSceneAlert.selNieLianCell.selectedLabel.text = "捏脸换装"
            selSceneAlert.selPaoTuCell.selectedLabel.text = "跑图面捕"
            selSceneAlert.selYuLiaoCell.selectedLabel.text = "语聊"
        }
        currentSelBtnTag = sender.tag
        
        if sender.tag == 1001 {
            DispatchQueue.main.asyncAfter(deadline: DispatchTime.now() + 0.01, execute: {
                self.selSexAlert.isHidden = false
                self.selSexAlert.frame = frame
            })
        } else if sender.tag == 1002 {
            DispatchQueue.main.asyncAfter(deadline: DispatchTime.now() + 0.01, execute: {
                self.selSceneAlert.isHidden = false
                self.selSceneAlert.frame = frame
            })
        }
    }
    
    @IBAction func selectedAvatarAction(sender: UIButton) {
        selAvatarAlert.isHidden = false
    }
    
    @IBAction func cancelDownloadHandler(_ sender: Any) {
        downloadingBack.isHidden = true
        self.loginViewDelegate?.cancelDownload()
    }
    
    @IBAction func enterScene(sender: UIButton) {
        self.loginViewDelegate?.enterScene(sceneIndex: MetaSceneIndex.init(rawValue: selSceneAlert.selIndex) ?? .chat, role: selRoleLabel.text ?? "男", userName: userNameTF.text!, avatarIndex: selAvatarIndex)
    }
}

extension MetaLoginView: SelAlertCellDelegate {
    func onSelect(index: Int) {
        selCellIndex = index + 1
        
        if currentSelBtnTag == 1001 {
            if selCellIndex == 1 {
                selRoleLabel.text = "男"
            } else if selCellIndex == 2 {
                selRoleLabel.text = "女"
            } else if selCellIndex == 3 {
                selRoleLabel.text = "米娜"
            } else if selCellIndex == 4 {
                selRoleLabel.text = "kda"
            } else if selCellIndex == 5 {
                selRoleLabel.text = "花木兰"
            }
        } else {
            if selCellIndex == 1 {
                selSceneLable.text = "捏脸换装"
            } else if selCellIndex == 2 {
                selSceneLable.text = "跑图面捕"
            } else if selCellIndex == 3 {
                selSceneLable.text = "语聊"
            } else if selCellIndex == 4 {
                selSceneLable.text = "面捕语聊"
            }
        }
        
        selRoleIcon.image = UIImage.init(named: "arrow-down")
        selSceneIcon.image = UIImage.init(named: "arrow-down")
    }
    
    func onSelectCancel() {
        selRoleIcon.image = UIImage.init(named: "arrow-down")
        selSceneIcon.image = UIImage.init(named: "arrow-down")
    }
}
