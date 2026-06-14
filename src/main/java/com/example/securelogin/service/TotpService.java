package com.example.securelogin.service;

import org.apache.commons.codec.binary.Base32;
import org.springframework.stereotype.Service;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;

@Service
public class TotpService {

    public String generateSecretKey() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[20];
        random.nextBytes(bytes);
        Base32 base32 = new Base32();
        return base32.encodeToString(bytes);
    }

    public String getQrCodeUrl(String username, String secret) {
        // Formats the URL to be scanned by Authenticator apps
        return "otpauth://totp/SecureLogin:" + username + "?secret=" + secret + "&issuer=SecureLogin";
    }

    public boolean verifyCode(String secret, String codeStr) {
        if (secret == null || secret.isEmpty() || codeStr == null || codeStr.isEmpty()) {
            return false;
        }
        int code;
        try {
            code = Integer.parseInt(codeStr.trim());
        } catch (NumberFormatException e) {
            return false;
        }

        Base32 base32 = new Base32();
        byte[] decodedKey = base32.decode(secret);

        long timeWindow = System.currentTimeMillis() / 1000L / 30L;
        // Verify current, previous, and next 30-second windows to allow for clock drift
        for (int i = -1; i <= 1; i++) {
            if (calculateCode(decodedKey, timeWindow + i) == code) {
                return true;
            }
        }
        return false;
    }

    private int calculateCode(byte[] key, long time) {
        byte[] data = ByteBuffer.allocate(8).putLong(time).array();
        SecretKeySpec signKey = new SecretKeySpec(key, "HmacSHA1");
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(signKey);
            byte[] hash = mac.doFinal(data);

            int offset = hash[hash.length - 1] & 0xF;
            long truncatedHash = 0;
            for (int i = 0; i < 4; ++i) {
                truncatedHash <<= 8;
                truncatedHash |= (hash[offset + i] & 0xFF);
            }
            truncatedHash &= 0x7FFFFFFF;
            truncatedHash %= 1000000;
            return (int) truncatedHash;
        } catch (GeneralSecurityException e) {
            throw new RuntimeException("Error calculating TOTP code", e);
        }
    }
}
