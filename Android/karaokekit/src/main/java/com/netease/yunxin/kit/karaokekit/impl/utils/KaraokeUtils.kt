/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.karaokekit.impl.utils

import android.text.TextUtils
import com.netease.yunxin.kit.karaokekit.api.NEKaraokeKit
import com.netease.yunxin.kit.karaokekit.api.NEKaraokeRole
import com.netease.yunxin.kit.karaokekit.api.model.NEKaraokeAnchor
import com.netease.yunxin.kit.karaokekit.api.model.NEKaraokeBatchGiftModel
import com.netease.yunxin.kit.karaokekit.api.model.NEKaraokeLiveModel
import com.netease.yunxin.kit.karaokekit.api.model.NEKaraokeMember
import com.netease.yunxin.kit.karaokekit.api.model.NEKaraokeOrderSongOperatorUser
import com.netease.yunxin.kit.karaokekit.api.model.NEKaraokeRoomInfo
import com.netease.yunxin.kit.karaokekit.api.model.NEKaraokeRoomList
import com.netease.yunxin.kit.karaokekit.api.model.NEKaraokeSeatInfo
import com.netease.yunxin.kit.karaokekit.api.model.NEKaraokeSeatItem
import com.netease.yunxin.kit.karaokekit.api.model.NEKaraokeSeatRequestItem
import com.netease.yunxin.kit.karaokekit.api.model.NEKaraokeSongModel
import com.netease.yunxin.kit.karaokekit.impl.model.KaraokeGiftModel
import com.netease.yunxin.kit.karaokekit.impl.model.KaraokeRoomInfo
import com.netease.yunxin.kit.karaokekit.impl.model.response.KaraokeChorusInfo
import com.netease.yunxin.kit.karaokekit.impl.model.response.KaraokeRoomList
import com.netease.yunxin.kit.karaokekit.impl.model.response.KaraokeSongInfo
import com.netease.yunxin.kit.karaokekit.impl.model.response.Operator
import com.netease.yunxin.kit.roomkit.api.NERoomMember
import com.netease.yunxin.kit.roomkit.api.service.NESeatInfo
import com.netease.yunxin.kit.roomkit.api.service.NESeatItem
import com.netease.yunxin.kit.roomkit.api.service.NESeatRequestItem

internal object KaraokeUtils {

    fun isLocal(uuid: String?): Boolean {
        return NEKaraokeKit.getInstance().localMember != null &&
            TextUtils.equals(NEKaraokeKit.getInstance().localMember!!.account, uuid)
    }

    fun isHost(uuid: String?): Boolean {
        val member: NEKaraokeMember =
            getMember(uuid)
                ?: return false
        return TextUtils.equals(member.role, NEKaraokeRole.HOST.value)
    }

    fun getMember(uuid: String?): NEKaraokeMember? {
        val allMemberList = NEKaraokeKit.getInstance().allMemberList
        for (i in allMemberList.indices) {
            val member = allMemberList[i]
            if (TextUtils.equals(member.account, uuid)) {
                return member
            }
        }
        return null
    }

    fun karaokeRoomInfo2NEKaraokeRoomInfo(karaokeRoomInfo: KaraokeRoomInfo): NEKaraokeRoomInfo {
        return NEKaraokeRoomInfo(
            NEKaraokeAnchor(
                karaokeRoomInfo.anchor.userUuid,
                karaokeRoomInfo.anchor.userName,
                karaokeRoomInfo.anchor.icon
            ),
            NEKaraokeLiveModel(
                karaokeRoomInfo.liveModel.roomUuid, karaokeRoomInfo.liveModel.liveRecordId, karaokeRoomInfo.liveModel.userUuid,
                karaokeRoomInfo.liveModel.liveType, karaokeRoomInfo.liveModel.live, karaokeRoomInfo.liveModel.liveTopic, karaokeRoomInfo.liveModel.cover, karaokeRoomInfo.liveModel.rewardTotal,
                karaokeRoomInfo.liveModel.audienceCount, karaokeRoomInfo.liveModel.onSeatCount
            )
        )
    }

