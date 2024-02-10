package top.ninng.demo;

import org.junit.Test;

import top.ninng.demo.utils.IdWorker;

/**
 * @Author OhmLaw
 * @Date 2022/10/7 17:11
 * @Version 1.0
 */
public class IdTest {

    @Test
    public void f() {
        for (int i = 0; i < 50; i++) {
            System.out.println("++" + IdWorker.getId());
        }
//        IdWorker.main();
    }
}
