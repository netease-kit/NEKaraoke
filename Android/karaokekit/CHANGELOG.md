# KaraokeKit Android ChangeLog

## v1.4.0(December 06, 2022)
### New Features
* 升级NERoom 1.10.0
* NEKaraokeListener回调中添加onMemberJoinChatroom(members: List<NEKaraokeMember>)
  ，onMemberLeaveChatroom(members: List<NEKaraokeMember>)方法

### Bug Fixes
* 修复断网后自动退出房间，再重新进入该房间，歌词组件卡住偶现问题

## v1.3.0(September 26, 2022)
### New Features
* 版权SDK接入咪咕渠道歌曲

## v1.2.0(Sept 02,2022)
### New Features
* 优化弱网环境K歌功能

### Bug Fixes
* 无网、弱网下SEI不准的问题。
* 音效未生效问题。

## v1.1.0(Aug 22,2022)
### New Features
* 物料缺失兜底逻辑。
* 全服广播新增主语。

## v1.0.0(Aug 01,2022)
### New Features
* 首次发布，支持快速搭建在线K歌工程，包含房间管理、麦位管理、K歌功能。