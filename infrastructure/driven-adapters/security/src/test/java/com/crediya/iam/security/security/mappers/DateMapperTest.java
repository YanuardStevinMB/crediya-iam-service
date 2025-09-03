package com.crediya.iam.security.security.mappers;

import com.crediya.iam.security.mappers.DateMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class DateMapperTest {

    private DateMapper mapper;

    @BeforeEach
    void setUp() {
        // Como los métodos son default, podemos instanciar con clase anónima
        mapper = new DateMapper() {};
    }

    @Test
    void toLocalDate_shouldParseIsoString() {
        String iso = "2025-09-03";
        LocalDate result = mapper.toLocalDate(iso);

        assertNotNull(result);
        assertEquals(2025, result.getYear());
        assertEquals(9, result.getMonthValue());
        assertEquals(3, result.getDayOfMonth());
    }

    @Test
    void fromLocalDate_shouldReturnIsoString() {
        LocalDate date = LocalDate.of(2025, 9, 3);
        String result = mapper.fromLocalDate(date);

        assertNotNull(result);
        assertEquals("2025-09-03", result);
    }

    @Test
    void fromLocalDate_shouldReturnNullWhenInputIsNull() {
        String result = mapper.fromLocalDate(null);
        assertNull(result);
    }
}
