//
//  KTVChooseSongVC.swift
//  MetaChatDemo
//
//  Created by FanPengpeng on 2022/7/11.
//

import UIKit
import JXSegmentedView
import SnapKit

private let kContainerWidth: CGFloat = 360
private let kButtonWidth: CGFloat = 40
private let kSegmentedViewHeight: CGFloat = 40
private let kSegmentedViewLeft: CGFloat = 45
private let kSegmentedViewRight: CGFloat = 92

class KTVChooseSongContainerVC: UIViewController {
    
    var defaultSelectedIndex = 0
    var selectedIndex: Int {
        get{
            segmentedView.selectedIndex
        }
    }
    
    private var segmentedDataSource: JXSegmentedBaseDataSource?
    private let segmentedView = JXSegmentedView()
    private lazy var listContainerView: JXSegmentedListContainerView! = {
        let containerView = JXSegmentedListContainerView(dataSource: self)
        return containerView
    }()
    
//    private let titles = [NSLocalizedString(kHotTypeRecommend, comment: ""),NSLocalizedString(kHotTypeDouyinHot, comment: ""),NSLocalizedString(kHotTypeClassic, comment: ""),NSLocalizedString(kHotTypeKTV, comment: "")]

    private let titles = [NSLocalizedString(kHotTypeRecommend, comment: ""),NSLocalizedString(kHotTypeDouyinHot, comment: ""),NSLocalizedString(kHotTypeClassic, comment: "")]
    
    override func viewDidLoad() {
        super.viewDidLoad()
        setUpUI()
        configDefaultIndex()
    }
    
    private func setUpUI(){
        view.backgroundColor = .clear

        //配置数据源
        let dataSource = JXSegmentedTitleDataSource()
        dataSource.isTitleColorGradientEnabled = true
        dataSource.titles = titles
        dataSource.titleNormalFont = UIFont.systemFont(ofSize: 12)
        dataSource.titleSelectedFont = UIFont.boldSystemFont(ofSize: 12)
        dataSource.titleNormalColor = UIColor(hexRGB: 0xffffff, alpha: 0.6)
        dataSource.titleSelectedColor = UIColor(hexRGB: 0xffffff)
        segmentedDataSource = dataSource
        
        segmentedView.backgroundColor = .clear
        segmentedView.dataSource = dataSource
        segmentedView.delegate = self
        
        view.addSubview(segmentedView)
        segmentedView.snp.makeConstraints { make in
            make.left.equalTo(kSegmentedViewLeft)
            make.top.equalToSuperview()
            make.width.equalTo(self.blurWidth - kSegmentedViewLeft)
            make.height.equalTo(kSegmentedViewHeight)
        }

        segmentedView.listContainer = listContainerView
        view.addSubview(listContainerView)
        listContainerView.snp.makeConstraints { make in
            make.right.equalToSuperview()
            make.left.equalToSuperview()
            make.top.equalTo(kSegmentedViewHeight)
            make.bottom.equalToSuperview()
        }
    }
    
    private func configDefaultIndex(){
        DLog("defaultSelectedIndex ==== \(defaultSelectedIndex)")
        segmentedView.defaultSelectedIndex = defaultSelectedIndex;
        listContainerView.defaultSelectedIndex = defaultSelectedIndex;
    }
}



extension KTVChooseSongContainerVC: JXSegmentedListContainerViewListDelegate {
    func listView() -> UIView {
        return view
    }
}



extension KTVChooseSongContainerVC: JXSegmentedViewDelegate {
    func segmentedView(_ segmentedView: JXSegmentedView, didSelectedItemAt index: Int) {
        if let dotDataSource = segmentedDataSource as? JXSegmentedDotDataSource {
            //先更新数据源的数据
            dotDataSource.dotStates[index] = false
            //再调用reloadItem(at: index)
            segmentedView.reloadItem(at: index)
        }

//        navigationController?.interactivePopGestureRecognizer?.isEnabled = (segmentedView.selectedIndex == 0)
    }
}

extension KTVChooseSongContainerVC: JXSegmentedListContainerViewDataSource {
    
    func numberOfLists(in listContainerView: JXSegmentedListContainerView) -> Int {
        if let titleDataSource = segmentedView.dataSource as? JXSegmentedBaseDataSource {
            return titleDataSource.dataSource.count
        }
        return 0
    }

    func listContainerView(_ listContainerView: JXSegmentedListContainerView, initListAt index: Int) -> JXSegmentedListContainerViewListDelegate {
        let vc = KTVChooseSongVC()
        vc.title = titles[index]
        return vc as! JXSegmentedListContainerViewListDelegate
    }
}
