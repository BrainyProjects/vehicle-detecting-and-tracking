package com.joseph;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by IntelliJ on Friday, 02 August, 2019 at 18:17.
 *
 * @author Joseph Maria
 */
public class HelloWorldTest {
    private HelloWorld helloWorld = new HelloWorld();

    @Test
    public void helloWorld() {
        assertEquals("Hello Universe.", helloWorld.helloWorld(true));
        assertEquals("Hello World.", helloWorld.helloWorld(false));
    }
}
