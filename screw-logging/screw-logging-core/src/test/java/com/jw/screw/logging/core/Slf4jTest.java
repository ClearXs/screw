package com.jw.screw.logging.core;

import com.jw.screw.storage.Executor;
import com.jw.screw.storage.ExecutorHousekeeper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;

@Slf4j
public class Slf4jTest {
    private Executor executor;

    @Before
    public void init() throws SQLException, IOException, ClassNotFoundException {
        executor = ExecutorHousekeeper.getExecutor();
    }

    @Test
    public void testLogback() {
        log.info("2121");
    }
}
