package com.github.btclib;

import java.util.LinkedHashMap;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;

/**
 * secp256k1 G = 0279be667ef9dcbbac55a06295ce870b07029bfcdb2dce28d959f2815b16f81798
 * hash160(0279be667ef9dcbbac55a06295ce870b07029bfcdb2dce28d959f2815b16f81798) = 751e76e8199196d454941c45d1b3a323f1433bd6 p2wpkh witness program
 * sha256(210279be667ef9dcbbac55a06295ce870b07029bfcdb2dce28d959f2815b16f81798ac) = 1863143c14c5166804bd19203356da136c985678cd4d27a1b8c6329604903262 p2wsh witness program
 * sha256(52210279be667ef9dcbbac55a06295ce870b07029bfcdb2dce28d959f2815b16f81798210279be667ef9dcbbac55a06295ce870b07029bfcdb2dce28d959f2815b16f8179852ae) = d714cd9a28197a7a65cccd77a20938194fdc05910105459ae6a9715918c6c408 = wsh(multi(2,0279be667ef9dcbbac55a06295ce870b07029bfcdb2dce28d959f2815b16f81798,0279be667ef9dcbbac55a06295ce870b07029bfcdb2dce28d959f2815b16f81798))#e2vfg425 p2wsh multisig
 * sha256(52210279be667ef9dcbbac55a06295ce870b07029bfcdb2dce28d959f2815b16f81798210279be667ef9dcbbac55a06295ce870b07029bfcdb2dce28d959f2815b16f81798210279be667ef9dcbbac55a06295ce870b07029bfcdb2dce28d959f2815b16f8179853ae) = 344ca405ea441a95efe903cc7325aa0303f0a398af394b0194962f3afbe7ce8e = wsh(multi(2,0279be667ef9dcbbac55a06295ce870b07029bfcdb2dce28d959f2815b16f81798,0279be667ef9dcbbac55a06295ce870b07029bfcdb2dce28d959f2815b16f81798,0279be667ef9dcbbac55a06295ce870b07029bfcdb2dce28d959f2815b16f81798))#q46wtff5 p2wsh multisig
 */
