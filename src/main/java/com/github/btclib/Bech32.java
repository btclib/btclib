package com.github.btclib;

import java.nio.charset.StandardCharsets;

/**
 * https://github.com/bitcoin/bips/blob/master/bip-0173.mediawiki
 * https://github.com/satoshilabs/slips/blob/master/slip-0173.md
 * https://github.com/satoshilabs/slips/blob/master/slip-0032.md
 * https://github.com/lightningnetwork/lightning-rfc/blob/master/11-payment-encoding.md
 */
public final class Bech32 {
  private static final int MIN_BECH32_LENGTH = 1 + 1 + 6; // min hrp length + separator length + checksum length
  private static final int MAX_BECH32_LENGTH = 90;
  private static final int MIN_HRP_LENGTH = 1;
  private static final int MAX_HRP_LENGTH = 83;
  private static final int SEPARATOR = '1';
  private static final byte[] SEPARATOR_ARRAY = { Bech32.SEPARATOR };
  private static final byte[] CHECKSUM = new byte[6];
  private static final byte[] CHARSET = { //
      'q', 'p', 'z', 'r', 'y', '9', 'x', '8', //
      'g', 'f', '2', 't', 'v', 'd', 'w', '0', //
      's', '3', 'j', 'n', '5', '4', 'k', 'h', //
      'c', 'e', '6', 'm', 'u', 'a', '7', 'l', //
  };
  private static final byte[] CHARSET_REVERSE = { //
      -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, //
      -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, //
      -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, //
      15, -1, 10, 17, 21, 20, 26, 30, 7, 5, -1, -1, -1, -1, -1, -1, //
      -1, 29, -1, 24, 13, 25, 9, 8, 23, -1, 18, 22, 31, 27, 19, -1, //
      1, 0, 3, 16, 11, 28, 12, 14, 6, 4, 2, -1, -1, -1, -1, -1, //
      -1, 29, -1, 24, 13, 25, 9, 8, 23, -1, 18, 22, 31, 27, 19, -1, //
      1, 0, 3, 16, 11, 28, 12, 14, 6, 4, 2, -1, -1, -1, -1, -1, //
  };

  /**
   * @param hrp
   * @param data byte array with element values in the range [0, 31] (base32 values)
   * @return
   */
  private static byte[] checksum(final byte[] hrp, final byte[] data) {
    final int polymod = Bech32.polymod(Bech32.expand(hrp), data, Bech32.CHECKSUM) ^ 1;
    return new byte[] { //
        (byte) ((polymod >>> 25) & 0x1f), //
        (byte) ((polymod >>> 20) & 0x1f), //
        (byte) ((polymod >>> 15) & 0x1f), //
        (byte) ((polymod >>> 10) & 0x1f), //
        (byte) ((polymod >>> 5) & 0x1f), //
        (byte) ((polymod >>> 0) & 0x1f), //
    };
  }

