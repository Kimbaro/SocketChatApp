package Server.NIO.util.impl;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public interface AES256CipherImpl {
    public String AES_Encode(String str) throws UnsupportedEncodingException, NoSuchAlgorithmException
            , NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException,
            IllegalBlockSizeException, BadPaddingException;

    public String AES_Encode(String str, String mainKey) throws UnsupportedEncodingException,
            NoSuchAlgorithmException
            , NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException,
            IllegalBlockSizeException, BadPaddingException;

    public String AES_Decode(String str, String key) throws UnsupportedEncodingException,
            NoSuchAlgorithmException
            , NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException,
            IllegalBlockSizeException, BadPaddingException;
}
