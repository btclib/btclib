package com.github.btclib;

import java.util.Arrays;
import java.util.LinkedHashMap;

import org.junit.Assert;
import org.junit.Test;

public class UtilTest {
  @Test
  public void test_concat_0() {
    final var e = Assert.assertThrows(NullPointerException.class, () -> {
      Util.concat((byte[][]) null);
    });
    Assert.assertEquals("input must not be null", e.getMessage());
  }

  @Test
  public void test_concat_1() {
    final var e = Assert.assertThrows(NullPointerException.class, () -> {
      Util.concat((byte[]) null);
    });
    Assert.assertEquals("element must not be null", e.getMessage());
  }

  @Test
  public void test_concat_2() {
    final var e = Assert.assertThrows(ArithmeticException.class, () -> {
      final var b = new byte[1024 * 1024]; // 2 ** 20
      final var ab = new byte[2 * 1024][]; // 2 ** 11
      Arrays.fill(ab, b); // (2 ** 20) * (2 ** 11) = 2 ** 31  = (Integer.MAX_VALUE + 1) = integer overflow
      Util.concat(ab);
    });
    Assert.assertEquals("combined length of input arrays is too large", e.getMessage());
  }

  @Test
  public void test_concat_3() {
    final var testVectors = new LinkedHashMap<byte[][], byte[]>();
    testVectors.put(new byte[][] { Util.EMPTY_BYTE_ARRAY, }, Util.EMPTY_BYTE_ARRAY);
    testVectors.put(new byte[][] { Util.EMPTY_BYTE_ARRAY, Util.EMPTY_BYTE_ARRAY, }, Util.EMPTY_BYTE_ARRAY);
    testVectors.put(new byte[][] { Util.EMPTY_BYTE_ARRAY, Util.fromHexString("00"), }, Util.fromHexString("00"));
    testVectors.put(new byte[][] { Util.EMPTY_BYTE_ARRAY, Util.fromHexString("01"), }, Util.fromHexString("01"));
    testVectors.put(new byte[][] { Util.EMPTY_BYTE_ARRAY, Util.fromHexString("ff"), }, Util.fromHexString("ff"));
    testVectors.put(new byte[][] { Util.EMPTY_BYTE_ARRAY, Util.fromHexString("80"), }, Util.fromHexString("80"));
    testVectors.put(new byte[][] { Util.fromHexString("01"), }, Util.fromHexString("01"));
    testVectors.put(new byte[][] { Util.fromHexString("01"), Util.fromHexString("02"), Util.fromHexString("03"), Util.fromHexString("04"), }, Util.fromHexString("01020304"));
    testVectors.put(new byte[][] { Util.fromHexString("0102"), Util.fromHexString("03"), Util.fromHexString("0405"), }, Util.fromHexString("0102030405"));
    for (final var entry : testVectors.entrySet()) {
      final var result = Util.concat(entry.getKey());
      Assert.assertArrayEquals(entry.getValue(), result);
    }
    // run a few using the variable argument syntax just to be sure
    Assert.assertArrayEquals(Util.EMPTY_BYTE_ARRAY, Util.concat());
    Assert.assertArrayEquals(Util.fromHexString("ff"), Util.concat(Util.fromHexString("ff")));
    Assert.assertArrayEquals(Util.fromHexString("0102"), Util.concat(Util.fromHexString("01"), Util.fromHexString("02")));
  }

  @Test
  public void test_concat_prefix() {
    final var e = Assert.assertThrows(NullPointerException.class, () -> {
      Util.concat(0, null);
    });
    Assert.assertEquals("input must not be null", e.getMessage());
    Assert.assertArrayEquals(Util.fromHexString("00"), Util.concat(0, Util.EMPTY_BYTE_ARRAY));
    Assert.assertArrayEquals(Util.fromHexString("ff"), Util.concat(-1, Util.EMPTY_BYTE_ARRAY));
    Assert.assertArrayEquals(Util.fromHexString("10010203"), Util.concat(16, Util.fromHexString("010203")));
    Assert.assertArrayEquals(Util.fromHexString("001f1c"), Util.concat(0, new byte[] { 0b11111, 0b11100, }));
  }

