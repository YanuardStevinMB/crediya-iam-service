package com.crediya.iam.usecase.authenticate;

public record TokenResult(String token, String tokenType, long expiresAtEpochSec) {}
