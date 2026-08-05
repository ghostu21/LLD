package com.spotify.lld.demo;

import com.spotify.lld.catalog.MusicCatalog;
import com.spotify.lld.catalog.Song;

import java.util.List;

public class CatalogSearchScenario implements FeatureScenario {
    @Override
    public void run(DemoFixtures fx) {
        System.out.println("--- Catalog Search (Trie) ---");
        MusicCatalog catalog = new MusicCatalog();
        catalog.addSong(fx.song1);
        catalog.addSong(fx.song2);
        catalog.addSong(fx.song3);

        List<Song> byTitle = catalog.searchByTitle("bohe");
        System.out.println("Title 'bohe' → " + byTitle.get(0).getTitle());

        List<Song> byArtist = catalog.searchByArtist("sheer");
        System.out.println("Artist 'sheer' → " + byArtist.get(0).getArtist());

        List<Song> partial = catalog.searchByTitle("jai");
        System.out.println("Title 'jai' → " + partial.get(0).getTitle());
    }
}
