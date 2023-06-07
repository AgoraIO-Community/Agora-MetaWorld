//
//  DressView.swift
//  MetaChatDemo
//
//  Created by ZhouRui on 2023/4/27.
//

import Foundation
import UIKit

let CellIdentifier = "CustomDressCell"
private let kDressTypeMargin: CGFloat = 10
private let kDressTypeWidth: CGFloat = 60

class ZRCustomView: UIView {
    
    // MARK: - Properties
    
    private let scrollView: UIScrollView = {
        let scrollView = UIScrollView()
        scrollView.showsHorizontalScrollIndicator = false
        scrollView.showsVerticalScrollIndicator = false
        scrollView.translatesAutoresizingMaskIntoConstraints = false
        scrollView.isUserInteractionEnabled = true
        return scrollView
    }()
    
    private let collectionView: UICollectionView = {
        let layout = UICollectionViewFlowLayout()
        layout.scrollDirection = .vertical
        layout.minimumInteritemSpacing = 0
        layout.minimumLineSpacing = 0
        
        let collectionView = UICollectionView(frame: .zero, collectionViewLayout: layout)
        collectionView.backgroundColor = .white
        collectionView.translatesAutoresizingMaskIntoConstraints = false
        return collectionView
    }()
    
    private let slider: UISlider = {
        let slider = UISlider()
        slider.minimumValue = 0.0
        slider.maximumValue = 1.0
        slider.value = 0.5
        slider.addTarget(self, action: #selector(sliderValueChanged), for: .valueChanged)
        return slider
    }()
    
    public var selectIndex = 0
    private var dressIndex = 0
    private var faceIndex = 0
    
    public var scrollViewItems = [String]()
    
    public var collectionViewItems = [String]() {
        didSet {
            self.collectionView.reloadData()
        }
    }
        
    public var sliderValueChangedBlock: ((Float) -> Void)?
    public var scrollViewLabelClickBlock: ((Int) -> Void)?
    public var collectionViewCellClickBlock: ((Int) -> Void)?
    
    // MARK: - Initializers
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        setupViews()
        setupCollectionView()
    }
    
    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
        setupViews()
        setupCollectionView()
    }
    
    // MARK: - Private methods
    
    private func setupViews() {
        addSubview(scrollView)
        addSubview(collectionView)
        addSubview(slider)
        
        // Set up scrollView constraints
        NSLayoutConstraint.activate([
            scrollView.leadingAnchor.constraint(equalTo: leadingAnchor),
            scrollView.trailingAnchor.constraint(equalTo: trailingAnchor),
            scrollView.topAnchor.constraint(equalTo: topAnchor, constant: 60),
            scrollView.heightAnchor.constraint(equalToConstant: 60)
        ])
        
        // Set up collectionView constraints
        NSLayoutConstraint.activate([
            collectionView.leadingAnchor.constraint(equalTo: scrollView.leadingAnchor),
            collectionView.trailingAnchor.constraint(equalTo: scrollView.trailingAnchor),
            collectionView.topAnchor.constraint(equalTo: scrollView.bottomAnchor),
            collectionView.bottomAnchor.constraint(equalTo: bottomAnchor)
        ])
        
        slider.translatesAutoresizingMaskIntoConstraints = false
        // Set up slider constraints
        NSLayoutConstraint.activate([
            slider.centerXAnchor.constraint(equalTo: centerXAnchor),
            slider.topAnchor.constraint(equalTo: topAnchor),
            slider.widthAnchor.constraint(equalToConstant: 100),
            slider.heightAnchor.constraint(equalToConstant: 60)
        ])
        
        // Set up collectionView cell size
        let cellWidth = frame.width / 4
        let layout = collectionView.collectionViewLayout as? UICollectionViewFlowLayout
        layout?.itemSize = CGSize(width: cellWidth, height: 50)
        
        slider.isHidden = true
    }
    
    private func setupCollectionView() {
        collectionView.dataSource = self
        collectionView.delegate = self
        collectionView.register(CustomCell.self, forCellWithReuseIdentifier: CellIdentifier)
    }
    
    public func setupScrollViewDataSource() {
        for subview in scrollView.subviews {
            if subview is UILabel {
                subview.removeFromSuperview()
            }
        }
        if selectIndex == 0 {
            slider.isHidden = true
            scrollViewLabelClickBlock?(self.dressIndex)
        } else {
            slider.isHidden = false
            scrollViewLabelClickBlock?(self.faceIndex)
        }
        var offsetX: CGFloat = 0
        
        for item in scrollViewItems {
            
            let label = UILabel()
            label.text = item
            label.frame = CGRect(x: 0, y: 4, width: kDressTypeWidth, height: kDressTypeWidth - 8)
            label.font = UIFont.systemFont(ofSize: 14)
            label.textAlignment = .center
            
            var frame = label.frame
            frame.origin.x = offsetX
            label.frame = frame
            
            let tapGesture = UITapGestureRecognizer(target: self, action: #selector(labelClicked(withGesture:)))
            label.addGestureRecognizer(tapGesture)
            label.isUserInteractionEnabled = true
            
            self.scrollView.addSubview(label)
            
            offsetX += label.frame.size.width + kDressTypeMargin
        }
        scrollView.contentSize = CGSizeMake(offsetX - kDressTypeMargin, 0)
    }
    
    @objc private func sliderValueChanged() {
        sliderValueChangedBlock?(slider.value)
        print("===== slider value changed: \(slider.value) =====")
    }
                    
    public func getDressIndex() -> Int {
        return dressIndex
    }
    
    public func getFaceIndex() -> Int {
        return faceIndex
    }
}

