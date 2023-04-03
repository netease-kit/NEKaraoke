# NEKaraoke

# 目录结构

```
> 对示例项目源码的目录结构进行说明，方便客户快速了解并定制。
┌
├── NEKaraokeUI.java                 # KaraokeUI入口 
├── NEKaraokeUIConstants.java        # 常量
├── activity
│   ├── AppStatusConstant.java       # App状态常量
│   ├── AppStatusManager.java        # App状态管理 
│   ├── BaseActivity.java            # Activity基础类
│   └── KaraokeRoomActivity.java     # Karaoke房间类
├── adapter
│   ├── BaseAdapter.java             #Adapter基类
│   ├── BaseViewHolder.java          # ViewHolder基类 
│   ├── OrderAdapter.java            # 点歌列表适配器
│   ├── OrderLoadMoreDecorator.java  # 加载更多
│   └── OrderedAdapter.java          # 已点歌曲列表适配器
├── chatroom
│   ├── ChatMessageSpannableStr.kt   # 聊天室消息文本管理
│   ├── ChatRoomMsgCreator.kt        # 聊天室消息构造器
│   └── VerticalImageSpan.kt         # 自定义ImageSpan
├── dialog
│   ├── ArrangeMicroDialog.kt         # 主播端排麦列表弹窗
│   ├── AudienceArrangeMicroDialog.kt # 观众端申请上麦列表弹窗
│   ├── BaseBottomDialog.kt            # 弹窗基类
│   ├── BaseBottomDialogFragment.java  # 弹窗基类
│   ├── BaseDialogFragment.java   #DialogFragment弹窗基类
│   ├── BottomBaseDialog.kt  # Dialog基类
│   ├── CommonDialog.java
│   ├── GiftDialog.kt        # 礼物弹窗
│   ├── OrderListFragment.java  # 点歌列表 
│   ├── OrderSongDialog.java  # 点歌弹窗
│   ├── OrderSongViewModel.kt  # 点歌ViewModel
│   └── OrderedListFragment.java  # 已点歌曲列表
├── fragment
│   ├── BaseFragment.kt              # Fragment基类
│   └── KaraokeRoomListFragment.java  # Karaoke房间列表
├── gift
│   ├── GiftCache.kt    # 礼物缓存
│   ├── GiftInfo.kt     # 礼物模型
│   ├── GiftRender.kt   # 礼物渲染 
│   └── ui
│       └── GifAnimationView.kt  # 礼物视图
├── helper
│   └── SeatHelper.java     # 麦位帮助类
├── list
│   ├── ApplySeatListAdapter.kt  # 麦位申请列表
│   ├── AudienceApplySeatListAdapter.kt # 麦位申请列表适配器
│   └── KaraokeListAdapter.java # 房间列表适配器
├── listener
│   ├── MyKaraokeListener.java  # Karaoke事件监听
│   ├── NEKaraokeCallbackWrapper.java # 回调类
├── model
│   ├── ApplySeatModel.java  # 申请麦位数据模型
│   ├── KaraokeOrderSongModel.java # 点歌模型
│   ├── KaraokeRoomModel.java # 房间模型
│   ├── LyricBusinessModel.java # 歌词业务模型
│   ├── OnSeatModel.java  # 麦位信息
│   └── VoiceRoomSeat.java # 麦位信息
├── statusbar
│   └── StatusBarConfig.java  # 状态栏配置
├── tone
│   ├── ToneContract.java   # 音效协议类
│   ├── ToneDialogFragment.java # 音效弹窗
│   └── ToneViewModel.java  # 音效ViewModel
├── utils                   # 工具类
│   ├── ClickUtils.kt       # 处理点击事件
├── view
│   ├── ChatMsgListAdapter.kt   # 聊天列表适配器
│   ├── ChatRoomMsgRecyclerView.kt # 聊天信息列表
│   ├── CircleImageView.java   # 圆形ImageView
│   ├── ExTextView.java   # 输入框
│   ├── FooterView.kt     # 自定义上拉加载UI
│   ├── GridRadioGroup.java  # 合唱选项选择器 
│   ├── HeadImageView.java  # 麦位头像
│   ├── HeaderView.kt  # 自定义下拉刷新UI
│   ├── ISingViewController.java  # 唱歌模块UI控制器
│   ├── LiveBaseAdapter.kt   # 适配器基类
│   ├── NESkipPreludeView.kt # 跳过前奏UI
│   ├── NESoloSingView.kt  # 独唱UI
│   ├── SeatView.java     # 麦位UI
│   ├── SingingControlView.java   # 房间页顶部视图
│   └── seat
│       ├── SeatAdapter.java  # 麦位UI适配器
│       └── SeatViewHolder.java  # 麦位UI ViewHolder
└── viewmodel
    └── KaraokeRoomViewModel.kt  # 房间ViewModel

```

# 开发环境要求
在开始运行示例项目之前，请确保开发环境满足以下要求：

| 环境要求                                                        | 说明                                                      |
| ------------------------------------------------------------ | ------------------------------------------------------------ |
|  JDK 版本  |  1.8.0 及以上版本   |
|  Android API 版本 | API 21、Android Studio 5.0 及以上版本   |
| CPU架构 | ARM 64、ARM V7   |
| IDE | Android Studio  |
| 其他 |  依赖 Androidx，不支持 support 库。android 系统 5.0 及以上版本的真机 |

# 前提条件

请确认您已完成以下操作：
- [已创建应用并获取AppKey](https://doc.yunxin.163.com/jcyOTA0ODM/docs/jcwMDQ2MTg)
- [已开通相关能力](https://doc.yunxin.163.com/docs/TA3ODAzNjE/zQ4MTI0Njc?platformId=50616)
- 已开通统一登录功能，具体请联系网易云信商务经理。
 
# 运行示例项目
>注意：
> * 在线 K 歌的示例源码仅供开发者接入参考，实际应用开发场景中，请结合具体业务需求修改使用。
>
> * 若您计划将源码用于生产环境，请确保应用正式上线前已经过全面测试，以免因兼容性等问题造成损失。
>

1. 克隆示例项目源码仓库至您本地工程。
2. 开启 Android 设备的开发者选项，通过 USB 连接线将 Android 设备接入电脑。
3. 通过 Android Studio 打开项目，在 app\src\main\java\com\netease\yunxin\app\karaoke\config\AppConfig.java  文件中配置应用的 App Key。
```
    private static final String APP_KEY = "your app key";
```
4. 在 Android Studio 中，单击 Sync Project with Gradle Files 按钮，同步工程依赖。
5. 选中设备直接运行，即可体验 Demo。
