# NEKaraoke

# 目录结构
> 对示例项目源码的目录结构进行说明，方便客户快速了解并定制。

```
┌
├── NEKaraokeCreateViewController              # 创建K歌房视图控制器
│   ├── NEKaraokeCreateTextField               # 房间名输入框
│   ├── NEKaraokeCreateCheckBox                # 合唱模式选择器
│   └── UIButton                               # 创建房间按钮
│
├── NEKaraokeViewController                    # K歌房视图控制器
│   ├── NEKaraokeHeaderView                    # 头部视图，包含人数、房间名等信息
│   ├── NEKaraokeControlView                   # 控制视图，包含调音、暂停、切歌、原唱按钮
│   ├── NEKaraokeSeatView                      # 麦位信息视图
│   ├── NEKaraokeInputToolBar                  # 底部工具栏，包含输入框等控件
│   ├── NEKaraokeChatView                      # 聊天室视图，显示系统通知消息、普通文本消息以及礼物消息
│   ├── NEKaraokeKeyboardToolbarView           # 聊天室文本输入框
│   ├── NEKaraokeAnimationView                 # 礼物动画视图
│   ├── NEKaraokeRoomInfo                      # 当前K歌房信息
│   ├── NEAudioEffectViewController            # 音效调节界面
│   ├── NEAudioEffectManager                   # 音效管理类
│   ├── NSMutableArray<NEKaraokeSeatItem *>    # 麦位信息
│   ├── NEKaraokeOrderSongModel                # 当前演唱歌曲信息
│   └── NEKaraokeLyricActionView               # 核心控制视图
│       ├── NEKaraokeChooseView                # 核心控制视图 - 点歌视图
│       ├── NEKaraokeChorusWaitView            # 核心控制视图 - 合唱准备视图
│       ├── NEKaraokeLyricView                 # 核心控制视图 - 歌词、打分展示视图
│       ├── NEKaraokeMatchView                 # 核心控制视图 - 合唱匹配视图
│       ├── NEKaraokeNoLyricView               # 核心控制视图 - 无歌词提示视图
│       └── NEKaraokeWaitView                  # 核心控制视图 - 独唱准备视图
|
├── NEKaraokeListViewController                # K歌房列表视图控制器
│   ├── NEKaraokeListEmptyView                 # 空列表提示视图
│   └── NEKaraokeListViewCell                  # K歌房信息预览
│
└── NEKaraokePickSongView                      # 点歌台弹出框  

```
# 开发环境要求
在开始运行示例项目之前，请确保开发环境满足以下要求：

| 环境要求                                                        | 说明                                                      |
| ------------------------------------------------------------ | ------------------------------------------------------------ |
|  iOS 版本  |  11.0 及以上的 iPhone 或者 iPad 真机   |
|  CPU 架构 | ARM64、ARMV7   |
| IDE | XCode   |
| 其他 | 安装 CocoaPods  |

# 前提条件

请确认您已完成以下操作：
- [已创建应用并获取AppKey](https://doc.yunxin.163.com/jcyOTA0ODM/docs/jcwMDQ2MTg)
- [已开通相关能力](https://doc.yunxin.163.com/docs/TA3ODAzNjE/zQ4MTI0Njc?platformId=50616)
- 已开通统一登录功能，具体请联系网易云信商务经理。
 
# 运行示例项目
注意：
 * 在线 K 歌的示例源码仅供开发者接入参考，实际应用开发场景中，请结合具体业务需求修改使用。
 * 若您计划将源码用于生产环境，请确保应用正式上线前已经过全面测试，以免因兼容性等问题造成损失。
 
1. 克隆示例项目源码仓库至您本地工程。
2. 在 Podfile 文件中添加类似如下命令导入目标文件。
```
    pod 'NEKaraokeKit';
```
3. 打开终端，在 Podfile 所在文件夹中执行如下命令进行安装：
```
     pod update
```
4. 完成安装后，通过 Xcode 打开 xxx.xcworkspace 工程

5. 在 NEKaraoke/AppDelegate.m中，替换您自己的 App Key，获取 App Key 的方法请参见获取 App Key。
```
     static const NSString *kAppKey = @"";
```
6. 运行工程。
    > 建议在真机上运行，不支持模拟器调试。
