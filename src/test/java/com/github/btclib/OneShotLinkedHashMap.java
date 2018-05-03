package com.github.btclib;

import java.util.LinkedHashMap;

class OneShotLinkedHashMap<K, V> extends LinkedHashMap<K, V> {
  private static final long serialVersionUID = 1L;

  @Override
  public V put(final K key, final V value) {
    if (this.containsKey(key)) {
      throw new IllegalStateException(key.toString());
    }
    return super.put(key, value);
  }
}