package edu.sjsu.cs166group2.test;

import edu.sjsu.cs166group2.util.HashUtil;

import junit.framework.TestCase;

import java.nio.charset.StandardCharsets;

public class HashUtilTest extends TestCase {

    private static HashUtil testUtil;
    private static boolean setup = false;

    public void setUp() {
        if(!setup){
            setup = true;
            System.out.println("Creating testUtil.");
            testUtil = new HashUtil();
        }
    }

    public void testApp()
    {
        assertTrue( true );
    }
   /* public void testingPassword() {
        assertEquals(testUtil.hash256("password"), "5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8");
    }

    public void testNumbers() {
        assertEquals(testUtil.hash256("1234567890"),"c775e7b757ede630cd0aa1113bd102661ab38829ca52a6422ab782862f268646");
    }

    public void testASCII() {
        assertEquals(testUtil.hash256(" !\"#$%&\\'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\\\]^_`abcdefghijklmnopqrstuvwxyz{|}~"),
                "58a43742aa05762489a8b0662ff8f8d1fc20444e0af90e8abc6bef14aa57ef1b");
    }*/
}
