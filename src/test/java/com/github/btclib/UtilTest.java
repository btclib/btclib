package com.github.btclib;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

public class UtilTest {
  @Test(expected = IllegalArgumentException.class)
  public void test_concat_0() {
    Util.concat((byte[][]) null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_concat_1() {
    Util.concat((byte[]) null);
  }

  @Test
  public void test_concat_2() {
    Assert.assertArrayEquals(Util.EMPTY_BYTE_ARRAY, Util.concat(Util.EMPTY_BYTE_ARRAY));
    Assert.assertArrayEquals(Util.EMPTY_BYTE_ARRAY, Util.concat(Util.EMPTY_BYTE_ARRAY, Util.EMPTY_BYTE_ARRAY));
    Assert.assertArrayEquals(new byte[] { 0x01 }, Util.concat(Util.EMPTY_BYTE_ARRAY, new byte[] { 0x01 }));
    Assert.assertArrayEquals(new byte[] { 0x01 }, Util.concat(new byte[] { 0x01 }, Util.EMPTY_BYTE_ARRAY));
    Assert.assertArrayEquals(new byte[] { 0x01 }, Util.concat(new byte[] { 0x01 }));
    Assert.assertArrayEquals(new byte[] { 0x01, 0x01, 0x01, 0x01 }, Util.concat(new byte[] { 0x01 }, new byte[] { 0x01 }, new byte[] { 0x01 }, new byte[] { 0x01 }));
    Assert.assertArrayEquals(new byte[] { 0x01, 0x02, 0x01, 0x01, 0x01 }, Util.concat(new byte[] { 0x01, 0x02 }, new byte[] { 0x01 }, new byte[] { 0x01 }, new byte[] { 0x01 }));
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_fromHexString_negative() {
    final String[] tests = { null, "a", "A", "AA", "z", "ａａ", "fff" };
    for (final String test : tests) {
      Assert.fail(Arrays.toString(Util.fromHexString(test)));
    }
  }

  @Test
  public void test_fromHexString_positive() {
    final Map<String, byte[]> tests = new LinkedHashMap<>();
    tests.put("", Util.EMPTY_BYTE_ARRAY);
    tests.put("00", new byte[] { 0x00 });
    tests.put("01", new byte[] { 0x01 });
    tests.put("12", new byte[] { 0x12 });
    tests.put("7f", new byte[] { 0x7f });
    tests.put("80", new byte[] { (byte) 0x80 });
    tests.put("aa", new byte[] { (byte) 0xaa });
    tests.put("ff", new byte[] { (byte) 0xff });
    tests.put("1234", new byte[] { 0x12, 0x34 });
    tests.put(new String(new char[64]).replace('\0', '0'), new byte[32]);
    tests.put(new String(new char[40]).replace('\0', '0'), new byte[20]);
    tests.put(new String(new char[4]).replace('\0', 'f'), new byte[] { (byte) 0xff, (byte) 0xff });
    for (final Map.Entry<String, byte[]> test : tests.entrySet()) {
      Assert.assertArrayEquals(test.getValue(), Util.fromHexString(test.getKey()));
    }
  }

  @Test
  public void test_hexToDecimal_negative() {
    final int[] tests = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 'A', 'B', 'C', 'D', 'E', 'F', -1, Integer.MIN_VALUE, Integer.MAX_VALUE, 'Ａ', 'ａ', '0' - 1, '9' + 1, 'a' - 1, 'f' + 1, 'O', 'I' };
    for (final int test : tests) {
      try {
        Assert.fail(Integer.toString(Util.hexToDecimal(test), 10));
      } catch (final IllegalArgumentException e) {
        Assert.assertEquals("invalid hex", e.getMessage());
      }
    }
  }

  @Test
  public void test_hexToDecimal_positive() {
    final int[] tests = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
    final int[] expected = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 };
    for (int i = 0; i < tests.length; i++) {
      Assert.assertEquals(expected[i], Util.hexToDecimal(tests[i]));
    }
  }

  @Test
  public void test_sha256d_0() {
    Assert.assertArrayEquals(Util.fromHexString("5df6e0e2761359d30a8275058e299fcc0381534545f55cf43e41983f5d4c9456"), Util.sha256d());
    Assert.assertArrayEquals(Util.fromHexString("5df6e0e2761359d30a8275058e299fcc0381534545f55cf43e41983f5d4c9456"), Util.sha256d(Util.EMPTY_BYTE_ARRAY));
    Assert.assertArrayEquals(Util.fromHexString("5df6e0e2761359d30a8275058e299fcc0381534545f55cf43e41983f5d4c9456"), Util.sha256d(Util.EMPTY_BYTE_ARRAY, Util.EMPTY_BYTE_ARRAY));
    Assert.assertArrayEquals(Util.fromHexString("1406e05881e299367766d313e26c05564ec91bf721d31726bd6e46e60689539a"), Util.sha256d(new byte[] { 0x00 }));
    Assert.assertArrayEquals(Util.fromHexString("c0b057f584795eff8b06d5e420e71d747587d20de836f501921fd1b5741f1283"), Util.sha256d(new byte[] { (byte) 0xff }));
    Assert.assertArrayEquals(Util.fromHexString("77fbd298d3d82ce5489e2eaac60cd97b2a5156c20517ef3a498965cd2f1b25e1"), Util.sha256d(new byte[] { (byte) 0xff, 0x22 }));
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_sha256d_1() {
    Util.sha256d((byte[][]) null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_sha256d_2() {
    Util.sha256d((byte[]) null);
  }
}