package cc.raynet.worldsharing.utils;

import cc.raynet.worldsharing.WorldsharingAddon;
import io.sentry.Sentry;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class CryptUtils {

    public static byte[] encryptWithPublicKey(byte[] data, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(data);
    }

    public static Cipher createCipher(int mode, SecretKeySpec secretKey, IvParameterSpec ivSpec) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CFB8/NoPadding");
            cipher.init(mode, secretKey, ivSpec);
            return cipher;
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] getServerIdHash(String input, byte[] publicKey) {
        try {
            return digestOperation(input.getBytes("ISO_8859_1"), publicKey);
        } catch (UnsupportedEncodingException e) {
            Sentry.captureException(e);
            WorldsharingAddon.INSTANCE.logger().error("Unsupported encoding", e);
            return null;
        }
    }

    private static byte[] digestOperation(byte[]... bytes) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");

            for (byte[] b : bytes) {
                digest.update(b);
            }

            return digest.digest();
        } catch (NoSuchAlgorithmException e) {
            Sentry.captureException(e);
            WorldsharingAddon.INSTANCE.logger().error("Unsupported algorithm", e);
        }
        return null;
    }

    public static PublicKey decodePKIXPublicKey(byte[] keyBytes, String algorithm) throws NoSuchAlgorithmException, InvalidKeySpecException {
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
        return keyFactory.generatePublic(spec);
    }

    public static PublicKey convertPKCS1ToPublicKey(String pemKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        String[] lines = pemKey.split("\n");
        StringBuilder result = new StringBuilder();
        for (int i = 1; i < lines.length - 1; i++) {
            result.append(lines[i]);
        }

        // Parse the PKCS#1 key
        BigInteger modulus;
        BigInteger exponent;
        try (ASN1InputStream asn1InputStream = new ASN1InputStream(Base64.getDecoder()
                .decode(result.toString().getBytes(StandardCharsets.UTF_8)))) {
            ASN1Primitive primitive = asn1InputStream.readObject();
            ASN1Sequence sequence = (ASN1Sequence) primitive;
            modulus = ((ASN1Integer) sequence.getObjectAt(0)).getValue();
            exponent = ((ASN1Integer) sequence.getObjectAt(1)).getValue();
        } catch (IOException e) {
            throw new InvalidKeySpecException("Invalid PKCS#1 key", e);
        }

        return KeyFactory.getInstance("RSA").generatePublic(new RSAPublicKeySpec(modulus, exponent));
    }
}