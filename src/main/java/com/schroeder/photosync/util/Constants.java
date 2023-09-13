package com.schroeder.photosync.util;

import java.util.ArrayList;
import java.util.List;

public class Constants {

  public static final String BACKSLASH = "\\";
  public static final List<String> SCOPES =
      new ArrayList<>(List.of("https://www.googleapis.com/auth/photoslibrary.readonly"));
  public static final String APP_NAME = "PhotoSync";
}
