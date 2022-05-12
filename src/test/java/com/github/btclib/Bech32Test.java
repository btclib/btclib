package com.github.btclib;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;

/**
 * https://github.com/bitcoin/bips/blob/master/bip-0084.mediawiki
 * https://github.com/bitcoin/bips/blob/master/bip-0086.mediawiki
 * https://github.com/bitcoin/bips/blob/master/bip-0141.mediawiki
 * https://github.com/bitcoin/bips/blob/master/bip-0143.mediawiki
 * https://github.com/bitcoin/bips/blob/master/bip-0173.mediawiki
 * https://github.com/sipa/bech32/issues/51
 * https://github.com/bitcoin/bips/blob/master/bip-0341.mediawiki
 * https://github.com/bitcoin/bips/blob/master/bip-0341/wallet-test-vectors.json
 * https://github.com/bitcoin/bips/blob/master/bip-0350.mediawiki
 * https://github.com/satoshilabs/slips/blob/master/slip-0032.md
 * https://github.com/satoshilabs/slips/blob/master/slip-0173.md
 * https://github.com/lightning/bolts/blob/master/11-payment-encoding.md
 */
public class Bech32Test {
  @Test
  public void test_convert_0() {
    final var e = Assert.assertThrows(NullPointerException.class, () -> {
      Bech32.Conversion.EIGHT_TO_FIVE.convert(null);
    });
    Assert.assertEquals("input must not be null", e.getMessage());
  }

  @Test
  public void test_convert_1() {
    final var e = Assert.assertThrows(NullPointerException.class, () -> {
      Bech32.Conversion.FIVE_TO_EIGHT.convert(null);
    });
    Assert.assertEquals("input must not be null", e.getMessage());
  }

  @Test
  public void test_convert_5_to_8() {
    final var testVectors = new LinkedHashMap<byte[], Object>();
    testVectors.put(new byte[Bech32.Conversion.FIVE_TO_EIGHT.getMaxInputLength() + 1], new IllegalArgumentException("input too long"));
    testVectors.put(new byte[Bech32.Conversion.FIVE_TO_EIGHT.getMaxInputLength()], new byte[Bech32.Conversion.EIGHT_TO_FIVE.getMaxInputLength()]); // max length input
    testVectors.put(Util.EMPTY_BYTE_ARRAY, Util.EMPTY_BYTE_ARRAY); // min length input
    testVectors.put(Util.fromHexString("ff"), new IllegalArgumentException("input element value invalid")); // max byte value
    testVectors.put(Util.fromHexString("80"), new IllegalArgumentException("input element value invalid")); // an invalid element value
    testVectors.put(Util.fromHexString("40"), new IllegalArgumentException("input element value invalid")); // an invalid element value
    testVectors.put(Util.fromHexString("20"), new IllegalArgumentException("input element value invalid")); // an invalid element value (one greater than max value for 5 bits)
    testVectors.put(Util.fromHexString("1f"), new IllegalArgumentException("invalid padding too many bits")); // only provide 5 bits of data which is not enough to convert back to 8
    testVectors.put(Util.fromHexString("00"), new IllegalArgumentException("invalid padding too many bits")); // only provide 5 bits of data which is not enough to convert back to 8
    testVectors.put(new byte[] { 0b11111, 0b11110, }, new IllegalArgumentException("invalid padding non-zero bits")); // padding bits should be zero
    testVectors.put(new byte[] { 0b11111, 0b11101, }, new IllegalArgumentException("invalid padding non-zero bits")); // padding bits should be zero
    testVectors.put(new byte[] { 0b11111, 0b11111, }, new IllegalArgumentException("invalid padding non-zero bits")); // padding bits should be zero
    testVectors.put(new byte[] { 0b11111, 0b11100, }, Util.fromHexString("ff")); // 2 good incoming padding bits
    testVectors.put(new byte[] { 0b11111, 0b11111, 0b11111, 0b10000, }, Util.fromHexString("ffff")); // 4 good incoming padding bits
    testVectors.put(new byte[] { 0b11111, 0b11111, 0b11111, 0b11111, 0b11110, }, Util.fromHexString("ffffff")); // 1 good incoming padding bits
    testVectors.put(new byte[] { 0b11111, 0b11111, 0b11111, 0b11111, 0b11111, 0b11111, 0b11000, }, Util.fromHexString("ffffffff")); // 3 good incoming padding bits
    testVectors.put(new byte[] { 0b11111, 0b11111, 0b11111, 0b11111, 0b11111, 0b11111, 0b11111, 0b11111, }, Util.fromHexString("ffffffffff")); // 0 good incoming padding bits
    testVectors.put(new byte[4], new byte[2]); // 4 is the minimum size of a 5 bit per element witness program
    testVectors.put(new byte[64], new byte[40]); // 64 is the maximum size of a 5 bit per element witness program
    testVectors.put(new byte[32], new byte[20]); // 32 is the size of a p2wpkh 5 bit per element witness program
    testVectors.put(new byte[52], new byte[32]); // 52 is the size of a p2wsh 5 bit per element witness program
    for (final var entry : testVectors.entrySet()) {
      try {
        final byte[] result = Bech32.Conversion.FIVE_TO_EIGHT.convert(entry.getKey());
        Assert.assertArrayEquals((byte[]) entry.getValue(), result);
      } catch (final Exception e) {
        Assert.assertEquals(entry.getValue().toString(), e.toString());
      }
    }
  }

  @Test
  public void test_convert_8_to_5() {
    final var testVectors = new LinkedHashMap<byte[], Object>();
    testVectors.put(new byte[Bech32.Conversion.EIGHT_TO_FIVE.getMaxInputLength() + 1], new IllegalArgumentException("input too long"));
    testVectors.put(new byte[Bech32.Conversion.EIGHT_TO_FIVE.getMaxInputLength()], new byte[Bech32.Conversion.FIVE_TO_EIGHT.getMaxInputLength()]); // max length input
    testVectors.put(Util.EMPTY_BYTE_ARRAY, Util.EMPTY_BYTE_ARRAY); // min length input
    testVectors.put(Util.fromHexString("ff"), new byte[] { 0b11111, 0b11100, }); // max byte value, tests sign extension
    testVectors.put(Util.fromHexString("00"), new byte[] { 0, 0, }); // min byte value
    testVectors.put(Util.fromHexString("1f"), new byte[] { 0b00011, 0b11100, });
    testVectors.put(Util.fromHexString("80"), new byte[] { 0b10000, 0b00000, });
    testVectors.put(Util.fromHexString("40"), new byte[] { 0b01000, 0b00000, });
    testVectors.put(Util.fromHexString("20"), new byte[] { 0b00100, 0b00000, });
    testVectors.put(Util.fromHexString("10"), new byte[] { 0b00010, 0b00000, });
    testVectors.put(Util.fromHexString("08"), new byte[] { 0b00001, 0b00000, });
    testVectors.put(Util.fromHexString("04"), new byte[] { 0b00000, 0b10000, });
    testVectors.put(Util.fromHexString("02"), new byte[] { 0b00000, 0b01000, });
    testVectors.put(Util.fromHexString("01"), new byte[] { 0b00000, 0b00100, }); // 2 padding bits necessary
    testVectors.put(Util.fromHexString("0102"), new byte[] { 0b00000, 0b00100, 0b00001, 0b00000, }); // 4 padding bits necessary
    testVectors.put(Util.fromHexString("010203"), new byte[] { 0b00000, 0b00100, 0b00001, 0b00000, 0b00110, }); // 1 padding bits necessary
    testVectors.put(Util.fromHexString("01020304"), new byte[] { 0b00000, 0b00100, 0b00001, 0b00000, 0b00110, 0b00001, 0b00000, }); // 3 padding bits necessary
    testVectors.put(Util.fromHexString("0102030405"), new byte[] { 0b00000, 0b00100, 0b00001, 0b00000, 0b00110, 0b00001, 0b00000, 0b00101, }); // 0 padding bits necessary
    testVectors.put(Util.fromHexString("ff"), new byte[] { 0b11111, 0b11100, }); // 2 padding bits necessary
    testVectors.put(Util.fromHexString("ffff"), new byte[] { 0b11111, 0b11111, 0b11111, 0b10000, }); // 4 padding bits necessary
    testVectors.put(Util.fromHexString("ffffff"), new byte[] { 0b11111, 0b11111, 0b11111, 0b11111, 0b11110, }); // 1 padding bits necessary
    testVectors.put(Util.fromHexString("ffffffff"), new byte[] { 0b11111, 0b11111, 0b11111, 0b11111, 0b11111, 0b11111, 0b11000, }); // 3 padding bits necessary
    testVectors.put(Util.fromHexString("ffffffffff"), new byte[] { 0b11111, 0b11111, 0b11111, 0b11111, 0b11111, 0b11111, 0b11111, 0b11111, }); // 0 padding bits necessary
    testVectors.put(new byte[2], new byte[4]); // 2 is the minimum size of a witness program
    testVectors.put(new byte[40], new byte[64]); // 40 is the maximum size of a witness program
    testVectors.put(new byte[20], new byte[32]); // 20 is the size of a p2wpkh witness program
    testVectors.put(new byte[32], new byte[52]); // 32 is the size of a p2wsh witness program
    for (final var entry : testVectors.entrySet()) {
      try {
        final byte[] result = Bech32.Conversion.EIGHT_TO_FIVE.convert(entry.getKey());
        Assert.assertArrayEquals((byte[]) entry.getValue(), result);
      } catch (final Exception e) {
        Assert.assertEquals(entry.getValue().toString(), e.toString());
      }
    }
  }

