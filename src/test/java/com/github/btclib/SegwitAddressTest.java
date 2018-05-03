package com.github.btclib;

import java.util.Locale;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

public class SegwitAddressTest {
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
    testVectors.put("A12UEL5L", new String[] { "invalid witness version" });
    testVectors.put("a12uel5l", new String[] { "invalid witness version" });
    testVectors.put("A12UeL5L", new String[] { "mixed case" });
    testVectors.put("a12uEl5l", new String[] { "mixed case" });
    testVectors.put("an83characterlonghumanreadablepartthatcontainsthenumber1andtheexcludedcharactersbio1tt5tgs", new String[] { "invalid witness version" });
    testVectors.put("abcdef1qpzry9x8gf2tvdw0s3jn54khce6mua7lmqqqxw", new String[] { "invalid witness program" });
    testVectors.put("11qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqc8247j", new String[] { "invalid witness program" });
    testVectors.put("x1llllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllelz3ww", new String[] { "invalid witness version" });
    testVectors.put("x1lllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllrdjtnk", new String[] { "invalid length" });
    testVectors.put("split1checkupstagehandshakeupstreamerranterredcaperred2y9e3w", new String[] { "invalid witness version" });
    testVectors.put("?1ezyfcl", new String[] { "invalid witness version" });
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
    testVectors.put("bc1q9zpgru", new String[] { "invalid witness program" });
    testVectors.put("bc1qqsa7s0f", new String[] { "invalid witness program" });
    testVectors.put("BC1QW508D6QEJXTDG4Y5R3ZARVARY0C5XW7KV8F3T4", new String[] { "bc", "00", "751e76e8199196d454941c45d1b3a323f1433bd6" });
    testVectors.put("bc1qw508d6qejxtdg4y5r3zarvary0c5xw7kv8f3t4", new String[] { "bc", "00", "751e76e8199196d454941c45d1b3a323f1433bd6" });
    testVectors.put("tb1qw508d6qejxtdg4y5r3zarvary0c5xw7kxpjzsx", new String[] { "tb", "00", "751e76e8199196d454941c45d1b3a323f1433bd6" });
    testVectors.put("bcrt1qw508d6qejxtdg4y5r3zarvary0c5xw7kygt080", new String[] { "bcrt", "00", "751e76e8199196d454941c45d1b3a323f1433bd6" });
    testVectors.put("bc1qrp33g0q5c5txsp9arysrx4k6zdkfs4nce4xj0gdcccefvpysxf3qccfmv3", new String[] { "bc", "00", "1863143c14c5166804bd19203356da136c985678cd4d27a1b8c6329604903262" }); // for reference, private key = 1 and sha256(210279BE667EF9DCBBAC55A06295CE870B07029BFCDB2DCE28D959F2815B16F81798AC)
    testVectors.put("tb1qrp33g0q5c5txsp9arysrx4k6zdkfs4nce4xj0gdcccefvpysxf3q0sl5k7", new String[] { "tb", "00", "1863143c14c5166804bd19203356da136c985678cd4d27a1b8c6329604903262" });
    testVectors.put("bcrt1qrp33g0q5c5txsp9arysrx4k6zdkfs4nce4xj0gdcccefvpysxf3qzf4jry", new String[] { "bcrt", "00", "1863143c14c5166804bd19203356da136c985678cd4d27a1b8c6329604903262" });
    testVectors.put("ltc1qw508d6qejxtdg4y5r3zarvary0c5xw7kgmn4n9", new String[] { "ltc", "00", "751e76e8199196d454941c45d1b3a323f1433bd6" });
    testVectors.put("tltc1qw508d6qejxtdg4y5r3zarvary0c5xw7klfsuq0", new String[] { "tltc", "00", "751e76e8199196d454941c45d1b3a323f1433bd6" });
    testVectors.put("bC1QW508D6QEJXTDG4Y5R3ZARVARY0C5XW7KV8F3T4", new String[] { "mixed case" });
    testVectors.put("bc1pw508d6qejxtdg4y5r3zarvary0c5xw7kw508d6qejxtdg4y5r3zarvary0c5xw7k7grplx", new String[] { "bc", "01", "751e76e8199196d454941c45d1b3a323f1433bd6751e76e8199196d454941c45d1b3a323f1433bd6" });
    testVectors.put("BC1SW50QA3JX3S", new String[] { "bc", "10", "751e" });
    testVectors.put("bc1zw508d6qejxtdg4y5r3zarvaryvg6kdaj", new String[] { "bc", "02", "751e76e8199196d454941c45d1b3a323" });
    testVectors.put("tb1qqqqqp399et2xygdj5xreqhjjvcmzhxw4aywxecjdzew6hylgvsesrxh6hy", new String[] { "tb", "00", "000000c4a5cad46221b2a187905e5266362b99d5e91c6ce24d165dab93e86433" });
    testVectors.put("tc1qw508d6qejxtdg4y5r3zarvary0c5xw7kg3g4ty", new String[] { "tc", "00", "751e76e8199196d454941c45d1b3a323f1433bd6" });
    testVectors.put("bc1qw508d6qejxtdg4y5r3zarvary0c5xw7kv8f3t5", new String[] { "invalid checksum" });
    testVectors.put("BC13W508D6QEJXTDG4Y5R3ZARVARY0C5XW7KN40WF2", new String[] { "invalid witness version" });
    testVectors.put("bc1rw5uspcuh", new String[] { "invalid witness program" });
    testVectors.put("bc1qqqglchaj", new String[] { "invalid witness program" });
    testVectors.put("bc10w508d6qejxtdg4y5r3zarvary0c5xw7kw508d6qejxtdg4y5r3zarvary0c5xw7kw5rljs90", new String[] { "invalid witness program" });
    testVectors.put("BC1QR508D6QEJXTDG4Y5R3ZARVARYV98GJ9P", new String[] { "invalid witness program length" });
    testVectors.put("tb1qrp33g0q5c5txsp9arysrx4k6zdkfs4nce4xj0gdcccefvpysxf3q0sL5k7", new String[] { "mixed case" });
    testVectors.put("Tb1qrp33g0q5c5txsp9arysrx4k6zdkfs4nce4xj0gdcccefvpysxf3q0sL5k7", new String[] { "mixed case" });
    testVectors.put("bc1zw508d6qejxtdg4y5r3zarvaryvqyzf3du", new String[] { "invalid witness program" });
    testVectors.put("tb1qrp33g0q5c5txsp9arysrx4k6zdkfs4nce4xj0gdcccefvpysxf3pjxtptv", new String[] { "invalid witness program" });
    testVectors.put("bc1gmk9yu", new String[] { "invalid witness version" });
    testVectors.put("bcrt1qqqqqp399et2xygdj5xreqhjjvcmzhxw4aywxecjdzew6hylgvseswlauz7", new String[] { "bcrt", "00", "000000c4a5cad46221b2a187905e5266362b99d5e91c6ce24d165dab93e86433" });
    testVectors.put("bc1qu9dgdg330r6r84g5mw7wqshg04exv2uttmw2elfwx74h5tgntuzs44gyfg", new String[] { "bc", "00", "e15a86a23178f433d514dbbce042e87d72662b8b5edcacfd2e37ab7a2d135f05" });
    testVectors.put("bc1q5fgkuac9s2ry56jka5s6zqsyfcugcchry5cwu0", new String[] { "bc", "00", "a2516e770582864a6a56ed21a102044e388c62e3" });
    testVectors.put("bc1qp0lfxhnscvsu0j36l36uurgv5tuck4pzuqytkvwqp3kh78cupttqyf705v", new String[] { "bc", "00", "0bfe935e70c321c7ca3afc75ce0d0ca2f98b5422e008bb31c00c6d7f1f1c0ad6" });
    //
    for (final Map.Entry<String, String[]> entry : testVectors.entrySet()) {
      try {
        final SegwitAddress segwitAddress = SegwitAddress.decode(entry.getKey());
        Assert.assertEquals(entry.getKey(), entry.getKey(), segwitAddress.toString());
        Assert.assertEquals(entry.getKey(), entry.getValue()[0].toLowerCase(Locale.ROOT), segwitAddress.getHumanReadablePart());
        Assert.assertArrayEquals(entry.getKey(), Util.fromHexString(entry.getValue()[1]), new byte[] { segwitAddress.getWitnessVersion() });
        Assert.assertArrayEquals(entry.getKey(), Util.fromHexString(entry.getValue()[2]), segwitAddress.getWitnessProgram());
      } catch (final DecodingException | IllegalArgumentException e) {
        Assert.assertEquals(entry.getKey(), entry.getValue()[0], e.getMessage());
      }
    }
  }

  @Test
  public void test_encode() {
    final Map<String[], String> testVectors = new OneShotLinkedHashMap<>();
    testVectors.put(new String[] { null, "00", null }, "witnessProgram must not be null");
    testVectors.put(new String[] { null, "00", "" }, "witnessProgram invalid length");
    testVectors.put(new String[] { "", "00", "00" }, "witnessProgram invalid length");
    testVectors.put(new String[] { " ", "11", "0000" }, "witnessVersion invalid");
    testVectors.put(new String[] { " ", "00", "0000" }, "witnessProgram invalid length for witnessVersion");
    testVectors.put(new String[] { " ", "10", "0000" }, "element value out of range");
    testVectors.put(new String[] { "a", "10", "201f" }, "a1syq0ss63a9k");
    testVectors.put(new String[] { "a", "10", "ffff" }, "a1sllls5gqxl5");
    testVectors.put(new String[] { "A", "00", "0000000000000000000000000000000000000000" }, "a1qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqj8j0sd");
    testVectors.put(new String[] { "A", "00", "0000000000000000000000000000000000000000000000000000000000000000" }, "a1qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqts4csw");
    testVectors.put(new String[] { "an83characterlonghumanreadablepartthatcontainsthenumber1andtheexcludedcharactersbio", "00", new String(new char[41]).replace("\0", "00") }, "witnessProgram invalid length");
    testVectors.put(new String[] { "abcdef", "ff", "000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f" }, "witnessVersion invalid");
    testVectors.put(new String[] { "abcdef", "00", "000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f" }, "abcdef1qqqqsyqcyq5rqwzqfpg9scrgwpugpzysnzs23v9ccrydpk8qarc0saqd9wh");
    testVectors.put(new String[] { "abcdef", "10", "000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f" }, "abcdef1sqqqsyqcyq5rqwzqfpg9scrgwpugpzysnzs23v9ccrydpk8qarc0sp5h8mm");
    testVectors.put(new String[] { "abcdef", "11", "000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f" }, "witnessVersion invalid");
    testVectors.put(new String[] { "1", "00", new String(new char[41]).replace("\0", "00") }, "witnessProgram invalid length");
    testVectors.put(new String[] { "1", "01", "00000000000000000000000000000000000000000000000000000000000000000000000000000000" }, "11pqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqha9xzw");
    testVectors.put(new String[] { "1", "01", "0000000000000000000000000000000000000000000000000000000000000000" }, "11pqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqq8p2umf");
    testVectors.put(new String[] { "?", "00", new String(new char[20]).replace("\0", "00") }, "?1qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqv2j5fd");
    testVectors.put(new String[] { "bc", "00", new String(new char[20]).replace("\0", "00") }, "bc1qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqq9e75rs");
    testVectors.put(new String[] { "tb", "00", new String(new char[20]).replace("\0", "00") }, "tb1qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqq0l98cr");
    testVectors.put(new String[] { "bcrt", "00", new String(new char[20]).replace("\0", "00") }, "bcrt1qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqdku202");
    testVectors.put(new String[] { " ", "00", new String(new char[20]).replace("\0", "00") }, "element value out of range");
    testVectors.put(new String[] { new StringBuilder().appendCodePoint(0x7F).toString(), "00", new String(new char[20]).replace("\0", "00") }, "element value out of range");
    testVectors.put(new String[] { new StringBuilder().appendCodePoint(0x80).toString(), "00", new String(new char[20]).replace("\0", "00") }, "element value out of range");
    testVectors.put(new String[] { new StringBuilder().appendCodePoint(0x800).toString(), "00", new String(new char[20]).replace("\0", "00") }, "element value out of range");
    testVectors.put(new String[] { new StringBuilder().appendCodePoint(0x10000).toString(), "00", new String(new char[20]).replace("\0", "00") }, "element value out of range");
    testVectors.put(new String[] { "\ud800\udc00", "00", new String(new char[20]).replace("\0", "00") }, "element value out of range");
    testVectors.put(new String[] { "b\u200dc", "00", new String(new char[20]).replace("\0", "00") }, "element value out of range");
    testVectors.put(new String[] { "\u0430", "00", new String(new char[20]).replace("\0", "00") }, "element value out of range");
    testVectors.put(new String[] { "an84characterslonghumanreadablepartthatcontainsthenumber1andtheexcludedcharactersbio", "00", new String(new char[20]).replace("\0", "00") }, "humanReadablePart invalid length");
    testVectors.put(new String[] { new StringBuilder().appendCodePoint(0x80).append("1eym55h").toString(), "00", new String(new char[20]).replace("\0", "00") }, "element value out of range");
    testVectors.put(new String[] { "bc", "00", "751e76e8199196d454941c45d1b3a323f1433bd6" }, "BC1QW508D6QEJXTDG4Y5R3ZARVARY0C5XW7KV8F3T4");
    testVectors.put(new String[] { "Bc", "00", "751e76e8199196d454941c45d1b3a323f1433bd6" }, "bc1qw508d6qejxtdg4y5r3zarvary0c5xw7kv8f3t4");
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
    testVectors.put(new String[] { "bc", "11", "751e76e8199196d454941c45d1b3a323f1433bd6" }, "witnessVersion invalid");
    testVectors.put(new String[] { "bc", "03", "75" }, "witnessProgram invalid length"); // bc1rw5uspcuh
    testVectors.put(new String[] { "bc", "00", "00" }, "witnessProgram invalid length"); // bc1qqqglchaj
    testVectors.put(new String[] { "bc", "00", "0000" }, "witnessProgram invalid length for witnessVersion");
    testVectors.put(new String[] { "bc", "01", "0000" }, "bc1pqqqq4yr79j");
    testVectors.put(new String[] { "bc", "0f", "751e76e8199196d454941c45d1b3a323f1433bd6751e76e8199196d454941c45d1b3a323f1433bd675" }, "witnessProgram invalid length"); // bc10w508d6qejxtdg4y5r3zarvary0c5xw7kw508d6qejxtdg4y5r3zarvary0c5xw7kw5rljs90
    testVectors.put(new String[] { "bc", "00", "1d1e76e8199196d454941c45d1b3a323" }, "witnessProgram invalid length for witnessVersion"); // "BC1QR508D6QEJXTDG4Y5R3ZARVARYV98GJ9P".toLowerCase(Locale.ROOT)
    testVectors.put(new String[] { "tb", "00", "1863143c14c5166804bd19203356da136c985678cd4d27a1b8c6329604903262" }, "tb1qrp33g0q5c5txsp9arysrx4k6zdkfs4nce4xj0gdcccefvpysxf3q0sL5k7");
    testVectors.put(new String[] { "bcrt", "00", "751e76e8199196d454941c45d1b3a323f1433bd6" }, "bcrt1qw508d6qejxtdg4y5r3zarvary0c5xw7kygt080");
    testVectors.put(new String[] { "bcrt", "00", "000000c4a5cad46221b2a187905e5266362b99d5e91c6ce24d165dab93e86433" }, "bcrt1qqqqqp399et2xygdj5xreqhjjvcmzhxw4aywxecjdzew6hylgvseswlauz7");
    testVectors.put(new String[] { "bc", "00", "e15a86a23178f433d514dbbce042e87d72662b8b5edcacfd2e37ab7a2d135f05" }, "bc1qu9dgdg330r6r84g5mw7wqshg04exv2uttmw2elfwx74h5tgntuzs44gyfg");
    testVectors.put(new String[] { "bc", "00", "a2516e770582864a6a56ed21a102044e388c62e3" }, "bc1q5fgkuac9s2ry56jka5s6zqsyfcugcchry5cwu0");
    testVectors.put(new String[] { "bc", "00", "0bfe935e70c321c7ca3afc75ce0d0ca2f98b5422e008bb31c00c6d7f1f1c0ad6" }, "bc1qp0lfxhnscvsu0j36l36uurgv5tuck4pzuqytkvwqp3kh78cupttqyf705v");
    //
    for (final Map.Entry<String[], String> entry : testVectors.entrySet()) {
      final String help = entry.getKey()[0] + "," + entry.getKey()[1] + "," + entry.getKey()[2];
      final byte witnessVersion = Util.fromHexString(entry.getKey()[1])[0];
      final byte[] witnessProgram = (entry.getKey()[2] == null) ? null : Util.fromHexString(entry.getKey()[2]);
      try {
        final SegwitAddress segwitAddress = SegwitAddress.encode(entry.getKey()[0], witnessVersion, witnessProgram);
        Assert.assertEquals(help, entry.getValue().toLowerCase(Locale.ROOT), segwitAddress.toString());
        Assert.assertArrayEquals(help, Util.fromHexString(entry.getKey()[1]), new byte[] { segwitAddress.getWitnessVersion() });
        Assert.assertArrayEquals(help, Util.fromHexString(entry.getKey()[2]), segwitAddress.getWitnessProgram());
      } catch (final IllegalArgumentException e) {
        Assert.assertEquals(help, entry.getValue(), e.getMessage());
      }
    }
  }

  @Test
  public void test_toScriptPubKey() throws DecodingException {
    final Map<String, String> testVectors = new OneShotLinkedHashMap<>();
    testVectors.put("BC1QW508D6QEJXTDG4Y5R3ZARVARY0C5XW7KV8F3T4", "0014751e76e8199196d454941c45d1b3a323f1433bd6");
    testVectors.put("tb1qrp33g0q5c5txsp9arysrx4k6zdkfs4nce4xj0gdcccefvpysxf3q0sl5k7", "00201863143c14c5166804bd19203356da136c985678cd4d27a1b8c6329604903262");
    testVectors.put("bc1pw508d6qejxtdg4y5r3zarvary0c5xw7kw508d6qejxtdg4y5r3zarvary0c5xw7k7grplx", "5128751e76e8199196d454941c45d1b3a323f1433bd6751e76e8199196d454941c45d1b3a323f1433bd6");
    testVectors.put("BC1SW50QA3JX3S", "6002751e");
    testVectors.put("bc1zw508d6qejxtdg4y5r3zarvaryvg6kdaj", "5210751e76e8199196d454941c45d1b3a323");
    testVectors.put("tb1qqqqqp399et2xygdj5xreqhjjvcmzhxw4aywxecjdzew6hylgvsesrxh6hy", "0020000000c4a5cad46221b2a187905e5266362b99d5e91c6ce24d165dab93e86433");
    //
    for (final Map.Entry<String, String> entry : testVectors.entrySet()) {
      final SegwitAddress segwitAddress = SegwitAddress.decode(entry.getKey());
      final byte[] scriptPubKey = Util.fromHexString(entry.getValue());
      Assert.assertArrayEquals(entry.getKey(), scriptPubKey, segwitAddress.toScriptPubKey());
    }
  }
}
