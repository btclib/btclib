package com.github.btclib;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class Util {
  public static final byte[] EMPTY_BYTE_ARRAY = {};

  /**
   * @param value
   * @throws IllegalArgumentException
   */
  public static void checkArgument(final boolean value) {
    if (!value) {
      throw new IllegalArgumentException();
    }
  }

  /**
   * @param value
   * @param message
   * @throws IllegalArgumentException
   */
  public static void checkArgument(final boolean value, final String message) {
    if (!value) {
      throw new IllegalArgumentException(message);
    }
  }

  /**
   * @param data
   * @return
   * @throws IllegalArgumentException
   * @throws ArithmeticException when combined length of arrays is too large to fit into one array
   */
  public static byte[] concat(final byte[]... data) {
    Util.checkArgument(data != null);
    long size = 0;
    for (final byte[] element : data) {
      Util.checkArgument(element != null, "element is null");
      size += element.length;
      if (size > Integer.MAX_VALUE) {
        throw new ArithmeticException(); // integer overflow. combined length of arrays is too large to fit into one array.
      }
    }
    final byte[] result = new byte[(int) size];
    int offset = 0;
    for (final byte[] element : data) {
      System.arraycopy(element, 0, result, offset, element.length);
      offset += element.length;
    }
    return result;
  }

  /**
   * @param data
   * @return
   * @throws IllegalArgumentException
   */
  public static byte[] fromHexString(final String data) {
    Util.checkArgument(data != null);
    Util.checkArgument((data.length() % 2) == 0); // must be of even length
    final byte[] result = new byte[data.length() / 2];
    for (int i = 0; i < (data.length() - 1); i += 2) {
      final int hiNibble = Util.hexToDecimal(data.charAt(i)) << 4;
      final int loNibble = Util.hexToDecimal(data.charAt(i + 1));
      result[i / 2] = (byte) (hiNibble | loNibble);
    }
    return result;
  }

  /**
   * @param hex
   * @return
   * @throws IllegalArgumentException
   */
  public static int hexToDecimal(final int hex) {
    if ((hex >= '0') && (hex <= '9')) {
      return hex - '0';
    } else if ((hex >= 'a') && (hex <= 'f')) {
      return (hex - 'a') + 10;
    } else {
      throw new IllegalArgumentException("invalid hex");
    }
  }

  /**
   * @param data
   * @return
   * @throws IllegalArgumentException
   */
  public static byte[] sha256d(final byte[]... data) {
    Util.checkArgument(data != null);
    try {
      final MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
      for (final byte[] element : data) {
        Util.checkArgument(element != null);
        messageDigest.update(element);
      }
      return messageDigest.digest(messageDigest.digest());
    } catch (final NoSuchAlgorithmException e) {
      throw new AssertionError("SHA-256 is a required algorithm");
    }
  }

  private Util() {
    throw new AssertionError("suppress default constructor for noninstantiability");
  }
}