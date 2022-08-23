// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.karaoke.test.api;

import com.google.gson.internal.$Gson$Types;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;

public final class ParameterizedTypeImpl implements ParameterizedType, Serializable {
  private final Type ownerType;
  private final Type rawType;
  private final Type[] typeArguments;
  private static final long serialVersionUID = 0L;

  public ParameterizedTypeImpl(Type ownerType, Type rawType, Type... typeArguments) {
    if (rawType instanceof Class) {
      Class<?> rawTypeAsClass = (Class) rawType;
      checkArgument(ownerType != null || rawTypeAsClass.getEnclosingClass() == null);
      checkArgument(ownerType == null || rawTypeAsClass.getEnclosingClass() != null);
    }

    this.ownerType = ownerType == null ? null : $Gson$Types.canonicalize(ownerType);
    this.rawType = $Gson$Types.canonicalize(rawType);
    this.typeArguments = (Type[]) typeArguments.clone();

    for (int t = 0; t < this.typeArguments.length; ++t) {
      checkNotNull(this.typeArguments[t]);
      checkNotPrimitive(this.typeArguments[t]);
      this.typeArguments[t] = $Gson$Types.canonicalize(this.typeArguments[t]);
    }
  }

  public Type[] getActualTypeArguments() {
    return (Type[]) this.typeArguments.clone();
  }

  public Type getRawType() {
    return this.rawType;
  }

  public Type getOwnerType() {
    return this.ownerType;
  }

  public boolean equals(Object other) {
    return other instanceof ParameterizedType
        && $Gson$Types.equals(this, (ParameterizedType) other);
  }

  public int hashCode() {
    return Arrays.hashCode(this.typeArguments)
        ^ this.rawType.hashCode()
        ^ hashCodeOrZero(this.ownerType);
  }

  public String toString() {
    StringBuilder stringBuilder = new StringBuilder(30 * (this.typeArguments.length + 1));
    stringBuilder.append($Gson$Types.typeToString(this.rawType));
    if (this.typeArguments.length == 0) {
      return stringBuilder.toString();
    } else {
      stringBuilder.append("<").append($Gson$Types.typeToString(this.typeArguments[0]));

      for (int i = 1; i < this.typeArguments.length; ++i) {
        stringBuilder.append(", ").append($Gson$Types.typeToString(this.typeArguments[i]));
      }

      return stringBuilder.append(">").toString();
    }
  }

  private static int hashCodeOrZero(Object o) {
    return o != null ? o.hashCode() : 0;
  }

  private static void checkArgument(boolean condition) {
    if (!condition) {
      throw new IllegalArgumentException();
    }
  }

  private static <T> T checkNotNull(T obj) {
    if (obj == null) {
      throw new NullPointerException();
    } else {
      return obj;
    }
  }

  private static void checkNotPrimitive(Type type) {
    checkArgument(!(type instanceof Class) || !((Class) type).isPrimitive());
  }
}
