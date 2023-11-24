# NEKaraoke

网易云信为您提供开源的示例项目，您可以参考本文档快速跑通示例项目，体验K歌房的效果。

## 开发环境要求
在开始运行示例项目之前，请确保开发环境满足以下要求：

| 环境要求                                                        | 说明                                                      |
| ------------------------------------------------------------ | ------------------------------------------------------------ |
|  iOS 版本  |  11.0 及以上的 iPhone 或者 iPad 真机   |
|  CPU 架构 | ARM64、ARMV7   |
| IDE | XCode   |
| 其他 | 安装 CocoaPods  |

## 前提条件

请确认您已完成以下操作：

- [已创建应用并获取AppKey](https://doc.yunxin.163.com/console/docs/TIzMDE4NTA?platform=console)
- [已开通IM 即时通讯、聊天室、音视频通话2.0 和 NERoom 房间组件](https://doc.yunxin.163.com/group-voice-room/docs/DM4NzM1Mzk?platform=iOS)
- [已配置 NERoom 的消息抄送地址（http://yiyong.netease.im/nemo/entertainmentLive/nim/notify）](https://doc.yunxin.163.com/docs/zU3Mjk0MTk/zYxNzIzMTE?platformId=121094)

    ![neroom消息抄送地址.png](https://yx-web-nosdn.netease.im/common/8cd222db079b0bbe16b3b246be350268/neroom消息抄送地址.png)
 
## 运行示例项目

> **注意**：
> 
>- 语聊房的示例源码仅供开发者接入参考，实际应用开发场景中，请结合具体业务需求修改使用。

>- 若您计划将源码用于生产环境，请确保应用正式上线前已经过全面测试，以免因兼容性等问题造成损失。
 
1. 克隆示例项目源码仓库至您本地工程。

> **注意**：

> 示例项目源码请存放至全英文的路径下。

2. 打开终端，在 `KaraokeKit/NEKaraoke/NEKaraoke/` 文件夹中执行如下命令进行安装：
```
     pod install
```
3. 完成安装后，通过 Xcode 打开 xxx.xcworkspace 工程

4. 在 `KaraokeKit/NEKaraoke/NEKaraoke/AppKey.swift` 中，替换您自己的 App Key 和 App Secret 。 
   

    ```
    // MARK: 请填写您的AppKey和AppSecret
    let APP_KEY: String = "your appkey" // 请填写应用对应的AppKey，可在云信控制台的“AppKey管理”页面获取
    let APP_SECRET: String = "your secret" // 请填写应用对应的AppSecret，可在云信控制台的“AppKey管理”页面获取

    // MARK: BASE_URL的默认地址仅用于跑通体验Demo，请勿用于正式产品上线。在产品上线前，请换为您自己实际的服务端地址
    let BASE_URL: String = "https://yiyong.netease.im" //云信派对服务端中国国内的体验地址

    ```


> **注意**：
> 
>- 获取 AppKey 和 AppSecret 的方法请参见<a href="https://doc.yunxin.163.com/console/docs/TIzMDE4NTA?platform=console#获取-appkey" target="_blank">创建应用并获取 AppKey</a>。
>- BASE_URL 地址 `https://yiyong.netease.im`和BASE_URL_OVERSEA 地址 `https://yiyong-sg.netease.im`为云信派对服务端体验地址，该地址仅用于体验 Demo，单次最大体验时长为1小时，**请勿用于生产环境**。
>- 如果您的应用的 AppKey 为海外，`IS_OVERSEA` 的值请设置为 `ture`。
    
5. 运行工程。

    建议在真机上运行，不支持模拟器调试。


## 示例项目结构

```
├── KaraokeKit
│   ├── NEAudioEffectKit // 音效模块
│   ├── NEAudioEffectUIKit // 音效UI模块
│   ├── NEKaraoke // Karaoke主工程
│   ├── NEKaraokeKit // Karaoke逻辑
│   ├── NEKaraokeUIKit // Karaoke UI
│   └── NELyricUIKit // 歌词UI
├── OneOnOne
│   └── NELoginSample // 登录模块
├── Party
│   └── NESocialUIKit // 娱乐社交通用UI
├── PodConfigs // cocoapos配置
├── UIKit
│   └── NEUIKit // 通用UI
└── third_party // 第三方依赖 

```
