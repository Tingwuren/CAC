package cn.edu.bupt.cac;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("cn.edu.bupt.cac.mapper")
public class CacApplication {

    public static void main(String[] args) {
        SpringApplication.run(CacApplication.class, args);
    }

}