  @Test
  public void test_convert_roundtrip() throws Exception {
    final var tests = new LinkedList<byte[]>();
    final byte[] allOnes = new byte[Bech32.Conversion.EIGHT_TO_FIVE.getMaxInputLength()];
    Arrays.fill(allOnes, (byte) 0xff);
    tests.add(allOnes);
    tests.add(new byte[Bech32.Conversion.EIGHT_TO_FIVE.getMaxInputLength()]);
    tests.add(Util.EMPTY_BYTE_ARRAY);
    tests.add(Util.fromHexString("ff"));
    tests.add(Util.fromHexString("20"));
    tests.add(Util.fromHexString("1f"));
    tests.add(Util.fromHexString("01"));
    tests.add(Util.fromHexString("00"));
    tests.add(Util.fromHexString("751e76e8199196d454941c45d1b3a323f1433bd6")); // hash160(0279be667ef9dcbbac55a06295ce870b07029bfcdb2dce28d959f2815b16f81798) p2wpkh witness program
    tests.add(Util.fromHexString("1863143c14c5166804bd19203356da136c985678cd4d27a1b8c6329604903262")); // sha256(210279be667ef9dcbbac55a06295ce870b07029bfcdb2dce28d959f2815b16f81798ac) p2wsh witness program
    tests.add(new byte[2]);
    tests.add(new byte[40]);
    tests.add(new byte[20]);
    tests.add(new byte[32]);
    for (final var test : tests) {
      final byte[] result = Bech32.Conversion.FIVE_TO_EIGHT.convert(Bech32.Conversion.EIGHT_TO_FIVE.convert(test));
      Assert.assertArrayEquals(test, result);
    }
  }

  @Test
  public void test_decode_0() {
    final var e = Assert.assertThrows(NullPointerException.class, () -> {
      Bech32.decode(null);
    });
    Assert.assertEquals("input must not be null", e.getMessage());
  }

  @Test
  public void test_decode_1() {
    final var testVectors = new LinkedHashMap<String, Object>();
    // exercise exceptional code paths
    testVectors.put("", new DecodingException("input length invalid"));
    testVectors.put(Util.multiply(new StringBuilder().appendCodePoint(0).toString(), Bech32.MIN_BECH32_LENGTH - 1), new DecodingException("input length invalid"));
    testVectors.put(Util.multiply(new StringBuilder().appendCodePoint(0).toString(), Bech32.MAX_BECH32_LENGTH + 1), new DecodingException("input length invalid"));
    testVectors.put(Util.multiply(new StringBuilder().appendCodePoint(0).toString(), Bech32.MIN_BECH32_LENGTH), new DecodingException("input element value invalid")); // all zeros
    testVectors.put(Util.multiply(new StringBuilder().appendCodePoint(0).toString(), Bech32.MAX_BECH32_LENGTH), new DecodingException("input element value invalid")); // all zeros
    testVectors.put(new StringBuilder().appendCodePoint(32).append("1234567").toString(), new DecodingException("input element value invalid")); // 32 out of [33, 126]
    testVectors.put(new StringBuilder().appendCodePoint(127).append("1234567").toString(), new DecodingException("input element value invalid")); // 127 out of [33, 126]
    testVectors.put(new StringBuilder().appendCodePoint(Character.codePointOf("BITCOIN SIGN")).append("1234567").toString(), new DecodingException("input element value invalid")); // code point in bmp
    testVectors.put(new StringBuilder().appendCodePoint(Character.codePointOf("PILE OF POO")).append("1234567").toString(), new DecodingException("input element value invalid")); // code point beyond bmp
    testVectors.put("abcdefgH", new DecodingException("input is mixed case"));
    testVectors.put("ABCDEFGh", new DecodingException("input is mixed case"));
    testVectors.put("aBcdefgh", new DecodingException("input is mixed case"));
    testVectors.put("A12UeL5L", new DecodingException("input is mixed case"));
    testVectors.put("abcdefgh", new DecodingException("separator location invalid")); // no separator
    testVectors.put("12345678", new DecodingException("separator location invalid")); // separator in first position
    testVectors.put(Util.multiply("h", Bech32.MAX_HRP_LENGTH + 1) + "1cccccc", new DecodingException("separator location invalid")); // separator one past the limit
    testVectors.put("11111111111111111111111111111111111111111111111111111111111111111111111111111111111114g3vaq", new DecodingException("separator location invalid"));
    testVectors.put("1111111111111111111111111111111111111111111111111111111111111111111111111111111111111q5pqcz", new DecodingException("separator location invalid"));
    testVectors.put("11145678", new DecodingException("separator location invalid")); // separator too late, it cuts into the checksum
    testVectors.put(Util.multiply("h", Bech32.MAX_HRP_LENGTH) + "1cccccc", new DecodingException("checksum invalid"));
    testVectors.put("01b234567", new DecodingException("data element not in Bech32 character set"));
    testVectors.put("0123456b", new DecodingException("checksum element not in Bech32 character set"));
    testVectors.put("b1q23456b", new DecodingException("checksum element not in Bech32 character set"));
    testVectors.put("b1b23456b", new DecodingException("data element not in Bech32 character set"));
    testVectors.put("b1qchksum", new DecodingException("checksum invalid"));
    testVectors.put("01234567", new DecodingException("checksum invalid"));
    // exercise basic successful code paths
    testVectors.put("bc1gmk9yu", new String[] { "bc", "BECH32", "", });
    testVectors.put("tb1cy0q7p", new String[] { "tb", "BECH32", "", });
    testVectors.put("bcrt17capp7", new String[] { "bcrt", "BECH32", "", });
    testVectors.put("?1ezyfcl", new String[] { "?", "BECH32", "", });
    testVectors.put(new String(new StringBuilder().appendCodePoint(Character.codePointOf("BITCOIN SIGN")).append("1ezyfcl").toString().getBytes(StandardCharsets.US_ASCII), StandardCharsets.US_ASCII), new String[] { "?", "BECH32", "", }); // demonstrate how the replacement string of "?" is used to replace unmappable characters when converting to US-ASCII
    testVectors.put("11merq64", new String[] { "1", "BECH32", "", });
    testVectors.put("bc1a8xfp7", new String[] { "bc", "BECH32M", "", });
    testVectors.put("tb1dclvmr", new String[] { "tb", "BECH32M", "", });
    testVectors.put("bcrt1tyddyu", new String[] { "bcrt", "BECH32M", "", });
    testVectors.put("?1v759aa", new String[] { "?", "BECH32M", "", });
    testVectors.put("11w9nvlh", new String[] { "1", "BECH32M", "", });
    testVectors.put("1111111111111111111111111111111111111111111111111111111111111111111111111111111111116v0k6w", new String[] { Util.multiply("1", Bech32.MAX_HRP_LENGTH), "BECH32", "", });
    testVectors.put("1111111111111111111111111111111111111111111111111111111111111111111111111111111111110sl6lv", new String[] { Util.multiply("1", Bech32.MAX_HRP_LENGTH), "BECH32M", "", });
    testVectors.put("bc1q9zpgru", new String[] { "bc", "BECH32", "00", });
    testVectors.put("tb1q06v2t0", new String[] { "tb", "BECH32", "00", });
    testVectors.put("bcrt1q08wsgc", new String[] { "bcrt", "BECH32", "00", });
    testVectors.put("bc1qs73yx7", new String[] { "bc", "BECH32M", "00", });
    testVectors.put("tb1q6xuxwd", new String[] { "tb", "BECH32M", "00", });
    testVectors.put("bcrt1q6m7ud6", new String[] { "bcrt", "BECH32M", "00", });
    testVectors.put("bc1pc54a7w", new String[] { "bc", "BECH32", "01", });
    testVectors.put("tb1pjvclka", new String[] { "tb", "BECH32", "01", });
    testVectors.put("bcrt1pj36942", new String[] { "bcrt", "BECH32", "01", });
    testVectors.put("bc1pdg93mv", new String[] { "bc", "BECH32M", "01", });
    testVectors.put("tb1p8sgnnl", new String[] { "tb", "BECH32M", "01", });
    testVectors.put("bcrt1p8d2fsg", new String[] { "bcrt", "BECH32M", "01", });
    testVectors.put("bc1qqsa7s0f", new String[] { "bc", "BECH32", "0000", });
    testVectors.put("bc1qylhukqn", new String[] { "bc", "BECH32", "0004", });
    testVectors.put("A12UEL5L", new String[] { "a", "BECH32", "", });
    testVectors.put("a12uel5l", new String[] { "a", "BECH32", "", });
    testVectors.put("abcdef1qpzry9x8gf2tvdw0s3jn54khce6mua7lmqqqxw", new String[] { "abcdef", "BECH32", "000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f", });
    testVectors.put("abcdef1qpzry9x8gf2tvdw0s3jn54khce6mua7lwusvrv", new String[] { "abcdef", "BECH32M", "000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f", });
    testVectors.put(Util.multiply("m", Bech32.MAX_HRP_LENGTH) + "1" + Util.multiply("q", Bech32.FIVE_TO_EIGHT_MAX_LENGTH) + "s7r420", new String[] { "mmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm", "BECH32", Util.multiply("00", Bech32.FIVE_TO_EIGHT_MAX_LENGTH), });
    testVectors.put(Util.multiply("m", Bech32.MAX_HRP_LENGTH) + "1" + Util.multiply("q", Bech32.FIVE_TO_EIGHT_MAX_LENGTH) + "9zne0d", new String[] { "mmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm", "BECH32M", Util.multiply("00", Bech32.FIVE_TO_EIGHT_MAX_LENGTH), });
    testVectors.put("bc1qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqq3sp7ks", new String[] { "bc", "BECH32", Util.multiply("00", 66 + 1), });
    for (final var entry : testVectors.entrySet()) {
      try {
        final var result = Bech32.decode(entry.getKey());
        final var expected = (String[]) entry.getValue();
        Assert.assertEquals(entry.getKey(), entry.getKey().toLowerCase(Locale.ROOT), result.toString());
        Assert.assertEquals(entry.getKey(), expected[0], result.getHumanReadablePart());
        Assert.assertEquals(entry.getKey(), Bech32.Variant.valueOf(expected[1]), result.getVariant());
        Assert.assertArrayEquals(entry.getKey(), Util.fromHexString(expected[2]), result.getData());
      } catch (final Exception e) {
        Assert.assertEquals(entry.getKey(), entry.getValue().toString(), e.toString());
      }
    }
  }

