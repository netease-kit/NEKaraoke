// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation
import NERoomKit

/// 麦位扩展
public extension NEKaraokeKit {
  /// 申请上麦
  /// - Parameter callback: 回调
  func requestSeat(_ callback: NEKaraokeCallback<AnyObject>? = nil) {
    NEKaraokeLog.apiLog(kitTag, desc: "Apply on seat.")
    Judge.preCondition({
      self.roomContext!.seatController.submitSeatRequest { code, msg, _ in
        if code == 0 {
          NEKaraokeLog.successLog(kitTag, desc: "Successfully apply on seat.")
        } else {
          NEKaraokeLog.errorLog(
            kitTag,
            desc: "Failed to apply on seat. Code: \(code). Msg: \(msg ?? "")"
          )
        }
        callback?(code, msg, nil)
      }
    }, failure: callback)
  }

  /// 取消申请上麦
  /// - Parameter callback: 回调
  func cancelRequestSeat(_ callback: NEKaraokeCallback<AnyObject>? = nil) {
    NEKaraokeLog.apiLog(kitTag, desc: "Cancel apply seat.")
    Judge.preCondition({
      self.roomContext!.seatController.cancelSeatRequest { code, msg, _ in
        if code == 0 {
          NEKaraokeLog.successLog(kitTag, desc: "Successfully cancel apply seat.")
        } else {
          NEKaraokeLog.errorLog(
            kitTag,
            desc: "Failed to cancel apply seat. Code: \(code). Msg: \(msg ?? "")"
          )
        }
        callback?(code, msg, nil)
      }
    }, failure: callback)
  }

  /// 同意申请上麦
  /// - Parameters:
  ///   - account: 同意上麦的用户Id
  ///   - callback: 回调
  func approveRequestSeat(account: String,
                          callback: NEKaraokeCallback<AnyObject>? = nil) {
    NEKaraokeLog.apiLog(kitTag, desc: "Agree on seat. UserUuid: \(account)")
    Judge.preCondition({
      self.roomContext!.seatController.approveSeatRequest(account, callback: { code, msg, _ in
        if code == 0 {
          NEKaraokeLog.successLog(kitTag, desc: "Successfully agree on seat.")
        } else {
          NEKaraokeLog.errorLog(
            kitTag,
            desc: "Failed to agree on seat. Code: \(code). Msg: \(msg ?? "")"
          )
        }
        callback?(code, msg, nil)
      })
    }, failure: callback)
  }

  /// 拒绝申请上麦
  /// - Parameters:
  ///   - account: 被拒绝上麦的用户Id
  ///   - callback: 回调
  func rejectRequestSeat(account: String,
                         callback: NEKaraokeCallback<AnyObject>? = nil) {
    NEKaraokeLog.apiLog(kitTag, desc: "Refuse on seat. UserUuid: \(account)")

    Judge.preCondition({
      self.roomContext!.seatController.rejectSeatRequest(account) { code, msg, _ in
        if code == 0 {
          NEKaraokeLog.successLog(kitTag, desc: "Successfully refuse on seat.")
        } else {
          NEKaraokeLog.errorLog(
            kitTag,
            desc: "Failed to refuse on seat. Code: \(code). Msg: \(msg ?? "")"
          )
        }
        callback?(code, msg, nil)
      }
    }, failure: callback)
  }

  /// 踢下麦
  /// - Parameters:
  ///   - account: 被踢下麦的用户Id
  ///   - callback: 回调
  func kickSeat(account: String,
                callback: NEKaraokeCallback<AnyObject>? = nil) {
    NEKaraokeLog.apiLog(kitTag, desc: "Kick seat. UserUuid: \(account).")
    Judge.preCondition({
      self.roomContext!.seatController.kickSeat(account) { code, msg, _ in
        if code == 0 {
          NEKaraokeLog.successLog(kitTag, desc: "Successfully kick seat.")
        } else {
          NEKaraokeLog.errorLog(
            kitTag,
            desc: "Failed to kick seat. Code: \(code). Msg: \(msg ?? "")"
          )
        }
        callback?(code, msg, nil)
      }
    }, failure: callback)
  }

  /// 下麦
  /// - Parameter callback: 回调
  func leaveSeat(_ callback: NEKaraokeCallback<AnyObject>? = nil) {
    NEKaraokeLog.apiLog(kitTag, desc: "Leave seat.")

    Judge.preCondition({
      self.roomContext!.seatController.leaveSeat { code, msg, _ in
        if code == 0 {
          NEKaraokeLog.successLog(kitTag, desc: "Successfully leaveseat.")
        } else {
          NEKaraokeLog.errorLog(
            kitTag,
            desc: "Failed to leave seat. Code: \(code). Msg: \(msg ?? "")"
          )
        }
        callback?(code, msg, nil)
      }
    }, failure: callback)
  }

  /// 获取麦位信息
  /// - Parameter callback: 回调
  func getSeatInfo(_ callback: NEKaraokeCallback<NEKaraokeSeatInfo>? = nil) {
    NEKaraokeLog.apiLog(kitTag, desc: "Get seat info.")
    Judge.preCondition({
      self.roomContext!.seatController.getSeatInfo { code, msg, info in
        if code == 0 {
          guard let info = info else {
            NEKaraokeLog.errorLog(
              kitTag,
              desc: "Failed to get seat info. Data structure error."
            )
            callback?(
              NEKaraokeErrorCode.failed,
              "Failed to get seat info. Data structure error",
              nil
            )
            return
          }
          NEKaraokeLog.successLog(kitTag, desc: "Successfully get seat info.")
          callback?(code, nil, NEKaraokeSeatInfo(info))
        } else {
          NEKaraokeLog.errorLog(
            kitTag,
            desc: "Failed to get seat info. Code: \(code). Msg: \(msg ?? "")"
          )
          callback?(code, msg, nil)
        }
      }
    }, failure: callback)
  }