public class SegwitAddressTest {
  @Test
  public void test_decode() {
    final var testVectors = new LinkedHashMap<String[], String[]>();
    // precondition and exceptional code path tests
    testVectors.put(new String[] { null, null, }, new String[] { "java.lang.NullPointerException: expectedHumanReadablePart must not be null", });
    testVectors.put(new String[] { "", null, }, new String[] { "java.lang.NullPointerException: address must not be null", });
    testVectors.put(new String[] { "", Util.multiply("a", Bech32.MIN_BECH32_LENGTH - 1), }, new String[] { "com.github.btclib.DecodingException: address length invalid", });
    testVectors.put(new String[] { "", Util.multiply("a", SegwitAddress.MAX_LENGTH + 1), }, new String[] { "com.github.btclib.DecodingException: address length invalid", });
    testVectors.put(new String[] { Util.multiply("a", Bech32.MIN_HRP_LENGTH - 1), "a12uel5l", }, new String[] { "com.github.btclib.DecodingException: expectedHumanReadablePart does not match decoded value", });
    testVectors.put(new String[] { Util.multiply("a", Bech32.MAX_HRP_LENGTH + 1), "a12uel5l", }, new String[] { "com.github.btclib.DecodingException: expectedHumanReadablePart does not match decoded value", });
    testVectors.put(new String[] { "A", "a12uel5l", }, new String[] { "com.github.btclib.DecodingException: expectedHumanReadablePart does not match decoded value", });
    testVectors.put(new String[] { new StringBuilder().appendCodePoint(Character.codePointOf("FULLWIDTH LATIN SMALL LETTER A")).toString(), "a12uel5l", }, new String[] { "com.github.btclib.DecodingException: expectedHumanReadablePart does not match decoded value", });
    testVectors.put(new String[] { "bc", "a12uel5l", }, new String[] { "com.github.btclib.DecodingException: expectedHumanReadablePart does not match decoded value", });
    testVectors.put(new String[] { "a", "bc1qw508d6qejxtdg4y5r3zarvary0c5xw7kv8f3t4", }, new String[] { "com.github.btclib.DecodingException: expectedHumanReadablePart does not match decoded value", });
    testVectors.put(new String[] { "bc", "bc1gmk9yu", }, new String[] { "com.github.btclib.DecodingException: decoded data length invalid", });
    testVectors.put(new String[] { "bc", "bc1qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqq3sp7ks", }, new String[] { "com.github.btclib.DecodingException: decoded data length invalid", });
    testVectors.put(new String[] { "bc", "bc13qqqqjggfzq", }, new String[] { "com.github.btclib.DecodingException: decoded version invalid", });
    testVectors.put(new String[] { "bc", "bc1lqqqqrx9yaw", }, new String[] { "com.github.btclib.DecodingException: decoded version invalid", });
    testVectors.put(new String[] { "bc", "bc1qqqqqye4593", }, new String[] { "com.github.btclib.DecodingException: bech32 variant invalid", });
    testVectors.put(new String[] { "bc", "bc1pqqqq4yr79j", }, new String[] { "com.github.btclib.DecodingException: bech32 variant invalid", });
    testVectors.put(new String[] { "bc", "bc1qw508d6qejxtdg4y5r3zarvaryvqkyqvzl", }, new String[] { "com.github.btclib.DecodingException: invalid padding too many bits", });
    testVectors.put(new String[] { "bc", "bc1pqqqqqq90twsu", }, new String[] { "com.github.btclib.DecodingException: invalid padding too many bits", });
    testVectors.put(new String[] { "bc", "bc1plllllq5980", }, new String[] { "com.github.btclib.DecodingException: invalid padding non-zero bits", });
    testVectors.put(new String[] { "bc", "bc1qqqqq399cqn", }, new String[] { "com.github.btclib.DecodingException: decoded program length invalid for version 0", });
    testVectors.put(new String[] { "bc", "bc1qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqsw47qg", }, new String[] { "com.github.btclib.DecodingException: decoded program length invalid for version 0", });
    //
    testVectors.put(new String[] { "bc", "bc1qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqthqst8", }, new String[] { "0", Util.multiply("00", 32), "BECH32", "0020" + Util.multiply("00", 32), });
    testVectors.put(new String[] { "bc", "bc1pqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqpqqenm", }, new String[] { "1", Util.multiply("00", 32), "BECH32M", "5120" + Util.multiply("00", 32), });
    testVectors.put(new String[] { "bc", "bc1zqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqfaekas", }, new String[] { "2", Util.multiply("00", 32), "BECH32M", "5220" + Util.multiply("00", 32), });
    testVectors.put(new String[] { "bc", "bc1rqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqkkfnqw", }, new String[] { "3", Util.multiply("00", 32), "BECH32M", "5320" + Util.multiply("00", 32), });
    testVectors.put(new String[] { "bc", "bc1yqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqewzgpx", }, new String[] { "4", Util.multiply("00", 32), "BECH32M", "5420" + Util.multiply("00", 32), });
    testVectors.put(new String[] { "bc", "bc19qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqx9jduc", }, new String[] { "5", Util.multiply("00", 32), "BECH32M", "5520" + Util.multiply("00", 32), });
    testVectors.put(new String[] { "bc", "bc1xqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqwctzjn", }, new String[] { "6", Util.multiply("00", 32), "BECH32M", "5620" + Util.multiply("00", 32), });
    testVectors.put(new String[] { "bc", "bc18qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqq3nm80d", }, new String[] { "7", Util.multiply("00", 32), "BECH32M", "5720" + Util.multiply("00", 32), });
    testVectors.put(new String[] { "bc", "bc1gqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqspaasr", }, new String[] { "8", Util.multiply("00", 32), "BECH32M", "5820" + Util.multiply("00", 32), });
    testVectors.put(new String[] { "bc", "bc1fqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqq02dcda", }, new String[] { "9", Util.multiply("00", 32), "BECH32M", "5920" + Util.multiply("00", 32), });
    testVectors.put(new String[] { "bc", "bc12qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqq8h5hrk", }, new String[] { "10", Util.multiply("00", 32), "BECH32M", "5a20" + Util.multiply("00", 32), });
    testVectors.put(new String[] { "bc", "bc1tqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqcuyj7g", }, new String[] { "11", Util.multiply("00", 32), "BECH32M", "5b20" + Util.multiply("00", 32), });
    testVectors.put(new String[] { "bc", "bc1vqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqhy0flq", }, new String[] { "12", Util.multiply("00", 32), "BECH32M", "5c20" + Util.multiply("00", 32), });
    testVectors.put(new String[] { "bc", "bc1dqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqg0lvz7", }, new String[] { "13", Util.multiply("00", 32), "BECH32M", "5d20" + Util.multiply("00", 32), });
    testVectors.put(new String[] { "bc", "bc1wqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqjxrv4", }, new String[] { "14", Util.multiply("00", 32), "BECH32M", "5e20" + Util.multiply("00", 32), });
    testVectors.put(new String[] { "bc", "bc10qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqlekx3t", }, new String[] { "15", Util.multiply("00", 32), "BECH32M", "5f20" + Util.multiply("00", 32), });
    testVectors.put(new String[] { "bc", "bc1sqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqzl27mf", }, new String[] { "16", Util.multiply("00", 32), "BECH32M", "6020" + Util.multiply("00", 32), });
    //
    testVectors.put(new String[] { "bc", "bc1pqqqqqcnjqs", }, new String[] { "1", Util.multiply("00", 2), "BECH32M", "5102" + Util.multiply("00", 2), });
    testVectors.put(new String[] { "bc", "bc1zqqqqvmec0n", }, new String[] { "2", Util.multiply("00", 2), "BECH32M", "5202" + Util.multiply("00", 2), });
    testVectors.put(new String[] { "bc", "bc1rqqqqg6l72j", }, new String[] { "3", Util.multiply("00", 2), "BECH32M", "5302" + Util.multiply("00", 2), });
    testVectors.put(new String[] { "bc", "bc1yqqqq5adv34", }, new String[] { "4", Util.multiply("00", 2), "BECH32M", "5402" + Util.multiply("00", 2), });
    testVectors.put(new String[] { "bc", "bc19qqqqsut255", }, new String[] { "5", Util.multiply("00", 2), "BECH32M", "5502" + Util.multiply("00", 2), });
    testVectors.put(new String[] { "bc", "bc1xqqqqulpqmh", }, new String[] { "6", Util.multiply("00", 2), "BECH32M", "5602" + Util.multiply("00", 2), });
    testVectors.put(new String[] { "bc", "bc18qqqqc78x7k", }, new String[] { "7", Util.multiply("00", 2), "BECH32M", "5702" + Util.multiply("00", 2), });
    testVectors.put(new String[] { "bc", "bc1gqqqqd3vdye", }, new String[] { "8", Util.multiply("00", 2), "BECH32M", "5802" + Util.multiply("00", 2), });
    testVectors.put(new String[] { "bc", "bc1fqqqqfs2tpc", }, new String[] { "9", Util.multiply("00", 2), "BECH32M", "5902" + Util.multiply("00", 2), });
    testVectors.put(new String[] { "bc", "bc12qqqq9nqpwm", }, new String[] { "10", Util.multiply("00", 2), "BECH32M", "5a02" + Util.multiply("00", 2), });
    testVectors.put(new String[] { "bc", "bc1tqqqqpjx8t6", }, new String[] { "11", Util.multiply("00", 2), "BECH32M", "5b02" + Util.multiply("00", 2), });
    testVectors.put(new String[] { "bc", "bc1vqqqqa454sa", }, new String[] { "12", Util.multiply("00", 2), "BECH32M", "5c02" + Util.multiply("00", 2), });
    testVectors.put(new String[] { "bc", "bc1dqqqqe5jn4u", }, new String[] { "13", Util.multiply("00", 2), "BECH32M", "5d02" + Util.multiply("00", 2), });
    testVectors.put(new String[] { "bc", "bc1wqqqq4hce6l", }, new String[] { "14", Util.multiply("00", 2), "BECH32M", "5e02" + Util.multiply("00", 2), });
    testVectors.put(new String[] { "bc", "bc10qqqq3k7ll7", }, new String[] { "15", Util.multiply("00", 2), "BECH32M", "5f02" + Util.multiply("00", 2), });
    testVectors.put(new String[] { "bc", "bc1sqqqqkfw08p", }, new String[] { "16", Util.multiply("00", 2), "BECH32M", "6002" + Util.multiply("00", 2), });
    //
    testVectors.put(new String[] { "bc", "bc1pqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqq2f6xj9", }, new String[] { "1", Util.multiply("00", 40), "BECH32M", "5128" + Util.multiply("00", 40), });
    testVectors.put(new String[] { "bc", "bc1zqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqmdjnz5", }, new String[] { "2", Util.multiply("00", 40), "BECH32M", "5228" + Util.multiply("00", 40), });
    testVectors.put(new String[] { "bc", "bc1rqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqq5kd84m", }, new String[] { "3", Util.multiply("00", 40), "BECH32M", "5328" + Util.multiply("00", 40), });
    testVectors.put(new String[] { "bc", "bc1yqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqs9zstl", }, new String[] { "4", Util.multiply("00", 40), "BECH32M", "5428" + Util.multiply("00", 40), });
    testVectors.put(new String[] { "bc", "bc19qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqql7ayus", }, new String[] { "5", Util.multiply("00", 40), "BECH32M", "5528" + Util.multiply("00", 40), });
    testVectors.put(new String[] { "bc", "bc1xqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqw643vp", }, new String[] { "6", Util.multiply("00", 40), "BECH32M", "5628" + Util.multiply("00", 40), });
    testVectors.put(new String[] { "bc", "bc18qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqpp29mw", }, new String[] { "7", Util.multiply("00", 40), "BECH32M", "5728" + Util.multiply("00", 40), });
    testVectors.put(new String[] { "bc", "bc1gqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqx4tkef", }, new String[] { "8", Util.multiply("00", 40), "BECH32M", "5828" + Util.multiply("00", 40), });
    testVectors.put(new String[] { "bc", "bc1fqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqfw5zwx", }, new String[] { "9", Util.multiply("00", 40), "BECH32M", "5928" + Util.multiply("00", 40), });
    testVectors.put(new String[] { "bc", "bc12qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqc2uh7h", }, new String[] { "10", Util.multiply("00", 40), "BECH32M", "5a28" + Util.multiply("00", 40), });
    testVectors.put(new String[] { "bc", "bc1tqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqh3rrfc", }, new String[] { "11", Util.multiply("00", 40), "BECH32M", "5b28" + Util.multiply("00", 40), });
    testVectors.put(new String[] { "bc", "bc1vqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqnzv5hu", }, new String[] { "12", Util.multiply("00", 40), "BECH32M", "5c28" + Util.multiply("00", 40), });
    testVectors.put(new String[] { "bc", "bc1dqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqquenqqn", }, new String[] { "13", Util.multiply("00", 40), "BECH32M", "5d28" + Util.multiply("00", 40), });
    testVectors.put(new String[] { "bc", "bc1wqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqdam4sz", }, new String[] { "14", Util.multiply("00", 40), "BECH32M", "5e28" + Util.multiply("00", 40), });
    testVectors.put(new String[] { "bc", "bc10qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqzxyp8d", }, new String[] { "15", Util.multiply("00", 40), "BECH32M", "5f28" + Util.multiply("00", 40), });
    testVectors.put(new String[] { "bc", "bc1sqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqrue65v", }, new String[] { "16", Util.multiply("00", 40), "BECH32M", "6028" + Util.multiply("00", 40), });
    //
    testVectors.put(new String[] { "bc", "bc1qw508d6qejxtdg4y5r3zarvary0c5xw7kv8f3t4", }, new String[] { "0", "751e76e8199196d454941c45d1b3a323f1433bd6", "BECH32", "0014751e76e8199196d454941c45d1b3a323f1433bd6", }); // p2wpkh
    testVectors.put(new String[] { "tb", "tb1qw508d6qejxtdg4y5r3zarvary0c5xw7kxpjzsx", }, new String[] { "0", "751e76e8199196d454941c45d1b3a323f1433bd6", "BECH32", "0014751e76e8199196d454941c45d1b3a323f1433bd6", }); // p2wpkh
    testVectors.put(new String[] { "bcrt", "bcrt1qw508d6qejxtdg4y5r3zarvary0c5xw7kygt080", }, new String[] { "0", "751e76e8199196d454941c45d1b3a323f1433bd6", "BECH32", "0014751e76e8199196d454941c45d1b3a323f1433bd6", }); // p2wpkh
    testVectors.put(new String[] { "bc", "BC1QW508D6QEJXTDG4Y5R3ZARVARY0C5XW7KV8F3T4", }, new String[] { "0", "751e76e8199196d454941c45d1b3a323f1433bd6", "BECH32", "0014751e76e8199196d454941c45d1b3a323f1433bd6", }); // p2wpkh
    testVectors.put(new String[] { "tb", "TB1QW508D6QEJXTDG4Y5R3ZARVARY0C5XW7KXPJZSX", }, new String[] { "0", "751e76e8199196d454941c45d1b3a323f1433bd6", "BECH32", "0014751e76e8199196d454941c45d1b3a323f1433bd6", }); // p2wpkh
    testVectors.put(new String[] { "bcrt", "BCRT1QW508D6QEJXTDG4Y5R3ZARVARY0C5XW7KYGT080", }, new String[] { "0", "751e76e8199196d454941c45d1b3a323f1433bd6", "BECH32", "0014751e76e8199196d454941c45d1b3a323f1433bd6", }); // p2wpkh
    //
    testVectors.put(new String[] { "bc", "bc1qrp33g0q5c5txsp9arysrx4k6zdkfs4nce4xj0gdcccefvpysxf3qccfmv3", }, new String[] { "0", "1863143c14c5166804bd19203356da136c985678cd4d27a1b8c6329604903262", "BECH32", "00201863143c14c5166804bd19203356da136c985678cd4d27a1b8c6329604903262", }); // p2wsh
    testVectors.put(new String[] { "tb", "tb1qrp33g0q5c5txsp9arysrx4k6zdkfs4nce4xj0gdcccefvpysxf3q0sl5k7", }, new String[] { "0", "1863143c14c5166804bd19203356da136c985678cd4d27a1b8c6329604903262", "BECH32", "00201863143c14c5166804bd19203356da136c985678cd4d27a1b8c6329604903262", }); // p2wsh
    testVectors.put(new String[] { "bcrt", "bcrt1qrp33g0q5c5txsp9arysrx4k6zdkfs4nce4xj0gdcccefvpysxf3qzf4jry", }, new String[] { "0", "1863143c14c5166804bd19203356da136c985678cd4d27a1b8c6329604903262", "BECH32", "00201863143c14c5166804bd19203356da136c985678cd4d27a1b8c6329604903262", }); // p2wsh
    testVectors.put(new String[] { "bc", "BC1QRP33G0Q5C5TXSP9ARYSRX4K6ZDKFS4NCE4XJ0GDCCCEFVPYSXF3QCCFMV3", }, new String[] { "0", "1863143c14c5166804bd19203356da136c985678cd4d27a1b8c6329604903262", "BECH32", "00201863143c14c5166804bd19203356da136c985678cd4d27a1b8c6329604903262", }); // p2wsh
    testVectors.put(new String[] { "tb", "TB1QRP33G0Q5C5TXSP9ARYSRX4K6ZDKFS4NCE4XJ0GDCCCEFVPYSXF3Q0SL5K7", }, new String[] { "0", "1863143c14c5166804bd19203356da136c985678cd4d27a1b8c6329604903262", "BECH32", "00201863143c14c5166804bd19203356da136c985678cd4d27a1b8c6329604903262", }); // p2wsh
    testVectors.put(new String[] { "bcrt", "BCRT1QRP33G0Q5C5TXSP9ARYSRX4K6ZDKFS4NCE4XJ0GDCCCEFVPYSXF3QZF4JRY", }, new String[] { "0", "1863143c14c5166804bd19203356da136c985678cd4d27a1b8c6329604903262", "BECH32", "00201863143c14c5166804bd19203356da136c985678cd4d27a1b8c6329604903262", }); // p2wsh
    // https://github.com/bitcoin/bips/blob/master/bip-0086.mediawiki
    testVectors.put(new String[] { "bc", "bc1p5cyxnuxmeuwuvkwfem96lqzszd02n6xdcjrs20cac6yqjjwudpxqkedrcr", }, new String[] { "1", "a60869f0dbcf1dc659c9cecbaf8050135ea9e8cdc487053f1dc6880949dc684c", "BECH32M", "5120a60869f0dbcf1dc659c9cecbaf8050135ea9e8cdc487053f1dc6880949dc684c", }); // p2tr
    testVectors.put(new String[] { "tb", "tb1p5cyxnuxmeuwuvkwfem96lqzszd02n6xdcjrs20cac6yqjjwudpxqp3mvzv", }, new String[] { "1", "a60869f0dbcf1dc659c9cecbaf8050135ea9e8cdc487053f1dc6880949dc684c", "BECH32M", "5120a60869f0dbcf1dc659c9cecbaf8050135ea9e8cdc487053f1dc6880949dc684c", }); // p2tr
    testVectors.put(new String[] { "bcrt", "bcrt1p5cyxnuxmeuwuvkwfem96lqzszd02n6xdcjrs20cac6yqjjwudpxqvg32hk", }, new String[] { "1", "a60869f0dbcf1dc659c9cecbaf8050135ea9e8cdc487053f1dc6880949dc684c", "BECH32M", "5120a60869f0dbcf1dc659c9cecbaf8050135ea9e8cdc487053f1dc6880949dc684c", }); // p2tr
    testVectors.put(new String[] { "bc", "BC1P5CYXNUXMEUWUVKWFEM96LQZSZD02N6XDCJRS20CAC6YQJJWUDPXQKEDRCR", }, new String[] { "1", "a60869f0dbcf1dc659c9cecbaf8050135ea9e8cdc487053f1dc6880949dc684c", "BECH32M", "5120a60869f0dbcf1dc659c9cecbaf8050135ea9e8cdc487053f1dc6880949dc684c", }); // p2tr
    testVectors.put(new String[] { "tb", "TB1P5CYXNUXMEUWUVKWFEM96LQZSZD02N6XDCJRS20CAC6YQJJWUDPXQP3MVZV", }, new String[] { "1", "a60869f0dbcf1dc659c9cecbaf8050135ea9e8cdc487053f1dc6880949dc684c", "BECH32M", "5120a60869f0dbcf1dc659c9cecbaf8050135ea9e8cdc487053f1dc6880949dc684c", }); // p2tr
    testVectors.put(new String[] { "bcrt", "BCRT1P5CYXNUXMEUWUVKWFEM96LQZSZD02N6XDCJRS20CAC6YQJJWUDPXQVG32HK", }, new String[] { "1", "a60869f0dbcf1dc659c9cecbaf8050135ea9e8cdc487053f1dc6880949dc684c", "BECH32M", "5120a60869f0dbcf1dc659c9cecbaf8050135ea9e8cdc487053f1dc6880949dc684c", }); // p2tr
    // https://github.com/bitcoin/bips/blob/master/bip-0173.mediawiki
    testVectors.put(new String[] { "bc", "bc1qw508d6qejxtdg4y5r3zarvary0c5xw7kv8f3t4", }, new String[] { "0", "751e76e8199196d454941c45d1b3a323f1433bd6", "BECH32", "0014751e76e8199196d454941c45d1b3a323f1433bd6", });
    testVectors.put(new String[] { "tb", "tb1qw508d6qejxtdg4y5r3zarvary0c5xw7kxpjzsx", }, new String[] { "0", "751e76e8199196d454941c45d1b3a323f1433bd6", "BECH32", "0014751e76e8199196d454941c45d1b3a323f1433bd6", });
    testVectors.put(new String[] { "bc", "bc1qrp33g0q5c5txsp9arysrx4k6zdkfs4nce4xj0gdcccefvpysxf3qccfmv3", }, new String[] { "0", "1863143c14c5166804bd19203356da136c985678cd4d27a1b8c6329604903262", "BECH32", "00201863143c14c5166804bd19203356da136c985678cd4d27a1b8c6329604903262", });
    testVectors.put(new String[] { "tb", "tb1qrp33g0q5c5txsp9arysrx4k6zdkfs4nce4xj0gdcccefvpysxf3q0sl5k7", }, new String[] { "0", "1863143c14c5166804bd19203356da136c985678cd4d27a1b8c6329604903262", "BECH32", "00201863143c14c5166804bd19203356da136c985678cd4d27a1b8c6329604903262", });
    testVectors.put(new String[] { "a", "A12UEL5L", }, new String[] { "com.github.btclib.DecodingException: decoded data length invalid", });
    testVectors.put(new String[] { "a", "a12uel5l", }, new String[] { "com.github.btclib.DecodingException: decoded data length invalid", });
    testVectors.put(new String[] { "an83characterlonghumanreadablepartthatcontainsthenumber1andtheexcludedcharactersbio", "an83characterlonghumanreadablepartthatcontainsthenumber1andtheexcludedcharactersbio1tt5tgs", }, new String[] { "com.github.btclib.DecodingException: decoded data length invalid", });
    testVectors.put(new String[] { "abcdef", "abcdef1qpzry9x8gf2tvdw0s3jn54khce6mua7lmqqqxw", }, new String[] { "com.github.btclib.DecodingException: invalid padding non-zero bits", }); // segwit_addr.bech32_encode('abcdef', [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31], segwit_addr.Encoding.BECH32)
    testVectors.put(new String[] { "1", "11qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqc8247j", }, new String[] { "com.github.btclib.DecodingException: decoded data length invalid", });
    testVectors.put(new String[] { "split", "split1checkupstagehandshakeupstreamerranterredcaperred2y9e3w", }, new String[] { "com.github.btclib.DecodingException: decoded version invalid", });
    testVectors.put(new String[] { "?", "?1ezyfcl", }, new String[] { "com.github.btclib.DecodingException: decoded data length invalid", });
    testVectors.put(new String[] { "\u0020", "\u00201nwldj5", }, new String[] { "com.github.btclib.DecodingException: input element value invalid", });
    testVectors.put(new String[] { "\u007f", "\u007f1axkwrx", }, new String[] { "com.github.btclib.DecodingException: input element value invalid", });
    testVectors.put(new String[] { "\u0080", "\u00801eym55h", }, new String[] { "com.github.btclib.DecodingException: input element value invalid", });
    testVectors.put(new String[] { "an84characterslonghumanreadablepartthatcontainsthenumber1andtheexcludedcharactersbio", "an84characterslonghumanreadablepartthatcontainsthenumber1andtheexcludedcharactersbio1569pvx", }, new String[] { "com.github.btclib.DecodingException: address length invalid", });
    testVectors.put(new String[] { "pzry9x0s0muk", "pzry9x0s0muk", }, new String[] { "com.github.btclib.DecodingException: separator location invalid", });
    testVectors.put(new String[] { "", "1pzry9x0s0muk", }, new String[] { "com.github.btclib.DecodingException: separator location invalid", });
    testVectors.put(new String[] { "x", "x1b4n0q5v", }, new String[] { "com.github.btclib.DecodingException: data element not in Bech32 character set", });
    testVectors.put(new String[] { "li", "li1dgmt3", }, new String[] { "com.github.btclib.DecodingException: separator location invalid", });
    testVectors.put(new String[] { "de", "de1lg7wt\u00ff", }, new String[] { "com.github.btclib.DecodingException: input element value invalid", });
    testVectors.put(new String[] { "a", "A1G7SGD8", }, new String[] { "com.github.btclib.DecodingException: checksum invalid", });
    testVectors.put(new String[] { "", "10a06t8", }, new String[] { "com.github.btclib.DecodingException: address length invalid", });
    testVectors.put(new String[] { "", "1qzzfhee", }, new String[] { "com.github.btclib.DecodingException: separator location invalid", });
    testVectors.put(new String[] { "bc", "BC1QW508D6QEJXTDG4Y5R3ZARVARY0C5XW7KV8F3T4", }, new String[] { "00", "751e76e8199196d454941c45d1b3a323f1433bd6", "BECH32", "0014751e76e8199196d454941c45d1b3a323f1433bd6", });
    testVectors.put(new String[] { "tb", "tb1qrp33g0q5c5txsp9arysrx4k6zdkfs4nce4xj0gdcccefvpysxf3q0sl5k7", }, new String[] { "0", "1863143c14c5166804bd19203356da136c985678cd4d27a1b8c6329604903262", "BECH32", "00201863143c14c5166804bd19203356da136c985678cd4d27a1b8c6329604903262", });
    testVectors.put(new String[] { "bc", "bc1pw508d6qejxtdg4y5r3zarvary0c5xw7kw508d6qejxtdg4y5r3zarvary0c5xw7k7grplx", }, new String[] { "com.github.btclib.DecodingException: bech32 variant invalid", });
    testVectors.put(new String[] { "bc", "BC1SW50QA3JX3S", }, new String[] { "com.github.btclib.DecodingException: bech32 variant invalid", });
    testVectors.put(new String[] { "bc", "bc1zw508d6qejxtdg4y5r3zarvaryvg6kdaj", }, new String[] { "com.github.btclib.DecodingException: bech32 variant invalid", });
    testVectors.put(new String[] { "tb", "tb1qqqqqp399et2xygdj5xreqhjjvcmzhxw4aywxecjdzew6hylgvsesrxh6hy", }, new String[] { "0", "000000c4a5cad46221b2a187905e5266362b99d5e91c6ce24d165dab93e86433", "BECH32", "0020000000c4a5cad46221b2a187905e5266362b99d5e91c6ce24d165dab93e86433", });
    testVectors.put(new String[] { "tb", "tc1qw508d6qejxtdg4y5r3zarvary0c5xw7kg3g4ty", }, new String[] { "com.github.btclib.DecodingException: expectedHumanReadablePart does not match decoded value", });
    testVectors.put(new String[] { "bc", "bc1qw508d6qejxtdg4y5r3zarvary0c5xw7kv8f3t5", }, new String[] { "com.github.btclib.DecodingException: checksum invalid", });
    testVectors.put(new String[] { "bc", "BC13W508D6QEJXTDG4Y5R3ZARVARY0C5XW7KN40WF2", }, new String[] { "com.github.btclib.DecodingException: decoded version invalid", });
    testVectors.put(new String[] { "bc", "bc1rw5uspcuh", }, new String[] { "com.github.btclib.DecodingException: decoded data length invalid", });
    testVectors.put(new String[] { "bc", "bc10w508d6qejxtdg4y5r3zarvary0c5xw7kw508d6qejxtdg4y5r3zarvary0c5xw7kw5rljs90", }, new String[] { "com.github.btclib.DecodingException: decoded data length invalid", });
    testVectors.put(new String[] { "bc", "BC1QR508D6QEJXTDG4Y5R3ZARVARYV98GJ9P", }, new String[] { "com.github.btclib.DecodingException: decoded program length invalid for version 0", });
    testVectors.put(new String[] { "tb", "tb1qrp33g0q5c5txsp9arysrx4k6zdkfs4nce4xj0gdcccefvpysxf3q0sL5k7", }, new String[] { "com.github.btclib.DecodingException: input is mixed case", });
    testVectors.put(new String[] { "bc", "bc1zw508d6qejxtdg4y5r3zarvaryvqyzf3du", }, new String[] { "com.github.btclib.DecodingException: bech32 variant invalid", });
    testVectors.put(new String[] { "tb", "tb1qrp33g0q5c5txsp9arysrx4k6zdkfs4nce4xj0gdcccefvpysxf3pjxtptv", }, new String[] { "com.github.btclib.DecodingException: invalid padding non-zero bits", });
    testVectors.put(new String[] { "bc", "bc1gmk9yu", }, new String[] { "com.github.btclib.DecodingException: decoded data length invalid", });
    // https://github.com/bitcoin/bips/blob/master/bip-0350.mediawiki
    testVectors.put(new String[] { "a", "A1LQFN3A", }, new String[] { "com.github.btclib.DecodingException: decoded data length invalid", });
    testVectors.put(new String[] { "a", "a1lqfn3a", }, new String[] { "com.github.btclib.DecodingException: decoded data length invalid", });
    testVectors.put(new String[] { "an83characterlonghumanreadablepartthatcontainsthetheexcludedcharactersbioandnumber1", "an83characterlonghumanreadablepartthatcontainsthetheexcludedcharactersbioandnumber11sg7hg6", }, new String[] { "com.github.btclib.DecodingException: decoded data length invalid", });
    testVectors.put(new String[] { "abcdef", "abcdef1l7aum6echk45nj3s0wdvt2fg8x9yrzpqzd3ryx", }, new String[] { "com.github.btclib.DecodingException: decoded version invalid", });
    testVectors.put(new String[] { "1", "11llllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllludsr8", }, new String[] { "com.github.btclib.DecodingException: decoded data length invalid", });
    testVectors.put(new String[] { "split", "split1checkupstagehandshakeupstreamerranterredcaperredlc445v", }, new String[] { "com.github.btclib.DecodingException: decoded version invalid", });
    testVectors.put(new String[] { "?", "?1v759aa", }, new String[] { "com.github.btclib.DecodingException: decoded data length invalid", });
    testVectors.put(new String[] { "\u0020", "\u00201xj0phk", }, new String[] { "com.github.btclib.DecodingException: input element value invalid", });
    testVectors.put(new String[] { "\u007f", "\u007f1g6xzxy", }, new String[] { "com.github.btclib.DecodingException: input element value invalid", });
    testVectors.put(new String[] { "\u0080", "\u00801vctc34", }, new String[] { "com.github.btclib.DecodingException: input element value invalid", });
    testVectors.put(new String[] { "an84characterslonghumanreadablepartthatcontainsthetheexcludedcharactersbioandnumber1", "an84characterslonghumanreadablepartthatcontainsthetheexcludedcharactersbioandnumber11d6pts4", }, new String[] { "com.github.btclib.DecodingException: address length invalid", });
    testVectors.put(new String[] { "qyrz8wqd2c9m", "qyrz8wqd2c9m", }, new String[] { "com.github.btclib.DecodingException: separator location invalid", });
    testVectors.put(new String[] { "", "1qyrz8wqd2c9m", }, new String[] { "com.github.btclib.DecodingException: separator location invalid", });
    testVectors.put(new String[] { "y", "y1b0jsk6g", }, new String[] { "com.github.btclib.DecodingException: data element not in Bech32 character set", });
    testVectors.put(new String[] { "lt", "lt1igcx5c0", }, new String[] { "com.github.btclib.DecodingException: data element not in Bech32 character set", });
    testVectors.put(new String[] { "in", "in1muywd", }, new String[] { "com.github.btclib.DecodingException: separator location invalid", });
    testVectors.put(new String[] { "mm", "mm1crxm3i", }, new String[] { "com.github.btclib.DecodingException: checksum element not in Bech32 character set", });
    testVectors.put(new String[] { "au", "au1s5cgom", }, new String[] { "com.github.btclib.DecodingException: checksum element not in Bech32 character set", });
    testVectors.put(new String[] { "m", "M1VUXWEZ", }, new String[] { "com.github.btclib.DecodingException: checksum invalid", });
    testVectors.put(new String[] { "", "16plkw9", }, new String[] { "com.github.btclib.DecodingException: address length invalid", });
    testVectors.put(new String[] { "", "1p2gdwpf", }, new String[] { "com.github.btclib.DecodingException: separator location invalid", });
    testVectors.put(new String[] { "bc", "BC1QW508D6QEJXTDG4Y5R3ZARVARY0C5XW7KV8F3T4", }, new String[] { "0", "751e76e8199196d454941c45d1b3a323f1433bd6", "BECH32", "0014751e76e8199196d454941c45d1b3a323f1433bd6", });
    testVectors.put(new String[] { "tb", "tb1qrp33g0q5c5txsp9arysrx4k6zdkfs4nce4xj0gdcccefvpysxf3q0sl5k7", }, new String[] { "0", "1863143c14c5166804bd19203356da136c985678cd4d27a1b8c6329604903262", "BECH32", "00201863143c14c5166804bd19203356da136c985678cd4d27a1b8c6329604903262", });
    testVectors.put(new String[] { "bc", "bc1pw508d6qejxtdg4y5r3zarvary0c5xw7kw508d6qejxtdg4y5r3zarvary0c5xw7kt5nd6y", }, new String[] { "1", "751e76e8199196d454941c45d1b3a323f1433bd6751e76e8199196d454941c45d1b3a323f1433bd6", "BECH32M", "5128751e76e8199196d454941c45d1b3a323f1433bd6751e76e8199196d454941c45d1b3a323f1433bd6", });
    testVectors.put(new String[] { "bc", "BC1SW50QGDZ25J", }, new String[] { "16", "751e", "BECH32M", "6002751e", });
    testVectors.put(new String[] { "bc", "bc1zw508d6qejxtdg4y5r3zarvaryvaxxpcs", }, new String[] { "2", "751e76e8199196d454941c45d1b3a323", "BECH32M", "5210751e76e8199196d454941c45d1b3a323", });
    testVectors.put(new String[] { "tb", "tb1qqqqqp399et2xygdj5xreqhjjvcmzhxw4aywxecjdzew6hylgvsesrxh6hy", }, new String[] { "0", "000000c4a5cad46221b2a187905e5266362b99d5e91c6ce24d165dab93e86433", "BECH32", "0020000000c4a5cad46221b2a187905e5266362b99d5e91c6ce24d165dab93e86433", });
    testVectors.put(new String[] { "tb", "tb1pqqqqp399et2xygdj5xreqhjjvcmzhxw4aywxecjdzew6hylgvsesf3hn0c", }, new String[] { "1", "000000c4a5cad46221b2a187905e5266362b99d5e91c6ce24d165dab93e86433", "BECH32M", "5120000000c4a5cad46221b2a187905e5266362b99d5e91c6ce24d165dab93e86433", });
    testVectors.put(new String[] { "bc", "bc1p0xlxvlhemja6c4dqv22uapctqupfhlxm9h8z3k2e72q4k9hcz7vqzk5jj0", }, new String[] { "1", "79be667ef9dcbbac55a06295ce870b07029bfcdb2dce28d959f2815b16f81798", "BECH32M", "512079be667ef9dcbbac55a06295ce870b07029bfcdb2dce28d959f2815b16f81798", });
    testVectors.put(new String[] { "tb", "tc1p0xlxvlhemja6c4dqv22uapctqupfhlxm9h8z3k2e72q4k9hcz7vq5zuyut", }, new String[] { "com.github.btclib.DecodingException: expectedHumanReadablePart does not match decoded value", });
    testVectors.put(new String[] { "bc", "bc1p0xlxvlhemja6c4dqv22uapctqupfhlxm9h8z3k2e72q4k9hcz7vqh2y7hd", }, new String[] { "com.github.btclib.DecodingException: bech32 variant invalid", });
    testVectors.put(new String[] { "tb", "tb1z0xlxvlhemja6c4dqv22uapctqupfhlxm9h8z3k2e72q4k9hcz7vqglt7rf", }, new String[] { "com.github.btclib.DecodingException: bech32 variant invalid", });
    testVectors.put(new String[] { "bc", "BC1S0XLXVLHEMJA6C4DQV22UAPCTQUPFHLXM9H8Z3K2E72Q4K9HCZ7VQ54WELL", }, new String[] { "com.github.btclib.DecodingException: bech32 variant invalid", });
    testVectors.put(new String[] { "bc", "bc1qw508d6qejxtdg4y5r3zarvary0c5xw7kemeawh", }, new String[] { "com.github.btclib.DecodingException: bech32 variant invalid", });
    testVectors.put(new String[] { "tb", "tb1q0xlxvlhemja6c4dqv22uapctqupfhlxm9h8z3k2e72q4k9hcz7vq24jc47", }, new String[] { "com.github.btclib.DecodingException: bech32 variant invalid", });
    testVectors.put(new String[] { "bc", "bc1p38j9r5y49hruaue7wxjce0updqjuyyx0kh56v8s25huc6995vvpql3jow4", }, new String[] { "com.github.btclib.DecodingException: checksum element not in Bech32 character set", });
    testVectors.put(new String[] { "bc", "BC130XLXVLHEMJA6C4DQV22UAPCTQUPFHLXM9H8Z3K2E72Q4K9HCZ7VQ7ZWS8R", }, new String[] { "com.github.btclib.DecodingException: decoded version invalid", });
    testVectors.put(new String[] { "bc", "bc1pw5dgrnzv", }, new String[] { "com.github.btclib.DecodingException: decoded data length invalid", });
    testVectors.put(new String[] { "bc", "bc1p0xlxvlhemja6c4dqv22uapctqupfhlxm9h8z3k2e72q4k9hcz7v8n0nx0muaewav253zgeav", }, new String[] { "com.github.btclib.DecodingException: decoded data length invalid", });
    testVectors.put(new String[] { "bc", "BC1QR508D6QEJXTDG4Y5R3ZARVARYV98GJ9P", }, new String[] { "com.github.btclib.DecodingException: decoded program length invalid for version 0", });
    testVectors.put(new String[] { "tb", "tb1p0xlxvlhemja6c4dqv22uapctqupfhlxm9h8z3k2e72q4k9hcz7vq47Zagq", }, new String[] { "com.github.btclib.DecodingException: input is mixed case", });
    testVectors.put(new String[] { "bc", "bc1p0xlxvlhemja6c4dqv22uapctqupfhlxm9h8z3k2e72q4k9hcz7v07qwwzcrf", }, new String[] { "com.github.btclib.DecodingException: invalid padding too many bits", });
    testVectors.put(new String[] { "tb", "tb1p0xlxvlhemja6c4dqv22uapctqupfhlxm9h8z3k2e72q4k9hcz7vpggkg4j", }, new String[] { "com.github.btclib.DecodingException: invalid padding non-zero bits", });
    testVectors.put(new String[] { "bc", "bc1gmk9yu", }, new String[] { "com.github.btclib.DecodingException: decoded data length invalid", });
    // https://github.com/bitcoin/bips/blob/master/bip-0084.mediawiki
    testVectors.put(new String[] { "bc", "bc1qcr8te4kr609gcawutmrza0j4xv80jy8z306fyu", }, new String[] { "0", "c0cebcd6c3d3ca8c75dc5ec62ebe55330ef910e2", "BECH32", "0014c0cebcd6c3d3ca8c75dc5ec62ebe55330ef910e2", });
    testVectors.put(new String[] { "bc", "bc1qnjg0jd8228aq7egyzacy8cys3knf9xvrerkf9g", }, new String[] { "0", "9c90f934ea51fa0f6504177043e0908da6929983", "BECH32", "00149c90f934ea51fa0f6504177043e0908da6929983", });
    testVectors.put(new String[] { "bc", "bc1q8c6fshw2dlwun7ekn9qwf37cu2rn755upcp6el", }, new String[] { "0", "3e34985dca6fddc9fb369940e4c7d8e2873f529c", "BECH32", "00143e34985dca6fddc9fb369940e4c7d8e2873f529c", });
    // https://github.com/bitcoin/bips/blob/master/bip-0086.mediawiki
    testVectors.put(new String[] { "bc", "bc1p5cyxnuxmeuwuvkwfem96lqzszd02n6xdcjrs20cac6yqjjwudpxqkedrcr", }, new String[] { "1", "a60869f0dbcf1dc659c9cecbaf8050135ea9e8cdc487053f1dc6880949dc684c", "BECH32M", "5120a60869f0dbcf1dc659c9cecbaf8050135ea9e8cdc487053f1dc6880949dc684c", });
    testVectors.put(new String[] { "bc", "bc1p4qhjn9zdvkux4e44uhx8tc55attvtyu358kutcqkudyccelu0was9fqzwh", }, new String[] { "1", "a82f29944d65b86ae6b5e5cc75e294ead6c59391a1edc5e016e3498c67fc7bbb", "BECH32M", "5120a82f29944d65b86ae6b5e5cc75e294ead6c59391a1edc5e016e3498c67fc7bbb", });
    testVectors.put(new String[] { "bc", "bc1p3qkhfews2uk44qtvauqyr2ttdsw7svhkl9nkm9s9c3x4ax5h60wqwruhk7", }, new String[] { "1", "882d74e5d0572d5a816cef0041a96b6c1de832f6f9676d9605c44d5e9a97d3dc", "BECH32M", "5120882d74e5d0572d5a816cef0041a96b6c1de832f6f9676d9605c44d5e9a97d3dc", });
    // https://raw.githubusercontent.com/bitcoin/bips/master/bip-0341/wallet-test-vectors.json
    testVectors.put(new String[] { "bc", "bc1p2wsldez5mud2yam29q22wgfh9439spgduvct83k3pm50fcxa5dps59h4z5", }, new String[] { "1", "53a1f6e454df1aa2776a2814a721372d6258050de330b3c6d10ee8f4e0dda343", "BECH32M", "512053a1f6e454df1aa2776a2814a721372d6258050de330b3c6d10ee8f4e0dda343", });
    testVectors.put(new String[] { "bc", "bc1pz37fc4cn9ah8anwm4xqqhvxygjf9rjf2resrw8h8w4tmvcs0863sa2e586", }, new String[] { "1", "147c9c57132f6e7ecddba9800bb0c4449251c92a1e60371ee77557b6620f3ea3", "BECH32M", "5120147c9c57132f6e7ecddba9800bb0c4449251c92a1e60371ee77557b6620f3ea3", });
    testVectors.put(new String[] { "bc", "bc1punvppl2stp38f7kwv2u2spltjuvuaayuqsthe34hd2dyy5w4g58qqfuag5", }, new String[] { "1", "e4d810fd50586274face62b8a807eb9719cef49c04177cc6b76a9a4251d5450e", "BECH32M", "5120e4d810fd50586274face62b8a807eb9719cef49c04177cc6b76a9a4251d5450e", });
    testVectors.put(new String[] { "bc", "bc1pwyjywgrd0ffr3tx8laflh6228dj98xkjj8rum0zfpd6h0e930h6saqxrrm", }, new String[] { "1", "712447206d7a5238acc7ff53fbe94a3b64539ad291c7cdbc490b7577e4b17df5", "BECH32M", "5120712447206d7a5238acc7ff53fbe94a3b64539ad291c7cdbc490b7577e4b17df5", });
    testVectors.put(new String[] { "bc", "bc1pwl3s54fzmk0cjnpl3w9af39je7pv5ldg504x5guk2hpecpg2kgsqaqstjq", }, new String[] { "1", "77e30a5522dd9f894c3f8b8bd4c4b2cf82ca7da8a3ea6a239655c39c050ab220", "BECH32M", "512077e30a5522dd9f894c3f8b8bd4c4b2cf82ca7da8a3ea6a239655c39c050ab220", });
    testVectors.put(new String[] { "bc", "bc1pjxmy65eywgafs5tsunw95ruycpqcqnev6ynxp7jaasylcgtcxczs6n332e", }, new String[] { "1", "91b64d5324723a985170e4dc5a0f84c041804f2cd12660fa5dec09fc21783605", "BECH32M", "512091b64d5324723a985170e4dc5a0f84c041804f2cd12660fa5dec09fc21783605", });
    testVectors.put(new String[] { "bc", "bc1pw5tf7sqp4f50zka7629jrr036znzew70zxyvvej3zrpf8jg8hqcssyuewe", }, new String[] { "1", "75169f4001aa68f15bbed28b218df1d0a62cbbcf1188c6665110c293c907b831", "BECH32M", "512075169f4001aa68f15bbed28b218df1d0a62cbbcf1188c6665110c293c907b831", });
    //
    testVectors.put(new String[] { "bc", "bc1q6u2vmx3gr9a85ewve4m6yzfcr98acpv3qyz5txhx49c4jxxxcsyqy9crte", }, new String[] { "0", "d714cd9a28197a7a65cccd77a20938194fdc05910105459ae6a9715918c6c408", "BECH32", "0020d714cd9a28197a7a65cccd77a20938194fdc05910105459ae6a9715918c6c408", });
    testVectors.put(new String[] { "bc", "bc1qx3x2gp02gsdftmlfq0x8xfd2qvplpguc4uu5kqv5jchn47l8e68qrgyhzz", }, new String[] { "0", "344ca405ea441a95efe903cc7325aa0303f0a398af394b0194962f3afbe7ce8e", "BECH32", "0020344ca405ea441a95efe903cc7325aa0303f0a398af394b0194962f3afbe7ce8e", });
    //
    for (final var entry : testVectors.entrySet()) {
      final String help = entry.getKey()[0] + "," + entry.getKey()[1];
      final String hrp = (entry.getKey()[0] == null) ? null : entry.getKey()[0];
      try {
        final var result = SegwitAddress.of(hrp, entry.getKey()[1]);
        final var version = Integer.parseInt(entry.getValue()[0], 10);
        final var program = Util.fromHexString(entry.getValue()[1]);
        final var variant = Bech32.Variant.valueOf(entry.getValue()[2]);
        final var outputScript = Util.fromHexString(entry.getValue()[3]);
        Assert.assertEquals(help, version, result.getVersion());
        Assert.assertArrayEquals(help, program, result.getProgram());
        Assert.assertEquals(help, variant, result.getVariant());
        Assert.assertArrayEquals(help, outputScript, result.toOutputScript());
      } catch (final Exception e) {
        Assert.assertEquals(help, entry.getValue()[0].toString(), e.toString());
      }
    }
  }

