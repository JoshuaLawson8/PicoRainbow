package edu.sjsu.cs166group2.util;

import org.apache.commons.codec.digest.DigestUtils;

public class HashUtil {
    public String hash256(String password){
        return DigestUtils.sha256Hex(password);
    }
}
