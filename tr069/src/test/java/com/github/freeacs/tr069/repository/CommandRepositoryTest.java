package com.github.freeacs.tr069.repository;

import com.github.freeacs.tr069.entity.Command;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CommandRepositoryTest {

    private @Autowired CommandRepository commandRepository;

    @Test
    public void findByDeviceIdTest(){
        Command command = commandRepository.findFirstByDeviceIdAndCmdStatus("111", "01");
        System.err.println(command);
    }

}