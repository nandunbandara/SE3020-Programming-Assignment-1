/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package security;

import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import sun.misc.*;

/**
 *
 * @author ntban_000
 */
public class AESEncryption {
    private static final String ALG = "AES";
    private static final byte[] KEY = new byte[]{'h','f',4,'R','y','z','O','J','h',5,'g','w','W','r','a','z'};
    
    public static String encrypt(String data) throws Exception{
        Key key = generateKey();
        Cipher cipher = Cipher.getInstance(ALG);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encryptValue = cipher.doFinal(data.getBytes());
        return new BASE64Encoder().encode(encryptValue);
    }
    
    public static String decrypt(String data) throws Exception{
        Key key = generateKey();
        Cipher cipher = Cipher.getInstance(ALG);
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decodedValue = new BASE64Decoder().decodeBuffer(data);
        byte[] value = cipher.doFinal(decodedValue);
        return new String(value);
    }
    
    private static Key generateKey(){
        Key key = new SecretKeySpec(KEY, ALG);
        return key;
    }
}
