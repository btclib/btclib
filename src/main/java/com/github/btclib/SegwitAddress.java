package com.github.btclib;

/**
 * https://github.com/bitcoin/bips/blob/master/bip-0173.mediawiki
 * https://github.com/satoshilabs/slips/blob/master/slip-0173.md
 * https://github.com/bitcoin/bips/blob/master/bip-0350.mediawiki
 */
public final class SegwitAddress {
  /**
   * Users of this decoder must verify that the returned human readable part is valid for their application.
   * For example, for Bitcoin, the human readable part should be "bc" for mainnet and "tb" for testnet.
   * @param address
   * @throws DecodingException
   * @throws IllegalArgumentException
   */
  public static SegwitAddress decode(final String address) throws DecodingException {
    final Bech32 bech32 = Bech32.decode(address);
    final byte[] data = bech32.getData();
    SegwitAddress.ensure((data.length >= 1) && (data[0] >= 0) && (data[0] <= 16), "invalid witness version");
    final byte[] base256_program = Bech32.convert(data, 5, 1, data.length - 1);
    SegwitAddress.ensure((base256_program != null) && (base256_program.length >= 2) && (base256_program.length <= 40), "invalid witness program");
    SegwitAddress.ensure(!((data[0] == 0) && (base256_program.length != 20) && (base256_program.length != 32)), "invalid witness program length");
    SegwitAddress.ensure(((data[0] == 0) && Bech32.Encoding.BECH32.equals(bech32.getEncoding())) || ((data[0] != 0) && Bech32.Encoding.BECH32M.equals(bech32.getEncoding())), "invalid encoding");
    return new SegwitAddress(bech32, data[0], base256_program);
  }

  /**
   * @param humanReadablePart application specific human readable part of the address
   * @param witnessVersion must be in the range [0, 16], note that these values are NOT "OP_n" push opcode values
   * @param witnessProgram must be [2, 40] bytes long and must be either 20 or 32 bytes long when witnessVersion is 0
   * @throws IllegalArgumentException
   */
  public static SegwitAddress encode(final String humanReadablePart, final int witnessVersion, final byte[] witnessProgram) {
    Util.checkArgument((witnessVersion >= 0) && (witnessVersion <= 16), "witnessVersion invalid");
    Util.checkArgument(witnessProgram != null, "witnessProgram must not be null");
    Util.checkArgument((witnessProgram.length >= 2) && (witnessProgram.length <= 40), "witnessProgram invalid length");
    Util.checkArgument(!((witnessVersion == 0) && (witnessProgram.length != 20) && (witnessProgram.length != 32)), "witnessProgram invalid length for witnessVersion");
    final byte[] base256_program = witnessProgram.clone();
    final byte[] base32_program = Bech32.convert(base256_program, 8, 0, base256_program.length);
    final byte[] data = new byte[1 + base32_program.length];
    data[0] = (byte) witnessVersion;
    System.arraycopy(base32_program, 0, data, 1, base32_program.length);
    final Bech32.Encoding encoding = (witnessVersion == 0) ? Bech32.Encoding.BECH32 : Bech32.Encoding.BECH32M;
    return new SegwitAddress(Bech32.encode(humanReadablePart, data, encoding), data[0], base256_program);
  }

  private static void ensure(final boolean value, final String message) throws DecodingException {
    if (!value) {
      throw new DecodingException(message);
    }
  }

  private final Bech32 bech32;
  private final byte witnessVersion;
  private final byte[] witnessProgram;

  private SegwitAddress(final Bech32 bech32, final byte witnessVersion, final byte[] witnessProgram) {
    this.bech32 = bech32;
    this.witnessVersion = witnessVersion;
    this.witnessProgram = witnessProgram;
  }

  /**
   * @return the bech32
   */
  public Bech32 getBech32() {
    return this.bech32;
  }

  /**
   * @return the humanReadablePart
   */
  public String getHumanReadablePart() {
    return this.bech32.getHumanReadablePart();
  }

  /**
   * @return the witnessProgram
   */
  public byte[] getWitnessProgram() {
    return this.witnessProgram.clone();
  }

  /**
   * @return the witnessVersion
   */
  public byte getWitnessVersion() {
    return this.witnessVersion;
  }

  /**
   * @return A byte array containing the scriptPubKey where the witnessVersion has been correctly converted to "OP_n" push opcodes.
   */
  public byte[] toScriptPubKey() {
    final byte[] result = new byte[2 + this.witnessProgram.length];
    result[0] = this.versionToPushOpcode();
    result[1] = (byte) this.witnessProgram.length; // this is intentionally not a minimal push opcode (reference Bitcoin Core source for CheckMinimalPush and CScript::IsWitnessProgram)
    System.arraycopy(this.witnessProgram, 0, result, 2, this.witnessProgram.length);
    return result;
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return this.bech32.toString();
  }

  private byte versionToPushOpcode() {
    // OP_0 -> 0x00, OP_1 -> 0x51, ..., OP_16 -> 0x60
    if ((this.witnessVersion >= 1) && (this.witnessVersion <= 16)) {
      return (byte) (this.witnessVersion + 0x50);
    }
    return this.witnessVersion;
  }
}
