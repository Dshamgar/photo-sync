package com.schroeder.photosync.util;

import static com.schroeder.photosync.util.Constants.APP_NAME;

import com.google.photos.types.proto.MediaItem;
import com.google.protobuf.Timestamp;
import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImageWriteException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.common.RationalNumber;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.jpeg.exif.ExifRewriter;
import org.apache.commons.imaging.formats.tiff.TiffField;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.constants.*;
import org.apache.commons.imaging.formats.tiff.taginfos.TagInfo;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputDirectory;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputSet;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MediaUtil {
  private static final Logger logger = LoggerFactory.getLogger(MediaUtil.class);

  public static void downloadMedia(MediaItem mediaItem, String albumPath) throws IOException {

    if (logger.isDebugEnabled()) {
      Timestamp ts = mediaItem.getMediaMetadata().getCreationTime();
      LocalDateTime localDateTime =
          Instant.ofEpochSecond(ts.getSeconds(), ts.getNanos())
              .atZone(ZoneId.of("America/Montreal"))
              .toLocalDateTime();

      logger.debug("Creation DateTime: " + localDateTime);
      logger.debug("Has Photo?: " + mediaItem.getMediaMetadata().hasPhoto());
    }

    String fullFilePath = albumPath + mediaItem.getFilename();
    logger.debug("Full file path: " + fullFilePath);

    File photoFile = new File(fullFilePath);
    if (!photoFile.exists()) {
      String myBaseUrl = mediaItem.getBaseUrl() + "=d";
      URL url = new URL(myBaseUrl);
      ReadableByteChannel readableByteChannel = Channels.newChannel(url.openStream());
      try (FileOutputStream fileOutputStream = new FileOutputStream(fullFilePath)) {
        fileOutputStream.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
      }
    }
  }

  public static void updateMediaAttributes(MediaItem mediaItem, String albumPath)
      throws IOException, ImageReadException {
    final Path path = Paths.get(albumPath + mediaItem.getFilename());
    final String fullPath = albumPath + mediaItem.getFilename();
    final String dstFilePath =
        albumPath
            + FilenameUtils.removeExtension(mediaItem.getFilename())
            + "_dst."
            + FilenameUtils.getExtension(mediaItem.getFilename());
    File file = new File(fullPath);
    File dst = new File(dstFilePath);
    OutputStream outputStream;

    try (FileOutputStream fos = new FileOutputStream(dst);
        OutputStream os = new BufferedOutputStream(fos)) {
      TiffOutputSet outputSet = null;

      logger.debug("Camera Make: " + mediaItem.getMediaMetadata().getPhoto().getCameraMake());
      logger.debug("Camera Model: " + mediaItem.getMediaMetadata().getPhoto().getCameraModel());
      logger.debug("ISO: " + mediaItem.getMediaMetadata().getPhoto().getIsoEquivalent());
      logger.debug("Metadata: " + mediaItem.getMediaMetadata());
      logger.debug("Photo: " + mediaItem.getMediaMetadata().getPhoto());
      logger.debug("All Fields: " + mediaItem.getMediaMetadata().getAllFields());
      logger.debug("output dst file: " + dstFilePath);

      ImageMetadata metadata;

      try {
        metadata = Imaging.getMetadata(file);
      } catch (ImageReadException imageReadException) {
        throw new ImageReadException("Image Read Exception");
      } catch (IOException ioException) {
        throw new IOException("IO Exception");
      }

      logger.debug(metadata.toString());

      if (metadata instanceof JpegImageMetadata) {
        logger.debug(fullPath + " is INSTANCEOF JpegImageMetadata!!!");
        final JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
        if (null != jpegMetadata) {
          // note that exif might be null if no Exif metadata is found.
          final TiffImageMetadata exif = jpegMetadata.getExif();

          if (null != exif) {
            // TiffImageMetadata class is immutable (read-only).
            // TiffOutputSet class represents the Exif data to write.
            //
            // Usually, we want to update existing Exif metadata by
            // changing
            // the values of a few fields, or adding a field.
            // In these cases, it is easiest to use getOutputSet() to
            // start with a "copy" of the fields read from the image.
            outputSet = exif.getOutputSet();
          }
        }

        // if file does not contain any exif metadata, we create an empty
        // set of exif metadata. Otherwise, we keep all of the other
        // existing tags.
        if (null == outputSet) {
          outputSet = new TiffOutputSet();
        }
        {
          // Example of how to add/update GPS info to output set.

          // New York City
          final double longitude = -74.0; // 74 degrees W (in Degrees East)
          //          final double longitude = mediaItem.getMediaMetadata().getPhoto().get
          final double latitude = 40 + 43 / 60.0; // 40 degrees N (in Degrees North)

          outputSet.setGPSInDegrees(longitude, latitude);
        }

        // new ExifRewriter().updateExifMetadataLossless(file, os, outputSet);

        final TiffOutputDirectory exifDirectory = outputSet.getOrCreateRootDirectory();

        exifDirectory.removeField(ExifTagConstants.EXIF_TAG_SOFTWARE);
        exifDirectory.removeField(ExifTagConstants.EXIF_TAG_EXIF_IMAGE_WIDTH);
        exifDirectory.removeField(ExifTagConstants.EXIF_TAG_EXIF_IMAGE_LENGTH);
        exifDirectory.removeField(ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL);
        exifDirectory.removeField(TiffTagConstants.TIFF_TAG_DATE_TIME);

        exifDirectory.add(ExifTagConstants.EXIF_TAG_SOFTWARE, APP_NAME);
        exifDirectory.add(
            ExifTagConstants.EXIF_TAG_EXIF_IMAGE_WIDTH,
            (short) mediaItem.getMediaMetadata().getWidth());
        exifDirectory.add(
            ExifTagConstants.EXIF_TAG_EXIF_IMAGE_LENGTH,
            (short) mediaItem.getMediaMetadata().getHeight());
        final String formattedDateTime =
            formatDateTime(mediaItem.getMediaMetadata().getCreationTime());
        logger.debug("setting create datetime to: " + formattedDateTime);
        exifDirectory.add(ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL, formattedDateTime);
        exifDirectory.add(TiffTagConstants.TIFF_TAG_DATE_TIME, formattedDateTime);

        if (null != mediaItem.getMediaMetadata().getPhoto()) {
          exifDirectory.removeField(ExifTagConstants.EXIF_TAG_FOCAL_LENGTH);
          exifDirectory.removeField(ExifTagConstants.EXIF_TAG_MAX_APERTURE_VALUE);
          exifDirectory.removeField(ExifTagConstants.EXIF_TAG_ISO);
          exifDirectory.removeField(ExifTagConstants.EXIF_TAG_EXPOSURE_TIME);
          exifDirectory.removeField(TiffTagConstants.TIFF_TAG_MAKE);
          exifDirectory.removeField(TiffTagConstants.TIFF_TAG_MODEL);

          RationalNumber focalLength =
              new RationalNumber(
                  (int) ((double) mediaItem.getMediaMetadata().getPhoto().getFocalLength() * 100.0),
                  100);
          logger.debug("setting focal length to: " + focalLength.toDisplayString());
          exifDirectory.add(ExifTagConstants.EXIF_TAG_FOCAL_LENGTH, focalLength);

          RationalNumber apertureFNumber =
              new RationalNumber(
                  (int)
                      ((double) mediaItem.getMediaMetadata().getPhoto().getApertureFNumber()
                          * 100.0),
                  100);
          logger.debug("setting aperture to: " + apertureFNumber.toDisplayString());
          exifDirectory.add(ExifTagConstants.EXIF_TAG_MAX_APERTURE_VALUE, apertureFNumber);

          short iso = (short) mediaItem.getMediaMetadata().getPhoto().getIsoEquivalent();
          logger.debug("setting ISO to: " + iso);
          exifDirectory.add(ExifTagConstants.EXIF_TAG_ISO, iso);

          RationalNumber exposureTime =
              new RationalNumber(
                  mediaItem.getMediaMetadata().getPhoto().getExposureTime().getNanos(), 1);
          logger.debug("setting exposure time to: " + exposureTime.toDisplayString());
          exifDirectory.add(ExifTagConstants.EXIF_TAG_EXPOSURE_TIME, exposureTime);

          exifDirectory.add(
              TiffTagConstants.TIFF_TAG_MAKE,
              mediaItem.getMediaMetadata().getPhoto().getCameraMake());

          exifDirectory.add(
              TiffTagConstants.TIFF_TAG_MODEL,
              mediaItem.getMediaMetadata().getPhoto().getCameraModel());
        }
        outputStream = new FileOutputStream(dst);
        outputStream = new BufferedOutputStream(outputStream);

        new ExifRewriter().updateExifMetadataLossless(file, outputStream, outputSet);

        // Jpeg EXIF metadata is stored in a TIFF-based directory structure
        // and is identified with TIFF tags.
        // Here we look for the "x resolution" tag, but
        // we could just as easily search for any other tag.
        //
        // see the TiffConstants file for a list of TIFF tags.

        System.out.println("file: " + file.getPath());

        // print out various interesting EXIF tags.
        //        printTagValue(jpegMetadata, TiffTagConstants.TIFF_TAG_XRESOLUTION);
        //        printTagValue(jpegMetadata, TiffTagConstants.TIFF_TAG_DATE_TIME);
        //        Iterator<TagInfo> tagInfo =
        // MicrosoftTagConstants.ALL_MICROSOFT_TAGS.stream().iterator();
        //        while (tagInfo.hasNext()) {
        //          TagInfo ti = tagInfo.next();
        //          logger.debug("Microsoft tag: " + ti.toString());
        //          printTagValue(jpegMetadata, ti);
        //        }
        //        printTagValue(jpegMetadata, ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL);
        //        printTagValue(jpegMetadata, ExifTagConstants.EXIF_TAG_DATE_TIME_DIGITIZED);
        //        printTagValue(jpegMetadata, ExifTagConstants.EXIF_TAG_ISO);
        //        printTagValue(jpegMetadata, ExifTagConstants.EXIF_TAG_SHUTTER_SPEED_VALUE);
        //        printTagValue(jpegMetadata, ExifTagConstants.EXIF_TAG_APERTURE_VALUE);
        //        printTagValue(jpegMetadata, ExifTagConstants.EXIF_TAG_BRIGHTNESS_VALUE);
        //        printTagValue(jpegMetadata, GpsTagConstants.GPS_TAG_GPS_LATITUDE_REF);
        //        printTagValue(jpegMetadata, GpsTagConstants.GPS_TAG_GPS_LATITUDE);
        //        printTagValue(jpegMetadata, GpsTagConstants.GPS_TAG_GPS_LONGITUDE_REF);
        //        printTagValue(jpegMetadata, GpsTagConstants.GPS_TAG_GPS_LONGITUDE);
      } else {
        logger.debug("PHOTO IS NOT JPEG!!!");
      }
    } catch (ImageWriteException ex) {
      throw new RuntimeException(ex);
    }
  }

  private static void printTagValue(final JpegImageMetadata jpegMetadata, final TagInfo tagInfo) {
    final TiffField field = jpegMetadata.findEXIFValueWithExactMatch(tagInfo);
    if (field == null) {
      System.out.println(tagInfo.name + ": " + "Not Found.");
    } else {
      System.out.println(tagInfo.name + ": " + field.getValueDescription());
    }
  }

  private static String formatDateTime(Timestamp timestamp) {
    Instant instant = Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos());
    ZonedDateTime zdt = instant.atZone(ZoneId.of("America/Chicago"));
    return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.US)
        .format(zdt.toLocalDateTime());
  }
}