  /**
   * @param input An array of bytes containing either 8-bit (base256 -- 2^8) or 5-bit (base32 -- 2^5) values.
   * @param base Number of bits per byte of the input data. Valid values are 8 or 5, meaning 8-bit (base256 -- 2^8) or 5-bit (base32 -- 2^5), respectively.
   * @param offset Start processing at this index into the input array.
   * @param length Process this many bytes of the input array. Maximum is 4096 (4 KiB) for base256 and 6554 for base32.
   * @return null invalid padding is encountered while converting from base 5 to 8. Otherwise, an array of bytes where each byte has been converted from the original base to the new base.
   * @throws IllegalArgumentException if the input array contains values out of range for the specified input base
   */
  public static byte[] convert(final byte[] input, final int base, final int offset, final int length) {
    Util.checkArgument(input != null, "input must not be null");
    Util.checkArgument((base == 8) || (base == 5), "invalid base");
    Util.checkArgument(offset >= 0, "invalid offset");
    Util.checkArgument(length >= 0, "invalid length");
    Util.checkArgument(((base == 8) && (length <= 4096)) || ((base == 5) && (length <= 6554)), "invalid length"); // primarily an integer overflow prevention mechanism
    Util.checkArgument(offset <= (input.length - length), "invalid offset");
    final int resultBase = (base == 8) ? 5 : 8;
    final int inputBaseMask = (1 << base) - 1;
    final int resultBaseMask = (1 << resultBase) - 1;
    final boolean pad = (base == 8); // BIP-0173 specific rule
    final byte[] result = new byte[((length * base) + ((pad ? 1 : 0) * (resultBase - 1))) / resultBase];
    int resultIndex = 0;
    int bits = 0;
    int bitsAvailable = 0;
    for (int i = offset; i < (offset + length); i++) {
      final int temp = input[i] & 0xff; // stop sign extension from causing trouble
      Util.checkArgument((temp & ~inputBaseMask) == 0, "invalid element value for input base");
      bits = (bits << base) | temp;
      bitsAvailable += base;
      while (bitsAvailable >= resultBase) {
        bitsAvailable -= resultBase;
        result[resultIndex] = (byte) ((bits >>> bitsAvailable) & resultBaseMask);
        resultIndex++;
      }
    }
    // BIP-0173 specific rules - note that this method is intended to be used to invert its prior output
    // (i.e.- go from 8-bit bytes, to 5-bit bytes, back to the exact original 8-bit bytes.
    if (bitsAvailable != 0) {
      final int paddedRemainingBits = (bits << (resultBase - bitsAvailable)) & resultBaseMask;
      if (pad) { // save the bits with zero padding
        result[resultIndex] = (byte) paddedRemainingBits;
        resultIndex++;
      } else { // successfully discard bits only if they are actually padding bits
        if ((bitsAvailable >= base) || (paddedRemainingBits != 0)) {
          return null;
        }
      }
    }
    return result;
  }

  public static Bech32 decode(final String bech32) throws DecodingException {
    Util.checkArgument(bech32 != null, "bech32 must not be null");
    Bech32.ensure((bech32.length() >= Bech32.MIN_BECH32_LENGTH) && (bech32.length() <= Bech32.MAX_BECH32_LENGTH), "invalid length");
    int seperatorIndex = -1;
    boolean hasLower = false;
    boolean hasUpper = false;
    for (int i = 0; i < bech32.length(); i++) {
      final int element = bech32.charAt(i); // do widening primitive conversion once
      Bech32.ensure((element >= 33) && (element <= 126), "element value out of range");
      hasLower |= (element >= 'a') && (element <= 'z');
      hasUpper |= (element >= 'A') && (element <= 'Z');
      Bech32.ensure(!(hasLower && hasUpper), "mixed case");
      if (Bech32.SEPARATOR == element) {
        seperatorIndex = i; // the last one found is the separator
      }
    }
    Bech32.ensure((seperatorIndex >= 1) && (seperatorIndex <= (bech32.length() - Bech32.CHECKSUM.length - Bech32.SEPARATOR_ARRAY.length)), "invalid separator location");
    // process the human readable part
    final byte[] hrp = new byte[seperatorIndex];
    for (int i = 0; i < hrp.length; i++) {
      int element = bech32.charAt(i);
      if ((element >= 'A') && (element <= 'Z')) {
        element |= 0x20; // to ensure that the checksum is computed over the lower case form
      }
      hrp[i] = (byte) element;
    }
    // process the data part
    final byte[] data = new byte[bech32.length() - Bech32.CHECKSUM.length - Bech32.SEPARATOR_ARRAY.length - hrp.length];
    for (int i = 0; i < data.length; i++) {
      final int element = bech32.charAt(hrp.length + Bech32.SEPARATOR_ARRAY.length + i);
      final byte lookup = Bech32.CHARSET_REVERSE[element];
      Bech32.ensure(lookup != -1, "data element not in Bech32 character set");
      data[i] = lookup;
    }
    // process the checksum
    final byte[] checksum = new byte[Bech32.CHECKSUM.length];
    for (int i = 0; i < checksum.length; i++) {
      final int element = bech32.charAt((bech32.length() - Bech32.CHECKSUM.length) + i);
      final byte lookup = Bech32.CHARSET_REVERSE[element];
      Bech32.ensure(lookup != -1, "checksum element not in Bech32 character set");
      checksum[i] = lookup;
    }
    Bech32.ensure(Bech32.verify(hrp, data, checksum), "invalid checksum");
    return new Bech32(new String(hrp, StandardCharsets.US_ASCII), data, bech32);
  }

