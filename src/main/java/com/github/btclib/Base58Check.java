package com.github.btclib;

import java.util.Objects;

/**
 * "Base58 Check" is a checksummed (truncated double-SHA256) encoding format for the transfer of binary data.
 * This encoding is commonly used to encode: 1) the hash of a public point on the secp256k1 curve to create a
 * Bitcoin P2PKH address, 2) the hash of a scriptPubKey to create a Bitcoin P2SH address, 3) secp256k1 private
 * key data, and 4) BIP32 extended public or private key data. This format is case sensitive.
 */
public final class Base58Check {
  private static final String SYMBOLS = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz";

  /**
   * @param data An array of bytes representing an unsigned big-endian number in the base specified in base. Must not be null.
   * @param base The base of the number in the data array. Must be 256 or 58.
   * @return An array of bytes representing an unsigned big-endian number.
   */
  private static byte[] convertBase(final byte[] data, final int base) {
    assert data != null;
    assert (base == 256) || (base == 58);
    final int resultBase = (base == 256) ? 58 : 256;
    int leadingZeros = 0; // find the first significant figure (the most-significant non-zero)
    while ((leadingZeros < data.length) && (data[leadingZeros] == 0)) {
      leadingZeros++;
    }
    // for all base256 and base58 inputs with length <= 8192 and using the largest values (the worst case),
    // these sizing calculations were verified to be sufficient.
    // integer overflow is prevented by the caller's length restriction on the array.
    int resultSize = data.length - leadingZeros;
    if (base == 256) {
      // converting from base256 to base58 results in a size increase of about 36.6% = log(256) / log(58).
      resultSize = ((resultSize * 1366) / 1000) + 1; // 36.6% increase and add 1 to round up
    } else {
      // converting from base58 to base256 results in a size decrease of about 26.7% = log(58) / log(256).
      resultSize = ((resultSize * 733) / 1000) + 1; // 26.7% decrease and add 1 to round up
    }
    byte[] result = new byte[resultSize];
    int significantFigureCount = 0;
    for (int i = leadingZeros; i < data.length; i++) {
      int carry = data[i] & 0xff; // mask off sign extension during widening primitive conversion
      int calculationIndex;
      for (calculationIndex = 0; (carry > 0) || (calculationIndex < significantFigureCount); calculationIndex++) {
        // this loop implements the logic: result = result * base + carry
        carry += base * (result[result.length - 1 - calculationIndex] & 0xff);
        result[result.length - 1 - calculationIndex] = (byte) (carry % resultBase);
        carry /= resultBase;
      }
      significantFigureCount = calculationIndex;
    }
    // make the result have exactly the same number of leading insignificant zeros as the input data
    if ((result.length - significantFigureCount) != leadingZeros) {
      final byte[] adjustedResult = new byte[leadingZeros + significantFigureCount];
      System.arraycopy(result, result.length - significantFigureCount, adjustedResult, leadingZeros, significantFigureCount);
      result = adjustedResult;
    }
    return result;
  }

  /**
   * Decodes the given "Base58 Check" String into the bytes that it encodes.
   * @param data The String to decode. Must not be null. Must be less than or equal to 5600 bytes in length.
   * @return The bytes encoded by the input "Base58 Check" String.
   */
  public static byte[] decode(final String data) throws DecodingException {
    Objects.requireNonNull(data, "data must not be null");
    Util.check(data.length() <= 5600, "data too long"); // corresponds to the 4096 encoding max worst case
    final byte[] dataWithChecksum = Base58Check.decodeBase58(data);
    if (dataWithChecksum.length < 4) { // the checksum size is always four bytes.
      throw new DecodingException("invalid checksum");
    }
    final byte[] result = new byte[dataWithChecksum.length - 4];
    System.arraycopy(dataWithChecksum, 0, result, 0, result.length); // don't copy the checksum
    final byte[] hash = Util.sha256d(result); // compute the checksum and match to verify
    final boolean match = (hash[0] == dataWithChecksum[dataWithChecksum.length - 4]) //
                          && (hash[1] == dataWithChecksum[dataWithChecksum.length - 3]) //
                          && (hash[2] == dataWithChecksum[dataWithChecksum.length - 2]) //
                          && (hash[3] == dataWithChecksum[dataWithChecksum.length - 1]);
    if (!match) {
      throw new DecodingException("checksum failure");
    }
    return result;
  }

  static byte[] decodeBase58(final String data) throws DecodingException {
    final byte[] base58 = Base58Check.fromBase58String(data);
    return Base58Check.convertBase(base58, 58);
  }

  /**
   * Encodes the given byte array into a "Base58 Check" String.
   * @param data The data to encode. Must not be null. Must be less than or equal to 4096 bytes (4 KiB) in length.
   * @return A "Base58 Check" encoded String.
   */
  public static String encode(final byte[] data) {
    Objects.requireNonNull(data, "data must not be null");
    Util.check(data.length <= 4096, "data too long");
    // the limit of 4096 bytes was selected because it provides more than sufficient space
    // to accommodate all known uses, and because it provides an upper bound for testing
    // the array size increase when converting from base256 to base58. it also avoids
    // possible integer overflow during calculations that use the data array size.
    final byte[] hash = Util.sha256d(data);
    final byte[] checksum = { hash[0], hash[1], hash[2], hash[3] };
    return Base58Check.encodeBase58(Util.concat(data, checksum));
  }

  static String encodeBase58(final byte[] data) {
    final byte[] base58 = Base58Check.convertBase(data, 256);
    return Base58Check.toBase58String(base58);
  }

  private static byte[] fromBase58String(final String data) throws DecodingException {
    final byte[] result = new byte[data.length()];
    for (int i = 0; i < result.length; i++) {
      final int val = Base58Check.SYMBOLS.indexOf(data.charAt(i)); // could use a lookup table if performance is a problem
      if (val == -1) {
        throw new DecodingException("invalid symbol found");
      }
      result[i] = (byte) val;
    }
    return result;
  }

  private static String toBase58String(final byte[] base58) {
    final StringBuilder result = new StringBuilder(base58.length);
    for (final byte b : base58) {
      result.append(Base58Check.SYMBOLS.charAt(b));
    }
    return result.toString();
  }

  private Base58Check() {
    throw new AssertionError("suppress default constructor for noninstantiability");
  }
}
