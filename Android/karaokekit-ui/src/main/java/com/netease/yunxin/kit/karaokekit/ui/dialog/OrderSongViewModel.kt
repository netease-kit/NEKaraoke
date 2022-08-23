/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.karaokekit.ui.dialog

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.netease.yunxin.kit.alog.ALog
import com.netease.yunxin.kit.common.network.NetRequestCallback
import com.netease.yunxin.kit.copyrightedmedia.api.NECopyrightedMedia
import com.netease.yunxin.kit.copyrightedmedia.api.NEErrorCode
import com.netease.yunxin.kit.copyrightedmedia.api.NESongPreloadCallback
import com.netease.yunxin.kit.copyrightedmedia.api.model.NECopyrightedSong
import com.netease.yunxin.kit.karaokekit.api.NEKaraokeKit
import com.netease.yunxin.kit.karaokekit.api.model.NEKaraokeOrderSongModel
import com.netease.yunxin.kit.karaokekit.ui.model.KaraokeOrderSongModel

class OrderSongViewModel : ViewModel() {
    private val copyRight = NECopyrightedMedia.getInstance()
    private val karaokeKit = NEKaraokeKit.getInstance()
    val orderSongListChangeEvent: MutableLiveData<List<NEKaraokeOrderSongModel>> = MutableLiveData()
    val performOrderSongEvent: MutableLiveData<KaraokeOrderSongModel> = MutableLiveData()
    val performDownloadSongEvent: MutableLiveData<KaraokeOrderSongModel> = MutableLiveData()
    var startOrderSongEvent: MutableLiveData<KaraokeOrderSongModel> = MutableLiveData()

    fun refreshSongList(
        pageNum: Int,
        pageSize: Int,
        callback: NECopyrightedMedia.Callback<List<KaraokeOrderSongModel>>
    ) {
        copyRight.getSongList(
            null,
            pageNum,
            pageSize,
            object : NECopyrightedMedia.Callback<List<NECopyrightedSong>> {
                override fun success(info: List<NECopyrightedSong>?) {
                    ALog.i("getSongList success:$info")
                    var songList = info?.filter { it.hasAccompany != 0 }?.map {
                        KaraokeOrderSongModel(it)
                    }
                    callback.success(songList)
                }

                override fun error(code: Int, msg: String?) {
                    ALog.e("getSongList fail:$msg")
                    callback.error(code, msg)
                }
            }
        )
    }

    fun searchSong(
        keyword: String,
        pageNum: Int,
        pageSize: Int,
        callback: NECopyrightedMedia.Callback<List<KaraokeOrderSongModel>>
    ) {
        copyRight.searchSong(
            keyword,
            pageNum,
            pageSize,
            object : NECopyrightedMedia.Callback<List<NECopyrightedSong>> {
                override fun success(info: List<NECopyrightedSong>?) {
                    ALog.i("searchSong success:$info")
                    var songList = info?.filter { it.hasAccompany != 0 }?.map {
                        KaraokeOrderSongModel(it)
                    }
                    callback.success(songList)
                }

                override fun error(code: Int, msg: String?) {
                    ALog.e("searchSong fail:$msg")
                    callback.error(code, msg)
                }
            }
        )
    }

    fun refreshOrderSongs() {
        karaokeKit.getOrderedSongs(object : NetRequestCallback<List<NEKaraokeOrderSongModel>> {
            override fun success(info: List<NEKaraokeOrderSongModel>?) {
                ALog.i("getOrderSongs success:$info")
                if (info != null) orderSongListChangeEvent.postValue(info)
            }

            override fun error(code: Int, msg: String?) {
                ALog.e("getOrderSongs fail:$msg")
            }
        })
    }

    fun preloadSong(songId: String, callback: NESongPreloadCallback) {
        if (copyRight.isSongPreloaded(songId)) {
            callback.onPreloadComplete(songId, NEErrorCode.OK, "")
        } else {
            copyRight.preloadSong(
                songId,
                object :
                    NESongPreloadCallback {
                    override fun onPreloadStart(songId: String?) {
                        ALog.i("onPreloadStart $songId")
                        callback.onPreloadStart(songId)
                    }

                    override fun onPreloadProgress(songId: String?, progress: Float) {
                        ALog.i("onPreloadProgress $songId $progress")
                        callback.onPreloadProgress(songId, progress)
                    }

                    override fun onPreloadComplete(songId: String?, errorCode: Int, msg: String?) {
                        ALog.i("onPreloadComplete $songId $errorCode $msg")
                        callback.onPreloadComplete(songId, errorCode, msg)
                    }
                }
            )
        }
    }

    fun orderSong(
        copyrightSong: KaraokeOrderSongModel,
        callback: NECopyrightedMedia.Callback<Boolean>
    ) {
        karaokeKit.orderSong(
            buildLocalOrderSong(copyrightSong),
            object : NetRequestCallback<NEKaraokeOrderSongModel> {
                override fun success(info: NEKaraokeOrderSongModel?) {
                    ALog.i("orderSong success:$info")
                    callback.success(true)
                }

                override fun error(code: Int, msg: String?) {
                    ALog.e("orderSong fail:$msg")
                    callback.error(code, msg)
                }
            }
        )
    }

    fun deleteSong(orderId: Long, callback: NECopyrightedMedia.Callback<Boolean>) {
        karaokeKit.deleteSong(
            orderId,
            object : NetRequestCallback<Boolean> {
                override fun success(info: Boolean?) {
                    ALog.i("deleteSong success:$info")
                    callback.success(true)
                }

                override fun error(code: Int, msg: String?) {
                    ALog.e("deleteSong fail:$msg")
                    callback.error(code, msg)
                }
            }
        )
    }

    fun topSong(orderId: Long, callback: NECopyrightedMedia.Callback<Boolean>) {
        karaokeKit.topSong(
            orderId,
            object : NetRequestCallback<Boolean> {
                override fun success(info: Boolean?) {
                    ALog.i("topSong success:$info")
                    callback.success(true)
                }

                override fun error(code: Int, msg: String?) {
                    ALog.e("topSong fail:$msg")
                    callback.error(code, msg)
                }
            }
        )
    }

    fun nextSong() {
        if (orderSongListChangeEvent.value?.size ?: 0 > 0) {
            orderSongListChangeEvent.value?.get(0)?.orderId?.apply {
                karaokeKit.nextSong(
                    this,
                    object : NetRequestCallback<Boolean> {
                        override fun success(info: Boolean?) {
                            ALog.i("nextSong success:$info")
                        }

                        override fun error(code: Int, msg: String?) {
                            ALog.e("nextSong fail:$msg")
                        }
                    }
                )
            }
        }
    }

    private fun buildLocalOrderSong(copyrightSong: KaraokeOrderSongModel): NEKaraokeOrderSongModel {
        return NEKaraokeOrderSongModel(
            copyrightSong.songId,
            copyrightSong.songName,
            copyrightSong.songCover,
            copyrightSong.songTime,
            copyrightSong.channel
        )
    }
}
