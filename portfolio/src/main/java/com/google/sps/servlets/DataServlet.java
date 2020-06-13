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

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.gson.Gson; 
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.time.*;
import java.time.format.DateTimeFormatter;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that returns some example content. TODO: modify this file to handle comments data */
@WebServlet("/data")
public class DataServlet extends HttpServlet {
  private DatastoreService datastore;
  
  @Override
  public void init() {
    datastore = DatastoreServiceFactory.getDatastoreService();
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Query query = new Query("Comment").addSort("timestamp", SortDirection.DESCENDING);
    PreparedQuery results = datastore.prepare(query);
    List<String> comments = new ArrayList<>();

    for (Entity comment : results.asIterable()) {
      String commenterName = (String) comment.getProperty("commenter");
      String text = (String) comment.getProperty("text");
      Instant timestamp = Instant.parse((String) comment.getProperty("timestamp"));
      DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        .withZone(ZoneId.systemDefault());
      String strDate = dateFormat.format(timestamp);

      String formmatedComment = String.format("[%s] %s says: \"%s\"", strDate, commenterName, text);
      comments.add(formmatedComment);
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

    Entity commentEntity = new Entity("Comment");
    commentEntity.setProperty("commenter", commenterName);
    commentEntity.setProperty("text", comment);
    commentEntity.setProperty("timestamp", timestamp.toString());

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
}
