//
//  MetaChatDressViewController.swift
//  MetaChatDemo
//
//  Created by ZhouRui on 2022/11/7.
//

import Foundation
import UIKit

//let COLLECTION_CELL_ID = "dressID"
//let kDressTypeMargin: CGFloat = 20
//let kDressTypeWidth: CGFloat = 60
//let kCellMargin: CGFloat = 10
//let SCREEN_WIDTH: CGFloat = UIScreen.main.bounds.size.width
//let SCREEN_HEIGHT: CGFloat = UIScreen.main.bounds.size.height

class MetaChatDressViewController: UIViewController {
    
//    var delegate: storeDressInfoDelegate?
//
//    @IBOutlet weak var collectionView: UICollectionView!
//    @IBOutlet weak var scrollView: UIScrollView!
//
//    lazy var dressType: [String] = ["clothes_icon", "shoes_icon", "trousers_icon", "hairpin_icon"]
//    lazy var dressTypeToDresses: Dictionary<String, Any> = ["clothes_icon": clothes, "shoes_icon": shoes, "trousers_icon": trousers, "hairpin_icon": hairpins]
//    lazy var clothes: [String] = ["clothes1", "clothes2", "clothes3", "clothes4", "clothes5", "clothes6", "clothes7", "clothes8"]
//    lazy var shoes: [String] = ["shoes1", "shoes2", "shoes3", "shoes4", "shoes5"]
//    lazy var trousers: [String] = ["trousers1", "trousers2", "trousers3"]
//    lazy var hairpins: [String] = ["hairpin1", "hairpin2", "hairpin3", "hairpin4", "hairpin5", "hairpin6"]
//
//    var selectIndex: Int = 0
//    var selectIndexPath: [IndexPath] = Array(repeating: IndexPath(), count: 4)
//
//    override func viewDidLoad() {
//        super.viewDidLoad()
//        self.view.backgroundColor = .lightGray
//        setupUI()
//    }
//
//    override func didReceiveMemoryWarning() {
//        super.didReceiveMemoryWarning()
//        // Dispose of any resources that can be recreated.
//    }
//
//    override var shouldAutorotate: Bool {
//        return false
//    }
//
//    func setupUI() {
//        setupScrollView()
//        changeIcon(2, isSelected: true)
//    }
//
//    func setupScrollView() {
//        self.scrollView.backgroundColor = .white
//        var offsetX: CGFloat = kDressTypeMargin
//
//        for dress in dressType {
//
//            let imageView = UIImageView()
//            imageView.frame = CGRect(x: 0, y: 4, width: kDressTypeWidth, height: kDressTypeWidth - 8)
//
//            imageView.image = UIImage.init(named: dress)
//
//            var frame = imageView.frame
//            frame.origin.x = offsetX
//            imageView.frame = frame
//
//            let tapGesture = UITapGestureRecognizer(target: self, action: #selector(imageViewClicked(withGesture:)))
//            imageView.addGestureRecognizer(tapGesture)
//            imageView.isUserInteractionEnabled = true
//
//            self.scrollView.addSubview(imageView)
//
//            offsetX += imageView.frame.size.width + kDressTypeMargin
//        }
//    }
//
//    @IBAction func exit(sender: UIButton) {
//        MetaChatEngine.sharedEngine.leaveRtcChannel()
//        MetaChatEngine.sharedEngine.leaveScene()
//        self.dismiss(animated: true) { }
//    }
//
//    @IBAction func store(sender: UIButton) {
//        // store current dress info
//        var dressInfo = AgoraMetachatDressInfo()
//        let dict = ["gender": 0, "hair": 1, "tops": 1]
//        let data = try? JSONSerialization.data(withJSONObject: dict, options: [])
//        let str = String(data: data!, encoding: String.Encoding.utf8)
//        dressInfo.extraCustomInfo = str!.data(using: String.Encoding.utf8)
//        MetaChatEngine.sharedEngine.currentDressInfo = dressInfo
//
//        self.delegate?.storeDressInfo()
//
//        self.dismiss(animated: true) { }
//    }
}