  @Test
  public void test_decode_vectors() {
    final var testVectors = new LinkedHashMap<String, Object>();
    // https://github.com/bitcoin/bips/blob/master/bip-0173.mediawiki#test-vectors
    testVectors.put("A12UEL5L", new String[] { "a", "BECH32", });
    testVectors.put("a12uel5l", new String[] { "a", "BECH32", });
    testVectors.put("an83characterlonghumanreadablepartthatcontainsthenumber1andtheexcludedcharactersbio1tt5tgs", new String[] { "an83characterlonghumanreadablepartthatcontainsthenumber1andtheexcludedcharactersbio", "BECH32", });
    testVectors.put("abcdef1qpzry9x8gf2tvdw0s3jn54khce6mua7lmqqqxw", new String[] { "abcdef", "BECH32", });
    testVectors.put("11qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqc8247j", new String[] { "1", "BECH32", });
    testVectors.put("split1checkupstagehandshakeupstreamerranterredcaperred2y9e3w", new String[] { "split", "BECH32", });
    testVectors.put("?1ezyfcl", new String[] { "?", "BECH32", });
    testVectors.put(new String("\u00801ezyfcl".getBytes(StandardCharsets.US_ASCII), StandardCharsets.US_ASCII), new String[] { "?", "BECH32", }); // "?1ezyfcl" character unmappable to ascii replaced by ?
    testVectors.put(new StringBuilder().appendCodePoint(0x20).append("1nwldj5").toString(), new DecodingException("input element value invalid"));
    testVectors.put(new StringBuilder().appendCodePoint(0x7f).append("1axkwrx").toString(), new DecodingException("input element value invalid"));
    testVectors.put(new StringBuilder().appendCodePoint(0x80).append("1eym55h").toString(), new DecodingException("input element value invalid"));
    testVectors.put("an84characterslonghumanreadablepartthatcontainsthenumber1andtheexcludedcharactersbio1569pvx", new DecodingException("separator location invalid"));
    testVectors.put("pzry9x0s0muk", new DecodingException("separator location invalid"));
    testVectors.put("1pzry9x0s0muk", new DecodingException("separator location invalid"));
    testVectors.put("x1b4n0q5v", new DecodingException("data element not in Bech32 character set"));
    testVectors.put("li1dgmt3", new DecodingException("separator location invalid"));
    testVectors.put(new StringBuilder().append("de1lg7wt").appendCodePoint(0xff).toString(), new DecodingException("input element value invalid"));
    testVectors.put("A1G7SGD8", new DecodingException("checksum invalid"));
    testVectors.put("10a06t8", new DecodingException("input length invalid"));
    testVectors.put("1qzzfhee", new DecodingException("separator location invalid"));
    testVectors.put("BC1QW508D6QEJXTDG4Y5R3ZARVARY0C5XW7KV8F3T4", new String[] { "bc", "BECH32", "751e76e8199196d454941c45d1b3a323f1433bd6", });
    testVectors.put("tb1qrp33g0q5c5txsp9arysrx4k6zdkfs4nce4xj0gdcccefvpysxf3q0sl5k7", new String[] { "tb", "BECH32", "1863143c14c5166804bd19203356da136c985678cd4d27a1b8c6329604903262", });
    testVectors.put("bc1pw508d6qejxtdg4y5r3zarvary0c5xw7kw508d6qejxtdg4y5r3zarvary0c5xw7k7grplx", new String[] { "bc", "BECH32", "751e76e8199196d454941c45d1b3a323f1433bd6751e76e8199196d454941c45d1b3a323f1433bd6", });
    testVectors.put("BC1SW50QA3JX3S", new String[] { "bc", "BECH32", "751e", });
    testVectors.put("bc1zw508d6qejxtdg4y5r3zarvaryvg6kdaj", new String[] { "bc", "BECH32", "751e76e8199196d454941c45d1b3a323", });
    testVectors.put("tb1qqqqqp399et2xygdj5xreqhjjvcmzhxw4aywxecjdzew6hylgvsesrxh6hy", new String[] { "tb", "BECH32", "000000c4a5cad46221b2a187905e5266362b99d5e91c6ce24d165dab93e86433", });
    testVectors.put("tc1qw508d6qejxtdg4y5r3zarvary0c5xw7kg3g4ty", new String[] { "tc", "BECH32", }); // this should fail for the SegwitAddress class when specifying a tb hrp, but it should Bech32 decode just fine
    testVectors.put("bc1qw508d6qejxtdg4y5r3zarvary0c5xw7kv8f3t5", new DecodingException("checksum invalid"));
    testVectors.put("BC13W508D6QEJXTDG4Y5R3ZARVARY0C5XW7KN40WF2", new String[] { "bc", "BECH32", }); // this should fail for the SegwitAddress class
    testVectors.put("bc1rw5uspcuh", new String[] { "bc", "BECH32", }); // this should fail for the SegwitAddress class
    testVectors.put("bc10w508d6qejxtdg4y5r3zarvary0c5xw7kw508d6qejxtdg4y5r3zarvary0c5xw7kw5rljs90", new String[] { "bc", "BECH32", }); // this should fail for the SegwitAddress class
    testVectors.put("BC1QR508D6QEJXTDG4Y5R3ZARVARYV98GJ9P", new String[] { "bc", "BECH32", }); // this should fail for the SegwitAddress class
    testVectors.put("tb1qrp33g0q5c5txsp9arysrx4k6zdkfs4nce4xj0gdcccefvpysxf3q0sL5k7", new DecodingException("input is mixed case"));
    testVectors.put("bc1zw508d6qejxtdg4y5r3zarvaryvqyzf3du", new String[] { "bc", "BECH32", "java.lang.IllegalArgumentException: invalid padding too many bits", }); // this should fail for the SegwitAddress class
    testVectors.put("tb1qrp33g0q5c5txsp9arysrx4k6zdkfs4nce4xj0gdcccefvpysxf3pjxtptv", new String[] { "tb", "BECH32", "java.lang.IllegalArgumentException: invalid padding non-zero bits", }); // this should fail for the SegwitAddress class
    testVectors.put("bc1gmk9yu", new String[] { "bc", "BECH32", }); // this should fail for the SegwitAddress class
    // https://github.com/sipa/bech32/issues/51
    testVectors.put("ii2134hk2xmat79tp", new String[] { "ii2", "BECH32", });
    testVectors.put("ii2134hk2xmat79tqp", new String[] { "ii2", "BECH32", });
    testVectors.put("ii2134hk2xmat79tqqp", new String[] { "ii2", "BECH32", });
    testVectors.put("eyg5bsz1l2mrq5ypl40hqqqp", new String[] { "eyg5bsz", "BECH32", });
    testVectors.put("eyg5bsz1l2mrq5ypl40hqqp", new String[] { "eyg5bsz", "BECH32", });
    // https://github.com/bitcoin/bips/blob/master/bip-0350.mediawiki#test-vectors
    testVectors.put("A1LQFN3A", new String[] { "a", "BECH32M", });
    testVectors.put("a1lqfn3a", new String[] { "a", "BECH32M", });
    testVectors.put("an83characterlonghumanreadablepartthatcontainsthetheexcludedcharactersbioandnumber11sg7hg6", new String[] { "an83characterlonghumanreadablepartthatcontainsthetheexcludedcharactersbioandnumber1", "BECH32M", });
    testVectors.put("abcdef1l7aum6echk45nj3s0wdvt2fg8x9yrzpqzd3ryx", new String[] { "abcdef", "BECH32M", });
    testVectors.put("11llllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllludsr8", new String[] { "1", "BECH32M", });
    testVectors.put("split1checkupstagehandshakeupstreamerranterredcaperredlc445v", new String[] { "split", "BECH32M", });
    testVectors.put("?1v759aa", new String[] { "?", "BECH32M", });
    testVectors.put(new StringBuilder().appendCodePoint(0x20).append("1xj0phk").toString(), new DecodingException("input element value invalid"));
    testVectors.put(new StringBuilder().appendCodePoint(0x7f).append("1g6xzxy").toString(), new DecodingException("input element value invalid"));
    testVectors.put(new StringBuilder().appendCodePoint(0x80).append("1vctc34").toString(), new DecodingException("input element value invalid"));
    testVectors.put("an84characterslonghumanreadablepartthatcontainsthetheexcludedcharactersbioandnumber11d6pts4", new DecodingException("separator location invalid"));
    testVectors.put("qyrz8wqd2c9m", new DecodingException("separator location invalid"));
    testVectors.put("1qyrz8wqd2c9m", new DecodingException("separator location invalid"));
    testVectors.put("y1b0jsk6g", new DecodingException("data element not in Bech32 character set"));
    testVectors.put("lt1igcx5c0", new DecodingException("data element not in Bech32 character set"));
    testVectors.put("in1muywd", new DecodingException("separator location invalid"));
    testVectors.put("mm1crxm3i", new DecodingException("checksum element not in Bech32 character set"));
    testVectors.put("au1s5cgom", new DecodingException("checksum element not in Bech32 character set"));
    testVectors.put("M1VUXWEZ", new DecodingException("checksum invalid"));
    testVectors.put("16plkw9", new DecodingException("input length invalid"));
    testVectors.put("1p2gdwpf", new DecodingException("separator location invalid"));
    testVectors.put("BC1QW508D6QEJXTDG4Y5R3ZARVARY0C5XW7KV8F3T4", new String[] { "bc", "BECH32", "751e76e8199196d454941c45d1b3a323f1433bd6", });
    testVectors.put("tb1qrp33g0q5c5txsp9arysrx4k6zdkfs4nce4xj0gdcccefvpysxf3q0sl5k7", new String[] { "tb", "BECH32", "1863143c14c5166804bd19203356da136c985678cd4d27a1b8c6329604903262", });
    testVectors.put("bc1pw508d6qejxtdg4y5r3zarvary0c5xw7kw508d6qejxtdg4y5r3zarvary0c5xw7kt5nd6y", new String[] { "bc", "BECH32M", "751e76e8199196d454941c45d1b3a323f1433bd6751e76e8199196d454941c45d1b3a323f1433bd6", });
    testVectors.put("BC1SW50QGDZ25J", new String[] { "bc", "BECH32M", "751e", });
    testVectors.put("bc1zw508d6qejxtdg4y5r3zarvaryvaxxpcs", new String[] { "bc", "BECH32M", "751e76e8199196d454941c45d1b3a323", });
    testVectors.put("tb1qqqqqp399et2xygdj5xreqhjjvcmzhxw4aywxecjdzew6hylgvsesrxh6hy", new String[] { "tb", "BECH32", "000000c4a5cad46221b2a187905e5266362b99d5e91c6ce24d165dab93e86433", });
    testVectors.put("tb1pqqqqp399et2xygdj5xreqhjjvcmzhxw4aywxecjdzew6hylgvsesf3hn0c", new String[] { "tb", "BECH32M", "000000c4a5cad46221b2a187905e5266362b99d5e91c6ce24d165dab93e86433", });
    testVectors.put("bc1p0xlxvlhemja6c4dqv22uapctqupfhlxm9h8z3k2e72q4k9hcz7vqzk5jj0", new String[] { "bc", "BECH32M", "79be667ef9dcbbac55a06295ce870b07029bfcdb2dce28d959f2815b16f81798", });
    testVectors.put("tc1p0xlxvlhemja6c4dqv22uapctqupfhlxm9h8z3k2e72q4k9hcz7vq5zuyut", new String[] { "tc", "BECH32M", }); // this should fail for the SegwitAddress class when specifying a tb hrp, but it should Bech32 decode just fine
    testVectors.put("bc1p0xlxvlhemja6c4dqv22uapctqupfhlxm9h8z3k2e72q4k9hcz7vqh2y7hd", new String[] { "bc", "BECH32", });
    testVectors.put("tb1z0xlxvlhemja6c4dqv22uapctqupfhlxm9h8z3k2e72q4k9hcz7vqglt7rf", new String[] { "tb", "BECH32", });
    testVectors.put("BC1S0XLXVLHEMJA6C4DQV22UAPCTQUPFHLXM9H8Z3K2E72Q4K9HCZ7VQ54WELL", new String[] { "bc", "BECH32", });
    testVectors.put("bc1qw508d6qejxtdg4y5r3zarvary0c5xw7kemeawh", new String[] { "bc", "BECH32M", });
    testVectors.put("tb1q0xlxvlhemja6c4dqv22uapctqupfhlxm9h8z3k2e72q4k9hcz7vq24jc47", new String[] { "tb", "BECH32M", });
    testVectors.put("bc1p38j9r5y49hruaue7wxjce0updqjuyyx0kh56v8s25huc6995vvpql3jow4", new DecodingException("checksum element not in Bech32 character set"));
    testVectors.put("BC130XLXVLHEMJA6C4DQV22UAPCTQUPFHLXM9H8Z3K2E72Q4K9HCZ7VQ7ZWS8R", new String[] { "bc", "BECH32M", }); // this should fail for the SegwitAddress class
    testVectors.put("bc1pw5dgrnzv", new String[] { "bc", "BECH32M", }); // this should fail for the SegwitAddress class
    testVectors.put("bc1p0xlxvlhemja6c4dqv22uapctqupfhlxm9h8z3k2e72q4k9hcz7v8n0nx0muaewav253zgeav", new String[] { "bc", "BECH32M", }); // this should fail for the SegwitAddress class
    testVectors.put("BC1QR508D6QEJXTDG4Y5R3ZARVARYV98GJ9P", new String[] { "bc", "BECH32", }); // this should fail for the SegwitAddress class
    testVectors.put("tb1p0xlxvlhemja6c4dqv22uapctqupfhlxm9h8z3k2e72q4k9hcz7vq47Zagq", new DecodingException("input is mixed case"));
    testVectors.put("bc1p0xlxvlhemja6c4dqv22uapctqupfhlxm9h8z3k2e72q4k9hcz7v07qwwzcrf", new String[] { "bc", "BECH32M", "java.lang.IllegalArgumentException: invalid padding too many bits", });
    testVectors.put("tb1p0xlxvlhemja6c4dqv22uapctqupfhlxm9h8z3k2e72q4k9hcz7vpggkg4j", new String[] { "tb", "BECH32M", "java.lang.IllegalArgumentException: invalid padding non-zero bits", });
    testVectors.put("bc1gmk9yu", new String[] { "bc", "BECH32", }); // this should fail for the SegwitAddress class
    // https://github.com/satoshilabs/slips/blob/master/slip-0173.md
    // https://github.com/bitcoin/bips/blob/master/bip-0086.mediawiki#test-vectors
    // https://github.com/bitcoin/bips/blob/master/bip-0084.mediawiki#Test_vectors
    testVectors.put("bc1qw508d6qejxtdg4y5r3zarvary0c5xw7kv8f3t4", new String[] { "bc", "BECH32", "751e76e8199196d454941c45d1b3a323f1433bd6", }); // version 0 program 751e76e8199196d454941c45d1b3a323f1433bd6 p2wpkh
    testVectors.put("tb1qw508d6qejxtdg4y5r3zarvary0c5xw7kxpjzsx", new String[] { "tb", "BECH32", "751e76e8199196d454941c45d1b3a323f1433bd6", }); // version 0 program 751e76e8199196d454941c45d1b3a323f1433bd6 p2wpkh
    testVectors.put("bcrt1qw508d6qejxtdg4y5r3zarvary0c5xw7kygt080", new String[] { "bcrt", "BECH32", "751e76e8199196d454941c45d1b3a323f1433bd6", }); // version 0 program 751e76e8199196d454941c45d1b3a323f1433bd6 p2wpkh
    testVectors.put("bc1qrp33g0q5c5txsp9arysrx4k6zdkfs4nce4xj0gdcccefvpysxf3qccfmv3", new String[] { "bc", "BECH32", "1863143c14c5166804bd19203356da136c985678cd4d27a1b8c6329604903262", }); // version 0 program 1863143c14c5166804bd19203356da136c985678cd4d27a1b8c6329604903262 p2wsh
    testVectors.put("tb1qrp33g0q5c5txsp9arysrx4k6zdkfs4nce4xj0gdcccefvpysxf3q0sl5k7", new String[] { "tb", "BECH32", "1863143c14c5166804bd19203356da136c985678cd4d27a1b8c6329604903262", }); // version 0 program 1863143c14c5166804bd19203356da136c985678cd4d27a1b8c6329604903262 p2wsh
    testVectors.put("bcrt1qrp33g0q5c5txsp9arysrx4k6zdkfs4nce4xj0gdcccefvpysxf3qzf4jry", new String[] { "bcrt", "BECH32", "1863143c14c5166804bd19203356da136c985678cd4d27a1b8c6329604903262", }); // version 0 program 1863143c14c5166804bd19203356da136c985678cd4d27a1b8c6329604903262 p2wsh
    testVectors.put("bc1p5cyxnuxmeuwuvkwfem96lqzszd02n6xdcjrs20cac6yqjjwudpxqkedrcr", new String[] { "bc", "BECH32M", "a60869f0dbcf1dc659c9cecbaf8050135ea9e8cdc487053f1dc6880949dc684c", }); // version 1 program a60869f0dbcf1dc659c9cecbaf8050135ea9e8cdc487053f1dc6880949dc684c p2tr
    testVectors.put("tb1p5cyxnuxmeuwuvkwfem96lqzszd02n6xdcjrs20cac6yqjjwudpxqp3mvzv", new String[] { "tb", "BECH32M", "a60869f0dbcf1dc659c9cecbaf8050135ea9e8cdc487053f1dc6880949dc684c", }); // version 1 program a60869f0dbcf1dc659c9cecbaf8050135ea9e8cdc487053f1dc6880949dc684c p2tr
    testVectors.put("bcrt1p5cyxnuxmeuwuvkwfem96lqzszd02n6xdcjrs20cac6yqjjwudpxqvg32hk", new String[] { "bcrt", "BECH32M", "a60869f0dbcf1dc659c9cecbaf8050135ea9e8cdc487053f1dc6880949dc684c", }); // version 1 program a60869f0dbcf1dc659c9cecbaf8050135ea9e8cdc487053f1dc6880949dc684c p2tr
    testVectors.put("bc1qcr8te4kr609gcawutmrza0j4xv80jy8z306fyu", new String[] { "bc", "BECH32", "c0cebcd6c3d3ca8c75dc5ec62ebe55330ef910e2", }); // version 0 program hash160(0330d54fd0dd420a6e5f8d3624f5f3482cae350f79d5f0753bf5beef9c2d91af3c)
    // https://github.com/satoshilabs/slips/blob/master/slip-0032.md
    testVectors.put("xpub1qpujxsyd4hfu0dtwa524vac84e09mjsgnh5h9crl8wrqg58z5wmsuq7eqte474swq3cvvvcncumfz6xe6l0j6jdl990an7mukyyuemsyjszuwypl", new String[] { "xpub", "BECH32", "007923408dadd3c7b56eed15567707ae5e5dca089de972e07f3b860450e2a3b70e03d902f35f560e0470c63313c7369168d9d7df2d49bf295fd9fb7cb109ccee0494", });
    // https://github.com/lightning/bolts/blob/master/11-payment-encoding.md
    testVectors.put("lnbc1pvjluezsp5zyg3zyg3zyg3zyg3zyg3zyg3zyg3zyg3zyg3zyg3zyg3zyg3zygspp5qqqsyqcyq5rqwzqfqqqsyqcyq5rqwzqfqqqsyqcyq5rqwzqfqypqdpl2pkx2ctnv5sxxmmwwd5kgetjypeh2ursdae8g6twvus8g6rfwvs8qun0dfjkxaq9qrsgq357wnc5r2ueh7ck6q93dj32dlqnls087fxdwk8qakdyafkq3yap9us6v52vjjsrvywa6rt52cm9r9zqt8r2t7mlcwspyetp5h2tztugp9lfyql", new String[] { "lnbc", "BECH32", });
    testVectors.put("lnbc2500u1pvjluezsp5zyg3zyg3zyg3zyg3zyg3zyg3zyg3zyg3zyg3zyg3zyg3zyg3zygspp5qqqsyqcyq5rqwzqfqqqsyqcyq5rqwzqfqqqsyqcyq5rqwzqfqypqdq5xysxxatsyp3k7enxv4jsxqzpu9qrsgquk0rl77nj30yxdy8j9vdx85fkpmdla2087ne0xh8nhedh8w27kyke0lp53ut353s06fv3qfegext0eh0ymjpf39tuven09sam30g4vgpfna3rh", new String[] { "lnbc2500u", "BECH32", });
    for (final var entry : testVectors.entrySet()) {
      try {
        final var result = Bech32.decode(entry.getKey());
        final var expected = (String[]) entry.getValue();
        Assert.assertEquals(entry.getKey(), entry.getKey().toLowerCase(Locale.ROOT), result.toString());
        Assert.assertEquals(entry.getKey(), expected[0], result.getHumanReadablePart());
        Assert.assertEquals(entry.getKey(), Bech32.Variant.valueOf(expected[1]), result.getVariant());
        if (expected.length == 3) {
          // check the data bytes if test data is provided
          // this is funky, but i wanted to test directly from the test vectors
          // without converting from 8 bits per byte to 5 so that it is easier
          // to compare what is expected to the published test vectors
          final byte[] data5 = result.getData();
          if (((1 + 4) <= data5.length) && (data5.length <= (1 + 64))) {
            final byte[] program5 = Arrays.copyOfRange(data5, 1, data5.length);
            try {
              final byte[] program8 = Bech32.Conversion.FIVE_TO_EIGHT.convert(program5);
              Assert.assertArrayEquals(entry.getKey(), Util.fromHexString(expected[2]), program8); // compare the program portion of the data that is published in the test vectors
            } catch (final Exception e) {
              Assert.assertEquals(entry.getKey(), expected[2], e.toString());
            }
          }
        }
      } catch (final Exception e) {
        Assert.assertEquals(entry.getKey(), entry.getValue().toString(), e.toString());
      }
    }
  }

