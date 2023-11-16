// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation

struct NEAPIItem: NEAPIProtocol {
  let urlPath: String
  var url: String { NE.config.baseUrl + urlPath }
  let description: String
  let extra: String?
  var method: NEHttpMethod
  init(_ url: String,
       desc: String,
       method: NEHttpMethod = .post,
       extra: String? = nil) {
    urlPath = url
    self.method = method
    description = desc
    self.extra = extra
  }
}

enum NEAPI {
  // 房间模块
  enum Room {
    static let prePath = "/nemo/entertainmentLive/live"
    static let create = NEAPIItem("\(prePath)/createLive", desc: "创建房间")
    static let roomList = NEAPIItem("\(prePath)/list", desc: "获取房间列表")
    static let destroy = NEAPIItem("\(prePath)/destroyLive", desc: "结束房间")
    static let batchReward = NEAPIItem("\(prePath)/batch/reward", desc: "批量打赏功能")
    static let info = NEAPIItem("\(prePath)/info", desc: "获取房间详情")
  }

  // 音乐模块
  enum Music {
    static let pre_path = "/nemo/entertainmentLive/ktv/sing"

    static func start() -> NEAPIItem {
      NEAPIItem("\(pre_path)/start", desc: "开始唱歌")
    }

    static func invite() -> NEAPIItem {
      NEAPIItem("\(pre_path)/chorus/invite", desc: "发起合唱邀请")
    }

    static func join() -> NEAPIItem {
      NEAPIItem("\(pre_path)/chorus/join", desc: "加入ktv合唱")
    }

    static func cancelInvite() -> NEAPIItem {
      NEAPIItem("\(pre_path)/chorus/cancel", desc: "取消合唱邀请")
    }

    static func ready() -> NEAPIItem {
      NEAPIItem("\(pre_path)/chorus/ready", desc: "歌曲资源下载完成")
    }

    static func abandon() -> NEAPIItem {
      NEAPIItem("\(pre_path)/abandon", desc: "放弃演唱")
    }

    static func action() -> NEAPIItem {
      NEAPIItem("\(pre_path)/action", desc: "暂停/继续/结束演唱")
    }

    static func info() -> NEAPIItem {
      NEAPIItem("\(pre_path)/info", desc: "获取房间当前演唱信息")
    }
  }

  enum PickSong {
    static let pre_path = "/nemo/entertainmentLive/live/song"
    static func getMusicToken() -> NEAPIItem {
      NEAPIItem("\(pre_path)/getMusicToken", desc: "获取实时计算Token")
    }

    static func orderSong() -> NEAPIItem {
      NEAPIItem("\(pre_path)/orderSong", desc: "点歌")
    }

    static func getOrderedSongs(_ liveRecordId: String) -> NEAPIItem {
      NEAPIItem("\(pre_path)/getOrderSongs?liveRecordId=\(liveRecordId)", desc: "获取已点列表", method: .get)
    }

    static func deleteSong() -> NEAPIItem {
      NEAPIItem("\(pre_path)/cancelOrderSong", desc: "删除已点歌曲")
    }

    static func topSong() -> NEAPIItem {
      NEAPIItem("\(pre_path)/songSetTop", desc: "置顶歌曲")
    }

    static func switchSong() -> NEAPIItem {
      NEAPIItem("\(pre_path)/switchSong", desc: "切歌")
    }
  }
}
