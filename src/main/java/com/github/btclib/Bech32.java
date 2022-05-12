package com.github.btclib;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * https://github.com/bitcoin/bips/blob/master/bip-0173.mediawiki
 * https://github.com/bitcoin/bips/blob/master/bip-0350.mediawiki
 * https://github.com/sipa/bech32/blob/master/ref/python/segwit_addr.py
 * https://medium.com/@meshcollider/some-of-the-math-behind-bech32-addresses-cf03c7496285
 */
public final class Bech32 {
  public enum Conversion {
    // the max input length limit is primarily a mechanism to: 1) avoid integer overflow, and 2) reduce the resource requirements of boundary testing.
    // the limits (10 * 1024 = 10240 = 10 KiB and 16 * 1024 = 16384 = 16 KiB) are set well below what is needed to avoid overflow (Integer.MAX_VALUE / 8 = 268435455).
    // secondarily, the limits were selected to limit the length of the resulting array of converting from 8 bits per element to 5 bits per element while accommodating
    // known uses. factors that went into determining the desirable result array size limit include:
    // 1) it is much bigger than the limit that makes sense for Bech32 -- From BIP 173: "Even though the chosen code performs reasonably well up to 1023 characters,
    //    other designs are preferable for lengths above 89 characters (excluding the separator)." And: "A Bech32 string is at most 90 characters long".
    // 2) it is bigger than the LND lightning network node's invoice limit of 7089. see https://github.com/lightningnetwork/lnd/issues/4415
    // 3) it is bigger than the QR code alphanumeric limit of 4296
    // 4) it is an exact power of 2 (2 ** 14 = 16384 = 16 KiB)
    EIGHT_TO_FIVE(Bech32.EIGHT_TO_FIVE_MAX_LENGTH, 8, 5), FIVE_TO_EIGHT(Bech32.FIVE_TO_EIGHT_MAX_LENGTH, 5, 8);

    private final int maxInputLength;
    private final int fromBits;
    private final int toBits;
    private final boolean pad;

    Conversion(final int maxInputLength, final int fromBits, final int toBits) {
      assert (0 <= maxInputLength) && (maxInputLength <= 268435455);
      assert ((fromBits == 8) && (toBits == 5)) || ((fromBits == 5) && (toBits == 8));
      this.maxInputLength = maxInputLength;
      this.fromBits = fromBits;
      this.toBits = toBits;
      this.pad = ((this.fromBits == 8) && (this.toBits == 5));
    }

    /**
     * @param input an array of length [0, maxInputLength] with each element containing fromBits bits of data per element
     * @return an array with each element having toBits bits of data per element
     * @throws NullPointerException if the input array is null
     * @throws IllegalArgumentException if the input array is longer than the maxInputLength, or if an input element value
     * is invalid (meaning the value is >= 2 ** fromBits), or if invalid padding bits are encountered.
     */
    public byte[] convert(final byte[] input) {
      Objects.requireNonNull(input, "input must not be null");
      Util.check(input.length <= this.maxInputLength, "input too long");
      final int inputBitCount = input.length * this.fromBits;
      final int wholeGroups = inputBitCount / this.toBits;
      final int remainingBits = inputBitCount % this.toBits;
      final byte[] result = new byte[wholeGroups + ((this.pad && (remainingBits != 0)) ? 1 : 0)];
      final int bitsMask = (1 << this.toBits) - 1;
      int resultIndex = 0;
      int bits = 0;
      int bitsAvailable = 0;
      // process the whole groups in this loop
      for (final byte element : input) {
        final int value = element & 0xff; // mask to discard any 1 bits added during widening primitive conversion sign extension
        Util.check((value >>> this.fromBits) == 0, "input element value invalid"); // make sure no unexpected higher order bits are set
        bits = (bits << this.fromBits) | value;
        bitsAvailable += this.fromBits;
        for (; bitsAvailable >= this.toBits; resultIndex++) {
          bitsAvailable -= this.toBits;
          result[resultIndex] = (byte) ((bits >>> bitsAvailable) & bitsMask);
        }
      }
      assert wholeGroups == resultIndex;
      assert remainingBits == bitsAvailable;
      // process any remaining bits
      if (bitsAvailable > 0) {
        final int paddedRemainingBits = (bits << (this.toBits - bitsAvailable)) & bitsMask; // add the zero valued padding bits via left shift
        // when we go from 8 to 5, we pad with zero valued bits, if necessary. this mechanism is a way to invert
        // the prior conversion by discarding valid padding bits when going from 5 to 8 so that we conclude with the original data.
        if (this.pad) {
          // when inputBitCount is not evenly divisible by toBits, 1 to (toBits - 1) padding bits
          // aka (toBits - remainingBits), each with value 0, will be appended to the input data bits
          result[resultIndex] = (byte) paddedRemainingBits;
          resultIndex++;
        } else {
          // successfully discard bits only if they are actually valid padding bits
          // bip 173: "Any incomplete group at the end MUST be 4 bits or less, MUST be all zeroes, and is discarded."
          Util.check(bitsAvailable < this.fromBits, "invalid padding too many bits");
          Util.check(paddedRemainingBits == 0, "invalid padding non-zero bits");
        }
      }
      assert result.length == resultIndex;
      return result;
    }

