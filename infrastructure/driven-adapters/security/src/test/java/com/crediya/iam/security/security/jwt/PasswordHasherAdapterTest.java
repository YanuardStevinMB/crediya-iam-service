package com.crediya.iam.security.security.jwt;


import com.crediya.iam.security.jwt.PasswordHasherAdapter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;


@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class PasswordHasherAdapterTest {

    @Mock
    PasswordEncoder encoder;

    @Test
    void matches_delegatesToPasswordEncoder_true() {
        // given
        var adapter = new PasswordHasherAdapter(encoder);
        String raw = "plain123";
        String hash = "$2a$12$abcdefghijklmnopqrstuvwxabcdefghijklmnopqrstuvwx";
        when(encoder.matches(raw, hash)).thenReturn(true);

        // when
        boolean ok = adapter.matches(raw, hash);

        // then
        assertThat(ok).isTrue();
        verify(encoder, times(1)).matches(raw, hash);
        verifyNoMoreInteractions(encoder);
    }

    @Test
    void matches_delegatesToPasswordEncoder_false() {
        var adapter = new PasswordHasherAdapter(encoder);
        String raw = "plain123";
        String hash = "$2a$12$abcdefghijklmnopqrstuvwxabcdefghijklmnopqrstuvwx";
        when(encoder.matches(raw, hash)).thenReturn(false);

        boolean ok = adapter.matches(raw, hash);

        assertThat(ok).isFalse();
        verify(encoder, times(1)).matches(raw, hash);
        verifyNoMoreInteractions(encoder);
    }

    @Test
    void matches_passesExactArguments() {
        var adapter = new PasswordHasherAdapter(encoder);
        String raw = "   with spaces  ";
        String hash = "   hash   ";

        when(encoder.matches(anyString(), anyString())).thenReturn(false);

        adapter.matches(raw, hash);

        ArgumentCaptor<String> rawCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> hashCap = ArgumentCaptor.forClass(String.class);

        verify(encoder).matches(rawCap.capture(), hashCap.capture());
        assertThat(rawCap.getValue()).isEqualTo(raw);
        assertThat(hashCap.getValue()).isEqualTo(hash);
    }
}