package com.spotify.lld.playlist;

/**
 * ACL visibility scopes for a playlist (used with SocialGraph).
 * <p>
 * Why: "share playlist" without visibility rules is a privacy hole.
 * <p>
 * Logic (enforced in PlaylistAccessControl):
 * <ul>
 *   <li>PUBLIC — anyone can view</li>
 *   <li>FOLLOWERS_ONLY — owner or users who follow the owner</li>
 *   <li>PRIVATE — owner only</li>
 * </ul>
 */
public enum PlaylistVisibility {
    PUBLIC, FOLLOWERS_ONLY, PRIVATE
}
