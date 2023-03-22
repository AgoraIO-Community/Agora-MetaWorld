//
//  ActionView.swift
//  MetaChatDemo
//
//  Created by ZhouRui on 2023/2/9.
//

import Foundation
import UIKit
import SnapKit

public protocol ActionViewDelegate: AnyObject {
    func actionView(_ actionView: ActionView, didSelectItem item: String, atIndex index: Int)
}

public class ActionView: UIView {
    
    public var selectedIndex: Int? {
        didSet {
            
        }
    }
    
    public weak var delegate: ActionViewDelegate?
    
    public var scrollingEnabled: Bool = false {
        didSet {
            itemsTableView.isScrollEnabled = scrollingEnabled
        }
    }
    
    public var items = [String]() {
        didSet {
            self.itemsTableView.reloadData()
        }
    }
    
    private var itemsTableView: UITableView!
    private var width: CGFloat!
    private var height: CGFloat!
    private var setuped: Bool = false
    
    @IBInspectable public var borderColor: UIColor =  UIColor.clear {
        didSet {
            layer.borderColor = borderColor.cgColor
        }
    }
    
    @IBInspectable public var borderWidth: CGFloat = 0.0 {
        didSet {
            layer.borderWidth = borderWidth
        }
    }
    
    @IBInspectable public var cornerRadius: CGFloat = 8.0 {
        didSet {
            layer.cornerRadius = cornerRadius
        }
    }
    
    @IBInspectable public var rowHeight: Double = 45
    
    @IBInspectable public var rowBackgroundColor: UIColor = .clear {
        didSet {
            itemsTableView.backgroundColor = rowBackgroundColor
        }
    }
    
    // UIColor(red: 74.0/255.0, green: 74.0/255.0, blue: 74.0/255.0, alpha: 1.0)
    @IBInspectable public var itemTextColor: UIColor = .white
    
    public override init(frame: CGRect) {
        super.init(frame: frame)
        
        itemsTableView = UITableView(frame: frame, style: .grouped)
    }
    
    public required init(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)!
        
        itemsTableView = UITableView()
    }
    
    public override func layoutSubviews() {
        super.layoutSubviews()
        
        if !setuped {
            setupUI()
            setuped = true
        }
    }
}

extension ActionView {
    private func setupUI() {
        setupView()
        setupDataTableView()
    }
    
    private func setupView() {
       clipsToBounds = true
       layer.cornerRadius = cornerRadius
       layer.borderWidth = borderWidth
       layer.borderColor = borderColor.cgColor
   }
    
    private func setupDataTableView() {
        self.addSubview(itemsTableView)
        
        itemsTableView.snp.makeConstraints { maker in
            maker.leading.trailing.bottom.top.equalTo(self)
        }
        
        itemsTableView.delegate = self
        itemsTableView.dataSource = self
        itemsTableView.rowHeight = CGFloat(rowHeight)
        itemsTableView.separatorStyle = .none
        itemsTableView.separatorInset.left = 8
        itemsTableView.separatorInset.right = 8
        itemsTableView.backgroundColor = rowBackgroundColor
        itemsTableView.isScrollEnabled = scrollingEnabled
        itemsTableView.register(UITableViewCell.self, forCellReuseIdentifier: "OptionCell")
        itemsTableView.showsVerticalScrollIndicator = false
    }
}

extension ActionView: UITableViewDataSource {
    public func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
       return 1
   }
    
    public func numberOfSections(in tableView: UITableView) -> Int {
        return items.count
    }
   
   public func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
       let cell = tableView.dequeueReusableCell(withIdentifier: "OptionCell", for: indexPath)
       cell.textLabel?.text = items[indexPath.section]
       cell.textLabel?.textColor = itemTextColor
       cell.textLabel?.font = UIFont.systemFont(ofSize: 14)
       cell.tintColor = itemTextColor
       cell.backgroundColor = rowBackgroundColor
       cell.selectionStyle = .none
       cell.layer.borderWidth = 1.0
       cell.layer.borderColor = UIColor.gray.cgColor
       cell.layer.cornerRadius = cornerRadius
       cell.layer.masksToBounds = true
       
       return cell
   }
    
    public func tableView(_ tableView: UITableView, heightForHeaderInSection section: Int) -> CGFloat {
        return 5.0
    }
    
    public func tableView(_ tableView: UITableView, viewForHeaderInSection section: Int) -> UIView? {
        let view = UIView(frame: CGRect(x: 0, y: 0, width: tableView.frame.size.width, height: 1))
        view.backgroundColor = UIColor.clear
        return view
    }
    
    public func tableView(_ tableView: UITableView, heightForFooterInSection section: Int) -> CGFloat {
        return 5.0
    }
    
    public func tableView(_ tableView: UITableView, viewForFooterInSection section: Int) -> UIView? {
        let view = UIView(frame: CGRect(x: 0, y: 0, width: tableView.frame.size.width, height: 1))
        view.backgroundColor = UIColor.clear
        return view
    }
}

extension ActionView: UITableViewDelegate {
    public func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        return CGFloat(rowHeight)
    }
    
    public func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        selectedIndex = indexPath.section
        let selectedText = self.items[self.selectedIndex!]
        delegate?.actionView(self, didSelectItem: selectedText, atIndex: indexPath.section)
        tableView.reloadData()
    }
}