  @Test
  public void test_fromHexString() {
    final var testVectors = new LinkedHashMap<String, Object>();
    testVectors.put(null, new NullPointerException("input must not be null"));
    testVectors.put("0", new IllegalArgumentException("input length must be even"));
    testVectors.put("123", new IllegalArgumentException("input length must be even"));
    testVectors.put("", Util.EMPTY_BYTE_ARRAY);
    testVectors.put("00", new byte[] { 0, });
    testVectors.put("01", new byte[] { 1, });
    testVectors.put("7f", new byte[] { 0x7f, });
    testVectors.put("80", new byte[] { (byte) 0x80, });
    testVectors.put("ff", new byte[] { (byte) 0xff, });
    testVectors.put("FF", new byte[] { (byte) 0xff, });
    testVectors.put("beef", new byte[] { (byte) 0xbe, (byte) 0xef, });
    testVectors.put("0123456789aAbBcCdDeEfF", new byte[] { (byte) 0x01, (byte) 0x23, (byte) 0x45, (byte) 0x67, (byte) 0x89, (byte) 0xaa, (byte) 0xbb, (byte) 0xcc, (byte) 0xdd, (byte) 0xee, (byte) 0xff, });
    testVectors.put("0000000000000000000000000000000000000000", new byte[20]);
    testVectors.put("0000000000000000000000000000000000000000000000000000000000000000", new byte[32]);
    testVectors.put("zz", new IllegalArgumentException("input invalid"));
    testVectors.put("ａａ", new IllegalArgumentException("input invalid")); // U+FF41 FULLWIDTH LATIN SMALL LETTER A
    testVectors.put(new StringBuilder(2).appendCodePoint(Character.codePointOf("BITCOIN SIGN")).appendCodePoint(Character.codePointOf("BITCOIN SIGN")).toString(), new IllegalArgumentException("input invalid")); // invalid BMP code points
    testVectors.put(new StringBuilder(2).appendCodePoint(Character.codePointOf("PILE OF POO")).toString(), new IllegalArgumentException("input invalid")); // invalid supplementary character code point which encodes to 2 chars in UTF-16
    testVectors.put(new StringBuilder(4).append("12").appendCodePoint(Character.codePointOf("PILE OF POO")).toString(), new IllegalArgumentException("input invalid")); // supplementary character PILE OF POO UTF-16 surrogate pair (chars 0xd83d + 0xdca9)
    for (final var entry : testVectors.entrySet()) {
      try {
        final var result = Util.fromHexString(entry.getKey());
        Assert.assertArrayEquals(entry.getKey(), (byte[]) entry.getValue(), result);
      } catch (final Exception e) {
        Assert.assertEquals(entry.getKey(), entry.getValue().toString(), e.toString());
      }
    }
  }

