package com.spotify.lld.social;

import com.spotify.lld.playlist.Playlist;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Strategy/policy for playlist visibility ACL.
 * <p>
 * Why: keep access rules out of {@link Playlist} itself so visibility policy
 * can change without mutating the composite structure.
 * <p>
 * Logic of {@link #canView}:
 * <ul>
 *   <li>PUBLIC → always true</li>
 *   <li>PRIVATE → viewer must be owner</li>
 *   <li>FOLLOWERS_ONLY → owner OR viewer follows owner</li>
 * </ul>
 */
public class PlaylistAccessControl {
    private final SocialGraph socialGraph;

    public PlaylistAccessControl(SocialGraph socialGraph) {
        this.socialGraph = socialGraph;
    }

    /**
     * Decides whether {@code viewerId} may see {@code playlist}.
     */
    public boolean canView(String viewerId, Playlist playlist) {
        return switch (playlist.getVisibility()) {
            case PUBLIC -> true;
            case PRIVATE -> playlist.getOwnerId().equals(viewerId);
            case FOLLOWERS_ONLY -> playlist.getOwnerId().equals(viewerId)
                    || socialGraph.isFollowing(viewerId, playlist.getOwnerId());
        };
    }

    /**
     * Filters an owner's playlists to those visible to {@code viewerId}.
     */
    public List<Playlist> getVisiblePlaylists(String viewerId, String ownerId,
                                              List<Playlist> playlists) {
        return playlists.stream()
                .filter(p -> canView(viewerId, p))
                .collect(Collectors.toList());
    }
}