class CustomCell: UICollectionViewCell {
    let label = UILabel()
    let imageView = UIImageView()
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        
        label.translatesAutoresizingMaskIntoConstraints = false
        contentView.addSubview(label)
        label.font = UIFont.systemFont(ofSize: 14)
        label.textAlignment = .center
        
        NSLayoutConstraint.activate([
            label.leadingAnchor.constraint(equalTo: contentView.leadingAnchor),
            label.trailingAnchor.constraint(equalTo: contentView.trailingAnchor),
            label.topAnchor.constraint(equalTo: contentView.topAnchor),
            label.bottomAnchor.constraint(equalTo: contentView.bottomAnchor)
        ])
        
        imageView.translatesAutoresizingMaskIntoConstraints = false
        contentView.addSubview(imageView)
        
        NSLayoutConstraint.activate([
            imageView.leadingAnchor.constraint(equalTo: contentView.leadingAnchor, constant: 10),
            imageView.trailingAnchor.constraint(equalTo: contentView.trailingAnchor, constant: -10),
            imageView.topAnchor.constraint(equalTo: contentView.topAnchor, constant: 10),
            imageView.bottomAnchor.constraint(equalTo: contentView.bottomAnchor, constant: -10)
        ])
        
        imageView.isHidden = true
        label.isHidden = true
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}


// MARK: - UICollectionViewDataSource

extension ZRCustomView: UICollectionViewDataSource {
    
    func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        return collectionViewItems.count
    }
    
    func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        let cell = collectionView.dequeueReusableCell(withReuseIdentifier: CellIdentifier, for: indexPath) as! CustomCell
        if selectIndex == 0 {
            let image = UIImage(named: collectionViewItems[indexPath.row])
            cell.imageView.image = image
            cell.imageView.isHidden = false
            cell.label.isHidden = true
        } else {
            cell.label.text = collectionViewItems[indexPath.row]
            cell.imageView.isHidden = true
            cell.label.isHidden = false
        }
        return cell
    }
}

// MARK: - UICollectionViewDelegateFlowLayout

extension ZRCustomView: UICollectionViewDelegateFlowLayout {
    
    func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, sizeForItemAt indexPath: IndexPath) -> CGSize {
        // 返回cell的大小
        let cellWidth = frame.width / 4
        return CGSize(width: cellWidth, height: cellWidth)
    }
    
    func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
        // 处理cell的点击事件
        print("===== collectionView select row: \(indexPath.row) =====")
        collectionViewCellClickBlock?(indexPath.row)
    }
}

extension ZRCustomView {
    @objc func labelClicked(withGesture gesture: UITapGestureRecognizer) {

        guard let view = gesture.view,
            let index: Int = self.scrollView.subviews.firstIndex(of: view)
            else { return }
        if selectIndex == 0 {
            dressIndex = index
        } else {
            faceIndex = index
        }
            
        scrollViewLabelClickBlock?(index)
        print("===== scrollView select index: \(index) =====")
    }
}

