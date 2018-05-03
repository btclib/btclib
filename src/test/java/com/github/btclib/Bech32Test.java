package com.github.btclib;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

public class Bech32Test {
  @Test
  public void test_convert_00() {
    final Map<String, String> testVectors = new OneShotLinkedHashMap<>();
    testVectors.put("", "");
    testVectors.put("00", "0000");
    testVectors.put("01", "0004");
    testVectors.put("02", "0008");
    testVectors.put("04", "0010");
    testVectors.put("08", "0100");
    testVectors.put("10", "0200");
    testVectors.put("20", "0400");
    testVectors.put("40", "0800");
    testVectors.put("80", "1000");
    testVectors.put("81", "1004");
    testVectors.put("82", "1008");
    testVectors.put("84", "1010");
    testVectors.put("88", "1100");
    testVectors.put("90", "1200");
    testVectors.put("a0", "1400");
    testVectors.put("c0", "1800");
    testVectors.put("c1", "1804");
    testVectors.put("c2", "1808");
    testVectors.put("c4", "1810");
    testVectors.put("c8", "1900");
    testVectors.put("d0", "1a00");
    testVectors.put("e0", "1c00");
    testVectors.put("e1", "1c04");
    testVectors.put("e2", "1c08");
    testVectors.put("e4", "1c10");
    testVectors.put("e8", "1d00");
    testVectors.put("f0", "1e00");
    testVectors.put("f1", "1e04");
    testVectors.put("f2", "1e08");
    testVectors.put("f4", "1e10");
    testVectors.put("f8", "1f00");
    testVectors.put("f9", "1f04");
    testVectors.put("fa", "1f08");
    testVectors.put("fc", "1f10");
    testVectors.put("fd", "1f14");
    testVectors.put("fe", "1f18");
    testVectors.put("ff", "1f1c"); // padding = 2 bits = 10 % 8
    testVectors.put("ff80", "1f1e0000");
    testVectors.put("ffc0", "1f1f0000");
    testVectors.put("ffe0", "1f1f1000");
    testVectors.put("fff0", "1f1f1800");
    testVectors.put("0100", "00040000");
    testVectors.put("01ff", "00071f10");
    testVectors.put("00ff", "00031f10");
    testVectors.put("ffff", "1f1f1f10"); // padding = 4 bits = 20 % 8
    testVectors.put("ffffff", "1f1f1f1f1e"); // padding = 1 bit = 25 % 8
    testVectors.put("ffffffff", "1f1f1f1f1f1f18"); // padding = 3 bits = 35 % 8
    testVectors.put("ffffffffff", "1f1f1f1f1f1f1f1f"); // padding = 0 bits = 40 % 8
    testVectors.put(new String(new char[4096]).replace("\0", "00"), new String(new char[6554]).replace("\0", "00")); // max length all zeros
    testVectors.put(new String(new char[4096]).replace("\0", "ff"), new String(new char[6553]).replace("\0", "1f") + "1c"); // max length all ones
    //
    for (final Map.Entry<String, String> entry : testVectors.entrySet()) {
      final byte[] base256 = Util.fromHexString(entry.getKey());
      final byte[] base32 = Util.fromHexString(entry.getValue());
      //
      final byte[] result_base32 = Bech32.convert(base256, 8, 0, base256.length);
      Assert.assertArrayEquals(entry.getKey(), base32, result_base32);
      //
      final byte[] result_base256 = Bech32.convert(result_base32, 5, 0, result_base32.length);
      Assert.assertArrayEquals(entry.getKey(), base256, result_base256);
    }
  }

