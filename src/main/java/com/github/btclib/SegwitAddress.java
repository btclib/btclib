package com.github.btclib;

import java.util.Arrays;
import java.util.Objects;

/**
 * https://github.com/bitcoin/bips/blob/master/bip-0141.mediawiki
 * https://github.com/bitcoin/bips/blob/master/bip-0173.mediawiki
 * https://github.com/bitcoin/bips/blob/master/bip-0350.mediawiki
 * https://github.com/sipa/bech32/blob/master/ref/python/segwit_addr.py
 */
public final class SegwitAddress {
  // BIP 173: "A Bech32 string is at most 90 characters long ..."
  // BIP 173: "... other designs are preferable for lengths above 89 characters (excluding the separator)."
  // this is not the limit followed in the Bech32 class so that applications besides encoding segwit addresses can be supported
  public static final int MAX_LENGTH = Bech32.MAX_HRP_LENGTH + Bech32.SEPARATOR_LENGTH + Bech32.CHECKSUM_LENGTH;

  /**
   * @param humanReadablePart application specific human readable part of the address; must not be null, must be of length [1, 83], each element must be in the range [33, 126], must not contain any upper case letters
   * @param version the witness version, must be in the range [0, 16]
   * @param program the witness program, must be [2, 40] bytes long and must be {20, 32} bytes long if version is 0
   * @return the SegwitAddress encoded with the given arguments
   * @throws NullPointerException if humanReadablePart or program are null
   * @throws IllegalArgumentException if version not in the range [0, 16], if program length not in [2, 40], if version is 0 and the program length
   * is not 20 or 32, if humanReadablePart fails any of the validity checks specified by https://github.com/bitcoin/bips/blob/master/bip-0173.mediawiki
   */
  public static SegwitAddress of(final String humanReadablePart, final int version, final byte[] program) {
    Objects.requireNonNull(humanReadablePart, "humanReadablePart must not be null");
    Objects.requireNonNull(program, "program must not be null");
    Util.check((Bech32.MIN_HRP_LENGTH <= humanReadablePart.length()) && (humanReadablePart.length() <= Bech32.MAX_HRP_LENGTH), "humanReadablePart length invalid");
    Util.check((0 <= version) && (version <= 16), "version invalid");
    Util.check((2 <= program.length) && (program.length <= 40), "program length invalid");
    Util.check((version != 0) || (program.length == 20 /* v0 p2wpkh */) || (program.length == 32 /* v0 p2wsh */), "program length invalid for version 0");
    final byte[] program8 = program.clone(); // make a defensive copy after the length has been sanity checked. the 8 denotes 8 data bits per element.
    final byte[] program5 = Bech32.convert8to5(program8); // the 5 denotes 5 data bits per element.
    final byte[] data5 = Util.concat(version, program5); // each element contains 5 bits of data
    Util.check(humanReadablePart.length() <= (SegwitAddress.MAX_LENGTH - Bech32.CHECKSUM_LENGTH - data5.length - Bech32.SEPARATOR_LENGTH), "humanReadablePart length invalid");
    final var variant = (version == 0) ? Bech32.Variant.BECH32 : Bech32.Variant.BECH32M;
    final var bech32 = Bech32.encode(humanReadablePart, data5, variant); // humanReadablePart is validated further within
    return new SegwitAddress(bech32, version, program8);
  }

