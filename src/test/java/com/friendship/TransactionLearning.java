package com.friendship;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TransactionLearning {
    @Test
    @Transactional
    public void test() {
        System.out.println("Hello World!!!");
    }
}
