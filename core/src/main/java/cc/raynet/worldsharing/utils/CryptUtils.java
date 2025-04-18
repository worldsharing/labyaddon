package cc.raynet.worldsharing.utils;

import cc.raynet.worldsharing.WorldsharingAddon;
import io.sentry.Sentry;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.params.X25519KeyGenerationParameters;
import org.bouncycastle.crypto.params.X25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.X25519PublicKeyParameters;
import org.bouncycastle.math.ec.rfc7748.X25519;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.*;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public final class CryptUtils {

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

    public static ECCKeyPair generateECCKeyPair() throws Exception {
        byte[] privateKey = new byte[32];
        X25519.generatePrivateKey(SecureRandom.getInstanceStrong(), privateKey);

        byte[] publicKey = new byte[32];
        X25519.generatePublicKey(privateKey, 0, publicKey, 0);

        return new ECCKeyPair(privateKey, publicKey);
    }

    public record ECCKeyPair(byte[] privateKey, byte[] publicKey) {
        public ECCKeyPair(byte[] privateKey, byte[] publicKey) {
            this.publicKey = publicKey.clone();
            this.privateKey = privateKey.clone();
        }
    }
}