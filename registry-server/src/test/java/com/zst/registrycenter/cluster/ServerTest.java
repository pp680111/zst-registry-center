package com.zst.registrycenter.cluster;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ServerTest {
    @Test
    public void hashcode_test() {
        Server s1 = new Server();
        s1.setIp("192.168.123.1");
        s1.setPort(80);

        Server s2 = new Server();
        s2.setAddress("192.168.123.1:80");

        Assertions.assertEquals(s1.hashCode(), s2.hashCode());
    }

    @Test
    public void equals_test() {
        Server s1 = new Server();
        s1.setIp("192.168.123.1");
        s1.setPort(80);

        Server s2 = new Server();
        s2.setAddress("192.168.123.1:80");

        Assertions.assertTrue(s1.equals(s2));
    }
}