  @Test
  public void test_encode() {
    final var testVectors = new LinkedHashMap<String[], String[]>();
    // precondition tests
    testVectors.put(new String[] { null, Integer.toString(Integer.MAX_VALUE, 10), null, }, new String[] { "java.lang.NullPointerException: humanReadablePart must not be null", });
    testVectors.put(new String[] { "", Integer.toString(Integer.MAX_VALUE, 10), null, }, new String[] { "java.lang.NullPointerException: program must not be null", });
    testVectors.put(new String[] { Util.multiply("a", Bech32.MIN_HRP_LENGTH - 1), "-1", "", }, new String[] { "java.lang.IllegalArgumentException: humanReadablePart length invalid", });
    testVectors.put(new String[] { Util.multiply("a", Bech32.MAX_HRP_LENGTH + 1), "-1", "", }, new String[] { "java.lang.IllegalArgumentException: humanReadablePart length invalid", });
    testVectors.put(new String[] { "a", "-1", "", }, new String[] { "java.lang.IllegalArgumentException: version invalid", });
    testVectors.put(new String[] { "a", "17", "", }, new String[] { "java.lang.IllegalArgumentException: version invalid", });
    testVectors.put(new String[] { "a", Integer.toString(Integer.MAX_VALUE, 10), "", }, new String[] { "java.lang.IllegalArgumentException: version invalid", });
    testVectors.put(new String[] { "a", Integer.toString(Integer.MIN_VALUE, 10), "", }, new String[] { "java.lang.IllegalArgumentException: version invalid", });
    testVectors.put(new String[] { "a", "0", "", }, new String[] { "java.lang.IllegalArgumentException: program length invalid", });
    testVectors.put(new String[] { "a", "0", "00", }, new String[] { "java.lang.IllegalArgumentException: program length invalid", });
    testVectors.put(new String[] { "a", "0", Util.multiply("00", 41), }, new String[] { "java.lang.IllegalArgumentException: program length invalid", });
    testVectors.put(new String[] { "a", "0", "0000", }, new String[] { "java.lang.IllegalArgumentException: program length invalid for version 0", });
    testVectors.put(new String[] { Util.multiply("a", Bech32.MAX_HRP_LENGTH), "1", "0000", }, new String[] { "java.lang.IllegalArgumentException: humanReadablePart length invalid", });
    testVectors.put(new String[] { Util.multiply("a", (SegwitAddress.MAX_LENGTH - Bech32.CHECKSUM_LENGTH - 5 - Bech32.SEPARATOR_LENGTH) + 1), "1", Util.multiply("00", 2), }, new String[] { "java.lang.IllegalArgumentException: humanReadablePart length invalid", });
    testVectors.put(new String[] { Util.multiply("a", (SegwitAddress.MAX_LENGTH - Bech32.CHECKSUM_LENGTH - 65 - Bech32.SEPARATOR_LENGTH) + 1), "1", Util.multiply("00", 40), }, new String[] { "java.lang.IllegalArgumentException: humanReadablePart length invalid", });
    testVectors.put(new String[] { Util.multiply("a", (SegwitAddress.MAX_LENGTH - Bech32.CHECKSUM_LENGTH - 33 - Bech32.SEPARATOR_LENGTH) + 1), "0", Util.multiply("00", 20), }, new String[] { "java.lang.IllegalArgumentException: humanReadablePart length invalid", });
    testVectors.put(new String[] { Util.multiply("a", (SegwitAddress.MAX_LENGTH - Bech32.CHECKSUM_LENGTH - 53 - Bech32.SEPARATOR_LENGTH) + 1), "0", Util.multiply("00", 32), }, new String[] { "java.lang.IllegalArgumentException: humanReadablePart length invalid", });
    testVectors.put(new String[] { "", "1", "0000", }, new String[] { "java.lang.IllegalArgumentException: humanReadablePart length invalid", });
    testVectors.put(new String[] { Util.multiply("a", Bech32.MAX_HRP_LENGTH + 1), "1", "0000", }, new String[] { "java.lang.IllegalArgumentException: humanReadablePart length invalid", });
    testVectors.put(new String[] { new StringBuilder().appendCodePoint(32).toString(), "1", "0000", }, new String[] { "java.lang.IllegalArgumentException: humanReadablePart element value invalid", });
    testVectors.put(new String[] { new StringBuilder().appendCodePoint(127).toString(), "1", "0000", }, new String[] { "java.lang.IllegalArgumentException: humanReadablePart element value invalid", });
    testVectors.put(new String[] { "A", "1", "0000", }, new String[] { "java.lang.IllegalArgumentException: humanReadablePart element value invalid", });
    testVectors.put(new String[] { "Z", "1", "0000", }, new String[] { "java.lang.IllegalArgumentException: humanReadablePart element value invalid", });
    testVectors.put(new String[] { Util.multiply("a", Bech32.MAX_HRP_LENGTH), "1", Util.multiply("00", 40), }, new String[] { "java.lang.IllegalArgumentException: humanReadablePart length invalid", });
    //
    testVectors.put(new String[] { Util.multiply("a", 78), "1", Util.multiply("00", 2), }, new String[] { "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa1pqqqq4mgrs4", "51020000", "BECH32M", });
    testVectors.put(new String[] { Util.multiply("a", 18), "1", Util.multiply("00", 40), }, new String[] { "aaaaaaaaaaaaaaaaaa1pqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqayew88", "512800000000000000000000000000000000000000000000000000000000000000000000000000000000", "BECH32M", });
    testVectors.put(new String[] { Util.multiply("a", 50), "0", Util.multiply("00", 20), }, new String[] { "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa1qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqq5fcruz", "00140000000000000000000000000000000000000000", "BECH32", });
    testVectors.put(new String[] { Util.multiply("a", 30), "0", Util.multiply("00", 32), }, new String[] { "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaa1qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqq6nf3d", "00200000000000000000000000000000000000000000000000000000000000000000", "BECH32", });
    //
    testVectors.put(new String[] { "bc", "0", Util.multiply("00", 32), }, new String[] { "bc1qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqthqst8", "00200000000000000000000000000000000000000000000000000000000000000000", "BECH32", });
    testVectors.put(new String[] { "bc", "1", Util.multiply("00", 32), }, new String[] { "bc1pqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqpqqenm", "51200000000000000000000000000000000000000000000000000000000000000000", "BECH32M", });
    testVectors.put(new String[] { "bc", "2", Util.multiply("00", 32), }, new String[] { "bc1zqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqfaekas", "52200000000000000000000000000000000000000000000000000000000000000000", "BECH32M", });
    testVectors.put(new String[] { "bc", "3", Util.multiply("00", 32), }, new String[] { "bc1rqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqkkfnqw", "53200000000000000000000000000000000000000000000000000000000000000000", "BECH32M", });
    testVectors.put(new String[] { "bc", "4", Util.multiply("00", 32), }, new String[] { "bc1yqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqewzgpx", "54200000000000000000000000000000000000000000000000000000000000000000", "BECH32M", });
    testVectors.put(new String[] { "bc", "5", Util.multiply("00", 32), }, new String[] { "bc19qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqx9jduc", "55200000000000000000000000000000000000000000000000000000000000000000", "BECH32M", });
    testVectors.put(new String[] { "bc", "6", Util.multiply("00", 32), }, new String[] { "bc1xqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqwctzjn", "56200000000000000000000000000000000000000000000000000000000000000000", "BECH32M", });
    testVectors.put(new String[] { "bc", "7", Util.multiply("00", 32), }, new String[] { "bc18qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqq3nm80d", "57200000000000000000000000000000000000000000000000000000000000000000", "BECH32M", });
    testVectors.put(new String[] { "bc", "8", Util.multiply("00", 32), }, new String[] { "bc1gqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqspaasr", "58200000000000000000000000000000000000000000000000000000000000000000", "BECH32M", });
    testVectors.put(new String[] { "bc", "9", Util.multiply("00", 32), }, new String[] { "bc1fqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqq02dcda", "59200000000000000000000000000000000000000000000000000000000000000000", "BECH32M", });
    testVectors.put(new String[] { "bc", "10", Util.multiply("00", 32), }, new String[] { "bc12qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqq8h5hrk", "5a200000000000000000000000000000000000000000000000000000000000000000", "BECH32M", });
    testVectors.put(new String[] { "bc", "11", Util.multiply("00", 32), }, new String[] { "bc1tqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqcuyj7g", "5b200000000000000000000000000000000000000000000000000000000000000000", "BECH32M", });
    testVectors.put(new String[] { "bc", "12", Util.multiply("00", 32), }, new String[] { "bc1vqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqhy0flq", "5c200000000000000000000000000000000000000000000000000000000000000000", "BECH32M", });
    testVectors.put(new String[] { "bc", "13", Util.multiply("00", 32), }, new String[] { "bc1dqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqg0lvz7", "5d200000000000000000000000000000000000000000000000000000000000000000", "BECH32M", });
    testVectors.put(new String[] { "bc", "14", Util.multiply("00", 32), }, new String[] { "bc1wqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqjxrv4", "5e200000000000000000000000000000000000000000000000000000000000000000", "BECH32M", });
    testVectors.put(new String[] { "bc", "15", Util.multiply("00", 32), }, new String[] { "bc10qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqlekx3t", "5f200000000000000000000000000000000000000000000000000000000000000000", "BECH32M", });
    testVectors.put(new String[] { "bc", "16", Util.multiply("00", 32), }, new String[] { "bc1sqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqzl27mf", "60200000000000000000000000000000000000000000000000000000000000000000", "BECH32M", });
    //
    testVectors.put(new String[] { "bc", "0", Util.multiply("00", 2), }, new String[] { "java.lang.IllegalArgumentException: program length invalid for version 0", });
    testVectors.put(new String[] { "bc", "1", Util.multiply("00", 2), }, new String[] { "bc1pqqqqqcnjqs", "51020000", "BECH32M", });
    testVectors.put(new String[] { "bc", "2", Util.multiply("00", 2), }, new String[] { "bc1zqqqqvmec0n", "52020000", "BECH32M", });
    testVectors.put(new String[] { "bc", "3", Util.multiply("00", 2), }, new String[] { "bc1rqqqqg6l72j", "53020000", "BECH32M", });
    testVectors.put(new String[] { "bc", "4", Util.multiply("00", 2), }, new String[] { "bc1yqqqq5adv34", "54020000", "BECH32M", });
    testVectors.put(new String[] { "bc", "5", Util.multiply("00", 2), }, new String[] { "bc19qqqqsut255", "55020000", "BECH32M", });
    testVectors.put(new String[] { "bc", "6", Util.multiply("00", 2), }, new String[] { "bc1xqqqqulpqmh", "56020000", "BECH32M", });
    testVectors.put(new String[] { "bc", "7", Util.multiply("00", 2), }, new String[] { "bc18qqqqc78x7k", "57020000", "BECH32M", });
    testVectors.put(new String[] { "bc", "8", Util.multiply("00", 2), }, new String[] { "bc1gqqqqd3vdye", "58020000", "BECH32M", });
    testVectors.put(new String[] { "bc", "9", Util.multiply("00", 2), }, new String[] { "bc1fqqqqfs2tpc", "59020000", "BECH32M", });
    testVectors.put(new String[] { "bc", "10", Util.multiply("00", 2), }, new String[] { "bc12qqqq9nqpwm", "5a020000", "BECH32M", });
    testVectors.put(new String[] { "bc", "11", Util.multiply("00", 2), }, new String[] { "bc1tqqqqpjx8t6", "5b020000", "BECH32M", });
    testVectors.put(new String[] { "bc", "12", Util.multiply("00", 2), }, new String[] { "bc1vqqqqa454sa", "5c020000", "BECH32M", });
    testVectors.put(new String[] { "bc", "13", Util.multiply("00", 2), }, new String[] { "bc1dqqqqe5jn4u", "5d020000", "BECH32M", });
    testVectors.put(new String[] { "bc", "14", Util.multiply("00", 2), }, new String[] { "bc1wqqqq4hce6l", "5e020000", "BECH32M", });
    testVectors.put(new String[] { "bc", "15", Util.multiply("00", 2), }, new String[] { "bc10qqqq3k7ll7", "5f020000", "BECH32M", });
    testVectors.put(new String[] { "bc", "16", Util.multiply("00", 2), }, new String[] { "bc1sqqqqkfw08p", "60020000", "BECH32M", });
    //
    testVectors.put(new String[] { "bc", "0", "751e76e8199196d454941c45d1b3a323f1433bd6", }, new String[] { "bc1qw508d6qejxtdg4y5r3zarvary0c5xw7kv8f3t4", "0014751e76e8199196d454941c45d1b3a323f1433bd6", "BECH32", }); // p2wpkh
    testVectors.put(new String[] { "tb", "0", "751e76e8199196d454941c45d1b3a323f1433bd6", }, new String[] { "tb1qw508d6qejxtdg4y5r3zarvary0c5xw7kxpjzsx", "0014751e76e8199196d454941c45d1b3a323f1433bd6", "BECH32", }); // p2wpkh
    testVectors.put(new String[] { "bcrt", "0", "751e76e8199196d454941c45d1b3a323f1433bd6", }, new String[] { "bcrt1qw508d6qejxtdg4y5r3zarvary0c5xw7kygt080", "0014751e76e8199196d454941c45d1b3a323f1433bd6", "BECH32", }); // p2wpkh
    //
    testVectors.put(new String[] { "bc", "0", "1863143c14c5166804bd19203356da136c985678cd4d27a1b8c6329604903262", }, new String[] { "bc1qrp33g0q5c5txsp9arysrx4k6zdkfs4nce4xj0gdcccefvpysxf3qccfmv3", "00201863143c14c5166804bd19203356da136c985678cd4d27a1b8c6329604903262", "BECH32", }); // p2wsh
    testVectors.put(new String[] { "tb", "0", "1863143c14c5166804bd19203356da136c985678cd4d27a1b8c6329604903262", }, new String[] { "tb1qrp33g0q5c5txsp9arysrx4k6zdkfs4nce4xj0gdcccefvpysxf3q0sl5k7", "00201863143c14c5166804bd19203356da136c985678cd4d27a1b8c6329604903262", "BECH32", }); // p2wsh
    testVectors.put(new String[] { "bcrt", "0", "1863143c14c5166804bd19203356da136c985678cd4d27a1b8c6329604903262", }, new String[] { "bcrt1qrp33g0q5c5txsp9arysrx4k6zdkfs4nce4xj0gdcccefvpysxf3qzf4jry", "00201863143c14c5166804bd19203356da136c985678cd4d27a1b8c6329604903262", "BECH32", }); // p2wsh
    // https://github.com/bitcoin/bips/blob/master/bip-0086.mediawiki
    testVectors.put(new String[] { "bc", "1", "a60869f0dbcf1dc659c9cecbaf8050135ea9e8cdc487053f1dc6880949dc684c", }, new String[] { "bc1p5cyxnuxmeuwuvkwfem96lqzszd02n6xdcjrs20cac6yqjjwudpxqkedrcr", "5120a60869f0dbcf1dc659c9cecbaf8050135ea9e8cdc487053f1dc6880949dc684c", "BECH32M", }); // p2tr
    testVectors.put(new String[] { "tb", "1", "a60869f0dbcf1dc659c9cecbaf8050135ea9e8cdc487053f1dc6880949dc684c", }, new String[] { "tb1p5cyxnuxmeuwuvkwfem96lqzszd02n6xdcjrs20cac6yqjjwudpxqp3mvzv", "5120a60869f0dbcf1dc659c9cecbaf8050135ea9e8cdc487053f1dc6880949dc684c", "BECH32M", }); // p2tr
    testVectors.put(new String[] { "bcrt", "1", "a60869f0dbcf1dc659c9cecbaf8050135ea9e8cdc487053f1dc6880949dc684c", }, new String[] { "bcrt1p5cyxnuxmeuwuvkwfem96lqzszd02n6xdcjrs20cac6yqjjwudpxqvg32hk", "5120a60869f0dbcf1dc659c9cecbaf8050135ea9e8cdc487053f1dc6880949dc684c", "BECH32M", }); // p2tr
    // https://github.com/bitcoin/bips/blob/master/bip-0173.mediawiki
    testVectors.put(new String[] { "bc", "0", "751e76e8199196d454941c45d1b3a323f1433bd6", }, new String[] { "bc1qw508d6qejxtdg4y5r3zarvary0c5xw7kv8f3t4", "0014751e76e8199196d454941c45d1b3a323f1433bd6", "BECH32", });
    testVectors.put(new String[] { "tb", "0", "751e76e8199196d454941c45d1b3a323f1433bd6", }, new String[] { "tb1qw508d6qejxtdg4y5r3zarvary0c5xw7kxpjzsx", "0014751e76e8199196d454941c45d1b3a323f1433bd6", "BECH32", });
    testVectors.put(new String[] { "bc", "0", "1863143c14c5166804bd19203356da136c985678cd4d27a1b8c6329604903262", }, new String[] { "bc1qrp33g0q5c5txsp9arysrx4k6zdkfs4nce4xj0gdcccefvpysxf3qccfmv3", "00201863143c14c5166804bd19203356da136c985678cd4d27a1b8c6329604903262", "BECH32", });
    testVectors.put(new String[] { "tb", "0", "1863143c14c5166804bd19203356da136c985678cd4d27a1b8c6329604903262", }, new String[] { "tb1qrp33g0q5c5txsp9arysrx4k6zdkfs4nce4xj0gdcccefvpysxf3q0sl5k7", "00201863143c14c5166804bd19203356da136c985678cd4d27a1b8c6329604903262", "BECH32", });
    testVectors.put(new String[] { "bc", "0", "751e76e8199196d454941c45d1b3a323f1433bd6", }, new String[] { "BC1QW508D6QEJXTDG4Y5R3ZARVARY0C5XW7KV8F3T4".toLowerCase(Locale.ROOT), "0014751e76e8199196d454941c45d1b3a323f1433bd6", "BECH32", });
    testVectors.put(new String[] { "tb", "0", "1863143c14c5166804bd19203356da136c985678cd4d27a1b8c6329604903262", }, new String[] { "tb1qrp33g0q5c5txsp9arysrx4k6zdkfs4nce4xj0gdcccefvpysxf3q0sl5k7", "00201863143c14c5166804bd19203356da136c985678cd4d27a1b8c6329604903262", "BECH32", });
    testVectors.put(new String[] { "bc", "1", "751e76e8199196d454941c45d1b3a323f1433bd6751e76e8199196d454941c45d1b3a323f1433bd6", }, new String[] { "bc1pw508d6qejxtdg4y5r3zarvary0c5xw7kw508d6qejxtdg4y5r3zarvary0c5xw7kt5nd6y", "5128751e76e8199196d454941c45d1b3a323f1433bd6751e76e8199196d454941c45d1b3a323f1433bd6", "BECH32M", }); // replaces bc1pw508d6qejxtdg4y5r3zarvary0c5xw7kw508d6qejxtdg4y5r3zarvary0c5xw7k7grplx
    testVectors.put(new String[] { "bc", "16", "751e", }, new String[] { "bc1sw50qgdz25j", "6002751e", "BECH32M", }); // replaces BC1SW50QA3JX3S
    testVectors.put(new String[] { "bc", "2", "751e76e8199196d454941c45d1b3a323", }, new String[] { "bc1zw508d6qejxtdg4y5r3zarvaryvaxxpcs", "5210751e76e8199196d454941c45d1b3a323", "BECH32M", }); // replaces bc1zw508d6qejxtdg4y5r3zarvaryvg6kdaj
    testVectors.put(new String[] { "tb", "0", "000000c4a5cad46221b2a187905e5266362b99d5e91c6ce24d165dab93e86433", }, new String[] { "tb1qqqqqp399et2xygdj5xreqhjjvcmzhxw4aywxecjdzew6hylgvsesrxh6hy", "0020000000c4a5cad46221b2a187905e5266362b99d5e91c6ce24d165dab93e86433", "BECH32", });
    // https://github.com/bitcoin/bips/blob/master/bip-0350.mediawiki
    testVectors.put(new String[] { "bc", "0", "751e76e8199196d454941c45d1b3a323f1433bd6", }, new String[] { "BC1QW508D6QEJXTDG4Y5R3ZARVARY0C5XW7KV8F3T4".toLowerCase(Locale.ROOT), "0014751e76e8199196d454941c45d1b3a323f1433bd6", "BECH32", });
    testVectors.put(new String[] { "tb", "0", "1863143c14c5166804bd19203356da136c985678cd4d27a1b8c6329604903262", }, new String[] { "tb1qrp33g0q5c5txsp9arysrx4k6zdkfs4nce4xj0gdcccefvpysxf3q0sl5k7", "00201863143c14c5166804bd19203356da136c985678cd4d27a1b8c6329604903262", "BECH32", });
    testVectors.put(new String[] { "bc", "1", "751e76e8199196d454941c45d1b3a323f1433bd6751e76e8199196d454941c45d1b3a323f1433bd6", }, new String[] { "bc1pw508d6qejxtdg4y5r3zarvary0c5xw7kw508d6qejxtdg4y5r3zarvary0c5xw7kt5nd6y", "5128751e76e8199196d454941c45d1b3a323f1433bd6751e76e8199196d454941c45d1b3a323f1433bd6", "BECH32M", });
    testVectors.put(new String[] { "bc", "16", "751e", }, new String[] { "BC1SW50QGDZ25J".toLowerCase(Locale.ROOT), "6002751e", "BECH32M", });
    testVectors.put(new String[] { "bc", "2", "751e76e8199196d454941c45d1b3a323", }, new String[] { "bc1zw508d6qejxtdg4y5r3zarvaryvaxxpcs", "5210751e76e8199196d454941c45d1b3a323", "BECH32M", });
    testVectors.put(new String[] { "tb", "0", "000000c4a5cad46221b2a187905e5266362b99d5e91c6ce24d165dab93e86433", }, new String[] { "tb1qqqqqp399et2xygdj5xreqhjjvcmzhxw4aywxecjdzew6hylgvsesrxh6hy", "0020000000c4a5cad46221b2a187905e5266362b99d5e91c6ce24d165dab93e86433", "BECH32", });
    testVectors.put(new String[] { "bc", "1", "79be667ef9dcbbac55a06295ce870b07029bfcdb2dce28d959f2815b16f81798", }, new String[] { "bc1p0xlxvlhemja6c4dqv22uapctqupfhlxm9h8z3k2e72q4k9hcz7vqzk5jj0", "512079be667ef9dcbbac55a06295ce870b07029bfcdb2dce28d959f2815b16f81798", "BECH32M", });
    // https://github.com/bitcoin/bips/blob/master/bip-0084.mediawiki
    testVectors.put(new String[] { "bc", "0", "c0cebcd6c3d3ca8c75dc5ec62ebe55330ef910e2", }, new String[] { "bc1qcr8te4kr609gcawutmrza0j4xv80jy8z306fyu", "0014c0cebcd6c3d3ca8c75dc5ec62ebe55330ef910e2", "BECH32", }); // hash160(0330d54fd0dd420a6e5f8d3624f5f3482cae350f79d5f0753bf5beef9c2d91af3c) = c0cebcd6c3d3ca8c75dc5ec62ebe55330ef910e2
    testVectors.put(new String[] { "bc", "0", "9c90f934ea51fa0f6504177043e0908da6929983", }, new String[] { "bc1qnjg0jd8228aq7egyzacy8cys3knf9xvrerkf9g", "00149c90f934ea51fa0f6504177043e0908da6929983", "BECH32", });
    testVectors.put(new String[] { "bc", "0", "3e34985dca6fddc9fb369940e4c7d8e2873f529c", }, new String[] { "bc1q8c6fshw2dlwun7ekn9qwf37cu2rn755upcp6el", "00143e34985dca6fddc9fb369940e4c7d8e2873f529c", "BECH32", });
    // https://github.com/bitcoin/bips/blob/master/bip-0086.mediawiki
    testVectors.put(new String[] { "bc", "1", "a60869f0dbcf1dc659c9cecbaf8050135ea9e8cdc487053f1dc6880949dc684c", }, new String[] { "bc1p5cyxnuxmeuwuvkwfem96lqzszd02n6xdcjrs20cac6yqjjwudpxqkedrcr", "5120a60869f0dbcf1dc659c9cecbaf8050135ea9e8cdc487053f1dc6880949dc684c", "BECH32M", });
    testVectors.put(new String[] { "bc", "1", "a82f29944d65b86ae6b5e5cc75e294ead6c59391a1edc5e016e3498c67fc7bbb", }, new String[] { "bc1p4qhjn9zdvkux4e44uhx8tc55attvtyu358kutcqkudyccelu0was9fqzwh", "5120a82f29944d65b86ae6b5e5cc75e294ead6c59391a1edc5e016e3498c67fc7bbb", "BECH32M", });
    testVectors.put(new String[] { "bc", "1", "882d74e5d0572d5a816cef0041a96b6c1de832f6f9676d9605c44d5e9a97d3dc", }, new String[] { "bc1p3qkhfews2uk44qtvauqyr2ttdsw7svhkl9nkm9s9c3x4ax5h60wqwruhk7", "5120882d74e5d0572d5a816cef0041a96b6c1de832f6f9676d9605c44d5e9a97d3dc", "BECH32M", });
    // https://raw.githubusercontent.com/bitcoin/bips/master/bip-0341/wallet-test-vectors.json
    testVectors.put(new String[] { "bc", "1", "53a1f6e454df1aa2776a2814a721372d6258050de330b3c6d10ee8f4e0dda343", }, new String[] { "bc1p2wsldez5mud2yam29q22wgfh9439spgduvct83k3pm50fcxa5dps59h4z5", "512053a1f6e454df1aa2776a2814a721372d6258050de330b3c6d10ee8f4e0dda343", "BECH32M", });
    testVectors.put(new String[] { "bc", "1", "147c9c57132f6e7ecddba9800bb0c4449251c92a1e60371ee77557b6620f3ea3", }, new String[] { "bc1pz37fc4cn9ah8anwm4xqqhvxygjf9rjf2resrw8h8w4tmvcs0863sa2e586", "5120147c9c57132f6e7ecddba9800bb0c4449251c92a1e60371ee77557b6620f3ea3", "BECH32M", });
    testVectors.put(new String[] { "bc", "1", "e4d810fd50586274face62b8a807eb9719cef49c04177cc6b76a9a4251d5450e", }, new String[] { "bc1punvppl2stp38f7kwv2u2spltjuvuaayuqsthe34hd2dyy5w4g58qqfuag5", "5120e4d810fd50586274face62b8a807eb9719cef49c04177cc6b76a9a4251d5450e", "BECH32M", });
    testVectors.put(new String[] { "bc", "1", "712447206d7a5238acc7ff53fbe94a3b64539ad291c7cdbc490b7577e4b17df5", }, new String[] { "bc1pwyjywgrd0ffr3tx8laflh6228dj98xkjj8rum0zfpd6h0e930h6saqxrrm", "5120712447206d7a5238acc7ff53fbe94a3b64539ad291c7cdbc490b7577e4b17df5", "BECH32M", });
    testVectors.put(new String[] { "bc", "1", "77e30a5522dd9f894c3f8b8bd4c4b2cf82ca7da8a3ea6a239655c39c050ab220", }, new String[] { "bc1pwl3s54fzmk0cjnpl3w9af39je7pv5ldg504x5guk2hpecpg2kgsqaqstjq", "512077e30a5522dd9f894c3f8b8bd4c4b2cf82ca7da8a3ea6a239655c39c050ab220", "BECH32M", });
    testVectors.put(new String[] { "bc", "1", "91b64d5324723a985170e4dc5a0f84c041804f2cd12660fa5dec09fc21783605", }, new String[] { "bc1pjxmy65eywgafs5tsunw95ruycpqcqnev6ynxp7jaasylcgtcxczs6n332e", "512091b64d5324723a985170e4dc5a0f84c041804f2cd12660fa5dec09fc21783605", "BECH32M", });
    testVectors.put(new String[] { "bc", "1", "75169f4001aa68f15bbed28b218df1d0a62cbbcf1188c6665110c293c907b831", }, new String[] { "bc1pw5tf7sqp4f50zka7629jrr036znzew70zxyvvej3zrpf8jg8hqcssyuewe", "512075169f4001aa68f15bbed28b218df1d0a62cbbcf1188c6665110c293c907b831", "BECH32M", });
    //
    testVectors.put(new String[] { "bc", "0", "d714cd9a28197a7a65cccd77a20938194fdc05910105459ae6a9715918c6c408", }, new String[] { "bc1q6u2vmx3gr9a85ewve4m6yzfcr98acpv3qyz5txhx49c4jxxxcsyqy9crte", "0020d714cd9a28197a7a65cccd77a20938194fdc05910105459ae6a9715918c6c408", "BECH32", });
    testVectors.put(new String[] { "bc", "0", "344ca405ea441a95efe903cc7325aa0303f0a398af394b0194962f3afbe7ce8e", }, new String[] { "bc1qx3x2gp02gsdftmlfq0x8xfd2qvplpguc4uu5kqv5jchn47l8e68qrgyhzz", "0020344ca405ea441a95efe903cc7325aa0303f0a398af394b0194962f3afbe7ce8e", "BECH32", });
    //
    for (final var entry : testVectors.entrySet()) {
      final String help = entry.getKey()[0] + "," + entry.getKey()[1] + "," + entry.getKey()[2];
      final String hrp = (entry.getKey()[0] == null) ? null : entry.getKey()[0];
      final int version = Integer.parseInt(entry.getKey()[1], 10);
      final byte[] program = (entry.getKey()[2] == null) ? null : Util.fromHexString(entry.getKey()[2]);
      try {
        final var result = SegwitAddress.of(hrp, version, program);
        Assert.assertEquals(help, entry.getValue()[0], result.toString());
        Assert.assertArrayEquals(help, Util.fromHexString(entry.getValue()[1]), result.toOutputScript());
        Assert.assertEquals(help, Bech32.Variant.valueOf(entry.getValue()[2]), result.getVariant());
        Assert.assertEquals(help, version, result.getVersion());
        Assert.assertArrayEquals(help, program, result.getProgram());
        Assert.assertEquals(help, hrp, result.getHumanReadablePart());
      } catch (final Exception e) {
        Assert.assertEquals(help, entry.getValue()[0].toString(), e.toString());
      }
    }
  }
}
