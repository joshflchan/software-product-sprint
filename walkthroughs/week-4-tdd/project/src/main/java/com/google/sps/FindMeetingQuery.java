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
    ArrayList<TimeRange> busyTimes = getBusyTimes(events, request);

    if (request.getAttendees().isEmpty()){
      return Arrays.asList(TimeRange.WHOLE_DAY);
    }

    if (request.getDuration() > TimeRange.WHOLE_DAY.duration()){
      return freeTimes;
    }

    if (busyTimes.isEmpty()){
      freeTimes.add(TimeRange.WHOLE_DAY);
    }

    for (int i = 0; i < busyTimes.size(); i++){
      TimeRange timeRange = busyTimes.get(i);
      if (i == 0){
        TimeRange firstTimeRange = TimeRange.fromStartEnd(TimeRange.START_OF_DAY, timeRange.start(), false);
        if (firstTimeRange.start() != firstTimeRange.end()){
          freeTimes.add(firstTimeRange); 
        }
      } else {
        TimeRange previousTimeRange = busyTimes.get(i-1);
        TimeRange betweenTimeRange = TimeRange.fromStartEnd(previousTimeRange.end(), timeRange.start(), false);
        if (betweenTimeRange.duration() >= request.getDuration()){
          freeTimes.add(betweenTimeRange);
        }
      }
      if (i == busyTimes.size() - 1){
        TimeRange endTimeRange = TimeRange.fromStartEnd(timeRange.end(), TimeRange.END_OF_DAY, true);
        if (endTimeRange.start() != endTimeRange.end()){
          freeTimes.add(endTimeRange);
        }
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
    ArrayList<TimeRange> sortedBusyTimes = sortBusyTimes(busyTimes);
    return sortedBusyTimes;
  }

  /**
   * Sorts busyTimes by start time and updates time ranges if there are overlaps
   * or nested events. 
   */
  private ArrayList<TimeRange> sortBusyTimes(ArrayList<TimeRange> busyTimes){
    Collections.sort(busyTimes, TimeRange.ORDER_BY_START);
    for (int i = 0; i < busyTimes.size() - 1; i++){
      TimeRange timeRange = busyTimes.get(i);
      TimeRange nextTimeRange = busyTimes.get(i+1);
      if (timeRange.contains(nextTimeRange)){
        // Keep the longer time
        busyTimes.remove(nextTimeRange);
      } else if (nextTimeRange.contains(timeRange)){
        // Keep the longer time
        busyTimes.remove(timeRange);
      } else if (timeRange.overlaps(nextTimeRange)){
        TimeRange mergedTimeRange = TimeRange.fromStartEnd(timeRange.start(), nextTimeRange.end(),false);
        busyTimes.remove(timeRange);
        busyTimes.remove(nextTimeRange);
        busyTimes.add(mergedTimeRange);
      } 
    }
    return busyTimes;
  }
}
