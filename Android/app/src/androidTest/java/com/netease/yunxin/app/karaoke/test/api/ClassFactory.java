// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.karaoke.test.api;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.netease.yunxin.kit.common.network.NetRequestCallback;
import com.netease.yunxin.kit.copyrightedmedia.api.LyricCallback;
import com.netease.yunxin.kit.copyrightedmedia.api.NECopyrightedMedia;
import com.netease.yunxin.kit.copyrightedmedia.api.NECopyrightedMedia.Callback;
import com.netease.yunxin.kit.copyrightedmedia.api.NESongPreloadCallback;
import com.netease.yunxin.kit.copyrightedmedia.api.SongResType;
import com.netease.yunxin.kit.copyrightedmedia.api.SongScene;
import com.netease.yunxin.kit.copyrightedmedia.impl.NECopyrightedEventHandler;
import com.netease.yunxin.kit.integrationtest.Hawk;
import com.netease.yunxin.kit.integrationtest.register.ClassMappingRegister;
import com.netease.yunxin.kit.karaokekit.api.NEKaraokeAuthEvent;
import com.netease.yunxin.kit.karaokekit.api.NEKaraokeAuthListener;
import com.netease.yunxin.kit.karaokekit.api.NEKaraokeCallback;
import com.netease.yunxin.kit.karaokekit.api.NEKaraokeEndReason;
import com.netease.yunxin.kit.karaokekit.api.NEKaraokeKit;
import com.netease.yunxin.kit.karaokekit.api.NEKaraokeListener;
import com.netease.yunxin.kit.karaokekit.api.NEKaraokeSongMode;
import com.netease.yunxin.kit.karaokekit.ui.listener.MyKaraokeListener;
import java.lang.Object;
import java.lang.String;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ClassFactory implements ClassMappingRegister {
  private static final String TAG = "ClassFactory";

  private Context mContext;

  public ClassFactory(Context context) {
    this.mContext = context;
  }

  /** rule0 */
  private static List<String> rule0 =
      new ArrayList<String>() {
        {
          add("NEKaraokeKit");
        }
      };
  /** rule1 */
  private static List<String> rule1 =
      new ArrayList<String>() {
        {
          add("NECopyrightedMedia");
        }
      };

  /** 类Class映射表 */
  private static HashMap<String, Class<?>> classHashMap =
      new HashMap<String, Class<?>>() {
        {
          // 通用方法
          put("Log", Log.class);
          // 基本类型
          put("boolean", boolean.class);
          put("byte", byte.class);
          put("short", short.class);
          put("int", int.class);
          put("long", long.class);
          put("float", float.class);
          put("double", double.class);
          put("Boolean", Boolean.class);
          put("Byte", Byte.class);
          put("Short", Short.class);
          put("Integer", Integer.class);
          put("Long", Long.class);
          put("Float", Float.class);
          put("Double", Double.class);
          put("String", String.class);

          //容器类型
          put("List", List.class);
          put("Map", Map.class);
          put("Set", Set.class);
          put("Array", Array.class);

          // SDK Service 和 SDK枚举类型
          put("NEKaraokeKit", NEKaraokeKit.class);
          put("NECopyrightedMedia", NECopyrightedMedia.class);
          put("NEKaraokeEndReason", NEKaraokeEndReason.class);
          put("NEKaraokeSongMode", NEKaraokeSongMode.class);
          put("SongResType", SongResType.class);
          put("SongScene", SongScene.class);
          put("NEKaraokeListener", NEKaraokeListener.class);
          put("NEKaraokeCallback", NEKaraokeCallback.class);
          put("NESongPreloadCallback", NESongPreloadCallback.class);
          put("NECopyrightedEventHandler", NECopyrightedEventHandler.class);
          put("Callback", Callback.class);
          put("LyricCallback", LyricCallback.class);
          put("NetRequestCallback", NetRequestCallback.class);
          put("NEKaraokeAuthListener", NEKaraokeAuthListener.class);
        }
      };

  @Override
  public Class<?> getClass(String name) {
    if (name == null || name.length() == 0) {
      return null;
    }

    int index = name.indexOf("[]");
    if (index < 0) {
      return classHashMap.get(name);
    }

    String className = name.substring(0, index);
    Class<?> aClass = classHashMap.get(className);
    int lastIndex = name.lastIndexOf("[]");
    int count = 1;
    if (lastIndex > index) {
      count = (lastIndex - index + 2) / 2;
    }
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < count; i++) {
      sb.append("[");
    }
    sb.append(getClassDesciptor(aClass));
    String arrayClassName = sb.toString();
    try {
      return Class.forName(arrayClassName);
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * 获取类型的描述符
   *
   * @param aClass
   * @return
   */
  private static String getClassDesciptor(Class<?> aClass) {
    if (aClass.equals(boolean.class)) {
      return "Z";
    } else if (aClass.equals(int.class)) {
      return "I";
    } else if (aClass.equals(long.class)) {
      return "J";
    } else if (aClass.equals(float.class)) {
      return "F";
    } else if (aClass.equals(double.class)) {
      return "D";
    } else if (aClass.equals(byte.class)) {
      return "B";
    } else if (aClass.equals(char.class)) {
      return "C";
    } else if (aClass.equals(short.class)) {
      return "S";
    } else {
      return "L" + aClass.getName().replace('.', '/') + ";";
    }
  }

  @Override
  /** 获取对象 */
  public Object getObject(String name) {

    Class<?> clazz = classHashMap.get(name);

    // rule0: NEKaraokeKit.getInstance()
    if (rule0.contains(name)) {
      return NEKaraokeKit.getInstance();
    }
    // rule1: NECopyrightedMedia.getInstance()
    if (rule1.contains(name)) {
      return NECopyrightedMedia.getInstance();
    }

    if (NEKaraokeAuthListener.class.getSimpleName().equals(name)) {

      HashMap<String, Object> globalResult = new HashMap<>();

      NEKaraokeAuthListener observer =
          new NEKaraokeAuthListener() {
            @Override
            public void onKaraokeAuthEvent(@NonNull NEKaraokeAuthEvent evt) {
              reportInListener(this, false, globalResult, "onKaraokeAuthEvent", evt);
            }
          };
      Hawk.getInstance().putListener(observer);
      return observer;
    }
    if (NEKaraokeListener.class
        .getSimpleName()
        .equals(name)) {
      NEKaraokeListener observer =
          new NEKaraokeListener() {

            HashMap<String, Object> globalResult = new HashMap<>();

            @Override
            public void onAudioOutputDeviceChanged(
                com.netease.yunxin.kit.karaokekit.api.NEKaraokeAudioOutputDevice arg0) {

              reportInListener(this, false, globalResult, "onAudioOutputDeviceChanged", arg0);
            }

            @Override
            public void onMemberAudioMuteChanged(
                com.netease.yunxin.kit.karaokekit.api.model.NEKaraokeMember arg0,
                boolean arg1,
                com.netease.yunxin.kit.karaokekit.api.model.NEKaraokeMember arg2) {

              reportInListener(
                  this, false, globalResult, "onMemberAudioMuteChanged", arg0, arg1, arg2);
            }

            @Override
            public void onMemberJoinRoom(List arg0) {

              reportInListener(this, false, globalResult, "onMemberJoinRoom", arg0);
            }

            @Override
            public void onMemberLeaveRoom(List arg0) {

              reportInListener(this, false, globalResult, "onMemberLeaveRoom", arg0);
            }

            @Override
            public void onNextSong(
                com.netease.yunxin.kit.karaokekit.api.model.NEKaraokeOrderSongModel arg0) {

              reportInListener(this, false, globalResult, "onNextSong", arg0);
            }

            @Override
            public void onReceiveChorusMessage(
                com.netease.yunxin.kit.karaokekit.api.NEKaraokeChorusActionType arg0,
                com.netease.yunxin.kit.karaokekit.api.model.NEKaraokeSongModel arg1) {

              reportInListener(this, false, globalResult, "onReceiveChorusMessage", arg0, arg1);
            }

            @Override
            public void onReceiveGift(
                com.netease.yunxin.kit.karaokekit.api.model.NEKaraokeGiftModel arg0) {

              reportInListener(this, false, globalResult, "onReceiveGift", arg0);
            }

            @Override
            public void onReceiveTextMessage(
                com.netease.yunxin.kit.karaokekit.api.model.NEKaraokeChatTextMessage arg0) {

              reportInListener(this, false, globalResult, "onReceiveTextMessage", arg0);
            }

            @Override
            public void onRecordingAudioFrame(
                com.netease.yunxin.kit.karaokekit.api.NEKaraokeAudioFrame arg0) {

              reportInListener(this, false, globalResult, "onRecordingAudioFrame", arg0);
            }

            @Override
            public void onRoomEnded(NEKaraokeEndReason arg0) {

              reportInListener(this, false, globalResult, "onRoomEnded", arg0);
            }

            @Override
            public void onRtcChannelError(int arg0) {

              reportInListener(this, false, globalResult, "onRtcChannelError", arg0);
            }

            @Override
            public void onSeatKicked(int arg0, String arg1, String arg2) {

              reportInListener(this, false, globalResult, "onSeatKicked", arg0, arg1, arg2);
            }

            @Override
            public void onSeatLeave(int arg0, String arg1) {

              reportInListener(this, false, globalResult, "onSeatLeave", arg0, arg1);
            }

            @Override
            public void onSeatListChanged(List arg0) {

              reportInListener(this, false, globalResult, "onSeatListChanged", arg0);
            }

            @Override
            public void onSeatRequestApproved(
                int arg0, String arg1, String arg2) {

              reportInListener(
                  this, false, globalResult, "onSeatRequestApproved", arg0, arg1, arg2);
            }

            @Override
            public void onSeatRequestCancelled(int arg0, String arg1) {

              reportInListener(this, false, globalResult, "onSeatRequestCancelled", arg0, arg1);
            }

            @Override
            public void onSeatRequestRejected(
                int arg0, String arg1, String arg2) {

              reportInListener(
                  this, false, globalResult, "onSeatRequestRejected", arg0, arg1, arg2);
            }

            @Override
            public void onSeatRequestSubmitted(int arg0, String arg1) {

              reportInListener(this, false, globalResult, "onSeatRequestSubmitted", arg0, arg1);
            }

            @Override
            public void onSongDeleted(
                com.netease.yunxin.kit.karaokekit.api.model.NEKaraokeOrderSongModel arg0) {

              reportInListener(this, false, globalResult, "onSongDeleted", arg0);
            }

            @Override
            public void onSongListChanged() {

              reportInListener(this, false, globalResult, "onSongListChanged");
            }

            @Override
            public void onSongOrdered(
                com.netease.yunxin.kit.karaokekit.api.model.NEKaraokeOrderSongModel arg0) {

              reportInListener(this, false, globalResult, "onSongOrdered", arg0);
            }

            @Override
            public void onSongPlayingCompleted() {

              reportInListener(this, false, globalResult, "onSongPlayingCompleted", null);
            }

            @Override
            public void onSongPlayingPosition(long arg0) {

              reportInListener(this, false, globalResult, "onSongPlayingPosition", arg0);
            }

            @Override
            public void onSongTopped(
                com.netease.yunxin.kit.karaokekit.api.model.NEKaraokeOrderSongModel arg0) {

              reportInListener(this, false, globalResult, "onSongTopped", arg0);
            }
          };
      Hawk.getInstance().putListener(observer);
      return observer;
    }
    if (NEKaraokeCallback.class
        .getSimpleName()
        .equals(name)) {
      NEKaraokeCallback observer =
          new NEKaraokeCallback() {

            HashMap<String, Object> globalResult = new HashMap<>();

            @Override
            public void onFailure(int arg0, String arg1) {

              reportInListener(this, true, globalResult, "onFailure", arg0, arg1);
            }

            @Override
            public void onSuccess(Object arg0) {

              reportInListener(this, true, globalResult, "onSuccess", arg0);
            }
          };
      Hawk.getInstance().putListener(observer);
      return observer;
    }
    if (NESongPreloadCallback.class
        .getSimpleName()
        .equals(name)) {
      NESongPreloadCallback observer =
          new NESongPreloadCallback() {

            HashMap<String, Object> globalResult = new HashMap<>();

            @Override
            public void onPreloadComplete(
                String arg0, int channel, int arg1, String arg2) {

              reportInListener(
                  this, true, globalResult, "onPreloadComplete", arg0, channel, arg1, arg2);
            }

            @Override
            public void onPreloadProgress(String arg0, int channel, float arg1) {

              reportInListener(this, false, globalResult, "onPreloadProgress", arg0, channel, arg1);
            }

            @Override
            public void onPreloadStart(String arg0, int channel) {

              reportInListener(this, false, globalResult, "onPreloadStart", arg0, channel);
            }
          };
      Hawk.getInstance().putListener(observer);
      return observer;
    }
    if (NECopyrightedEventHandler.class
        .getSimpleName()
        .equals(name)) {
      NECopyrightedEventHandler observer =
          new NECopyrightedEventHandler() {

            @Override
            public void onTokenExpired() {

              reportInSingleMethodListener(this, false, "onTokenExpired", null);
            }
          };
      Hawk.getInstance().putListener(observer);
      return observer;
    }
    if (LyricCallback.class
        .getSimpleName()
        .equals(name)) {
      LyricCallback observer =
          new LyricCallback() {

            HashMap<String, Object> globalResult = new HashMap<>();

            @Override
            public void error(int arg0, String arg1) {
              saveGlobalData(arg0, arg1);

              reportInListener(this, true, globalResult, "error", arg0, arg1);
            }

            @Override
            public void success(String arg0, String arg1, int channel) {
              saveGlobalData(arg0, arg1);

              reportInListener(this, true, globalResult, "success", arg0, arg1);
            }
          };
      Hawk.getInstance().putListener(observer);
      return observer;
    }
    if (Callback.class
        .getSimpleName()
        .equals(name)) {
      Callback observer =
          new Callback() {

            HashMap<String, Object> globalResult = new HashMap<>();

            @Override
            public void error(int code, @Nullable String msg) {
              saveGlobalData(code, msg);

              reportInListener(this, true, globalResult, "error", code, msg);
            }

            @Override
            public void success(@Nullable Object info) {
              saveGlobalData(info);

              reportInListener(this, true, globalResult, "success", info);
            }
          };
      Hawk.getInstance().putListener(observer);
      return observer;
    }
    if (NetRequestCallback.class.getSimpleName().equals(name)) {
      NetRequestCallback netRequestCallback =
          new NetRequestCallback() {

            HashMap<String, Object> globalResult = new HashMap<>();

            @Override
            public void error(int code, @Nullable String msg) {
              saveGlobalData(code, msg);

              reportInListener(this, true, globalResult, "error", code, msg);
            }

            @Override
            public void success(@Nullable Object info) {
              saveGlobalData(info);

              reportInListener(this, true, globalResult, "success", info);
            }
          };
      Hawk.getInstance().putListener(netRequestCallback);
      return netRequestCallback;
    }
    return null;
  }

  private void reportInSingleMethodListener(
      Object listener, boolean needToAwait, String callbackMethodName, Object... args) {
    reportInListener(listener, needToAwait, null, null, args);
  }

  /**
   * 上报方法参数是回调observer的执行结果
   *
   * @param listener observer对象
   * @param needToAwait 是否触发平台下一条case
   * @param globalResult observer全局回调map，为null表示是单method的observer，不为null表示observer全量结果map上报
   * @param callbackMethodName observer内的方法名称
   * @param args 参数列表
   */
  private void reportInListener(
      Object listener,
      boolean needToAwait,
      HashMap<String, Object> globalResult,
      String callbackMethodName,
      Object... args) {
    Object result = "";
    // 处理method参数
    if (args != null) {
      if (args.length == 1) { // method 只有一个参数: {"value"}
        result = args[0];
      } else { // method 有多个参数: {"arg0": "value0", "arg1": "value1"...}
        HashMap<String, Object> argsMap = new HashMap<>();
        for (int index = 0; index < args.length; index++) {
          argsMap.put("arg" + index, args[index]);
        }
        result = argsMap;
      }
    }
    if (TextUtils.isEmpty(callbackMethodName)) { // 单method listener: {args}
      Hawk.getInstance().reportInListener(0, callbackMethodName, result, listener, needToAwait);
    } else if (globalResult
        != null) { // 多method listener: {"methodName1": args1, "methodName2": args2}
      globalResult.put(callbackMethodName, result);
      Hawk.getInstance()
          .reportInListener(0, callbackMethodName, globalResult, listener, needToAwait);
    }
  }

  private void saveGlobalData(Object... args) {
    for (int i = 0; i < args.length; i++) {
      Object arg = args[i];
      if (arg != null && globalData.contains(arg.getClass().getSimpleName())) {
        TestDataFactory.getInstance().put(arg.getClass().getSimpleName(), arg);
        Log.d(TAG, "Save global data: arg" + i + " is " + arg.getClass().getSimpleName());
      }
    }
  }

  @Override
  public Type getRealTypeByType(Type type) {
    if (type instanceof Class) {
      Class<?> clazz = (Class<?>) type;
      Class<?> aClass = interfaceImplClassHashMap.get(clazz.getSimpleName());
      if (aClass == null) {
        return type;
      } else {
        return aClass;
      }
    } else if (type instanceof ParameterizedType) {
      ParameterizedType p = (ParameterizedType) type;
      return new ParameterizedTypeImpl(
          p.getOwnerType(), p.getRawType(), p.getActualTypeArguments());
    } else {
      return type;
    }
  }

  @Override
  public Type getRealTypeByEnum(Enum<?> en) {
    Class<? extends Enum> enClass = en.getClass();
    if (fieldEnumSet.contains(enClass)) {
      try {
        Method getFieldType = enClass.getMethod("getFieldType");
        Class<?> clazz = (Class<?>) getFieldType.invoke(en);
        return clazz;
      } catch (NoSuchMethodException e) {
        e.printStackTrace();
        return null;
      } catch (IllegalAccessException | InvocationTargetException e) {
        e.printStackTrace();
        return null;
      }
    } else {
      return null;
    }
  }

  /** 枚举集合（特殊枚举，根据枚举确定数据类型） */
  private static Set<Class<? extends Enum>> fieldEnumSet =
      new HashSet<Class<? extends Enum>>() {
        {
          add(NEKaraokeEndReason.class);
          add(NEKaraokeSongMode.class);
          add(SongResType.class);
          add(SongScene.class);
        }
      };

  /** 接口和实现类映射表 */
  private static HashMap<String, Class<?>> interfaceImplClassHashMap =
      new HashMap<String, Class<?>>() {
        {
          put("NEKaraokeListener", MyKaraokeListener.class);
        }
      };

  /** 全局保存变量 */
  private static ArrayList<String> globalData =
      new ArrayList<String>() {
        {
          add("NERoomContext");
        }
      };
}
