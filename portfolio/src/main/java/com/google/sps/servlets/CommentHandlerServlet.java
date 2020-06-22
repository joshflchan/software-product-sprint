// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobInfoFactory;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.ServingUrlOptions;
import com.google.gson.Gson; 
import com.google.sps.data.Comment;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
<<<<<<< HEAD:portfolio/src/main/java/com/google/sps/servlets/CommentHandlerServlet.java
import java.time.format.DateTimeFormatter;
import java.time.Instant;
import java.time.ZoneId;
=======
import java.time.Instant;
>>>>>>> Add Comment class to convert to Json:portfolio/src/main/java/com/google/sps/servlets/DataServlet.java
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that processes and displays comment data including text and images.*/
@WebServlet("/comments")
public class CommentHandlerServlet extends HttpServlet {
  private DatastoreService datastore;
  
  @Override
  public void init() {
    datastore = DatastoreServiceFactory.getDatastoreService();
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Get comments and sort by most recent
    Query query = new Query("Comment").addSort("timestamp", SortDirection.DESCENDING);
    PreparedQuery results = datastore.prepare(query);
    List<Comment> comments = new ArrayList<>();

    // Loop over all Comment entities and format to display
    for (Entity commentEntity : results.asIterable()) {
      String commenterName = (String) commentEntity.getProperty("commenter");
      String text = (String) commentEntity.getProperty("text");
      Instant timestamp = Instant.parse((String) commentEntity.getProperty("timestamp"));
      String imageUrl = (String) commentEntity.getProperty("imageUrl");

      Comment comment = new Comment(timestamp, commenterName, text, imageUrl);
      comments.add(comment);
    }

    // Convert the list of comments to JSON
    Gson gson = new Gson();
    String json = gson.toJson(comments);

    // Send the JSON as the response
    response.setContentType("application/json;");
    response.getWriter().println(json);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Get the input from the form.
    String commenterName = getParameter(request, "commenter-name", "Anonymous");
    String comment = getParameter(request, "text-input", "Nothing...");
    Instant timestamp = Instant.now(); 
    // Get the URL of the image that the user uploaded to Blobstore.
    String imageUrl = getUploadedFileUrl(request, "image");

    // Create Comment Entity in Datastore service
    Entity commentEntity = new Entity("Comment");
    commentEntity.setProperty("commenter", commenterName);
    commentEntity.setProperty("text", comment);
    commentEntity.setProperty("timestamp", timestamp.toString());
    commentEntity.setProperty("imageUrl", imageUrl);
    datastore.put(commentEntity);

    // Redirect back to the HTML page.
    response.sendRedirect("/index.html");
  }

  /**
   * @return the request parameter, or the default value if the parameter
   *         was not specified by the client
   */
  private String getParameter(HttpServletRequest request, String name, String defaultValue) {
    String value = request.getParameter(name);
    if (value == null || value.isEmpty() || value.trim().isEmpty()) {
      return defaultValue;
    }
    return value;
  }

  /** Returns a URL that points to the uploaded file, or null if the user didn't upload a file. */
  private String getUploadedFileUrl(HttpServletRequest request, String formInputElementName) {
    BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
    Map<String, List<BlobKey>> blobs = blobstoreService.getUploads(request);
    List<BlobKey> blobKeys = blobs.get("image");

    // User submitted form without selecting a file, so we can't get a URL. (dev server)
    if (blobKeys == null || blobKeys.isEmpty()) {
      return null;
    }

    // Our form only contains a single file input, so get the first index.
    BlobKey blobKey = blobKeys.get(0);

    // User submitted form without selecting a file, so we can't get a URL. (live server)
    BlobInfo blobInfo = new BlobInfoFactory().loadBlobInfo(blobKey);
    if (blobInfo.getSize() == 0) {
      blobstoreService.delete(blobKey);
      return null;
    }

    // Use ImagesService to get a URL that points to the uploaded file.
    ImagesService imagesService = ImagesServiceFactory.getImagesService();
    ServingUrlOptions options = ServingUrlOptions.Builder.withBlobKey(blobKey);
    String url = imagesService.getServingUrl(options);

    // GCS's localhost preview is not actually on localhost,
    // so make the URL relative to the current domain.
    if(url.startsWith("http://localhost:8080/")){
      url = url.replace("http://localhost:8080/", "/");
    }
    return url;
  }
}
