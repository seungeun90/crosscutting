package com.example.crosscutting.demo.common.util;

import com.example.crosscutting.demo.config.properties.AESProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
@Component
@RequiredArgsConstructor
public class AESUtil {
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private final AESProperties aesProperties;

    public String encrypt(String data) {
        byte[] encryptedBytes = null;
        try{
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            SecretKey secretKey = new SecretKeySpec(getSecretKey(), ALGORITHM);
            IvParameterSpec ivParamSpec = new IvParameterSpec(getIv());
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParamSpec);
            encryptedBytes = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    public String decrypt(String encryptedData) throws Exception {
        byte[] decryptedBytes = null;
        try{
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            SecretKey secretKey = new SecretKeySpec(getSecretKey(), ALGORITHM);
            IvParameterSpec ivParamSpec = new IvParameterSpec(getIv());
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParamSpec);

            byte[] decode = Base64.getDecoder().decode(encryptedData);
            decryptedBytes = cipher.doFinal(decode);
            return new String(decryptedBytes);
        } catch (Exception e) {
            log.error("AesUtil Decrypt Failure = {}", encryptedData);
          //  throw new RuntimeException(e);
        }

        return null;
    }

    private byte[] getSecretKey(){
        String key = aesProperties.getSecretKey();
        return Base64.getDecoder().decode(key);
    }
    private byte[] getIv(){
        String key = aesProperties.getIv();
        return Base64.getDecoder().decode(key);
    }
}
