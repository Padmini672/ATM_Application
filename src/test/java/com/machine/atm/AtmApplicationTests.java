package com.machine.atm;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@SpringBootTest

@ExtendWith(SpringExtension.class)
class AtmApplicationTests {

    @Test
    void contextLoads() {
        AtmApplication.main(new String[]{});
    }

}
