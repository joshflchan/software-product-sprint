package com.google.sps.data;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;


/** Class containing comment data. */
public final class Comment {

  private final Instant timestamp;
  private final String commenterName;
  private final String text;
  private final String imageUrl;
  private final String strDate; 

  public Comment(Instant timestamp, String commenterName, String text, String imageUrl) {
    this.timestamp = timestamp;
    this.commenterName = commenterName;
    this.text = text;
    this.imageUrl = imageUrl;
    DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        .withZone(ZoneId.systemDefault());
    this.strDate = dateFormat.format(timestamp);
  }

  public Instant getTimestamp() {
    return timestamp;
  }

  public String getCommenterName() {
    return commenterName;
  }

  public String getText() {
    return text;
  }

  public String getImageUrl() {
    return imageUrl;
  }

  public String getStrDate() {
    return strDate;
  }

}
