//
//  KTVContainerViewController.swift
//  MetaChatDemo
//
//  Created by FanPengpeng on 2022/7/11.
//

import UIKit
import JXSegmentedView
import Masonry


private let kContainerWidth: CGFloat = 360
private let kButtonWidth: CGFloat = 40
private let kSegmentedViewHeight: CGFloat = 50
private let kSegmentedViewLeft: CGFloat = 45
private let kSegmentedViewRight: CGFloat = 92



class KTVContainerViewController: UIViewController {
    
    var defaultSelectedIndex = 0
    var defaultChooseVCIndex = 0;
    var selectedIndexWhenDismiss: ((_ index: Int, _ chooseIndex: Int)->())?
    
    private var segmentedDataSource: JXSegmentedNumberDataSource?
    private let segmentedView = JXSegmentedView()
    private let chooseContainerVC = KTVChooseSongContainerVC()
    
    private lazy var listContainerView: JXSegmentedListContainerView! = {
        let containerView = JXSegmentedListContainerView(dataSource: self)
        return containerView
    }()
    
    // 搜索
    private lazy var searchButton:UIButton = {
        let button = UIButton(type: .custom)
        button.setImage(UIImage(named: "search"), for: .normal)
        button.addTarget(self, action: #selector(didClickSearchButton), for: .touchUpInside)
        return button
    }()
    
    // 控制台
    private lazy var consoleButton:UIButton = {
        let button = UIButton(type: .custom)
        button.setImage(UIImage(named: "console"), for: .normal)
        button.addTarget(self, action: #selector(didClickConsoleButton), for: .touchUpInside)
        return button
    }()
    
    // 版权来源
    private lazy var sourceLabel:UILabel = {
        let label = UILabel()
        label.backgroundColor = UIColor(hexRGB: 0x000000, alpha: 0.5)
        label.textColor = UIColor(hexRGB: 0xC4C4C4)
        label.font = UIFont.systemFont(ofSize: 10)
        label.textAlignment = .center
        label.text = "The song is from Migu Music"
        return label
    }()

    override func viewDidLoad() {
        super.viewDidLoad()
        setUpUI()
        configDefaultIndex()
        addObserver()
    }

    
    private func setUpUI(){
        view.backgroundColor = .clear

        self.ktv_setBlurBackground()
        self.ktv_tapBlankAction {[weak self] in
            self?.dismiss(animated: true)
            if let currentIndex = self?.segmentedView.selectedIndex, let chooseIndex = self?.chooseContainerVC.selectedIndex {
                DLog("currentIndex ==== ",currentIndex, chooseIndex)
                self?.selectedIndexWhenDismiss?(currentIndex, chooseIndex)
            }
        }

        //配置数据源
        let dataSource = JXSegmentedNumberDataSource()
        dataSource.isTitleColorGradientEnabled = true
        let count = KTVDataManager.shared().localMusicList.count;
        let selectedStr = NSLocalizedString("Selected", comment: "")
        let chosenTitle = count > 0 ? "\(selectedStr) \(count)" : "\(selectedStr)"
        dataSource.titles = [NSLocalizedString("Choose a song", comment: ""),chosenTitle]
        dataSource.numbers = [0,0];
        dataSource.titleNormalFont = UIFont.systemFont(ofSize: 12)
        dataSource.titleSelectedFont = UIFont.boldSystemFont(ofSize: 16)
        dataSource.titleNormalColor = UIColor(hexRGB: 0xffffff, alpha: 0.6)
        dataSource.titleSelectedColor = UIColor(hexRGB: 0xffffff)
        segmentedDataSource = dataSource
        
        let indicator = JXSegmentedIndicatorLineView()
        indicator.indicatorColor = UIColor(hexRGB: 0xffffff)
        segmentedView.indicators = [indicator]
        segmentedView.backgroundColor = .clear
        
        segmentedView.dataSource = dataSource
        segmentedView.delegate = self

        segmentedView.listContainer = listContainerView
        view.addSubview(listContainerView)
        listContainerView.snp.makeConstraints { make in
            make.left.equalToSuperview()
            make.top.equalTo(kSegmentedViewHeight)
            make.width.equalTo(kContainerWidth)
            make.bottom.equalToSuperview()
        }
        
        view.addSubview(segmentedView)
        segmentedView.snp.makeConstraints { make in
            make.left.equalTo(kSegmentedViewLeft)
            make.right.equalTo(listContainerView).offset(-kSegmentedViewRight)
            make.top.equalToSuperview()
            make.height.equalTo(kSegmentedViewHeight)
        }
        
        // 搜索按钮
        searchButton.isHidden = true
        view.addSubview(searchButton)
        searchButton.snp.makeConstraints { make in
            make.right.equalTo(listContainerView).offset(-40)
            make.centerY.equalTo(segmentedView)
            make.width.height.equalTo(kButtonWidth)
        }
        
        // 控制台按钮
        view.addSubview(consoleButton)
        consoleButton.snp.makeConstraints { make in
            make.right.equalTo(listContainerView).offset(-6)
            make.centerY.equalTo(segmentedView)
            make.width.height.equalTo(kButtonWidth)
        }
        
        // 版权来源
        /*
        view.addSubview(sourceLabel)
        sourceLabel.snp.makeConstraints { make in
            make.left.right.bottom.equalTo(listContainerView)
            make.height.equalTo(24)
        }
         */
    }
    
    private func addObserver(){
        NotificationCenter.default.addObserver(forName: NSNotification.Name(kLocalMusicListDidChangeNotificationName), object: nil, queue: nil) { notify in
            DispatchQueue.main.async {
                self.hanldeNumberRefresh()
            }
        }
    }
    
    private func configDefaultIndex(){
        print("defaultSelectedIndex ==== \(defaultSelectedIndex)")
        segmentedView.defaultSelectedIndex = defaultSelectedIndex;
        listContainerView.defaultSelectedIndex = defaultSelectedIndex;
    }
    
    //MARK: 数字刷新
    @objc func hanldeNumberRefresh(){
        if let _segDataSource = segmentedDataSource {
            let count = KTVDataManager.shared().localMusicList.count;
            let selectedStr = NSLocalizedString("Selected", comment: "")
            let chosenTitle = count > 0 ? "\(selectedStr) \(count)" : "\(selectedStr)"
            _segDataSource.titles = [NSLocalizedString("Choose a song", comment: ""),chosenTitle]
            segmentedView.reloadDataWithoutListContainer()
        }
    }
    
    // MARK: - actions
    
    // 点击背景
    @objc private func didTapedBgView(){
        dismiss(animated: true)
    }
    
    // 点击搜索按钮
    @objc private func didClickSearchButton(){
        let searchVC = KTVSearchSongVC()
//        self.present(searchVC, animated: true)
        navigationController?.pushViewController(searchVC, animated: false)
    }
    
    // 点击控制台按钮
    @objc private func didClickConsoleButton(){
        let consoleVC = KTVConsoleVC()
        navigationController?.pushViewController(consoleVC, animated: false)
    }

}


extension KTVContainerViewController: JXSegmentedViewDelegate {
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

extension KTVContainerViewController: JXSegmentedListContainerViewDataSource {
    func numberOfLists(in listContainerView: JXSegmentedListContainerView) -> Int {
        if let titleDataSource = segmentedView.dataSource as? JXSegmentedBaseDataSource {
            return titleDataSource.dataSource.count
        }
        return 0
    }

    func listContainerView(_ listContainerView: JXSegmentedListContainerView, initListAt index: Int) -> JXSegmentedListContainerViewListDelegate {
        switch index {
        case 1:
            let vc = KTVHaveChosenVC() as! JXSegmentedListContainerViewListDelegate
            return vc
        default:
            chooseContainerVC.defaultSelectedIndex = defaultChooseVCIndex
            return chooseContainerVC
        }
    }
}