    fun karaokeRoomList2NEKaraokeRoomList(karaokeRoomList: KaraokeRoomList): NEKaraokeRoomList {
        return NEKaraokeRoomList(
            karaokeRoomList.pageNum,
            karaokeRoomList.hasNextPage,
            karaokeRoomList.list?.map {
                karaokeRoomInfo2NEKaraokeRoomInfo(it)
            }
        )
    }

    fun karaokeGiftModel2NEKaraokeGiftModel(karaokeGiftModel: KaraokeGiftModel): NEKaraokeBatchGiftModel {
        return NEKaraokeBatchGiftModel(
            karaokeGiftModel.senderUserUuid,
            karaokeGiftModel.userName,
            karaokeGiftModel.giftId
        )
    }

    fun karaokeSongInfo2NEKaraokeSongModel(karaokeSongModel: KaraokeSongInfo, operator: Operator?): NEKaraokeSongModel {
        return NEKaraokeSongModel(
            karaokeSongModel.chorusId, karaokeSongModel.roomUuid,
            karaokeSongModel.orderId, karaokeSongModel.liveRecordId, karaokeSongModel.liveTopic, karaokeSongModel.singMode, karaokeSongModel.userUuid,
            karaokeSongModel.userName, karaokeSongModel.icon, karaokeSongModel.assistantUuid, karaokeSongModel.assistantName, karaokeSongModel.assistantIcon,
            karaokeSongModel.songId, karaokeSongModel.songName, karaokeSongModel.songCover, karaokeSongModel.songTime, karaokeSongModel.singer,
            karaokeSongModel.singerCover, karaokeSongModel.songStatus, karaokeSongModel.chorusType,
            operator?.let {
                operator2NEOperator(it)
            },
            karaokeSongModel.ext, karaokeSongModel.channel
        )
    }

    fun karaokeChorusInfo2NEKaraokeSongModel(karaokeSongModel: KaraokeChorusInfo, operator: Operator?): NEKaraokeSongModel {
        return NEKaraokeSongModel(
            karaokeSongModel.chorusId, karaokeSongModel.roomUuid,
            karaokeSongModel.orderId, karaokeSongModel.liveRecordId, karaokeSongModel.liveTopic, karaokeSongModel.singMode, karaokeSongModel.userUuid,
            karaokeSongModel.userName, karaokeSongModel.icon, karaokeSongModel.assistantUuid, karaokeSongModel.assistantName, karaokeSongModel.assistantIcon,
            karaokeSongModel.songId, karaokeSongModel.songName, karaokeSongModel.songCover, karaokeSongModel.songTime, karaokeSongModel.singer,
            karaokeSongModel.singerCover, karaokeSongModel.songStatus, karaokeSongModel.chorusType,
            operator?.let {
                operator2NEOperator(it)
            },
            karaokeSongModel.ext, karaokeSongModel.channel
        )
    }

    private fun operator2NEOperator(operator: Operator): NEKaraokeOrderSongOperatorUser {
        return NEKaraokeOrderSongOperatorUser(operator.userUuid, operator.userName, operator.icon)
    }

    fun karaokeSeatItem2NEKaraokeSeatItem(seatItem: NESeatItem): NEKaraokeSeatItem {
        return NEKaraokeSeatItem(
            seatItem.index,
            seatItem.status,
            seatItem.user,
            seatItem.userName,
            seatItem.icon,
            seatItem.updated
        )
    }

    fun karaokeSeatInfo2NEKaraokeSeatInfo(seatInfo: NESeatInfo): NEKaraokeSeatInfo {
        return NEKaraokeSeatInfo(
            seatInfo.creator,
            seatInfo.managers,
            seatInfo.seatItems.map {
                karaokeSeatItem2NEKaraokeSeatItem(it)
            }
        )
    }

    fun karaokeSeatRequestItem2NEKaraokeSeatRequestItem(seatRequestItem: NESeatRequestItem): NEKaraokeSeatRequestItem {
        return NEKaraokeSeatRequestItem(
            seatRequestItem.index,
            seatRequestItem.user,
            seatRequestItem.userName,
            seatRequestItem.icon
        )
    }

    fun isAudioOn(roomMember: NERoomMember?): Boolean {
        if (roomMember == null) {
            return false
        }
        return roomMember.isAudioOn
    }
}
