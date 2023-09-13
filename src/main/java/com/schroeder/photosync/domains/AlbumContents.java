package com.schroeder.photosync.domains;

import com.google.photos.types.proto.Album;
import com.google.photos.types.proto.MediaItem;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AlbumContents {

  private Album album;
  private List<MediaItem> mediaItems;

  public Album getAlbum() {
    return album;
  }

  public void setAlbum(Album album) {
    this.album = album;
  }

  public List<MediaItem> getMediaItems() {
    return mediaItems;
  }

  public void setMediaItems(List<MediaItem> mediaItems) {
    this.mediaItems = mediaItems;
  }

  @Override
  public String toString() {
    return "AlbumContents{" + "album=" + album + ", mediaItems=" + mediaItems + '}';
  }
}
