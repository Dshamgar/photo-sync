package com.schroeder.photosync.services;

import static com.schroeder.photosync.util.Constants.BACKSLASH;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.api.gax.rpc.ApiException;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.photos.library.v1.PhotosLibraryClient;
import com.google.photos.library.v1.PhotosLibrarySettings;
import com.google.photos.types.proto.MediaItem;
import com.schroeder.photosync.domains.AlbumHeader;
import com.schroeder.photosync.domains.AlbumHeadersDto;
import com.schroeder.photosync.util.MediaUtil;
import java.io.File;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

@Service
public class AlbumService {
  @Value("${photosync.download.destination}")
  private String path;

  private static String downloadDestination;

  @Value("${photosync.download.destination}")
  public void setPathStatic(String path) {
    AlbumService.downloadDestination = path;
  }

  @Async
  public static void getAlbumContents(
      GoogleCredentials googleCredentials,
      AlbumHeadersDto albumHeadersDto,
      ResponseBodyEmitter responseBodyEmitter)
      throws IOException {

    final Logger logger = LoggerFactory.getLogger(AlbumService.class);

    logger.debug(
        "Executing method getAlbumContents Asynchronously: " + Thread.currentThread().getName());

    // Set up the Photos Library Client that interacts with the API
    PhotosLibrarySettings settings =
        PhotosLibrarySettings.newBuilder()
            .setCredentialsProvider(FixedCredentialsProvider.create(googleCredentials))
            .build();

    try (PhotosLibraryClient photosLibraryClient = PhotosLibraryClient.initialize(settings)) {

      for (AlbumHeader albumHeader : albumHeadersDto.getAlbumHeaderList()) {

        logger.debug("====================================");
        logger.debug(
            "album: " + albumHeader.getAlbumName() + " || download?: " + albumHeader.getDownload());
        logger.debug("====================================");

        if (albumHeader.getDownload()) {
          int count = 0;
          String albumPath = downloadDestination + albumHeader.getAlbumName() + BACKSLASH;
          File albumFolder = new File(albumPath);

          logger.debug("Downloading media items for album: " + albumHeader.getAlbumName());
          if (!albumFolder.exists()) {
            albumFolder.mkdir();
          }
          for (MediaItem mediaItem :
              photosLibraryClient.searchMediaItems(albumHeader.getAlbumId()).iterateAll()) {
            if (count < 5) {
              MediaUtil.downloadMedia(mediaItem, albumPath);
              count++;
              albumHeader.setItemsDownloaded((long) count);
              responseBodyEmitter.send(count);
            }
          }
        }
      }
      responseBodyEmitter.complete();
    } catch (ApiException e) {
      logger.debug("Exception: " + e);
    }
  }
}
