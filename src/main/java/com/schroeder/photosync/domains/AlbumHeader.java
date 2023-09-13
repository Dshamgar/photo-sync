package com.schroeder.photosync.domains;

import org.springframework.stereotype.Component;

@Component
public class AlbumHeader {

  private Boolean download;
  private String albumName;
  private String albumId;
  private Long numberItems;
  private Long itemsDownloaded;

  public AlbumHeader() {}

  public AlbumHeader(
      Boolean download, String albumName, String albumId, Long numberItems, Long itemsDownloaded) {
    this.download = download;
    this.albumName = albumName;
    this.albumId = albumId;
    this.numberItems = numberItems;
    this.itemsDownloaded = itemsDownloaded;
  }

  public Boolean getDownload() {
    return download;
  }

  public void setDownload(Boolean download) {
    this.download = download;
  }

  public String getAlbumName() {
    return albumName;
  }

  public void setAlbumName(String albumName) {
    this.albumName = albumName;
  }

  public String getAlbumId() {
    return albumId;
  }

  public void setAlbumId(String albumId) {
    this.albumId = albumId;
  }

  public Long getNumberItems() {
    return numberItems;
  }

  public void setNumberItems(Long numberItems) {
    this.numberItems = numberItems;
  }

  public Long getItemsDownloaded() {
    return itemsDownloaded;
  }

  public void setItemsDownloaded(Long itemsDownloaded) {
    this.itemsDownloaded = itemsDownloaded;
  }

  @Override
  public String toString() {
    return "AlbumHeader{"
        + "download="
        + download
        + ", albumName='"
        + albumName
        + '\''
        + ", albumId='"
        + albumId
        + '\''
        + ", numberItems="
        + numberItems
        + ", itemsDownloaded="
        + itemsDownloaded
        + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof AlbumHeader that)) return false;

    if (getDownload() != null
        ? !getDownload().equals(that.getDownload())
        : that.getDownload() != null) return false;
    if (getAlbumName() != null
        ? !getAlbumName().equals(that.getAlbumName())
        : that.getAlbumName() != null) return false;
    if (getAlbumId() != null ? !getAlbumId().equals(that.getAlbumId()) : that.getAlbumId() != null)
      return false;
    if (getNumberItems() != null
        ? !getNumberItems().equals(that.getNumberItems())
        : that.getNumberItems() != null) return false;
    return getItemsDownloaded() != null
        ? getItemsDownloaded().equals(that.getItemsDownloaded())
        : that.getItemsDownloaded() == null;
  }

  @Override
  public int hashCode() {
    int result = getDownload() != null ? getDownload().hashCode() : 0;
    result = 31 * result + (getAlbumName() != null ? getAlbumName().hashCode() : 0);
    result = 31 * result + (getAlbumId() != null ? getAlbumId().hashCode() : 0);
    result = 31 * result + (getNumberItems() != null ? getNumberItems().hashCode() : 0);
    result = 31 * result + (getItemsDownloaded() != null ? getItemsDownloaded().hashCode() : 0);
    return result;
  }
}
