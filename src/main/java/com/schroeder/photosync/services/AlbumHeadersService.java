package com.schroeder.photosync.services;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.photos.library.v1.PhotosLibraryClient;
import com.google.photos.library.v1.PhotosLibrarySettings;
import com.google.photos.types.proto.Album;
import com.schroeder.photosync.domains.AlbumHeader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AlbumHeadersService {
  private final Logger logger = LoggerFactory.getLogger(AlbumHeadersService.class);

  public List<AlbumHeader> getAlbumNames(GoogleCredentials googleCredentials) throws IOException {
    List<AlbumHeader> albumHeaderList = new ArrayList<>();

    // Set up the Photos Library Client that interacts with the API
    PhotosLibrarySettings settings =
        PhotosLibrarySettings.newBuilder()
            .setCredentialsProvider(FixedCredentialsProvider.create(googleCredentials))
            .build();

    try (PhotosLibraryClient photosLibraryClient = PhotosLibraryClient.initialize(settings)) {
      for (Album album : photosLibraryClient.listAlbums().iterateAll()) {
        AlbumHeader albumHeader = new AlbumHeader();
        logger.debug("====================================");
        logger.debug("album: " + album.getTitle() + "   Id: " + album.getId());
        logger.debug("====================================");
        albumHeader.setAlbumId(album.getId());
        albumHeader.setAlbumName(album.getTitle());
        albumHeader.setNumberItems(album.getMediaItemsCount());
        albumHeader.setDownload(false);
        albumHeaderList.add(albumHeader);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return albumHeaderList;
  }
}
