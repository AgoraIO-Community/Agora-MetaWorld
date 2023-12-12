//
//  MetaAlert.swift
//  MetaDemo
//
//  Created by ZhouRui on 2023/8/18.
//

import Foundation
import UIKit

protocol SelAlertCellDelegate: NSObjectProtocol {
    func onSelect(index: Int)
    func onSelectCancel()
}

protocol SelAvatarAlertDelegate: NSObjectProtocol {
    func onSelectAvatar(index: Int)
}

class SelAlertCell: UIView {
    @IBOutlet weak var selectedBack: UIView!
    @IBOutlet weak var selectedButton: UIButton!
    @IBOutlet weak var selectedLabel: UILabel!
}

class SelSexAlert: UIView {
    @IBOutlet weak var cancelButton: UIButton!
    @IBOutlet weak var selManCell: SelAlertCell!
    @IBOutlet weak var selWomanCell: SelAlertCell!
    @IBOutlet weak var selMinaCell: SelAlertCell!
    @IBOutlet weak var selKdaCell: SelAlertCell!
    @IBOutlet weak var selMulanCell: SelAlertCell!
    
    public var selIndex: Int = 0
    public weak var delegate: SelAlertCellDelegate?
    
    @IBAction func selectedAction(sender: UIButton) {
        if sender == selManCell.selectedButton {
            selIndex = 0
            selManCell.selectedBack.isHidden = false
            selWomanCell.selectedBack.isHidden = true
            selMinaCell.selectedBack.isHidden = true
            selKdaCell.selectedBack.isHidden = true
            selMulanCell.selectedBack.isHidden = true
        } else if sender == selWomanCell.selectedButton {
            selIndex = 1
            selManCell.selectedBack.isHidden = true
            selWomanCell.selectedBack.isHidden = false
            selMinaCell.selectedBack.isHidden = true
            selKdaCell.selectedBack.isHidden = true
            selMulanCell.selectedBack.isHidden = true
        } else if sender == selMinaCell.selectedButton {
            selIndex = 2
            selManCell.selectedBack.isHidden = true
            selWomanCell.selectedBack.isHidden = true
            selMinaCell.selectedBack.isHidden = false
            selKdaCell.selectedBack.isHidden = true
            selMulanCell.selectedBack.isHidden = true
        } else if sender == selKdaCell.selectedButton {
            selIndex = 3
            selManCell.selectedBack.isHidden = true
            selWomanCell.selectedBack.isHidden = true
            selMinaCell.selectedBack.isHidden = true
            selKdaCell.selectedBack.isHidden = false
            selMulanCell.selectedBack.isHidden = true
        } else if sender == selMulanCell.selectedButton {
            selIndex = 4
            selManCell.selectedBack.isHidden = true
            selWomanCell.selectedBack.isHidden = true
            selMinaCell.selectedBack.isHidden = true
            selKdaCell.selectedBack.isHidden = true
            selMulanCell.selectedBack.isHidden = false
        }
        
        delegate?.onSelect(index: selIndex)
        isHidden = true
    }
    
    @IBAction func cancelAction(sender: UIButton) {
        delegate?.onSelectCancel()
        isHidden = true
    }
}

class SelSceneAlert: UIView {
    @IBOutlet weak var cancelButton: UIButton!
    @IBOutlet weak var selNieLianCell: SelAlertCell!
    @IBOutlet weak var selPaoTuCell: SelAlertCell!
    @IBOutlet weak var selYuLiaoCell: SelAlertCell!
    @IBOutlet weak var selFaceCaptureYuLiaoCell: SelAlertCell!
    
    public var selIndex: Int = 0
    
    public weak var delegate: SelAlertCellDelegate?
        
    @IBAction func selectedAction(sender: UIButton) {
        if sender == selNieLianCell.selectedButton {
            selIndex = 0
            selNieLianCell.selectedBack.isHidden = false
            selPaoTuCell.selectedBack.isHidden = true
            selYuLiaoCell.selectedBack.isHidden = true
            selFaceCaptureYuLiaoCell.selectedBack.isHidden = true
        } else if sender == selPaoTuCell.selectedButton {
            selIndex = 1
            selNieLianCell.selectedBack.isHidden = true
            selPaoTuCell.selectedBack.isHidden = false
            selYuLiaoCell.selectedBack.isHidden = true
            selFaceCaptureYuLiaoCell.selectedBack.isHidden = true
        } else if sender == selYuLiaoCell.selectedButton {
            selIndex = 2
            selNieLianCell.selectedBack.isHidden = true
            selPaoTuCell.selectedBack.isHidden = true
            selYuLiaoCell.selectedBack.isHidden = false
            selFaceCaptureYuLiaoCell.selectedBack.isHidden = true
        } else if sender == selFaceCaptureYuLiaoCell.selectedButton {
            selIndex = 3
            selNieLianCell.selectedBack.isHidden = true
            selPaoTuCell.selectedBack.isHidden = true
            selYuLiaoCell.selectedBack.isHidden = true
            selFaceCaptureYuLiaoCell.selectedBack.isHidden = false
        }

        delegate?.onSelect(index: selIndex)
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

class SelAvatarAlert: UIView {
    @IBOutlet weak var blankButton: UIButton!

    @IBOutlet weak var avatarBoardView: UIView!
    @IBOutlet weak var cancelButton: UIButton!
    @IBOutlet weak var ensureButton: UIButton!
    
    public var selIndex: Int = 0

    public weak var delegate: SelAvatarAlertDelegate?
    
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

extension SelAvatarAlert: SelAvatarAlertDelegate {
    func onSelectAvatar(index: Int) {
//        selAvatarIndex = index
//
//        let localImageName = "avatar\(index+1)"
//        avatarImageView.image = UIImage.init(named: localImageName)
    }
}