  @Test
  public void test_convert_01() {
    final Map<String, String> testVectors = new OneShotLinkedHashMap<>();
    testVectors.put("", "");
    testVectors.put("00", null); // only 5 bits go in and padding is not desired, so a failure results
    testVectors.put("1f", null); // only 5 bits go in and padding is not desired, so a failure results
    testVectors.put("20", "invalid element value for input base");
    testVectors.put("ff", "invalid element value for input base");
    testVectors.put("1f20", "invalid element value for input base");
    testVectors.put("0000", "00");
    testVectors.put("1f1f", null); // non-zero padding bits
    testVectors.put("1f1e", null); // non-zero padding bits
    testVectors.put("1f1c", "ff");
    testVectors.put("1f18", "fe");
    testVectors.put("1f10", "fc");
    testVectors.put("1f00", "f8");
    testVectors.put("1e00", "f0");
    testVectors.put("1c00", "e0");
    testVectors.put("1f1f1f", null); // 7 non-zero padding bits
    testVectors.put("1f1f1f1f", null); // 4 non-zero padding bits
    testVectors.put("1f1f1f10", "ffff"); // 4 zero padding bits
    //
    for (final Map.Entry<String, String> entry : testVectors.entrySet()) {
      try {
        final byte[] base32 = Util.fromHexString(entry.getKey());
        final byte[] result_base256 = Bech32.convert(base32, 5, 0, base32.length);
        final byte[] base256 = (entry.getValue() == null) ? null : Util.fromHexString(entry.getValue());
        Assert.assertArrayEquals(entry.getKey(), base256, result_base256);
      } catch (final IllegalArgumentException e) {
        Assert.assertEquals(entry.getKey(), entry.getValue(), e.getMessage());
      }
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_convert_02() {
    Bech32.convert(null, 8, 0, 0);
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_convert_03() {
    Bech32.convert(Util.EMPTY_BYTE_ARRAY, 32, 0, 0);
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_convert_04() {
    Bech32.convert(Util.EMPTY_BYTE_ARRAY, 8, -1, 0);
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_convert_05() {
    Bech32.convert(Util.EMPTY_BYTE_ARRAY, 8, 0, -1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_convert_06() {
    Bech32.convert(Util.EMPTY_BYTE_ARRAY, 8, 0, 4097);
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_convert_07() {
    Bech32.convert(Util.EMPTY_BYTE_ARRAY, 5, 0, 6555);
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_convert_08() {
    Bech32.convert(Util.EMPTY_BYTE_ARRAY, 8, 0, 1);
  }

  @Test
  public void test_convert_09() {
    Assert.assertArrayEquals(Util.fromHexString("1f1c"), Bech32.convert(new byte[] { (byte) 0xff, 1, 2, }, 8, 0, 1));
    Assert.assertArrayEquals(Util.fromHexString("1f1c"), Bech32.convert(new byte[] { 0, (byte) 0xff, 2, }, 8, 1, 1));
    Assert.assertArrayEquals(Util.fromHexString("1f00"), Bech32.convert(new byte[] { 0, 2, (byte) 0xf8, }, 8, 2, 1));
    Assert.assertArrayEquals(Util.fromHexString(""), Bech32.convert(new byte[] { 0, 2, (byte) 0xf8, }, 8, 2, 0));
    Assert.assertArrayEquals(Util.fromHexString(""), Bech32.convert(new byte[] { 0, }, 5, 1, 0));
  }

  @Test
  public void test_decode() {
    final Map<String, String[]> testVectors = new OneShotLinkedHashMap<>();
    testVectors.put(null, new String[] { "bech32 must not be null" });
    testVectors.put("", new String[] { "invalid length" });
    testVectors.put(" ", new String[] { "invalid length" });
    testVectors.put("0", new String[] { "invalid length" });
    testVectors.put("1", new String[] { "invalid length" });
    testVectors.put("1234567", new String[] { "invalid length" });
    testVectors.put("12345678", new String[] { "invalid separator location" });
    testVectors.put("0123456", new String[] { "invalid length" });
    testVectors.put("01234567", new String[] { "invalid checksum" });
    testVectors.put("11234567", new String[] { "invalid checksum" });
    testVectors.put("10a06t8", new String[] { "invalid length" });
    testVectors.put("1qzzfhee", new String[] { "invalid separator location" });
    testVectors.put("A12UEL5L", new String[] { "a", "" });
    testVectors.put("a12uel5l", new String[] { "a", "" });
    testVectors.put("A12UeL5L", new String[] { "mixed case" });
    testVectors.put("a12uEl5l", new String[] { "mixed case" });
    testVectors.put("an83characterlonghumanreadablepartthatcontainsthenumber1andtheexcludedcharactersbio1tt5tgs", new String[] { "an83characterlonghumanreadablepartthatcontainsthenumber1andtheexcludedcharactersbio", "" });
    testVectors.put("abcdef1qpzry9x8gf2tvdw0s3jn54khce6mua7lmqqqxw", new String[] { "abcdef", "000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f" });
    testVectors.put("11qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqc8247j", new String[] { "1", "00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000" });
    testVectors.put("x1llllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllelz3ww", new String[] { "x", "1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f" });
    testVectors.put("x1lllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllrdjtnk", new String[] { "invalid length" });
    testVectors.put("split1checkupstagehandshakeupstreamerranterredcaperred2y9e3w", new String[] { "split", "18171918161c01100b1d0819171d130d10171d16191c01100b03191d1b1903031d130b190303190d181d01190303190d" });
    testVectors.put("?1ezyfcl", new String[] { "?", "" });
    testVectors.put(new StringBuilder().appendCodePoint(0x20).append("1nwldj5").toString(), new String[] { "element value out of range" });
    testVectors.put(new StringBuilder().appendCodePoint(0x7F).append("1axkwrx").toString(), new String[] { "element value out of range" });
    testVectors.put(new StringBuilder().appendCodePoint(0x80).append("1eym55h").toString(), new String[] { "element value out of range" });
    testVectors.put(new StringBuilder().appendCodePoint(0x80).append("17qyerv").toString(), new String[] { "element value out of range" });
    testVectors.put(new StringBuilder().appendCodePoint(0x800).append("13psnee").toString(), new String[] { "element value out of range" });
    testVectors.put(new StringBuilder().appendCodePoint(0x10000).append("1tagmg0").toString(), new String[] { "element value out of range" });
    testVectors.put("an84characterslonghumanreadablepartthatcontainsthenumber1andtheexcludedcharactersbio1569pvx", new String[] { "invalid length" });
    testVectors.put("pzry9x0s0muk", new String[] { "invalid separator location" });
    testVectors.put("1pzry9x0s0muk", new String[] { "invalid separator location" });
    testVectors.put("x1b4n0q5v", new String[] { "data element not in Bech32 character set" });
    testVectors.put("x14bn0q5v", new String[] { "checksum element not in Bech32 character set" });
    testVectors.put("li1dgmt3", new String[] { "invalid separator location" });
    testVectors.put("A1G7SGD8", new String[] { "invalid checksum" });
    testVectors.put(new StringBuilder().append("de1lg7wt").appendCodePoint(0xFF).toString(), new String[] { "element value out of range" });
    testVectors.put("bc1q9zpgru", new String[] { "bc", "00" });
    testVectors.put("bc1qqsa7s0f", new String[] { "bc", "0000" });
    //
    for (final Map.Entry<String, String[]> entry : testVectors.entrySet()) {
      try {
        final Bech32 bech32 = Bech32.decode(entry.getKey());
        Assert.assertEquals(entry.getKey(), entry.getKey(), bech32.toString());
        Assert.assertEquals(entry.getKey(), entry.getValue()[0].toLowerCase(Locale.ROOT), bech32.getHumanReadablePart());
        Assert.assertArrayEquals(entry.getKey(), Util.fromHexString(entry.getValue()[1]), bech32.getData());
      } catch (final DecodingException | IllegalArgumentException e) {
        Assert.assertEquals(entry.getKey(), entry.getValue()[0], e.getMessage());
      }
    }
  }

  @Test
  public void test_decode_segwit() {
    final Map<String, String[]> testVectors = new OneShotLinkedHashMap<>();
    testVectors.put("BC1QW508D6QEJXTDG4Y5R3ZARVARY0C5XW7KV8F3T4", new String[] { "bc", "00751e76e8199196d454941c45d1b3a323f1433bd6" });
    testVectors.put("bc1qw508d6qejxtdg4y5r3zarvary0c5xw7kv8f3t4", new String[] { "bc", "00751e76e8199196d454941c45d1b3a323f1433bd6" });
    testVectors.put("tb1qw508d6qejxtdg4y5r3zarvary0c5xw7kxpjzsx", new String[] { "tb", "00751e76e8199196d454941c45d1b3a323f1433bd6" });
    testVectors.put("bcrt1qw508d6qejxtdg4y5r3zarvary0c5xw7kygt080", new String[] { "bcrt", "00751e76e8199196d454941c45d1b3a323f1433bd6" });
    testVectors.put("bc1qrp33g0q5c5txsp9arysrx4k6zdkfs4nce4xj0gdcccefvpysxf3qccfmv3", new String[] { "bc", "001863143c14c5166804bd19203356da136c985678cd4d27a1b8c6329604903262" }); // for reference, private key = 1 and sha256(210279BE667EF9DCBBAC55A06295CE870B07029BFCDB2DCE28D959F2815B16F81798AC)
    testVectors.put("tb1qrp33g0q5c5txsp9arysrx4k6zdkfs4nce4xj0gdcccefvpysxf3q0sl5k7", new String[] { "tb", "001863143c14c5166804bd19203356da136c985678cd4d27a1b8c6329604903262" });
    testVectors.put("bcrt1qrp33g0q5c5txsp9arysrx4k6zdkfs4nce4xj0gdcccefvpysxf3qzf4jry", new String[] { "bcrt", "001863143c14c5166804bd19203356da136c985678cd4d27a1b8c6329604903262" });
    testVectors.put("ltc1qw508d6qejxtdg4y5r3zarvary0c5xw7kgmn4n9", new String[] { "ltc", "00751e76e8199196d454941c45d1b3a323f1433bd6" });
    testVectors.put("tltc1qw508d6qejxtdg4y5r3zarvary0c5xw7klfsuq0", new String[] { "tltc", "00751e76e8199196d454941c45d1b3a323f1433bd6" });
    testVectors.put("bC1QW508D6QEJXTDG4Y5R3ZARVARY0C5XW7KV8F3T4", new String[] { "mixed case" });
    testVectors.put("bc1pw508d6qejxtdg4y5r3zarvary0c5xw7kw508d6qejxtdg4y5r3zarvary0c5xw7k7grplx", new String[] { "bc", "01751e76e8199196d454941c45d1b3a323f1433bd6751e76e8199196d454941c45d1b3a323f1433bd6" });
    testVectors.put("BC1SW50QA3JX3S", new String[] { "bc", "10751e" });
    testVectors.put("bc1zw508d6qejxtdg4y5r3zarvaryvg6kdaj", new String[] { "bc", "02751e76e8199196d454941c45d1b3a323" });
    testVectors.put("tb1qqqqqp399et2xygdj5xreqhjjvcmzhxw4aywxecjdzew6hylgvsesrxh6hy", new String[] { "tb", "00000000c4a5cad46221b2a187905e5266362b99d5e91c6ce24d165dab93e86433" });
    testVectors.put("tc1qw508d6qejxtdg4y5r3zarvary0c5xw7kg3g4ty", new String[] { "tc", "00751e76e8199196d454941c45d1b3a323f1433bd6" });
    testVectors.put("bc1qw508d6qejxtdg4y5r3zarvary0c5xw7kv8f3t5", new String[] { "invalid checksum" });
    testVectors.put("BC13W508D6QEJXTDG4Y5R3ZARVARY0C5XW7KN40WF2", new String[] { "bc", "11751e76e8199196d454941c45d1b3a323f1433bd6" });
    testVectors.put("bc1rw5uspcuh", new String[] { "bc", "0375" });
    testVectors.put("bc1qqqglchaj", new String[] { "bc", "0000" });
    testVectors.put("bc10w508d6qejxtdg4y5r3zarvary0c5xw7kw508d6qejxtdg4y5r3zarvary0c5xw7kw5rljs90", new String[] { "bc", "0f751e76e8199196d454941c45d1b3a323f1433bd6751e76e8199196d454941c45d1b3a323f1433bd675" });
    testVectors.put("BC1QR508D6QEJXTDG4Y5R3ZARVARYV98GJ9P", new String[] { "bc", "001d1e76e8199196d454941c45d1b3a323" });
    testVectors.put("tb1qrp33g0q5c5txsp9arysrx4k6zdkfs4nce4xj0gdcccefvpysxf3q0sL5k7", new String[] { "mixed case" });
    testVectors.put("Tb1qrp33g0q5c5txsp9arysrx4k6zdkfs4nce4xj0gdcccefvpysxf3q0sL5k7", new String[] { "mixed case" });
    testVectors.put("bc1zw508d6qejxtdg4y5r3zarvaryvqyzf3du", new String[] { "bc", "02" }); // shorter than might be expected because 5 to 8 conversion fails due to "zero padding of more than 4 bits"
    testVectors.put("tb1qrp33g0q5c5txsp9arysrx4k6zdkfs4nce4xj0gdcccefvpysxf3pjxtptv", new String[] { "tb", "00" }); // shorter than might be expected because 5 to 8 conversion fails due to "non-zero padding"
    testVectors.put("bc1gmk9yu", new String[] { "bc", "" });
    testVectors.put("bcrt1qqqqqp399et2xygdj5xreqhjjvcmzhxw4aywxecjdzew6hylgvseswlauz7", new String[] { "bcrt", "00000000c4a5cad46221b2a187905e5266362b99d5e91c6ce24d165dab93e86433" });
    testVectors.put("bc1qu9dgdg330r6r84g5mw7wqshg04exv2uttmw2elfwx74h5tgntuzs44gyfg", new String[] { "bc", "00e15a86a23178f433d514dbbce042e87d72662b8b5edcacfd2e37ab7a2d135f05" });
    testVectors.put("bc1q5fgkuac9s2ry56jka5s6zqsyfcugcchry5cwu0", new String[] { "bc", "00a2516e770582864a6a56ed21a102044e388c62e3" });
    testVectors.put("bc1qp0lfxhnscvsu0j36l36uurgv5tuck4pzuqytkvwqp3kh78cupttqyf705v", new String[] { "bc", "000bfe935e70c321c7ca3afc75ce0d0ca2f98b5422e008bb31c00c6d7f1f1c0ad6" });
    //
    for (final Map.Entry<String, String[]> entry : testVectors.entrySet()) {
      try {
        final Bech32 bech32 = Bech32.decode(entry.getKey());
        Assert.assertEquals(entry.getKey(), entry.getKey(), bech32.toString());
        Assert.assertEquals(entry.getKey(), entry.getValue()[0].toLowerCase(Locale.ROOT), bech32.getHumanReadablePart());
        byte[] data = bech32.getData();
        // go through these gymnastics to get the test vectors closely resembling how they were presented in BIP-0173
        if (data.length > 0) {
          final byte[] converted = Bech32.convert(data, 5, 1, data.length - 1);
          if (converted == null) {
            data = new byte[] { data[0] }; // this value only since the witness version does not undergo base conversion
          } else {
            data = Util.concat(new byte[] { data[0] }, converted);
          }
        }
        Assert.assertArrayEquals(entry.getKey(), Util.fromHexString(entry.getValue()[1]), data);
      } catch (final DecodingException | IllegalArgumentException e) {
        Assert.assertEquals(entry.getKey(), entry.getValue()[0], e.getMessage());
      }
    }
  }

  @Test
  public void test_encode() {
    final Map<String[], String> testVectors = new OneShotLinkedHashMap<>();
    testVectors.put(new String[] { null, null }, "humanReadablePart must not be null");
    testVectors.put(new String[] { "", null }, "data must not be null");
    testVectors.put(new String[] { "", "" }, "humanReadablePart invalid length");
    testVectors.put(new String[] { "a", "0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000" }, "data invalid length");
    testVectors.put(new String[] { "a", "00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000" }, "a1qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqq87k0gd");
    testVectors.put(new String[] { new String(new char[84]).replace('\0', 'a'), "" }, "humanReadablePart invalid length");
    testVectors.put(new String[] { new String(new char[83]).replace('\0', 'a'), "00" }, "data invalid length");
    //
    testVectors.put(new String[] { new String(new char[83]).replace('\0', 'a'), "" }, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa17vhfd0");
    testVectors.put(new String[] { new String(new char[82]).replace('\0', 'a'), "00" }, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa1qk0vddj");
    testVectors.put(new String[] { "a", "" }, "a12uel5l");
    testVectors.put(new String[] { "A", "" }, "a12uel5l");
    testVectors.put(new String[] { "aZ", "" }, "az10klr02");
    testVectors.put(new String[] { "Z", "" }, "z106qa3w");
    testVectors.put(new String[] { " ", "" }, "element value out of range");
    testVectors.put(new String[] { "!", "" }, "!1wctc0x");
    testVectors.put(new String[] { "~", "" }, "~1qszm75");
    testVectors.put(new String[] { new String(new char[] { 127 }), "" }, "element value out of range");
    testVectors.put(new String[] { "Z", "ff" }, "element value not in base32");
    testVectors.put(new String[] { "Z", "80" }, "element value not in base32");
    testVectors.put(new String[] { "Z", "20" }, "element value not in base32");
    //
    testVectors.put(new String[] { "an83characterlonghumanreadablepartthatcontainsthenumber1andtheexcludedcharactersbio", "" }, "an83characterlonghumanreadablepartthatcontainsthenumber1andtheexcludedcharactersbio1tt5tgs");
    testVectors.put(new String[] { "an83characterlonghumanreadablepartthatcontainsthenumber1andtheexcludedcharactersbio", "00" }, "data invalid length");
    testVectors.put(new String[] { "an83characterlonghumanreadablepartthatcontainsthenumber1andtheexcludedcharactersbio", new String(new char[82 * 2]).replace('\0', '0') }, "data invalid length");
    testVectors.put(new String[] { "abcdef", "000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f" }, "abcdef1qpzry9x8gf2tvdw0s3jn54khce6mua7lmqqqxw");
    testVectors.put(new String[] { "1", "00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000" }, "11qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqc8247j");
    testVectors.put(new String[] { "x", "1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f" }, "x1llllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllelz3ww");
    testVectors.put(new String[] { "split", "18171918161c01100b1d0819171d130d10171d16191c01100b03191d1b1903031d130b190303190d181d01190303190d" }, "split1checkupstagehandshakeupstreamerranterredcaperred2y9e3w");
    testVectors.put(new String[] { "?", "" }, "?1ezyfcl");
    testVectors.put(new String[] { "bc", "" }, "bc1gmk9yu");
    testVectors.put(new String[] { "tb", "" }, "tb1cy0q7p");
    testVectors.put(new String[] { new StringBuilder().appendCodePoint(0x20).toString(), "" }, "element value out of range");
    testVectors.put(new String[] { new StringBuilder().appendCodePoint(0x7F).toString(), "" }, "element value out of range");
    testVectors.put(new String[] { new StringBuilder().appendCodePoint(0x80).toString(), "" }, "element value out of range");
    testVectors.put(new String[] { new StringBuilder().appendCodePoint(0x800).toString(), "" }, "element value out of range");
    testVectors.put(new String[] { new StringBuilder().appendCodePoint(0x10000).toString(), "" }, "element value out of range");
    testVectors.put(new String[] { "\ud800\udc00", "" }, "element value out of range");
    testVectors.put(new String[] { "b\u200dc", "" }, "element value out of range");
    testVectors.put(new String[] { "\u0430", "" }, "element value out of range");
    testVectors.put(new String[] { "an84characterslonghumanreadablepartthatcontainsthenumber1andtheexcludedcharactersbio", "" }, "humanReadablePart invalid length");
    testVectors.put(new String[] { new StringBuilder().appendCodePoint(0x80).append("1eym55h").toString(), "" }, "element value out of range");
    testVectors.put(new String[] { "bc", "00" }, "bc1q9zpgru");
    testVectors.put(new String[] { "bc", "0000" }, "bc1qqsa7s0f");
    //
    for (final Map.Entry<String[], String> entry : testVectors.entrySet()) {
      final String help = entry.getKey()[0] + "," + entry.getKey()[1];
      final byte[] base32 = (entry.getKey()[1] == null) ? null : Util.fromHexString(entry.getKey()[1]);
      try {
        final Bech32 bech32 = Bech32.encode(entry.getKey()[0], base32);
        Assert.assertEquals(help, entry.getValue().toLowerCase(Locale.ROOT), bech32.toString());
        Assert.assertEquals(help, bech32.toString().toLowerCase(Locale.ROOT), bech32.toString());
        Assert.assertEquals(help, entry.getKey()[0].toLowerCase(Locale.ROOT), bech32.getHumanReadablePart());
        Assert.assertEquals(help, bech32.getHumanReadablePart().toLowerCase(Locale.ROOT), bech32.getHumanReadablePart());
        Assert.assertArrayEquals(help, base32, bech32.getData());
      } catch (final IllegalArgumentException e) {
        Assert.assertEquals(help, entry.getValue(), e.getMessage());
      }
    }
  }

  @Test
  public void test_encode_segwit() {
    final Map<String[], String> testVectors = new OneShotLinkedHashMap<>();
    testVectors.put(new String[] { "bc", "00", "751e76e8199196d454941c45d1b3a323f1433bd6" }, "BC1QW508D6QEJXTDG4Y5R3ZARVARY0C5XW7KV8F3T4");
    testVectors.put(new String[] { "Bc", "00", "751e76e8199196d454941c45d1b3a323f1433bd6" }, "BC1QW508D6QEJXTDG4Y5R3ZARVARY0C5XW7KV8F3T4");
    testVectors.put(new String[] { "bC", "00", "751e76e8199196d454941c45d1b3a323f1433bd6" }, "BC1QW508D6QEJXTDG4Y5R3ZARVARY0C5XW7KV8F3T4");
    testVectors.put(new String[] { "BC", "00", "751e76e8199196d454941c45d1b3a323f1433bd6" }, "BC1QW508D6QEJXTDG4Y5R3ZARVARY0C5XW7KV8F3T4");
    testVectors.put(new String[] { "tb", "00", "1863143c14c5166804bd19203356da136c985678cd4d27a1b8c6329604903262" }, "tb1qrp33g0q5c5txsp9arysrx4k6zdkfs4nce4xj0gdcccefvpysxf3q0sl5k7");
    testVectors.put(new String[] { "bc", "00", "1863143c14c5166804bd19203356da136c985678cd4d27a1b8c6329604903262" }, "bc1qrp33g0q5c5txsp9arysrx4k6zdkfs4nce4xj0gdcccefvpysxf3qccfmv3");
    testVectors.put(new String[] { "bcrt", "00", "1863143c14c5166804bd19203356da136c985678cd4d27a1b8c6329604903262" }, "bcrt1qrp33g0q5c5txsp9arysrx4k6zdkfs4nce4xj0gdcccefvpysxf3qzf4jry");
    testVectors.put(new String[] { "ltc", "00", "751e76e8199196d454941c45d1b3a323f1433bd6" }, "ltc1qw508d6qejxtdg4y5r3zarvary0c5xw7kgmn4n9");
    testVectors.put(new String[] { "tltc", "00", "751e76e8199196d454941c45d1b3a323f1433bd6" }, "tltc1qw508d6qejxtdg4y5r3zarvary0c5xw7klfsuq0");
    testVectors.put(new String[] { "bc", "01", "751e76e8199196d454941c45d1b3a323f1433bd6751e76e8199196d454941c45d1b3a323f1433bd6" }, "bc1pw508d6qejxtdg4y5r3zarvary0c5xw7kw508d6qejxtdg4y5r3zarvary0c5xw7k7grplx");
    testVectors.put(new String[] { "bc", "10", "751e" }, "BC1SW50QA3JX3S");
    testVectors.put(new String[] { "bc", "02", "751e76e8199196d454941c45d1b3a323" }, "bc1zw508d6qejxtdg4y5r3zarvaryvg6kdaj");
    testVectors.put(new String[] { "tb", "00", "000000c4a5cad46221b2a187905e5266362b99d5e91c6ce24d165dab93e86433" }, "tb1qqqqqp399et2xygdj5xreqhjjvcmzhxw4aywxecjdzew6hylgvsesrxh6hy");
    testVectors.put(new String[] { "tc", "00", "751e76e8199196d454941c45d1b3a323f1433bd6" }, "tc1qw508d6qejxtdg4y5r3zarvary0c5xw7kg3g4ty");
    testVectors.put(new String[] { "bc", "11", "751e76e8199196d454941c45d1b3a323f1433bd6" }, "BC13W508D6QEJXTDG4Y5R3ZARVARY0C5XW7KN40WF2");
    testVectors.put(new String[] { "bc", "03", "75" }, "bc1rw5uspcuh");
    testVectors.put(new String[] { "bc", "00", "00" }, "bc1qqqglchaj");
    testVectors.put(new String[] { "bc", "0f", "751e76e8199196d454941c45d1b3a323f1433bd6751e76e8199196d454941c45d1b3a323f1433bd675" }, "bc10w508d6qejxtdg4y5r3zarvary0c5xw7kw508d6qejxtdg4y5r3zarvary0c5xw7kw5rljs90");
    testVectors.put(new String[] { "bc", "00", "1d1e76e8199196d454941c45d1b3a323" }, "BC1QR508D6QEJXTDG4Y5R3ZARVARYV98GJ9P");
    testVectors.put(new String[] { "tb", "00", "1863143c14c5166804bd19203356da136c985678cd4d27a1b8c6329604903262" }, "tb1qrp33g0q5c5txsp9arysrx4k6zdkfs4nce4xj0gdcccefvpysxf3q0sL5k7");
    testVectors.put(new String[] { "bcrt", "00", "751e76e8199196d454941c45d1b3a323f1433bd6" }, "bcrt1qw508d6qejxtdg4y5r3zarvary0c5xw7kygt080");
    testVectors.put(new String[] { "bcrt", "00", "000000c4a5cad46221b2a187905e5266362b99d5e91c6ce24d165dab93e86433" }, "bcrt1qqqqqp399et2xygdj5xreqhjjvcmzhxw4aywxecjdzew6hylgvseswlauz7");
    testVectors.put(new String[] { "bc", "00", "e15a86a23178f433d514dbbce042e87d72662b8b5edcacfd2e37ab7a2d135f05" }, "bc1qu9dgdg330r6r84g5mw7wqshg04exv2uttmw2elfwx74h5tgntuzs44gyfg");
    testVectors.put(new String[] { "bc", "00", "a2516e770582864a6a56ed21a102044e388c62e3" }, "bc1q5fgkuac9s2ry56jka5s6zqsyfcugcchry5cwu0");
    testVectors.put(new String[] { "bc", "00", "0bfe935e70c321c7ca3afc75ce0d0ca2f98b5422e008bb31c00c6d7f1f1c0ad6" }, "bc1qp0lfxhnscvsu0j36l36uurgv5tuck4pzuqytkvwqp3kh78cupttqyf705v");
    //
    for (final Map.Entry<String[], String> entry : testVectors.entrySet()) {
      final String help = entry.getKey()[0] + "," + entry.getKey()[1] + "," + entry.getKey()[2];
      final byte[] witnessVersion = Util.fromHexString(entry.getKey()[1]);
      final byte[] witnessProgramPreConversion = Util.fromHexString(entry.getKey()[2]);
      final byte[] witnessProgram = Bech32.convert(witnessProgramPreConversion, 8, 0, witnessProgramPreConversion.length);
      final byte[] input = Util.concat(witnessVersion, witnessProgram);
      final Bech32 bech32 = Bech32.encode(entry.getKey()[0], input);
      Assert.assertEquals(help, entry.getValue().toLowerCase(Locale.ROOT), bech32.toString());
      Assert.assertEquals(help, entry.getKey()[0].toLowerCase(Locale.ROOT), bech32.getHumanReadablePart());
      Assert.assertEquals(help, bech32.toString().toLowerCase(Locale.ROOT), bech32.toString());
      Assert.assertEquals(help, bech32.getHumanReadablePart().toLowerCase(Locale.ROOT), bech32.getHumanReadablePart());
    }
  }

  @Test
  public void test_roundtrip() throws DecodingException {
    final List<String> testVectors = new LinkedList<>();
    testVectors.add("a12uel5l");
    testVectors.add("A12UEL5L");
    testVectors.add("an83characterlonghumanreadablepartthatcontainsthenumber1andtheexcludedcharactersbio1tt5tgs");
    testVectors.add("abcdef1qpzry9x8gf2tvdw0s3jn54khce6mua7lmqqqxw");
    testVectors.add("11qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqc8247j");
    testVectors.add("x1llllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllelz3ww");
    testVectors.add("split1checkupstagehandshakeupstreamerranterredcaperred2y9e3w");
    testVectors.add("?1ezyfcl");
    testVectors.add("bc1q9zpgru");
    testVectors.add("bc1qqsa7s0f");
    testVectors.add("BC1QW508D6QEJXTDG4Y5R3ZARVARY0C5XW7KV8F3T4");
    testVectors.add("BC1QW508D6QEJXTDG4Y5R3ZARVARY0C5XW7KV8F3T4".toLowerCase(Locale.ROOT));
    testVectors.add("tb1qrp33g0q5c5txsp9arysrx4k6zdkfs4nce4xj0gdcccefvpysxf3q0sl5k7");
    testVectors.add("bc1pw508d6qejxtdg4y5r3zarvary0c5xw7kw508d6qejxtdg4y5r3zarvary0c5xw7k7grplx");
    testVectors.add("BC1SW50QA3JX3S");
    testVectors.add("bc1zw508d6qejxtdg4y5r3zarvaryvg6kdaj");
    testVectors.add("tb1qqqqqp399et2xygdj5xreqhjjvcmzhxw4aywxecjdzew6hylgvsesrxh6hy");
    testVectors.add("tc1qw508d6qejxtdg4y5r3zarvary0c5xw7kg3g4ty");
    testVectors.add("BC13W508D6QEJXTDG4Y5R3ZARVARY0C5XW7KN40WF2");
    testVectors.add("bc1rw5uspcuh");
    testVectors.add("bc1qqqglchaj");
    testVectors.add("bc10w508d6qejxtdg4y5r3zarvary0c5xw7kw508d6qejxtdg4y5r3zarvary0c5xw7kw5rljs90");
    testVectors.add("BC1QR508D6QEJXTDG4Y5R3ZARVARYV98GJ9P");
    testVectors.add("bc1zw508d6qejxtdg4y5r3zarvaryvqyzf3du");
    testVectors.add("tb1qrp33g0q5c5txsp9arysrx4k6zdkfs4nce4xj0gdcccefvpysxf3pjxtptv");
    testVectors.add("bc1gmk9yu");
    testVectors.add("bcrt1qw508d6qejxtdg4y5r3zarvary0c5xw7kygt080");
    testVectors.add("bcrt1qqqqqp399et2xygdj5xreqhjjvcmzhxw4aywxecjdzew6hylgvseswlauz7");
    testVectors.add("bc1qu9dgdg330r6r84g5mw7wqshg04exv2uttmw2elfwx74h5tgntuzs44gyfg");
    testVectors.add("bc1q5fgkuac9s2ry56jka5s6zqsyfcugcchry5cwu0");
    testVectors.add("bc1qp0lfxhnscvsu0j36l36uurgv5tuck4pzuqytkvwqp3kh78cupttqyf705v");
    //
    for (final String test : testVectors) {
      final Bech32 decoded = Bech32.decode(test);
      Assert.assertEquals(test, test, decoded.toString());
      final Bech32 encoded = Bech32.encode(decoded.getHumanReadablePart(), decoded.getData());
      Assert.assertEquals(test, test.toLowerCase(Locale.ROOT), encoded.toString());
    }
  }
}
