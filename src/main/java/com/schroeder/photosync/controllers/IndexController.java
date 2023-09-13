package com.schroeder.photosync.controllers;

import static com.schroeder.photosync.util.Constants.*;

import com.google.api.client.auth.oauth2.*;
import com.google.auth.oauth2.GoogleCredentials;
import com.schroeder.photosync.domains.AlbumHeader;
import com.schroeder.photosync.domains.AlbumHeadersDto;
import com.schroeder.photosync.services.AlbumHeadersService;
import com.schroeder.photosync.services.AlbumService;
import com.schroeder.photosync.util.AuthorizationUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

@Controller
public class IndexController {
  private final Logger logger = LoggerFactory.getLogger(IndexController.class);
  private final AlbumHeadersService albumHeadersService;
  private GoogleCredentials googleCredentials;
  private String secret;
  private String googleApiClientId;
  private String googleApiAuthUrl;
  private String photosyncCallbackUrl;

  public IndexController(
      AlbumHeadersService albumHeadersService,
      @Value("${encrypted.property}") String secret,
      @Value("${google.api.client.id}") String googleApiClientId,
      @Value("${google.api.auth.url}") String googleApiAuthUrl,
      @Value("${photosync.callback.url}") String photosyncCallbackUrl)
      throws IOException {
    this.albumHeadersService = albumHeadersService;
    this.secret = secret;
    this.googleApiClientId = googleApiClientId;
    this.googleApiAuthUrl = googleApiAuthUrl;
    this.photosyncCallbackUrl = photosyncCallbackUrl;
  }

  @RequestMapping({"auth/google/callback"})
  public String index(HttpServletRequest request, HttpServletResponse response, Model model)
      throws IOException {
    StringBuffer fullUrlBuf = request.getRequestURL();
    if (request.getQueryString() != null) {
      fullUrlBuf.append('?').append(request.getQueryString());
    }
    logger.debug("Callback request URL: " + fullUrlBuf.toString());
    AuthorizationCodeResponseUrl authResponse =
        new AuthorizationCodeResponseUrl(fullUrlBuf.toString()); // check for user-denied error
    if (authResponse.getError() != null) {
      logger.error("ACCESS DENIED!!!!");
    } else { // request access token using
      String code = authResponse.getCode();
      logger.debug("Authorization code: " + code);
    }
    logger.debug("AuthorizationCodeResponseUrl: " + authResponse);

    googleCredentials =
        AuthorizationUtil.getCredentials(
            authResponse.getCode(), secret, photosyncCallbackUrl, googleApiClientId);
    AlbumHeadersDto albumsForm = new AlbumHeadersDto();
    for (AlbumHeader albumHeader : albumHeadersService.getAlbumNames(googleCredentials)) {
      albumsForm.addAlbumHeader(albumHeader);
    }
    model.addAttribute("form", albumsForm);
    return "test";
  }

  @PostMapping("/download")
  public String downloadPhotos(@ModelAttribute AlbumHeadersDto form, Model model) {
    //  public ResponseBodyEmitter downloadPhotos(@ModelAttribute AlbumHeadersDto form, Model model)
    // {

    final ResponseBodyEmitter responseBodyEmitter = new ResponseBodyEmitter();
    logger.debug("---------------------------------------------");
    logger.debug("FORM SUBMITTED: album header size: " + form.getAlbumHeaderList().size());
    logger.debug("---------------------------------------------");
    if (logger.isDebugEnabled()) {
      for (AlbumHeader albumHeader : form.getAlbumHeaderList()) {
        logger.debug(albumHeader.getAlbumName() + ": " + albumHeader.getDownload());
      }
    }
    try {
      AlbumService.getAlbumContents(googleCredentials, form, responseBodyEmitter);
    } catch (IOException e) {
      logger.error("Exception: " + e);
    }
    model.addAttribute("form", form);
    return "test";
    // return responseBodyEmitter;
  }

  @RequestMapping({"", "/", "index.html"})
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String url =
        new AuthorizationCodeRequestUrl(googleApiAuthUrl, googleApiClientId)
            .setState(null)
            .setRedirectUri(photosyncCallbackUrl)
            .setScopes(SCOPES)
            .build();

    logger.debug("authorization request URL: " + url);
    response.sendRedirect(url);
  }
}
