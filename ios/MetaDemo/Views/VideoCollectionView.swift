//
//  VideoCollectionView.swift
//  MetaDemo
//
//  Created by ZhouRui on 2023/3/14.
//

import Foundation
import UIKit

class VideoCollectionViewCell: UICollectionViewCell {

    let bgView: UIView = {
        let view = UIView()
        view.translatesAutoresizingMaskIntoConstraints = false
        view.backgroundColor = .black
        view.layer.masksToBounds = true
        view.layer.cornerRadius = 10.0
        return view
    }()

    override init(frame: CGRect) {
        super.init(frame: frame)
        contentView.addSubview(bgView)
        self.backgroundColor = .clear
        NSLayoutConstraint.activate([
            bgView.leadingAnchor.constraint(equalTo: contentView.leadingAnchor),
            bgView.trailingAnchor.constraint(equalTo: contentView.trailingAnchor),
            bgView.topAnchor.constraint(equalTo: contentView.topAnchor),
            bgView.bottomAnchor.constraint(equalTo: contentView.bottomAnchor),
        ])
    }

    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

}

public class VideoCollectionView: UIView {
    
    let collectionView: UICollectionView = {
        let layout = UICollectionViewFlowLayout()
        layout.minimumLineSpacing = 10.0
        layout.scrollDirection = .horizontal
        let collectionView = UICollectionView(frame: .zero, collectionViewLayout: layout)
        collectionView.backgroundColor = .clear
        collectionView.translatesAutoresizingMaskIntoConstraints = false
        collectionView.showsHorizontalScrollIndicator = false
        return collectionView
    }()
    
    var items: [Any] = [] {
        didSet {
            collectionView.reloadData()
            
            if (!needChangeLayout) {
                return
            }
            
            let count = items.count
            var _frame = cvFrame
            var x = (cvFrame.width - cellSize.width * CGFloat(count) - 5.0 * CGFloat(count - 1)) / 2
            if count > 3 {
                x = 0
            }
            _frame.origin.x = x
            _frame.origin.y = 0
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.1) {
                self.collectionView.frame = _frame
            }
        }
    }
    
    var cvFrame: CGRect = CGRectZero

    var cellSize: CGSize = CGSize(width: 120, height: 120)

    var didSelectItem: ((Int) -> Void)?
    
    var needChangeLayout: Bool = false

    override init(frame: CGRect) {
        super.init(frame: frame)
        cvFrame = frame
        setupCollectionView()
    }

    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
        setupCollectionView()
    }

    private func setupCollectionView() {
        addSubview(collectionView)
        collectionView.delegate = self
        collectionView.dataSource = self
        collectionView.register(VideoCollectionViewCell.self, forCellWithReuseIdentifier: "cell")
        NSLayoutConstraint.activate([
            collectionView.leadingAnchor.constraint(equalTo: leadingAnchor),
            collectionView.trailingAnchor.constraint(equalTo: trailingAnchor),
            collectionView.topAnchor.constraint(equalTo: topAnchor),
            collectionView.bottomAnchor.constraint(equalTo: bottomAnchor),
        ])
        
        self.layer.masksToBounds = true
        self.layer.cornerRadius = 10;
    }
}

extension VideoCollectionView : UICollectionViewDelegate, UICollectionViewDataSource, UICollectionViewDelegateFlowLayout {
    public func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        return items.count
    }

    public func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        let cell = collectionView.dequeueReusableCell(withReuseIdentifier: "cell", for: indexPath)
        if let view = items[indexPath.row] as? UIView {
            if !cell.contentView.subviews.contains(view) {
                cell.contentView.addSubview(view)
            }
        }
        return cell
    }

    public func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, sizeForItemAt indexPath: IndexPath) -> CGSize {
        return cellSize
    }
    
    public func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, insetForSectionAt section: Int) -> UIEdgeInsets {
        return UIEdgeInsets(top: 10, left: 10, bottom: 10, right: 10)
    }
}
