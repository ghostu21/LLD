package com.spotify.lld.social;

import com.spotify.lld.playlist.Playlist;

import java.util.List;
import java.util.stream.Collectors;

public class PlaylistAccessControl {
    private final SocialGraph socialGraph;

    public PlaylistAccessControl(SocialGraph socialGraph) {
        this.socialGraph = socialGraph;
    }

    public boolean canView(String viewerId, Playlist playlist) {
        return switch (playlist.getVisibility()) {
            case PUBLIC -> true;
            case PRIVATE -> playlist.getOwnerId().equals(viewerId);
            case FOLLOWERS_ONLY -> playlist.getOwnerId().equals(viewerId)
                    || socialGraph.isFollowing(viewerId, playlist.getOwnerId());
        };
    }

    public List<Playlist> getVisiblePlaylists(String viewerId, String ownerId,
                                              List<Playlist> playlists) {
        return playlists.stream()
                .filter(p -> canView(viewerId, p))
                .collect(Collectors.toList());
    }
}
