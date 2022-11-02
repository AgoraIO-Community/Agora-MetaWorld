//
//  MCChatViewController.m
//  MetaChatDemo
//
//  Created by FanPengpeng on 2022/8/19.
//

#import "MCChatViewController.h"
#import "Masonry.h"
#import "MCChatCell.h"
#import <HyphenateChat/HyphenateChat.h>
#import "MCChatModel.h"
#import "MCUserInfo.h"

@interface MCChatViewController () <UITableViewDelegate, UITableViewDataSource, EMChatManagerDelegate,UITextFieldDelegate>

@property (weak, nonatomic) IBOutlet UIView *bgView;

@property (weak, nonatomic) IBOutlet UITableView *tableView;
@property (weak, nonatomic) IBOutlet UITextField *textField;
@property (strong, nonatomic) NSMutableArray *msgList;

@property (strong, nonatomic) NSDateFormatter *dateFormatter;

@end

@implementation MCChatViewController

- (instancetype)initWithCoder:(NSCoder *)coder {
    self = [super initWithCoder:coder];
    if (self) {
        [[EMClient sharedClient].chatManager addDelegate:self delegateQueue:nil];
    }
    return self;
}

- (void)dealloc {
    [[EMClient sharedClient].chatManager removeDelegate:self];
}

- (void)viewDidLoad {
    [super viewDidLoad];
    [self setUpUI];
}

- (void)setUpUI {
    self.tableView.tableFooterView = [UIView new];
    self.tableView.allowsSelection = NO;
    
    // blur
    UIBlurEffect *blurEffect = [UIBlurEffect effectWithStyle:UIBlurEffectStyleLight];
    UIVisualEffectView *visualView = [[UIVisualEffectView alloc] initWithEffect:blurEffect];
    visualView.userInteractionEnabled = NO;
    [self.bgView addSubview:visualView];
    self.bgView.clipsToBounds = YES;
    [visualView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.edges.mas_equalTo(self.bgView);
    }];
    [self.bgView sendSubviewToBack:visualView];
}

- (void)touchesEnded:(NSSet<UITouch *> *)touches withEvent:(UIEvent *)event {
    if (self.textField.isFirstResponder) {
        [self.textField resignFirstResponder];
    }else{
        [self dismissViewControllerAnimated:YES completion:nil];        
    }
}

- (IBAction)didClickSendButton:(UIButton *)sender {
    [self sendMessage];
}

- (NSString *)timeStrWithTimestamp:(NSTimeInterval)timestamp {
    return [self.dateFormatter stringFromDate:[NSDate dateWithTimeIntervalSince1970:timestamp]];
}

- (void)sendMessage{
    NSString *text = [self.textField.text stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet]];
    if (text == nil || text.length == 0) {
        return;
    }
    EMTextMessageBody *textMsgBody = [[EMTextMessageBody alloc] initWithText:text];
    EMChatMessage *msg = [[EMChatMessage alloc] initWithConversationID:self.roomId body:textMsgBody ext:@{@"img":self.userInfo.headImg,@"name":self.userInfo.nickname}];
    msg.chatType = EMChatTypeGroupChat;
    __weak typeof(self) weakSelf = self;
    [[EMClient sharedClient].chatManager sendMessage:msg progress:nil completion:^(EMChatMessage * _Nullable message, EMError * _Nullable error) {
        if (!error) {
            DLog("发送消息成功");
            MCChatModel *model = [MCChatModel new];
            model.text = text;
            model.isMe = YES;
            model.img = weakSelf.userInfo.headImg;
            model.name = self.userInfo.nickname;
            model.timeStr = [self timeStrWithTimestamp:[[NSDate date] timeIntervalSince1970]];
            [weakSelf.msgList addObject:model];
            [weakSelf.tableView reloadData];
            weakSelf.textField.text = nil;
            if ([weakSelf.delegate respondsToSelector:@selector(chatVC:didSendMessageContent:)]) {
                [weakSelf.delegate chatVC:weakSelf didSendMessageContent:text];
            }
        }else{
            DLog(@"发送消息结果======error == %zd",error.code);
        }
    }];
}

#pragma mark - text field delegate

- (BOOL)textFieldShouldReturn:(UITextField *)textField {
    [textField resignFirstResponder];
    [self sendMessage];
    return YES;
}

#pragma mark - EMChatManagerDelegate

- (void)messagesDidReceive:(NSArray<EMChatMessage *> *)aMessages {
    for (EMChatMessage *msg in aMessages) {
        if ([msg.body isKindOfClass:[EMTextMessageBody class]]) {
            NSDictionary *ext = msg.ext;
            EMTextMessageBody *body = (EMTextMessageBody *)msg.body;
            MCChatModel *model = [MCChatModel new];
            model.text = body.text;
            model.isMe = NO;
            model.name = ext[@"name"];
            model.img = ext[@"img"];
            model.timeStr = [self timeStrWithTimestamp:msg.localTime / 1000];
            [self.msgList addObject:model];
            [self.tableView reloadData];
            if ([self.delegate respondsToSelector:@selector(chatVC:didReceiveMessageContent:fromUserId:)]) {
                [self.delegate chatVC:self didReceiveMessageContent:body.text fromUserId:msg.from];
            }
        }
    }
}

#pragma mark - table view delegate & data source

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return self.msgList.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    MCChatCell *cell = [tableView dequeueReusableCellWithIdentifier:@"chatCell" forIndexPath:indexPath];
    MCChatModel *model = self.msgList[indexPath.row];
    [cell setNickname:model.name isMe:model.isMe time:model.timeStr msg: model.text img:model.img];
    return cell;
}

- (NSMutableArray *)msgList {
    if (!_msgList) {
        _msgList = [NSMutableArray array];
    }
    return _msgList;
}

- (NSDateFormatter *)dateFormatter {
    if (!_dateFormatter) {
        _dateFormatter = [[NSDateFormatter alloc] init];
        _dateFormatter.dateFormat = @"HH:mm";
    }
    return _dateFormatter;
}

@end
