package edu.sjsu.cs166group2.util;

import java.util.HashMap;
import org.apache.commons.codec.digest.DigestUtils;

public class HashUtil {

    // Allows HashDao functions to verify whether input hash is of appropriate length
    // for respective hash type - Please ensure hashes are lower cased for consistency
    public static HashMap<String, Integer> hashToLength = new HashMap<>();
    static {
        hashToLength.put("sha256",64);
        hashToLength.put("md5",32);
    }

    public String hash(String password, String hashType) {
        switch (hashType.toLowerCase()) {
            case "sha256":
                return DigestUtils.sha256Hex(password);
            case "md5":
                return DigestUtils.md5Hex(password);
            default:
                throw new IllegalArgumentException("Invalid hashType");
        }
    }
}
