package com.zhuaer.learning.java.proxy.static_proxy;

public class test {
    public static void main(String[] args) {
        Subject subject = new Proxy(new RealSubject());
        subject.request();
    }
}
