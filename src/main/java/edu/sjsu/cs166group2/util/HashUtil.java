package edu.sjsu.cs166group2.util;

public class HashUtil {
    // Initialize hash values
    // (first 32 bits of the fractional parts of the square roots of the first 8 primes 2..19):
    int h0 = 0x6a09e667;
    int h1 = 0xbb67ae85;
    int h2 = 0x3c6ef372;
    int h3 = 0xa54ff53a;
    int h4 = 0x510e527f;
    int h5 = 0x9b05688c;
    int h6 = 0x1f83d9ab;
    int h7 = 0x5be0cd19;

    // Initialize array of round constants
    // (first 32 bits of the fractional parts of the cube roots of the first 64 primes 2..311):
    int[] k = new int[]
        {
            0x428a2f98, 0x71374491, 0xb5c0fbcf, 0xe9b5dba5, 0x3956c25b, 0x59f111f1, 0x923f82a4, 0xab1c5ed5,
            0xd807aa98, 0x12835b01, 0x243185be, 0x550c7dc3, 0x72be5d74, 0x80deb1fe, 0x9bdc06a7, 0xc19bf174,
            0xe49b69c1, 0xefbe4786, 0x0fc19dc6, 0x240ca1cc, 0x2de92c6f, 0x4a7484aa, 0x5cb0a9dc, 0x76f988da,
            0x983e5152, 0xa831c66d, 0xb00327c8, 0xbf597fc7, 0xc6e00bf3, 0xd5a79147, 0x06ca6351, 0x14292967,
            0x27b70a85, 0x2e1b2138, 0x4d2c6dfc, 0x53380d13, 0x650a7354, 0x766a0abb, 0x81c2c92e, 0x92722c85,
            0xa2bfe8a1, 0xa81a664b, 0xc24b8b70, 0xc76c51a3, 0xd192e819, 0xd6990624, 0xf40e3585, 0x106aa070,
            0x19a4c116, 0x1e376c08, 0x2748774c, 0x34b0bcb5, 0x391c0cb3, 0x4ed8aa4a, 0x5b9cca4f, 0x682e6ff3,
            0x748f82ee, 0x78a5636f, 0x84c87814, 0x8cc70208, 0x90befffa, 0xa4506ceb, 0xbef9a3f7, 0xc67178f2
        };

    // Converter for Text to Binary Byte
    public static StringBuilder StringToBinaryByte(String s)
    {
        byte[] bytes = s.getBytes();
        StringBuilder binary = new StringBuilder();
        for (byte b : bytes)
        {
            int val = b;
            for (int i = 0; i < 8; i++)
            {
                binary.append((val & 128) == 0 ? 0 : 1);
                val <<= 1;
            }
        }
        return binary;
    }

    // Function to make a 512-bit padded message given a String
    public String makePaddedMessage(String s)
    {
        String messageLengthInBinary = Integer.toBinaryString(s.length()*8); // 8-bit ASCII
        StringBuilder paddedMessage = StringToBinaryByte(s);

        // append "1" at end of message
        paddedMessage.append("1");

        // append K "0" bits, where K is the minimum number >= 0 such that
        // (L + 1 + K + 64) is a multiple of 512
        int K = 512 - ((paddedMessage.length()+64) % 512); // not 100% sure if this works all the time
        for (int i = 0; i < K; i++)
        {
            paddedMessage.append("0");
        }

        // Lastly, append message length as 64-bit big-endian integer
        int numZeros = 64 - messageLengthInBinary.length();
        // First add any zeros
        for (int i = 0; i < numZeros; i++)
        {
            paddedMessage.append("0");
        }
        // Finish by appending the message length in binary
        paddedMessage.append(messageLengthInBinary);

        // Return padded message
        return paddedMessage.toString();
    }
}