  /**
   * @param humanReadablePart
   * @param data byte array with element values in the range [0, 31] (base32 values)
   * @return
   */
  public static Bech32 encode(final String humanReadablePart, final byte[] data) {
    Util.checkArgument(humanReadablePart != null, "humanReadablePart must not be null");
    Util.checkArgument(data != null, "data must not be null");
    Util.checkArgument((humanReadablePart.length() >= Bech32.MIN_HRP_LENGTH) && (humanReadablePart.length() <= Bech32.MAX_HRP_LENGTH), "humanReadablePart invalid length");
    Util.checkArgument(data.length <= (Bech32.MAX_BECH32_LENGTH - Bech32.CHECKSUM.length - Bech32.SEPARATOR_ARRAY.length - humanReadablePart.length()), "data invalid length");
    final byte[] hrp = new byte[humanReadablePart.length()];
    for (int i = 0; i < hrp.length; i++) {
      int element = humanReadablePart.charAt(i); // do widening primitive conversion once
      Util.checkArgument((element >= 33) && (element <= 126), "element value out of range");
      if ((element >= 'A') && (element <= 'Z')) {
        element |= 0x20; // ensure the encoded result is lower case and that the checksum is computed over the lower case form
      }
      hrp[i] = (byte) element;
    }
    final byte[] cloned = data.clone(); // make defensive copy after data length is sanity checked
    for (final byte element : cloned) { // verify that all data is in base32. it is a class invariant that the data member is all base32 data.
      Util.checkArgument((element >= 0) && (element <= 31), "element value not in base32");
    }
    final byte[] checksum = Bech32.checksum(hrp, cloned);
    final byte[] result = Util.concat(hrp, Bech32.SEPARATOR_ARRAY, cloned, checksum);
    for (int i = hrp.length + Bech32.SEPARATOR_ARRAY.length; i < result.length; i++) {
      result[i] = Bech32.CHARSET[result[i]];
    }
    return new Bech32(new String(hrp, StandardCharsets.US_ASCII), cloned, new String(result, StandardCharsets.US_ASCII));
  }

  private static void ensure(final boolean value, final String message) throws DecodingException {
    if (!value) {
      throw new DecodingException(message);
    }
  }

  private static byte[] expand(final byte[] hrp) {
    final byte[] result = new byte[(hrp.length * 2) + 1];
    for (int i = 0; i < hrp.length; i++) {
      final int temp = hrp[i] & 0xff;
      result[i] = (byte) (temp >>> 5); // top 3 bits of the byte
      result[hrp.length + 1 + i] = (byte) (temp & 0x1f); // bottom 5 bits of the byte
    }
    return result;
  }

  /**
   * @param values byte arrays with element values in the range [0, 31] (base32 values)
   * @return
   */
  private static int polymod(final byte[]... values) {
    int c = 1; // 6, 5-bit values are packed together as a single 30-bit integer
    for (final byte[] bytes : values) {
      for (final byte element : bytes) {
        final int value = element; // do widening primitive conversion once
        Util.checkArgument((value >= 0) && (value <= 31), "element value not in base32");
        final int c0 = c >>> 25;
        c = ((c & 0x01ffffff) << 5) ^ //
            value ^ //
            (-((c0 >>> 0) & 1) & 0x3b6a57b2) ^ // note: (value XOR 0) = value
            (-((c0 >>> 1) & 1) & 0x26508e6d) ^ //
            (-((c0 >>> 2) & 1) & 0x1ea119fa) ^ //
            (-((c0 >>> 3) & 1) & 0x3d4233dd) ^ //
            (-((c0 >>> 4) & 1) & 0x2a1462b3);
      }
    }
    return c;
  }

  /**
   * @param hrp
   * @param data byte array with element values in the range [0, 31] (base32 values)
   * @param checksum
   * @return
   */
  private static boolean verify(final byte[] hrp, final byte[] data, final byte[] checksum) {
    return Bech32.polymod(Bech32.expand(hrp), data, checksum) == 1;
  }

  private final String humanReadablePart;
  private final byte[] data;
  private final String bech32;

  private Bech32(final String humanReadablePart, final byte[] data, final String bech32) {
    this.humanReadablePart = humanReadablePart;
    this.data = data;
    this.bech32 = bech32;
  }

  /**
   * @return the data
   */
  public byte[] getData() {
    return this.data.clone();
  }

  /**
   * @return the humanReadablePart
   */
  public String getHumanReadablePart() {
    return this.humanReadablePart;
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return this.bech32;
  }
}
