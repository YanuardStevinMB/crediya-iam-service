package com.crediya.iam.api.dto;

public record LoginResponseDto (String accessToken, String tokenType, long expiresAt) {}