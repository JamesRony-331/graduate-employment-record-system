package com.wzbc.EmploymentServer;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties =
{"spring.config.import=",
                              "spring.datasource.url=jdbc:h2:mem:account-smoke;MODE=MySQL;DB_CLOSE_DELAY=-1",
                              "spring.datasource.username=sa", "spring.datasource.password=",
                              "spring.datasource.driver-class-name=org.h2.Driver", "spring.sql.init.mode=always",
                              "spring.sql.init.schema-locations=classpath:account-schema.sql"
                              })
class EmploymentServerApplicationTests
{

    @Test
    void contextLoads()
    {
    }
}
