// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation
import NERoomKit

@objc
/// SDK API的通用回调接口。SDK提供的接口多为异步实现，在调用这些接口时，需要提供一个该接口的实现作为回调参数
public protocol NEKaraokeListener: NSObjectProtocol {
  /// 成员进入房间回调
  /// - Parameter members: 成员列表
  @objc optional func onMemberJoinRoom(_ members: [NEKaraokeMember])

  /// 成员离开房间回调
  /// - Parameter members: 成员列表
  @objc optional func onMemberLeaveRoom(_ members: [NEKaraokeMember])

  /// 成员进入聊天室回调
  /// - Parameter members: 成员列表
  @objc optional func onMemberJoinChatroom(_ members: [NEKaraokeMember])

  /// 成员离开聊天室回调
  /// - Parameter members: 成员列表
  @objc optional func onMemberLeaveChatroom(_ members: [NEKaraokeMember])

  /// 房间结束回调
  /// - Parameter reason: 房间结束原因
  @objc optional func onRoomEnded(_ reason: NEKaraokeEndReason)

  /// Rtc频道错误回调
  /// - Parameter code: 错误码
  @objc optional func onRtcChannelError(_ code: Int)
  /// 本地音乐文件播放结束
  @objc optional func onSongPlayingCompleted()

  /// 本端音频输出设备变更通知，如切换到扬声器、听筒、耳机等
  /// - Parameter device: 音频输出类型
  @objc optional func onAudioOutputDeviceChanged(_ device: NEKaraokeAudioOutputDevice)

  /// 音效播放进度更新
  /// - Parameters:
  ///   - effectId: 音效Id
  ///   - timeStampMS: 播放进度
  @objc optional func onRtcAudioEffectTimestampUpdate(effectId: UInt32, timeStampMS: UInt64)

  /// 成员音频关闭回调
  /// - Parameters:
  ///   - member: 成员信息
  ///   - mute: 是否关闭
  ///
  ///   添加监听后，成员音频状态变更会触发此方法
  @objc optional func onMemberAudioMuteChanged(_ member: NEKaraokeMember,
                                               mute: Bool, operateBy: NEKaraokeMember?)

  /// 聊天室消息回调
  /// - Parameter message: 文本消息
  ///
  /// 添加监听后，收到聊天室消息会触发此方法
  @objc optional func onReceiveTextMessage(_ message: NEKaraokeChatTextMessage)

  /// 接收到合唱回调
  /// - Parameters:
  ///   - actionType: 合唱状态
  ///   - songModel: 消息模型
  @objc optional func onReceiveChorusMessage(_ actionType: NEKaraokeChorusActionType,
                                             songModel: NEKaraokeSongModel)

  /// 接收到发送批量礼物的回调
  /// - Parameter giftModel: 礼物模型
  @objc optional func onReceiveBatchGift(giftModel: NEKaraokeBatchGiftModel)
  /// 当前歌曲播放进度
  /// - Parameters:
  ///   - postion: 歌曲播放进度
  @objc optional func onSongPlayingPosition(_ postion: UInt64)

  /// 音频帧数据
  /// - Parameter frame: 音频帧数据
  @objc optional func onRecordingAudioFrame(frame: NEKaraokeAudioFrame)

  /// 成员[user]提交了位置为[seatIndex]的麦位申请
  /// - Parameters:
  ///   - seatIndex: 麦位位置，**-1**表示未指定位置
  ///   - account: 申请人的用户ID
  @objc optional func onSeatRequestSubmitted(_ seatIndex: Int, account: String)

  /// 成员[user]取消了位置为[seatIndex]的麦位申请
  /// - Parameters:
  ///   - seatIndex: 麦位位置，**-1**表示未指定位置
  ///   - account: 申请人的用户ID
  @objc optional func onSeatRequestCancelled(_ seatIndex: Int, account: String)

  /// 管理员通过了成员[user]的麦位申请，位置为[seatIndex]
  /// - Parameters:
  ///   - seatIndex: 麦位位置
  ///   - account: 申请人的用户ID
  ///   - operateBy: 同意该申请的用户ID
  @objc optional func onSeatRequestApproved(_ seatIndex: Int, account: String, operateBy: String)

  /// 管理员拒绝了成员[user]的麦位申请，位置为[seatIndex]
  /// - Parameters:
  ///   - seatIndex: 麦位位置，**-1**表示未指定位置
  ///   - account: 申请人的用户ID
  ///   - operateBy: 拒绝该申请的用户ID
  @objc optional func onSeatRequestRejected(_ seatIndex: Int, account: String, operateBy: String)

  /// 成员下麦，位置为[seatIndex]
  /// - Parameters:
  ///   - seatIndex: 麦位位置
  ///   - account: 下麦成员
  @objc optional func onSeatLeave(_ seatIndex: Int, account: String)

  /// 成员[user]被[operateBy]从位置为[seatIndex]的麦位踢掉
  /// - Parameters:
  ///   - seatIndex: 麦位位置
  ///   - account: 成员
  ///   - operateBy: 操作人
  @objc optional func onSeatKicked(_ seatIndex: Int, account: String, operateBy: String)

  /// 麦位变更通知
  /// - Parameter seatItems: 麦位列表
  @objc optional func onSeatListChanged(_ seatItems: [NEKaraokeSeatItem])

  /// 已点列表的更新
  @objc optional func onSongListChanged()

  /// 点歌
  /// - Parameter song: 歌曲
  @objc optional func onSongOrdered(_ song: NEKaraokeOrderSongModel?)

  /// 已点列表的删除
  /// - Parameter song: 歌曲
  @objc optional func onSongDeleted(_ song: NEKaraokeOrderSongModel?)

  /// 已点列表的置顶
  /// - Parameter song: 歌曲
  @objc optional func onSongTopped(_ song: NEKaraokeOrderSongModel?)

  /// 切歌
  /// - Parameter song: 被切歌曲
  @objc optional func onNextSong(_ song: NEKaraokeOrderSongModel?)
}