  /// 获取请求麦位列表
  /// - Parameter callback: 回调
  func getSeatRequestList(_ callback: NEKaraokeCallback<[NEKaraokeSeatRequestItem]>? = nil) {
    NEKaraokeLog.apiLog(kitTag, desc: "Get seat request list.")
    Judge.preCondition({
      self.roomContext!.seatController.getSeatRequestList { code, msg, items in
        if code == 0 {
          guard let items = items else {
            NEKaraokeLog.errorLog(
              kitTag,
              desc: "Failed to seat request list. Data structure error."
            )
            callback?(
              NEKaraokeErrorCode.failed,
              "Failed to seat request list. Data structure error",
              nil
            )
            return
          }
          let requestItems = items.map { item in
            NEKaraokeSeatRequestItem(item)
          }
          NEKaraokeLog.successLog(kitTag, desc: "Successfully seat request list.")
          callback?(code, nil, requestItems)
        } else {
          NEKaraokeLog.errorLog(
            kitTag,
            desc: "Failed to get seat request list. Code: \(code). Msg: \(msg ?? "")"
          )
          callback?(code, msg, nil)
        }
      }
    }, failure: callback)
  }
}

extension NEKaraokeKit: NESeatEventListener {
//  public func onSeatManagerAdded(_ managers: [String]) {
//
//  }
//
//  public func onSeatManagerRemoved(_ managers: [String]) {
//
//  }
//
//  public func onSeatInvitationReceived(_ seatIndex: Int, user: String, operateBy: String) {
//
//  }
//
//  public func onSeatInvitationCancelled(_ seatIndex: Int, user: String, operateBy: String) {
//
//  }
//
//  public func onSeatInvitationAccepted(_ seatIndex: Int, user: String, isAutoAgree: Bool) {
//
//  }
//
//  public func onSeatInvitationRejected(_ seatIndex: Int, user: String) {
//
//  }

  public func onSeatRequestSubmitted(_ seatIndex: Int, user: String) {
    for pointerListener in listeners.allObjects {
      guard pointerListener is NEKaraokeListener, let listener = pointerListener as? NEKaraokeListener else { continue }
      if listener
        .responds(to: #selector(NEKaraokeListener.onSeatRequestSubmitted(_:account:))) {
        listener.onSeatRequestSubmitted?(seatIndex, account: user)
      }
    }
  }

  public func onSeatRequestCancelled(_ seatIndex: Int, user: String) {
    for pointerListener in listeners.allObjects {
      guard pointerListener is NEKaraokeListener, let listener = pointerListener as? NEKaraokeListener else { continue }
      if listener
        .responds(to: #selector(NEKaraokeListener.onSeatRequestCancelled(_:account:))) {
        listener.onSeatRequestCancelled?(seatIndex, account: user)
      }
    }
  }

  public func onSeatRequestApproved(_ seatIndex: Int, user: String, operateBy: String, isAutoAgree: Bool) {
    for pointerListener in listeners.allObjects {
      guard pointerListener is NEKaraokeListener, let listener = pointerListener as? NEKaraokeListener else { continue }
      if listener
        .responds(to: #selector(NEKaraokeListener
            .onSeatRequestApproved(_:account:operateBy:))) {
        listener.onSeatRequestApproved?(seatIndex, account: user, operateBy: operateBy)
      }
    }
  }

  public func onSeatRequestRejected(_ seatIndex: Int, user: String, operateBy: String) {
    for pointerListener in listeners.allObjects {
      guard pointerListener is NEKaraokeListener, let listener = pointerListener as? NEKaraokeListener else { continue }
      if listener
        .responds(to: #selector(NEKaraokeListener
            .onSeatRequestRejected(_:account:operateBy:))) {
        listener.onSeatRequestRejected?(seatIndex, account: user, operateBy: operateBy)
      }
    }
  }

  public func onSeatLeave(_ seatIndex: Int, user: String) {
    for pointerListener in listeners.allObjects {
      guard pointerListener is NEKaraokeListener, let listener = pointerListener as? NEKaraokeListener else { continue }

      if listener.responds(to: #selector(NEKaraokeListener.onSeatLeave(_:account:))) {
        listener.onSeatLeave?(seatIndex, account: user)
      }
    }
  }

  public func onSeatKicked(_ seatIndex: Int, user: String, operateBy: String) {
    for pointerListener in listeners.allObjects {
      guard pointerListener is NEKaraokeListener, let listener = pointerListener as? NEKaraokeListener else { continue }
      if listener
        .responds(to: #selector(NEKaraokeListener.onSeatKicked(_:account:operateBy:))) {
        listener.onSeatKicked?(seatIndex, account: user, operateBy: operateBy)
      }
    }
  }

  public func onSeatListChanged(_ seatItems: [NESeatItem]) {
    let items: [NEKaraokeSeatItem] = seatItems.map { NEKaraokeSeatItem($0) }

    for pointerListener in listeners.allObjects {
      guard pointerListener is NEKaraokeListener, let listener = pointerListener as? NEKaraokeListener else { continue }

      if listener.responds(to: #selector(NEKaraokeListener.onSeatListChanged(_:))) {
        listener.onSeatListChanged?(items)
      }
    }
    guard let context = roomContext else { return }
    var isOnSeat = false
    for item in items {
      if item.user == context.localMember.uuid {
        if item.status == .taken {
          isOnSeat = true
        } else {}
      }
    }
    context.rtcController.setClientRole(isOnSeat ? .broadcaster : .audience)
  }
}