//extension MetaChatDressViewController {
//    @objc func imageViewClicked(withGesture gesture: UITapGestureRecognizer) {
//
//        guard let view = gesture.view,
//            let index: Int = self.scrollView.subviews.index(of: view)
//            else { return }
//
//        selectIndex = index
//        let indexPath = IndexPath(item: 0, section: 0)
//
//        self.collectionView.scrollToItem(at: indexPath, at: .init(rawValue: 0), animated: false)
//
//        for i in 0..<self.dressType.count {
//            let iv = self.scrollView.subviews[i]
//            if iv == gesture.view {
//                self.changeIcon(i, isSelected: true)
//            } else {
//                self.changeIcon(i, isSelected: false)
//            }
//        }
//        
//        self.collectionView.reloadData()
//    }
//    
//    func changeIcon(_ index: Int, isSelected: Bool) {
//        let iv = self.scrollView.subviews[index] as! UIImageView
//        if isSelected {
//            iv.image = UIImage.init(named: self.dressType[index] + "1")
//        } else {
//            iv.image = UIImage.init(named: self.dressType[index])
//        }
//    }
//}
//
//extension MetaChatDressViewController : UICollectionViewDataSource {
//    
//    func numberOfSections(in collectionView: UICollectionView) -> Int {
//        return 1
//    }
//    
//    func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
//        let data = self.dressTypeToDresses[self.dressType[selectIndex]] as! Array<String>
//        return data.count
//    }
//    
//    func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
//        let cell = collectionView.dequeueReusableCell(withReuseIdentifier: COLLECTION_CELL_ID, for: indexPath) as! DressCollectionCell
//        let data = self.dressTypeToDresses[self.dressType[selectIndex]] as! Array<String>
//        cell.dressImageView.image = UIImage.init(named: data[indexPath.row])
//        
//        cell.dressImageView.layer.borderColor = UIColor.clear.cgColor
//        cell.dressImageView.layer.borderWidth = 0.0
//        
//        if selectIndexPath.count > selectIndex && selectIndexPath[selectIndex] == indexPath {
//            cell.dressImageView.layer.borderColor = UIColor.lightGray.cgColor
//            cell.dressImageView.layer.borderWidth = 1.0
//        }
//        
//        return cell
//    }
//    
//}
//
//extension MetaChatDressViewController: UICollectionViewDelegate, UICollectionViewDelegateFlowLayout {
//    
//    func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
//        
//        if indexPath == selectIndexPath[selectIndex] {
//            let dressCell = collectionView.cellForItem(at: indexPath) as! DressCollectionCell
//            dressCell.dressImageView.layer.borderColor = UIColor.clear.cgColor
//            dressCell.dressImageView.layer.borderWidth = 0.0
//            
//            selectIndexPath[selectIndex] = IndexPath()
//            return
//        }
//        
//        if indexPath != selectIndexPath[selectIndex] && !(selectIndexPath[selectIndex].isEmpty) {
//            let tempIndexPath = selectIndexPath[selectIndex]
//            let dressCell = collectionView.cellForItem(at: tempIndexPath) as! DressCollectionCell
//            dressCell.dressImageView.layer.borderColor = UIColor.clear.cgColor
//            dressCell.dressImageView.layer.borderWidth = 0.0
//        }
//        
//        let dressCell = collectionView.cellForItem(at: indexPath) as! DressCollectionCell
//        dressCell.dressImageView.layer.borderColor = UIColor.lightGray.cgColor
//        dressCell.dressImageView.layer.borderWidth = 1.0
//        
//        selectIndexPath[selectIndex] = indexPath
//        
//    }
//    
//    func collectionView(_ collectionView: UICollectionView, didDeselectItemAt indexPath: IndexPath) {
//        let dressCell = collectionView.cellForItem(at: indexPath) as! DressCollectionCell
//        dressCell.dressImageView.layer.borderColor = UIColor.clear.cgColor
//        dressCell.dressImageView.layer.borderWidth = 0.0
//    }
//    
//    func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, sizeForItemAt indexPath: IndexPath) -> CGSize {
//        let cellWidth = (SCREEN_WIDTH - 3 * kCellMargin) / 4
//        return CGSize(width: cellWidth, height: cellWidth);
//    }
//
//    func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, insetForSectionAt section: Int) -> UIEdgeInsets {
//        return UIEdgeInsets.init(top: 0, left: 0, bottom: 0, right: 0)
//    }
//
//    func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, minimumLineSpacingForSectionAt section: Int) -> CGFloat {
//        return kCellMargin
//    }
//
//    func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, minimumInteritemSpacingForSectionAt section: Int) -> CGFloat {
//        return kCellMargin
//    }
//}

//class DressCollectionCell: UICollectionViewCell {
//
//    @IBOutlet weak var dressImageView: UIImageView!
//
//}
