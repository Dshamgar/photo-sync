package com.schroeder.photosync.domains;

import java.util.ArrayList;
import java.util.List;

public class AlbumHeadersDto {
  private List<AlbumHeader> albumHeaderList;

  public AlbumHeadersDto() {
    this.albumHeaderList = new ArrayList<>();
  }

  public void addAlbumHeader(AlbumHeader albumHeader) {
    this.albumHeaderList.add(albumHeader);
  }

  public List<AlbumHeader> getAlbumHeaderList() {
    return albumHeaderList;
  }

  public void setAlbumHeaderList(List<AlbumHeader> albumHeaderList) {
    this.albumHeaderList = albumHeaderList;
  }
  // getter and setter
}
