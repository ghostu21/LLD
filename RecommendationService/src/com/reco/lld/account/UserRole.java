package com.reco.lld.account;

/**
 * Authorization roles for the recommendation API.
 * <p>
 * Why: guests may only see non-personalized popular items; members get
 * personalized ranking; admins may debug another user's slate without
 * receiving that user's PII in the payload.
 */
public enum UserRole {
    GUEST,
    MEMBER,
    ADMIN
}