  @Test
  public void test_encode_0() {
    final var e = Assert.assertThrows(NullPointerException.class, () -> {
      Bech32.encode(null, null, null);
    });
    Assert.assertEquals("humanReadablePart must not be null", e.getMessage());
  }

  @Test
  public void test_encode_1() {
    final var e = Assert.assertThrows(NullPointerException.class, () -> {
      Bech32.encode("", null, null);
    });
    Assert.assertEquals("data5 must not be null", e.getMessage());
  }

  @Test
  public void test_encode_2() {
    final var e = Assert.assertThrows(NullPointerException.class, () -> {
      Bech32.encode("", Util.EMPTY_BYTE_ARRAY, null);
    });
    Assert.assertEquals("variant must not be null", e.getMessage());
  }

  @Test
  public void test_encode_3() {
    final var testVectors = new LinkedHashMap<String[], String>();
    // exercise exceptional code paths
    testVectors.put(new String[] { Util.multiply("a", Bech32.MIN_HRP_LENGTH - 1), "", "BECH32" }, "java.lang.IllegalArgumentException: humanReadablePart length invalid");
    testVectors.put(new String[] { Util.multiply("a", Bech32.MAX_HRP_LENGTH + 1), "", "BECH32" }, "java.lang.IllegalArgumentException: humanReadablePart length invalid");
    testVectors.put(new String[] { "a", Util.multiply("00", (Bech32.MAX_BECH32_LENGTH - Bech32.CHECKSUM_LENGTH - Bech32.SEPARATOR_LENGTH - 1) + 1), "BECH32" }, "java.lang.IllegalArgumentException: data5 length invalid");
    testVectors.put(new String[] { new StringBuilder().appendCodePoint(32).toString(), "", "BECH32" }, "java.lang.IllegalArgumentException: humanReadablePart element value invalid");
    testVectors.put(new String[] { new StringBuilder().appendCodePoint(127).toString(), "", "BECH32" }, "java.lang.IllegalArgumentException: humanReadablePart element value invalid");
    testVectors.put(new String[] { "A", "", "BECH32" }, "java.lang.IllegalArgumentException: humanReadablePart element value invalid");
    testVectors.put(new String[] { "Z", "", "BECH32" }, "java.lang.IllegalArgumentException: humanReadablePart element value invalid");
    testVectors.put(new String[] { "a", "20", "BECH32" }, "java.lang.IllegalArgumentException: data5 element value invalid");
    // exercise basic successful code paths
    testVectors.put(new String[] { "a", Util.multiply("00", Bech32.MAX_BECH32_LENGTH - Bech32.CHECKSUM_LENGTH - Bech32.SEPARATOR_LENGTH - 1), "BECH32" }, "a1" + Util.multiply("q", Bech32.MAX_BECH32_LENGTH - Bech32.CHECKSUM_LENGTH - Bech32.SEPARATOR_LENGTH - 1) + "qta7gs");
    testVectors.put(new String[] { "a", Util.multiply("00", Bech32.MAX_BECH32_LENGTH - Bech32.CHECKSUM_LENGTH - Bech32.SEPARATOR_LENGTH - 1), "BECH32M" }, "a1" + Util.multiply("q", Bech32.MAX_BECH32_LENGTH - Bech32.CHECKSUM_LENGTH - Bech32.SEPARATOR_LENGTH - 1) + "4hdjdj");
    testVectors.put(new String[] { Util.multiply("a", Bech32.MAX_HRP_LENGTH), "", "BECH32" }, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa17vhfd0");
    testVectors.put(new String[] { Util.multiply("a", Bech32.MAX_HRP_LENGTH), "", "BECH32M" }, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa1ts89gd");
    testVectors.put(new String[] { Util.multiply("a", Bech32.MAX_HRP_LENGTH - 1), "00", "BECH32" }, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa1qk0vddj");
    testVectors.put(new String[] { Util.multiply("a", Bech32.MAX_HRP_LENGTH - 1), "00", "BECH32M" }, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa1qrnupgs");
    testVectors.put(new String[] { "a", "", "BECH32" }, "a12uel5l");
    testVectors.put(new String[] { "!", "", "BECH32" }, "!1wctc0x");
    testVectors.put(new String[] { "~", "", "BECH32" }, "~1qszm75");
    testVectors.put(new String[] { "an83characterlonghumanreadablepartthatcontainsthenumber1andtheexcludedcharactersbio", "", "BECH32" }, "an83characterlonghumanreadablepartthatcontainsthenumber1andtheexcludedcharactersbio1tt5tgs");
    testVectors.put(new String[] { "bc", "", "BECH32" }, "bc1gmk9yu");
    testVectors.put(new String[] { "tb", "", "BECH32" }, "tb1cy0q7p");
    testVectors.put(new String[] { "bcrt", "", "BECH32" }, "bcrt17capp7");
    testVectors.put(new String[] { "?", "", "BECH32" }, "?1ezyfcl");
    testVectors.put(new String[] { new String(new StringBuilder().appendCodePoint(Character.codePointOf("BITCOIN SIGN")).toString().getBytes(StandardCharsets.US_ASCII), StandardCharsets.US_ASCII), "", "BECH32" }, "?1ezyfcl"); // demonstrate how the replacement string of "?" is used to replace unmappable characters when converting to US-ASCII
    testVectors.put(new String[] { "1", "", "BECH32" }, "11merq64");
    testVectors.put(new String[] { "bc", "", "BECH32M" }, "bc1a8xfp7");
    testVectors.put(new String[] { "tb", "", "BECH32M" }, "tb1dclvmr");
    testVectors.put(new String[] { "bcrt", "", "BECH32M" }, "bcrt1tyddyu");
    testVectors.put(new String[] { "?", "", "BECH32M" }, "?1v759aa");
    testVectors.put(new String[] { "1", "", "BECH32M" }, "11w9nvlh");
    testVectors.put(new String[] { "11111111111111111111111111111111111111111111111111111111111111111111111111111111111", "", "BECH32" }, "1111111111111111111111111111111111111111111111111111111111111111111111111111111111116v0k6w");
    testVectors.put(new String[] { "11111111111111111111111111111111111111111111111111111111111111111111111111111111111", "", "BECH32M" }, "1111111111111111111111111111111111111111111111111111111111111111111111111111111111110sl6lv");
    testVectors.put(new String[] { "bc", "00", "BECH32" }, "bc1q9zpgru");
    testVectors.put(new String[] { "tb", "00", "BECH32" }, "tb1q06v2t0");
    testVectors.put(new String[] { "bcrt", "00", "BECH32" }, "bcrt1q08wsgc");
    testVectors.put(new String[] { "bc", "00", "BECH32M" }, "bc1qs73yx7");
    testVectors.put(new String[] { "tb", "00", "BECH32M" }, "tb1q6xuxwd");
    testVectors.put(new String[] { "bcrt", "00", "BECH32M" }, "bcrt1q6m7ud6");
    testVectors.put(new String[] { "bc", "01", "BECH32" }, "bc1pc54a7w");
    testVectors.put(new String[] { "tb", "01", "BECH32" }, "tb1pjvclka");
    testVectors.put(new String[] { "bcrt", "01", "BECH32" }, "bcrt1pj36942");
    testVectors.put(new String[] { "bc", "01", "BECH32M" }, "bc1pdg93mv");
    testVectors.put(new String[] { "tb", "01", "BECH32M" }, "tb1p8sgnnl");
    testVectors.put(new String[] { "bcrt", "01", "BECH32M" }, "bcrt1p8d2fsg");
    testVectors.put(new String[] { "bc", "0000", "BECH32" }, "bc1qqsa7s0f");
    testVectors.put(new String[] { "bc", "0004", "BECH32" }, "bc1qylhukqn");
    testVectors.put(new String[] { "a", "", "BECH32" }, "a12uel5l");
    testVectors.put(new String[] { "abcdef", "000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f", "BECH32" }, "abcdef1qpzry9x8gf2tvdw0s3jn54khce6mua7lmqqqxw");
    testVectors.put(new String[] { "abcdef", "000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f", "BECH32M" }, "abcdef1qpzry9x8gf2tvdw0s3jn54khce6mua7lwusvrv");
    testVectors.put(new String[] { "1", "00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000", "BECH32" }, "11qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqc8247j");
    testVectors.put(new String[] { "x", "1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f", "BECH32" }, "x1llllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllelz3ww");
    testVectors.put(new String[] { "split", "18171918161c01100b1d0819171d130d10171d16191c01100b03191d1b1903031d130b190303190d181d01190303190d", "BECH32" }, "split1checkupstagehandshakeupstreamerranterredcaperred2y9e3w");
    testVectors.put(new String[] { "?", "", "BECH32" }, "?1ezyfcl");
    testVectors.put(new String[] { "bc", "", "BECH32" }, "bc1gmk9yu");
    testVectors.put(new String[] { "tb", "", "BECH32" }, "tb1cy0q7p");
    testVectors.put(new String[] { "bc", Util.multiply("00", 66 + 1), "BECH32" }, "bc1qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqq3sp7ks");
    for (final var entry : testVectors.entrySet()) {
      final String help = entry.getKey()[0] + "," + entry.getKey()[1] + "," + entry.getKey()[2];
      final byte[] data5 = Util.fromHexString(entry.getKey()[1]);
      final var variant = Bech32.Variant.valueOf(entry.getKey()[2]);
      try {
        final var result = Bech32.encode(entry.getKey()[0], data5, variant);
        Assert.assertEquals(help, entry.getValue(), result.toString());
        Assert.assertEquals(help, entry.getKey()[0], result.getHumanReadablePart());
        Assert.assertArrayEquals(help, data5, result.getData());
      } catch (final Exception e) {
        Assert.assertEquals(help, entry.getValue().toString(), e.toString());
      }
    }
  }

  @Test
  public void test_encode_vectors() {
    final var testVectors = new LinkedHashMap<String[], String>();
    // https://github.com/bitcoin/bips/blob/master/bip-0173.mediawiki#test-vectors
    testVectors.put(new String[] { "a", "", "BECH32" }, "a12uel5l");
    testVectors.put(new String[] { "an83characterlonghumanreadablepartthatcontainsthenumber1andtheexcludedcharactersbio", "", "BECH32" }, "an83characterlonghumanreadablepartthatcontainsthenumber1andtheexcludedcharactersbio1tt5tgs");
    testVectors.put(new String[] { "abcdef", "000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f", "BECH32" }, "abcdef1qpzry9x8gf2tvdw0s3jn54khce6mua7lmqqqxw");
    testVectors.put(new String[] { "1", Util.multiply("00", 82), "BECH32" }, "11qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqc8247j");
    testVectors.put(new String[] { "split", "18171918161c01100b1d0819171d130d10171d16191c01100b03191d1b1903031d130b190303190d181d01190303190d", "BECH32" }, "split1checkupstagehandshakeupstreamerranterredcaperred2y9e3w");
    testVectors.put(new String[] { "?", "", "BECH32" }, "?1ezyfcl");
    testVectors.put(new String[] { "bc", "000e140f070d1a001912060b0d081504140311021d030c1d03040f1814060e1e16", "BECH32" }, "BC1QW508D6QEJXTDG4Y5R3ZARVARY0C5XW7KV8F3T4");
    testVectors.put(new String[] { "tb", "0003011111080f001418140b061001051d030410030615161a020d160910151318191506120f080d18181819090c01041006091100", "BECH32" }, "tb1qrp33g0q5c5txsp9arysrx4k6zdkfs4nce4xj0gdcccefvpysxf3q0sl5k7");
    testVectors.put(new String[] { "bc", "010e140f070d1a001912060b0d081504140311021d030c1d03040f1814060e1e160e140f070d1a001912060b0d081504140311021d030c1d03040f1814060e1e16", "BECH32" }, "bc1pw508d6qejxtdg4y5r3zarvary0c5xw7kw508d6qejxtdg4y5r3zarvary0c5xw7k7grplx");
    testVectors.put(new String[] { "bc", "100e140f00", "BECH32" }, "BC1SW50QA3JX3S");
    testVectors.put(new String[] { "bc", "020e140f070d1a001912060b0d081504140311021d030c1d03040c", "BECH32" }, "bc1zw508d6qejxtdg4y5r3zarvaryvg6kdaj");
    testVectors.put(new String[] { "tb", "000000000001110505190b0a0604080d1214060319001712120c181b0217060e151d040e061918120d02190e1a17041f080c101910", "BECH32" }, "tb1qqqqqp399et2xygdj5xreqhjjvcmzhxw4aywxecjdzew6hylgvsesrxh6hy");
    testVectors.put(new String[] { "tc", "000e140f070d1a001912060b0d081504140311021d030c1d03040f1814060e1e16", "BECH32" }, "tc1qw508d6qejxtdg4y5r3zarvary0c5xw7kg3g4ty");
    testVectors.put(new String[] { "bc", "110e140f070d1a001912060b0d081504140311021d030c1d03040f1814060e1e16", "BECH32" }, "BC13W508D6QEJXTDG4Y5R3ZARVARY0C5XW7KN40WF2");
    testVectors.put(new String[] { "bc", "030e14", "BECH32" }, "bc1rw5uspcuh");
    testVectors.put(new String[] { "bc", "0f0e140f070d1a001912060b0d081504140311021d030c1d03040f1814060e1e160e140f070d1a001912060b0d081504140311021d030c1d03040f1814060e1e160e14", "BECH32" }, "bc10w508d6qejxtdg4y5r3zarvary0c5xw7kw508d6qejxtdg4y5r3zarvary0c5xw7kw5rljs90");
    testVectors.put(new String[] { "bc", "0003140f070d1a001912060b0d081504140311021d030c1d03040c", "BECH32" }, "BC1QR508D6QEJXTDG4Y5R3ZARVARYV98GJ9P");
    testVectors.put(new String[] { "bc", "", "BECH32" }, "bc1gmk9yu");
    // https://github.com/sipa/bech32/issues/51
    testVectors.put(new String[] { "ii2", "111517160a061b", "BECH32" }, "ii2134hk2xmat79tp");
    // https://github.com/bitcoin/bips/blob/master/bip-0350.mediawiki#test-vectors
    testVectors.put(new String[] { "a", "", "BECH32M" }, "a1lqfn3a");
    testVectors.put(new String[] { "an83characterlonghumanreadablepartthatcontainsthetheexcludedcharactersbioandnumber1", "", "BECH32M" }, "an83characterlonghumanreadablepartthatcontainsthetheexcludedcharactersbioandnumber11sg7hg6");
    testVectors.put(new String[] { "abcdef", "1f1e1d1c1b1a191817161514131211100f0e0d0c0b0a09080706050403020100", "BECH32M" }, "abcdef1l7aum6echk45nj3s0wdvt2fg8x9yrzpqzd3ryx");
    testVectors.put(new String[] { "1", "1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f", "BECH32M" }, "11llllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllludsr8");
    testVectors.put(new String[] { "split", "18171918161c01100b1d0819171d130d10171d16191c01100b03191d1b1903031d130b190303190d181d01190303190d", "BECH32M" }, "split1checkupstagehandshakeupstreamerranterredcaperredlc445v");
    testVectors.put(new String[] { "?", "", "BECH32M" }, "?1v759aa");
    testVectors.put(new String[] { "bc", "000e140f070d1a001912060b0d081504140311021d030c1d03040f1814060e1e16", "BECH32" }, "BC1QW508D6QEJXTDG4Y5R3ZARVARY0C5XW7KV8F3T4");
    testVectors.put(new String[] { "tb", "0003011111080f001418140b061001051d030410030615161a020d160910151318191506120f080d18181819090c01041006091100", "BECH32" }, "tb1qrp33g0q5c5txsp9arysrx4k6zdkfs4nce4xj0gdcccefvpysxf3q0sl5k7");
    testVectors.put(new String[] { "bc", "010e140f070d1a001912060b0d081504140311021d030c1d03040f1814060e1e160e140f070d1a001912060b0d081504140311021d030c1d03040f1814060e1e16", "BECH32M" }, "bc1pw508d6qejxtdg4y5r3zarvary0c5xw7kw508d6qejxtdg4y5r3zarvary0c5xw7kt5nd6y");
    testVectors.put(new String[] { "bc", "100e140f00", "BECH32M" }, "BC1SW50QGDZ25J");
    testVectors.put(new String[] { "bc", "020e140f070d1a001912060b0d081504140311021d030c1d03040c", "BECH32M" }, "bc1zw508d6qejxtdg4y5r3zarvaryvaxxpcs");
    testVectors.put(new String[] { "tb", "000000000001110505190b0a0604080d1214060319001712120c181b0217060e151d040e061918120d02190e1a17041f080c101910", "BECH32" }, "tb1qqqqqp399et2xygdj5xreqhjjvcmzhxw4aywxecjdzew6hylgvsesrxh6hy");
    testVectors.put(new String[] { "tb", "010000000001110505190b0a0604080d1214060319001712120c181b0217060e151d040e061918120d02190e1a17041f080c101910", "BECH32M" }, "tb1pqqqqp399et2xygdj5xreqhjjvcmzhxw4aywxecjdzew6hylgvsesf3hn0c");
    testVectors.put(new String[] { "bc", "010f061f060c1f17191b121d1a18150d000c0a0a1c1d01180b001c0109171f061b0517070211160a191e0a001516051718021e0c00", "BECH32M" }, "bc1p0xlxvlhemja6c4dqv22uapctqupfhlxm9h8z3k2e72q4k9hcz7vqzk5jj0");
    testVectors.put(new String[] { "tc", "010f061f060c1f17191b121d1a18150d000c0a0a1c1d01180b001c0109171f061b0517070211160a191e0a001516051718021e0c00", "BECH32M" }, "tc1p0xlxvlhemja6c4dqv22uapctqupfhlxm9h8z3k2e72q4k9hcz7vq5zuyut");
    testVectors.put(new String[] { "bc", "010f061f060c1f17191b121d1a18150d000c0a0a1c1d01180b001c0109171f061b0517070211160a191e0a001516051718021e0c00", "BECH32" }, "bc1p0xlxvlhemja6c4dqv22uapctqupfhlxm9h8z3k2e72q4k9hcz7vqh2y7hd");
    testVectors.put(new String[] { "tb", "020f061f060c1f17191b121d1a18150d000c0a0a1c1d01180b001c0109171f061b0517070211160a191e0a001516051718021e0c00", "BECH32" }, "tb1z0xlxvlhemja6c4dqv22uapctqupfhlxm9h8z3k2e72q4k9hcz7vqglt7rf");
    testVectors.put(new String[] { "bc", "100f061f060c1f17191b121d1a18150d000c0a0a1c1d01180b001c0109171f061b0517070211160a191e0a001516051718021e0c00", "BECH32" }, "BC1S0XLXVLHEMJA6C4DQV22UAPCTQUPFHLXM9H8Z3K2E72Q4K9HCZ7VQ54WELL");
    testVectors.put(new String[] { "bc", "000e140f070d1a001912060b0d081504140311021d030c1d03040f1814060e1e16", "BECH32M" }, "bc1qw508d6qejxtdg4y5r3zarvary0c5xw7kemeawh");
    testVectors.put(new String[] { "tb", "000f061f060c1f17191b121d1a18150d000c0a0a1c1d01180b001c0109171f061b0517070211160a191e0a001516051718021e0c00", "BECH32M" }, "tb1q0xlxvlhemja6c4dqv22uapctqupfhlxm9h8z3k2e72q4k9hcz7vq24jc47");
    testVectors.put(new String[] { "bc", "110f061f060c1f17191b121d1a18150d000c0a0a1c1d01180b001c0109171f061b0517070211160a191e0a001516051718021e0c00", "BECH32M" }, "BC130XLXVLHEMJA6C4DQV22UAPCTQUPFHLXM9H8Z3K2E72Q4K9HCZ7VQ7ZWS8R");
    testVectors.put(new String[] { "bc", "010e14", "BECH32M" }, "bc1pw5dgrnzv");
    testVectors.put(new String[] { "bc", "010f061f060c1f17191b121d1a18150d000c0a0a1c1d01180b001c0109171f061b0517070211160a191e0a001516051718021e0c07130f13060f1b1c1d190e1d0c0a14", "BECH32M" }, "bc1p0xlxvlhemja6c4dqv22uapctqupfhlxm9h8z3k2e72q4k9hcz7v8n0nx0muaewav253zgeav");
    testVectors.put(new String[] { "bc", "0003140f070d1a001912060b0d081504140311021d030c1d03040c", "BECH32" }, "BC1QR508D6QEJXTDG4Y5R3ZARVARYV98GJ9P");
    testVectors.put(new String[] { "bc", "", "BECH32" }, "bc1gmk9yu");
    // https://github.com/satoshilabs/slips/blob/master/slip-0173.md
    // https://github.com/bitcoin/bips/blob/master/bip-0086.mediawiki#test-vectors
    // https://github.com/bitcoin/bips/blob/master/bip-0084.mediawiki#Test_vectors
    testVectors.put(new String[] { "bc", "000e140f070d1a001912060b0d081504140311021d030c1d03040f1814060e1e16", "BECH32" }, "bc1qw508d6qejxtdg4y5r3zarvary0c5xw7kv8f3t4"); // version 0 program 751e76e8199196d454941c45d1b3a323f1433bd6 p2wpkh
    testVectors.put(new String[] { "tb", "000e140f070d1a001912060b0d081504140311021d030c1d03040f1814060e1e16", "BECH32" }, "tb1qw508d6qejxtdg4y5r3zarvary0c5xw7kxpjzsx"); // version 0 program 751e76e8199196d454941c45d1b3a323f1433bd6 p2wpkh
    testVectors.put(new String[] { "bcrt", "000e140f070d1a001912060b0d081504140311021d030c1d03040f1814060e1e16", "BECH32" }, "bcrt1qw508d6qejxtdg4y5r3zarvary0c5xw7kygt080"); // version 0 program 751e76e8199196d454941c45d1b3a323f1433bd6 p2wpkh
    testVectors.put(new String[] { "bc", "0003011111080f001418140b061001051d030410030615161a020d160910151318191506120f080d18181819090c01041006091100", "BECH32" }, "bc1qrp33g0q5c5txsp9arysrx4k6zdkfs4nce4xj0gdcccefvpysxf3qccfmv3"); // version 0 program 1863143c14c5166804bd19203356da136c985678cd4d27a1b8c6329604903262 p2wsh
    testVectors.put(new String[] { "tb", "0003011111080f001418140b061001051d030410030615161a020d160910151318191506120f080d18181819090c01041006091100", "BECH32" }, "tb1qrp33g0q5c5txsp9arysrx4k6zdkfs4nce4xj0gdcccefvpysxf3q0sl5k7"); // version 0 program 1863143c14c5166804bd19203356da136c985678cd4d27a1b8c6329604903262 p2wsh
    testVectors.put(new String[] { "bcrt", "0003011111080f001418140b061001051d030410030615161a020d160910151318191506120f080d18181819090c01041006091100", "BECH32" }, "bcrt1qrp33g0q5c5txsp9arysrx4k6zdkfs4nce4xj0gdcccefvpysxf3qzf4jry"); // version 0 program 1863143c14c5166804bd19203356da136c985678cd4d27a1b8c6329604903262 p2wsh
    testVectors.put(new String[] { "bc", "0114180406131c061b191c0e1c0c160e09191b051a1f000210020d0f0a131a060d181203100a0f181d181a040012120e1c0d010600", "BECH32M" }, "bc1p5cyxnuxmeuwuvkwfem96lqzszd02n6xdcjrs20cac6yqjjwudpxqkedrcr"); // version 1 program a60869f0dbcf1dc659c9cecbaf8050135ea9e8cdc487053f1dc6880949dc684c p2tr
    testVectors.put(new String[] { "tb", "0114180406131c061b191c0e1c0c160e09191b051a1f000210020d0f0a131a060d181203100a0f181d181a040012120e1c0d010600", "BECH32M" }, "tb1p5cyxnuxmeuwuvkwfem96lqzszd02n6xdcjrs20cac6yqjjwudpxqp3mvzv"); // version 1 program a60869f0dbcf1dc659c9cecbaf8050135ea9e8cdc487053f1dc6880949dc684c p2tr
    testVectors.put(new String[] { "bcrt", "0114180406131c061b191c0e1c0c160e09191b051a1f000210020d0f0a131a060d181203100a0f181d181a040012120e1c0d010600", "BECH32M" }, "bcrt1p5cyxnuxmeuwuvkwfem96lqzszd02n6xdcjrs20cac6yqjjwudpxqvg32hk"); // version 1 program a60869f0dbcf1dc659c9cecbaf8050135ea9e8cdc487053f1dc6880949dc684c p2tr
    testVectors.put(new String[] { "bc", "001803070b191516031a0f0508181d0e1c0b1b03021d0f1215060c070f12040702", "BECH32" }, "bc1qcr8te4kr609gcawutmrza0j4xv80jy8z306fyu"); // version 0 program hash160(0330d54fd0dd420a6e5f8d3624f5f3482cae350f79d5f0753bf5beef9c2d91af3c)
    // https://github.com/satoshilabs/slips/blob/master/slip-0032.md
    testVectors.put(new String[] { "xpub", "00011c120610040d1517091c0f0d0b0e1d140a150c1d180715190f051b121008131714170518031f070e030008140702140e1b101c001e19000b19151e15100e0011180c0c0c1813181c1b09021a06191a1f0f121a120d1f05050f1d131e1b1c1604041c191b10041210", "BECH32" }, "xpub1qpujxsyd4hfu0dtwa524vac84e09mjsgnh5h9crl8wrqg58z5wmsuq7eqte474swq3cvvvcncumfz6xe6l0j6jdl990an7mukyyuemsyjszuwypl");
    for (final var entry : testVectors.entrySet()) {
      final String help = entry.getKey()[0] + "," + entry.getKey()[1] + "," + entry.getKey()[2];
      final byte[] data5 = Util.fromHexString(entry.getKey()[1]);
      final var variant = Bech32.Variant.valueOf(entry.getKey()[2]);
      try {
        final var result = Bech32.encode(entry.getKey()[0], data5, variant);
        Assert.assertEquals(help, entry.getValue().toLowerCase(Locale.ROOT), result.toString());
        Assert.assertEquals(help, entry.getKey()[0], result.getHumanReadablePart());
        Assert.assertArrayEquals(help, data5, result.getData());
      } catch (final Exception e) {
        Assert.assertEquals(help, entry.getValue().toString(), e.toString());
      }
    }
  }
}