    public int getMaxInputLength() {
      return this.maxInputLength;
    }
  }

  public enum Variant {
    BECH32(1), BECH32M(0x2bc830a3);

    private final int constant;

    Variant(final int constant) {
      this.constant = constant;
    }

    int getConstant() {
      return this.constant;
    }
  }

  public static final int MIN_HRP_LENGTH = 1;
  public static final int MAX_HRP_LENGTH = 83;
  public static final int SEPARATOR_LENGTH = 1;
  public static final int CHECKSUM_LENGTH = 6;
  public static final int EIGHT_TO_FIVE_MAX_LENGTH = 10 * 1024;
  public static final int FIVE_TO_EIGHT_MAX_LENGTH = 16 * 1024;
  public static final int MIN_BECH32_LENGTH = Bech32.MIN_HRP_LENGTH + Bech32.SEPARATOR_LENGTH + Bech32.CHECKSUM_LENGTH;
  // // BIP 173 limits MAX_BECH32_LENGTH to 90. we relax this limit to accommodate other applications, like lightning invoices
  public static final int MAX_BECH32_LENGTH = Bech32.MAX_HRP_LENGTH + Bech32.SEPARATOR_LENGTH + Bech32.FIVE_TO_EIGHT_MAX_LENGTH + Bech32.CHECKSUM_LENGTH;
  public static final int SEPARATOR = '1';
  private static final byte[] SEPARATOR_ARRAY = { Bech32.SEPARATOR };
  private static final byte[] CHECKSUM = new byte[Bech32.CHECKSUM_LENGTH];
  private static final byte[] CHARSET = { //
      'q', 'p', 'z', 'r', 'y', '9', 'x', '8', //
      'g', 'f', '2', 't', 'v', 'd', 'w', '0', //
      's', '3', 'j', 'n', '5', '4', 'k', 'h', //
      'c', 'e', '6', 'm', 'u', 'a', '7', 'l', //
  };
  private static final byte[] CHARSET_REVERSE = new byte[128];
  static {
    Arrays.fill(Bech32.CHARSET_REVERSE, (byte) Bech32.CHARSET.length);
    for (int i = 0; i < Bech32.CHARSET.length; i++) {
      Bech32.CHARSET_REVERSE[Bech32.CHARSET[i]] = (byte) i;
    }
  }