  /**
   * @param expectedHumanReadablePart application specific human readable part of the address; must not be null, must be of length [1, 83], each element must be in the range [33, 126], must not contain any upper case letters
   * @param address a bech32 or bech32m encoded string to decode must be length [8, 90]
   * @return a SegwitAddress decoded from the given address
   * @throws NullPointerException
   * @throws DecodingException
   */
  public static SegwitAddress of(final String expectedHumanReadablePart, final String address) throws DecodingException {
    Objects.requireNonNull(expectedHumanReadablePart, "expectedHumanReadablePart must not be null");
    Objects.requireNonNull(address, "address must not be null");
    Util.ensure((Bech32.MIN_BECH32_LENGTH <= address.length()) && (address.length() <= SegwitAddress.MAX_LENGTH), "address length invalid");
    final var bech32 = Bech32.decode(address);
    Util.ensure(expectedHumanReadablePart.equals(bech32.getHumanReadablePart()), "expectedHumanReadablePart does not match decoded value");
    final byte[] data5 = bech32.getData();
    // 1 for version + [2, 40] for program length when 8 bits per element, which means
    // [Math.ceilDivExact(2 * 8, 5) = 4, Math.ceilDivExact(40 * 8, 5) = 64] for program length when 5 bits per element
    Util.ensure(((1 + 4) <= data5.length) && (data5.length <= (1 + 64)), "decoded data length invalid");
    final int version = data5[0] & 0xff;
    Util.ensure((0 <= version) && (version <= 16), "decoded version invalid");
    Util.ensure(((version == 0) && Bech32.Variant.BECH32.equals(bech32.getVariant())) || ((version != 0) && Bech32.Variant.BECH32M.equals(bech32.getVariant())), "bech32 variant invalid");
    final byte[] program5 = Arrays.copyOfRange(data5, 1, data5.length);
    final byte[] program = Bech32.convert5to8(program5);
    Util.ensure((version != 0) || (program.length == 20 /* v0 p2wpkh */) || (program.length == 32 /* v0 p2wsh */), "decoded program length invalid for version 0");
    return new SegwitAddress(bech32, version, program);
  }

  private final Bech32 bech32;
  private final int version; // witness version
  private final byte[] program; // witness program

  private SegwitAddress(final Bech32 bech32, final int version, final byte[] program) {
    assert bech32 != null;
    assert program != null;
    assert (0 <= version) && (version <= 16);
    assert (2 <= program.length) && (program.length <= 40);
    assert (version != 0) || (program.length == 20 /* v0 p2wpkh */) || (program.length == 32 /* v0 p2wsh */);
    this.bech32 = bech32;
    this.version = version;
    this.program = program; // we don't clone the input array since this is a private constructor and the static factory methods already make defensive copies
  }

  /**
   * @return the humanReadablePart
   */
  public String getHumanReadablePart() {
    return this.bech32.getHumanReadablePart();
  }

  /**
   * @return the program
   */
  public byte[] getProgram() {
    return this.program.clone();
  }

  /**
   * @return the bech32 variant
   */
  public Bech32.Variant getVariant() {
    return this.bech32.getVariant();
  }

  /**
   * @return the version
   */
  public int getVersion() {
    return this.version;
  }

  /**
   * @return a byte array containing the output script, also commonly known as the scriptPubKey, corresponding to this address
   */
  public byte[] toOutputScript() {
    final byte[] result = new byte[2 + this.program.length];
    // native witness output scripts must be of length 4 to 42 (version push opcode byte + push opcode byte + witness program bytes)
    assert (4 <= result.length) && (result.length <= 42);
    // from https://github.com/bitcoin/bips/blob/master/bip-0173.mediawiki
    // "Implementations should take special care when converting the address to a scriptPubkey, where witness version n is stored as
    // OP_n. OP_0 is encoded as 0x00, but OP_1 through OP_16 are encoded as 0x51 though 0x60 (81 to 96 in decimal)."
    result[0] = this.versionAsPushOpcode(); // convert version to a "OP_n" push opcode
    // for the program lengths that we are dealing with [2, 40],
    // a direct push (an opcode indicating the number of bytes to push + the bytes to push) is minimal.
    // reference Bitcoin Core source for ::CheckMinimalPush and CScript::IsWitnessProgram
    result[1] = (byte) this.program.length;
    System.arraycopy(this.program, 0, result, 2, this.program.length);
    return result;
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return this.bech32.toString();
  }

  private byte versionAsPushOpcode() {
    // OP_0 -> 0x00, OP_1 -> 0x51, ..., OP_16 -> 0x60
    if ((this.version >= 1) && (this.version <= 16)) {
      return (byte) (this.version + 0x50);
    }
    return (byte) this.version;
  }
}
