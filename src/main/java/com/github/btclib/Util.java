package com.github.btclib;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

public final class Util {
  public static final byte[] EMPTY_BYTE_ARRAY = {};

  /**
   * @param input
   * @param message
   * @throws IllegalArgumentException
   */
  public static void check(final boolean input, final String message) {
    if (!input) {
      throw new IllegalArgumentException(message);
    }
  }

  /**
   * @param input
   * @return an array that is a combined copy of all of the elements of the arrays passed in
   * @throws NullPointerException
   * @throws ArithmeticException if the combined length of the input arrays is too large to fit into one array
   */
  public static byte[] concat(final byte[]... input) {
    Objects.requireNonNull(input, "input must not be null");
    long size = 0;
    for (final var element : input) {
      Objects.requireNonNull(element, "element must not be null");
      size += element.length;
      if (size > Integer.MAX_VALUE) {
        throw new ArithmeticException("combined length of input arrays is too large"); // integer overflow
      }
    }
    final var result = new byte[(int) size];
    int offset = 0;
    for (final var element : input) {
      System.arraycopy(element, 0, result, offset, element.length);
      offset += element.length;
    }
    return result;
  }

  /**
   * @param prefix an int providing its low 8 bits to be prefixed to the input
   * @param input array of bytes to be prefixed by the low byte in prefix
   * @return a byte array consisting of input prefixed by the low byte of prefix
   * @throws NullPointerException if input is null
   */
  public static byte[] concat(final int prefix, final byte[] input) {
    Objects.requireNonNull(input, "input must not be null");
    return Util.concat(new byte[] { (byte) prefix, }, input);
  }

  /**
   * @param input
   * @param message
   * @throws DecodingException
   */
  public static void ensure(final boolean input, final String message) throws DecodingException {
    if (!input) {
      throw new DecodingException(message);
    }
  }

  /**
   * @param input
   * @return
   * @throws NullPointerException
   * @throws IllegalArgumentException
   */
  public static byte[] fromHexString(final String input) {
    Objects.requireNonNull(input, "input must not be null");
    Util.check((input.length() % 2) == 0, "input length must be even");
    final var result = new byte[input.length() / 2];
    for (int i = 0; i < (input.length() - 1); i += 2) {
      final int hiNibble = Util.hexToDecimal(input.charAt(i)) << 4;
      final int loNibble = Util.hexToDecimal(input.charAt(i + 1));
      result[i / 2] = (byte) (hiNibble | loNibble);
    }
    return result;
  }

  /**
   * @param input
   * @return
   * @throws IllegalArgumentException
   */
  public static int hexToDecimal(final int input) {
    if (('0' <= input) && (input <= '9')) {
      return input - '0';
    }
    if (('a' <= input) && (input <= 'f')) {
      return (input - 'a') + 10;
    }
    if (('A' <= input) && (input <= 'F')) {
      return (input - 'A') + 10;
    }
    throw new IllegalArgumentException("input invalid");
  }

  /**
   * @param input
   * @param multiplier
   * @return
   * @throws NullPointerException
   * @throws IllegalArgumentException
   * @throws ArithmeticException
   */
  public static String multiply(final String input, final int multiplier) {
    Objects.requireNonNull(input, "input must not be null");
    Util.check(0 <= multiplier, "multiplier invalid");
    final var result = new StringBuilder(Math.multiplyExact(multiplier, input.length()));
    for (int i = 0; i < multiplier; i++) {
      result.append(input);
    }
    return result.toString();
  }

  /**
   * @param input
   * @return
   * @throws NullPointerException
   */
  public static byte[] sha256d(final byte[]... input) {
    Objects.requireNonNull(input, "input must not be null");
    try {
      final var sha256 = MessageDigest.getInstance("SHA-256");
      for (final var element : input) {
        Objects.requireNonNull(element, "element must not be null");
        sha256.update(element);
      }
      return sha256.digest(sha256.digest());
    } catch (final NoSuchAlgorithmException e) {
      throw new AssertionError("SHA-256 is a required algorithm");
    }
  }

  private Util() {
    throw new AssertionError("suppress default constructor for noninstantiability");
  }
}