  /**
   * @param hrp
   * @param data an array with each element containing 5 data bits per element
   * @return an array with each element containing 5 data bits per element
   */
  private static byte[] checksum(final byte[] hrp, final byte[] data, final Variant variant) {
    final int polymod = Bech32.polymod(Bech32.expand(hrp), data, Bech32.CHECKSUM) ^ variant.getConstant();
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
   * @param input
   * @return
   * @throws NullPointerException
   * @throws DecodingException
   */
  public static Bech32 decode(final String input) throws DecodingException {
    Objects.requireNonNull(input, "input must not be null");
    Util.ensure((Bech32.MIN_BECH32_LENGTH <= input.length()) && (input.length() <= Bech32.MAX_BECH32_LENGTH), "input length invalid");
    int seperatorIndex = -1;
    boolean hasLower = false;
    boolean hasUpper = false;
    for (int i = 0; i < input.length(); i++) {
      final int element = input.charAt(i); // do widening primitive conversion once
      Util.ensure((33 <= element) && (element <= 126), "input element value invalid");
      hasLower |= ('a' <= element) && (element <= 'z');
      hasUpper |= ('A' <= element) && (element <= 'Z');
      Util.ensure((!hasLower || !hasUpper), "input is mixed case");
      if (Bech32.SEPARATOR == element) {
        seperatorIndex = i; // the last one found is the separator
      }
    }
    Util.ensure((Bech32.MIN_HRP_LENGTH <= seperatorIndex) && (seperatorIndex <= Bech32.MAX_HRP_LENGTH), "separator location invalid");
    Util.ensure(seperatorIndex <= (input.length() - Bech32.CHECKSUM_LENGTH - Bech32.SEPARATOR_LENGTH), "separator location invalid");
    // convert to lower case so that the checksum is computed over the lower case form and
    // so that the returned result contains the lower case form of the input and of the hrp
    // this is safe to do now because we know that all char in the string are [33, 126] and won't expand in length
    final String lowerInput = input.toLowerCase(Locale.ROOT);
    // process the human readable part
    final byte[] hrp = new byte[seperatorIndex];
    for (int i = 0; i < hrp.length; i++) {
      hrp[i] = (byte) lowerInput.charAt(i);
    }
    // process the data part
    final byte[] data5 = new byte[lowerInput.length() - Bech32.CHECKSUM_LENGTH - Bech32.SEPARATOR_LENGTH - hrp.length]; // may be zero length
    for (int i = 0; i < data5.length; i++) {
      final int element = lowerInput.charAt(hrp.length + Bech32.SEPARATOR_LENGTH + i);
      final byte lookup = Bech32.CHARSET_REVERSE[element];
      Util.ensure(lookup != Bech32.CHARSET.length, "data element not in Bech32 character set");
      data5[i] = lookup;
    }
    // process the checksum
    final byte[] checksum5 = new byte[Bech32.CHECKSUM_LENGTH];
    for (int i = 0; i < checksum5.length; i++) {
      final int element = lowerInput.charAt((lowerInput.length() - Bech32.CHECKSUM_LENGTH) + i);
      final byte lookup = Bech32.CHARSET_REVERSE[element];
      Util.ensure(lookup != Bech32.CHARSET.length, "checksum element not in Bech32 character set");
      checksum5[i] = lookup;
    }
    final Optional<Variant> variant = Bech32.verify(hrp, data5, checksum5);
    Util.ensure(variant.isPresent(), "checksum invalid");
    return new Bech32(new String(hrp, StandardCharsets.US_ASCII), lowerInput, data5, variant.get());
  }

  /**
   * @param humanReadablePart must not be null, must be of length [1, 83], each element must be in the range [33, 126], must not contain the upper case letters A-Z
   * @param data5 array with 5 bits of data per element, that is, each element has a value in [0, 31]
   * @return
   * @throws NullPointerException
   * @throws IllegalArgumentException
   */
  public static Bech32 encode(final String humanReadablePart, final byte[] data5, final Variant variant) {
    Objects.requireNonNull(humanReadablePart, "humanReadablePart must not be null");
    Objects.requireNonNull(data5, "data5 must not be null");
    Objects.requireNonNull(variant, "variant must not be null");
    Util.check((Bech32.MIN_HRP_LENGTH <= humanReadablePart.length()) && (humanReadablePart.length() <= Bech32.MAX_HRP_LENGTH), "humanReadablePart length invalid");
    Util.check(data5.length <= (Bech32.MAX_BECH32_LENGTH - Bech32.CHECKSUM_LENGTH - Bech32.SEPARATOR_LENGTH - humanReadablePart.length()), "data5 length invalid");
    final byte[] hrp = new byte[humanReadablePart.length()];
    for (int i = 0; i < hrp.length; i++) {
      final int element = humanReadablePart.charAt(i); // do widening primitive conversion once
      Util.check((33 <= element) && (element <= 126), "humanReadablePart element value invalid"); // BIP 173 requirement
      // ensure the encoded result is lower case and that the checksum is computed over only the lower case form
      // there is no reason for the caller to input a humanReadablePart with upper case letters A-Z, thus the exception throw in that case
      Util.check(((element < 'A') || ('Z' < element)), "humanReadablePart element value invalid");
      hrp[i] = (byte) element;
    }
    final byte[] cloned5 = data5.clone(); // make defensive copy after data length is sanity checked
    for (final byte element : cloned5) { // verify that each element contains only 5 bits of data
      Util.check((0 <= element) && (element <= 31), "data5 element value invalid");
    }
    final byte[] checksum5 = Bech32.checksum(hrp, cloned5, variant);
    final byte[] combined = Util.concat(hrp, Bech32.SEPARATOR_ARRAY, cloned5, checksum5);
    for (int i = hrp.length + Bech32.SEPARATOR_LENGTH; i < combined.length; i++) {
      combined[i] = Bech32.CHARSET[combined[i]];
    }
    return new Bech32(new String(hrp, StandardCharsets.US_ASCII), new String(combined, StandardCharsets.US_ASCII), cloned5, variant);
  }

  /**
   * @param hrp
   * @return an array with each element containing 5 data bits per element
   */
  private static byte[] expand(final byte[] hrp) {
    final byte[] result = new byte[(hrp.length * 2) + 1]; // the extra 1 is for the 0 in the middle
    for (int i = 0; i < hrp.length; i++) {
      final int value = hrp[i] & 0xff; // mask to discard any 1 bits added during widening primitive conversion sign extension
      result[i] = (byte) (value >>> 5); // top 3 bits of the byte
      result[hrp.length + 1 + i] = (byte) (value & 0x1f); // bottom 5 bits of the byte. add 1 to skip the 0 in the middle.
    }
    return result;
  }

  /**
   * @param input array(s) with each element containing 5 data bits per element, that is, each element has a value in the range [0, 31]
   * @return
   */
  private static int polymod(final byte[]... input) {
    int result = 1; // 6, 5-bit values are packed together as a single 30-bit integer
    for (final byte[] bytes : input) {
      for (final byte element : bytes) {
        final int value = element & 0xff; // mask to discard any 1 bits added during widening primitive conversion sign extension
        assert (value >>> 5) == 0; // make sure no unexpected higher order bits are set, only the lowest 5 bits should be used
        final int c0 = result >>> 25;
        result = ((result & 0x01ffffff) << 5) ^ value;
        if ((c0 & 1) != 0) {
          result ^= 0x3b6a57b2;
        }
        if ((c0 & 2) != 0) {
          result ^= 0x26508e6d;
        }
        if ((c0 & 4) != 0) {
          result ^= 0x1ea119fa;
        }
        if ((c0 & 8) != 0) {
          result ^= 0x3d4233dd;
        }
        if ((c0 & 16) != 0) {
          result ^= 0x2a1462b3;
        }
      }
    }
    return result;
  }

  /**
   * @param hrp
   * @param data byte array with element values in the range [0, 31] (base32 values)
   * @param checksum
   * @return
   */
  private static Optional<Variant> verify(final byte[] hrp, final byte[] data, final byte[] checksum) {
    final int constant = Bech32.polymod(Bech32.expand(hrp), data, checksum);
    if (Variant.BECH32.getConstant() == constant) {
      return Optional.of(Variant.BECH32);
    }
    if (Variant.BECH32M.getConstant() == constant) {
      return Optional.of(Variant.BECH32M);
    }
    return Optional.empty();
  }

  private final String humanReadablePart;
  private final String encoded;
  private final byte[] data;
  private final Variant variant;

  private Bech32(final String humanReadablePart, final String encoded, final byte[] data, final Variant variant) {
    assert humanReadablePart != null;
    assert encoded != null;
    assert data != null;
    assert variant != null;
    this.humanReadablePart = humanReadablePart;
    this.encoded = encoded;
    this.data = data;
    this.variant = variant;
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
   * @return the variant
   */
  public Variant getVariant() {
    return this.variant;
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return this.encoded;
  }
}