  @Test
  public void test_hexToDecimal_negative() {
    final int[] tests = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, -1, Integer.MIN_VALUE, Integer.MAX_VALUE, 'Ａ', 'ａ', '0' - 1, '9' + 1, 'a' - 1, 'f' + 1, 'A' - 1, 'F' + 1, 'O', 'I', 0x20bf, 0xffff, };
    for (final var test : tests) {
      final var e = Assert.assertThrows(IllegalArgumentException.class, () -> {
        Util.hexToDecimal(test);
      });
      Assert.assertEquals("input invalid", e.getMessage());
    }
  }

  @Test
  public void test_hexToDecimal_positive() {
    final int[] tests = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'A', 'B', 'C', 'D', 'E', 'F', };
    final int[] expected = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 10, 11, 12, 13, 14, 15, };
    for (int i = 0; i < tests.length; i++) {
      Assert.assertEquals(expected[i], Util.hexToDecimal(tests[i]));
    }
  }

  @Test
  public void test_multiply() {
    final var testVectors = new LinkedHashMap<String[], String>();
    testVectors.put(new String[] { null, "-1", }, "java.lang.NullPointerException: input must not be null");
    testVectors.put(new String[] { "", "-1", }, "java.lang.IllegalArgumentException: multiplier invalid");
    testVectors.put(new String[] { "12", "2147483647", }, "java.lang.ArithmeticException: integer overflow");
    testVectors.put(new String[] { "", "0", }, "");
    testVectors.put(new String[] { "", "1", }, "");
    // warning: passes, but slow: testVectors.put(new String[] { "", "2147483647", }, "");
    testVectors.put(new String[] { "a", "0", }, "");
    testVectors.put(new String[] { "a", "1", }, "a");
    testVectors.put(new String[] { "a", "2", }, "aa");
    testVectors.put(new String[] { "00", "1", }, "00");
    testVectors.put(new String[] { "00", "2", }, "0000");
    testVectors.put(new String[] { "00", "20", }, "0000000000000000000000000000000000000000");
    testVectors.put(new String[] { "00", "32", }, "0000000000000000000000000000000000000000000000000000000000000000");
    testVectors.put(new String[] { "00", "40", }, "00000000000000000000000000000000000000000000000000000000000000000000000000000000");
    testVectors.put(new String[] { new StringBuilder().appendCodePoint(Character.codePointOf("BITCOIN SIGN")).toString(), "3", }, new StringBuilder().appendCodePoint(Character.codePointOf("BITCOIN SIGN")).appendCodePoint(Character.codePointOf("BITCOIN SIGN")).appendCodePoint(Character.codePointOf("BITCOIN SIGN")).toString());
    for (final var entry : testVectors.entrySet()) {
      final String help = entry.getKey()[0] + "," + entry.getKey()[1];
      final String input = entry.getKey()[0];
      final int multiplier = Integer.parseInt(entry.getKey()[1], 10);
      try {
        final var result = Util.multiply(input, multiplier);
        Assert.assertEquals(help, entry.getValue(), result);
      } catch (final Exception e) {
        Assert.assertEquals(help, entry.getValue(), e.toString());
      }
    }
  }

  @Test
  public void test_sha256d_0() {
    final var e = Assert.assertThrows(NullPointerException.class, () -> {
      Util.sha256d((byte[][]) null);
    });
    Assert.assertEquals("input must not be null", e.getMessage());
  }

  @Test
  public void test_sha256d_1() {
    final var e = Assert.assertThrows(NullPointerException.class, () -> {
      Util.sha256d((byte[]) null);
    });
    Assert.assertEquals("element must not be null", e.getMessage());
  }

  @Test
  public void test_sha256d_2() {
    final var testVectors = new LinkedHashMap<byte[][], byte[]>();
    testVectors.put(new byte[][] { Util.EMPTY_BYTE_ARRAY, }, Util.fromHexString("5df6e0e2761359d30a8275058e299fcc0381534545f55cf43e41983f5d4c9456"));
    testVectors.put(new byte[][] { Util.EMPTY_BYTE_ARRAY, Util.EMPTY_BYTE_ARRAY, }, Util.fromHexString("5df6e0e2761359d30a8275058e299fcc0381534545f55cf43e41983f5d4c9456"));
    testVectors.put(new byte[][] { Util.fromHexString("00"), }, Util.fromHexString("1406e05881e299367766d313e26c05564ec91bf721d31726bd6e46e60689539a"));
    testVectors.put(new byte[][] { Util.fromHexString("ff"), }, Util.fromHexString("c0b057f584795eff8b06d5e420e71d747587d20de836f501921fd1b5741f1283"));
    testVectors.put(new byte[][] { Util.fromHexString("ff22"), }, Util.fromHexString("77fbd298d3d82ce5489e2eaac60cd97b2a5156c20517ef3a498965cd2f1b25e1"));
    testVectors.put(new byte[][] { Util.fromHexString("0100000000000000000000000000000000000000000000000000000000000000000000003ba3edfd7a7b12b27ac72c3e67768f617fc81bc3888a51323a9fb8aa4b1e5e4a29ab5f49ffff001d1dac2b7c"), }, Util.fromHexString("6fe28c0ab6f1b372c1a6a246ae63f74f931e8365e15a089c68d6190000000000")); // the Bitcoin genesis block header
    for (final var entry : testVectors.entrySet()) {
      Assert.assertArrayEquals(entry.getValue(), Util.sha256d(entry.getKey()));
    }
    // run one using the variable argument syntax just to be sure
    Assert.assertArrayEquals(Util.fromHexString("5df6e0e2761359d30a8275058e299fcc0381534545f55cf43e41983f5d4c9456"), Util.sha256d());
  }
}