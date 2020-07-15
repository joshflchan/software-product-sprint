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

package com.google.sps;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public final class FindMeetingQuery {
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    ArrayList<TimeRange> freeTimes = new ArrayList<>();

    if (request.getDuration() > TimeRange.WHOLE_DAY.duration()){
      return freeTimes;
    }

    ArrayList<TimeRange> busyTimes = getBusyTimes(events, request);
    int lastFinish = TimeRange.START_OF_DAY;

    if (request.getAttendees().isEmpty() || busyTimes.isEmpty()){
      return Arrays.asList(TimeRange.WHOLE_DAY);
    }

    Collections.sort(busyTimes, TimeRange.ORDER_BY_START);
    for (TimeRange timeRange : busyTimes){

        // Add the free time between meetings.
        if (timeRange.start() > lastFinish){
            TimeRange freeTimeRange = TimeRange.fromStartEnd(lastFinish, timeRange.start(), false);

            // Only add free time that's long enough for the requested meeting.
            if (freeTimeRange.duration() >= request.getDuration()){
                freeTimes.add(freeTimeRange);
            }
        }

        // Don't update lastFinish for meetings contained by timeRange.
        if (timeRange.end() >= lastFinish){
            lastFinish = timeRange.end();
        }
    }

    // Get free time after all other meetings end.
    if (lastFinish < TimeRange.END_OF_DAY){
        TimeRange freeTimeRange = TimeRange.fromStartEnd(lastFinish, TimeRange.END_OF_DAY, true);
        if (freeTimeRange.duration() >= request.getDuration()){
            freeTimes.add(freeTimeRange);
        }
    }
    return freeTimes;
  }

  /**
   * Considers all request attendees and creates a list of busy times
   */
  private ArrayList<TimeRange> getBusyTimes(Collection<Event> events, MeetingRequest request){
    ArrayList<TimeRange> busyTimes = new ArrayList<>();
    Collection<String> requestAttendees = request.getAttendees();
    for (Event e: events){
      Set<String> eventAttendees = e.getAttendees();
      //if at least one request attendee is in the event then add to busyTimes
      if (!Collections.disjoint(requestAttendees, eventAttendees)){
        busyTimes.add(e.getWhen());
      }
    }
    return busyTimes;
  }
}
