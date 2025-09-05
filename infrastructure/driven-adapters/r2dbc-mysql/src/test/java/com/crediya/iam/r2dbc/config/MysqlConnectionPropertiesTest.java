package com.crediya.iam.r2dbc.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MysqlConnectionPropertiesTest {

    @Test
    void shouldStoreAndReturnValues() {
        MysqlConnectionProperties props = new MysqlConnectionProperties(
                "localhost",
                3306,
                "testdb",
                "root",
                "secret"
        );

        assertEquals("localhost", props.host());
        assertEquals(3306, props.port());
        assertEquals("testdb", props.database());
        assertEquals("root", props.username());
        assertEquals("secret", props.password());
    }
}