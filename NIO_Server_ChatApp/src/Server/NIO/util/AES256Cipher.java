package Server.NIO.util;

import Server.NIO.util.impl.AES256CipherImpl;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public class AES256Cipher implements AES256CipherImpl {

    private static volatile AES256Cipher INSTANCE;
    public static String mainKey = "abcdefghijklmnopqrstuvwxyz123456"; //비밀키 복호화키
    private String secretKey = "abcdefghijklmnopqrstuvwxyz123456"; //need 32bit  -> createKey()에서 변수 init
    String IV = ""; //16bit

    public static AES256Cipher getInstance() {
        if (INSTANCE == null) {
            synchronized (AES256Cipher.class) {
                if (INSTANCE == null)
                    INSTANCE = new AES256Cipher();
            }
        }
        return INSTANCE;
    }

    private AES256Cipher() {
        IV = secretKey.substring(0, 16);
    }

    public String createKey() { //32bit random key
        Random rand = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < 32; i++) {
            int index = rand.nextInt(3);
            switch (index) {
                case 0:
                    sb.append((char) (rand.nextInt(26) + 97));
                    break;
                case 1:
                    sb.append((char) (rand.nextInt(26) + 65));
                    break;
                case 2:
                    sb.append(rand.nextInt(10));
                    break;
            }
        }
        secretKey = sb.toString();
        return sb.toString();
    }

    //암호화
    @Override
    public String AES_Encode(String str) throws UnsupportedEncodingException, NoSuchAlgorithmException
            , NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException,
            IllegalBlockSizeException, BadPaddingException {
        byte[] keyData = secretKey.getBytes();

        SecretKey secureKey = new SecretKeySpec(keyData, "AES");

        Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
        c.init(Cipher.ENCRYPT_MODE, secureKey, new IvParameterSpec(IV.getBytes()));

        byte[] encrypted = c.doFinal(str.getBytes("UTF-8"));
        String enStr = new String(Base64.encodeBase64(encrypted));

        return enStr;
    }

    //암호화
    @Override
    public String AES_Encode(String str, String mainKey) throws UnsupportedEncodingException,
            NoSuchAlgorithmException
            , NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException,
            IllegalBlockSizeException, BadPaddingException {
        byte[] keyData = mainKey.getBytes();

        SecretKey secureKey = new SecretKeySpec(keyData, "AES");

        Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
        c.init(Cipher.ENCRYPT_MODE, secureKey, new IvParameterSpec(IV.getBytes()));

        byte[] encrypted = c.doFinal(str.getBytes("UTF-8"));
        String enStr = new String(Base64.encodeBase64(encrypted));

        return enStr;
    }

    //복호화
    @Override
    public String AES_Decode(String str, String key) throws UnsupportedEncodingException,
            NoSuchAlgorithmException
            , NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException,
            IllegalBlockSizeException, BadPaddingException {
        byte[] keyData = key.getBytes();
        SecretKey secureKey = new SecretKeySpec(keyData, "AES");
        Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
        c.init(Cipher.DECRYPT_MODE, secureKey, new IvParameterSpec(IV.getBytes("UTF-8")));

        byte[] byteStr = Base64.decodeBase64(str.getBytes());

        return new String(c.doFinal(byteStr), "UTF-8");
    }

    //test
    public static void main(String[] args) {
        AES256Cipher a256 = AES256Cipher.getInstance();
        String key = a256.createKey();

        String data = "김바로김바로김바로짱짱맨";
        String encodeData = null;
        try {
            encodeData = a256.AES_Encode(data);
            System.out.println(encodeData);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }
        encodeData = "envelope:" + encodeData;
        System.out.println(encodeData);
        System.out.println(encodeData.split(":")[0]);
        System.out.println(encodeData.split(":")[1]);
        encodeData = encodeData.split(":")[1];

        String decodeData = null;
        try {
            decodeData = a256.AES_Decode(encodeData, key);
            System.out.println(decodeData);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }


    }
}
