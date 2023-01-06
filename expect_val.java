package com.mycompany.stockgo;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class expect_val {

  private final ArrayList<String> data, time;
  private final ArrayList<Integer> mark;
  private final DateTimeFormatter uni_date;
  private double plus_odd, minus_odd, plus_points, minus_points;

  public expect_val(ArrayList<String> in, ArrayList<Integer> mark_in) {
    data = new ArrayList<>();
    time = new ArrayList<>();
    mark = new ArrayList<>(mark_in);
    uni_date = new checksyn().getUni_date();

    for (var count = 0; count < in.size(); count++) {
      time.add(in.get(count));
      data.add(in.get(count++));
    }
  }

  public String compare(String in) {
    var out = "";
    plus_odd = plus_points = minus_odd = minus_points = 0;

    mark.forEach((mark_tmp)-> {
      var time_tmp = LocalDate.parse(time.get(mark_tmp), uni_date);
      var target = 0;

      try {
        switch (in) {
          case "D" -> {
            var tmp = time_tmp.plusDays(1);
            for (var count = 1; !time.contains(tmp.toString()); count++) {
              tmp = time_tmp.plusDays(count);
            }
            target = time.indexOf(tmp.toString());
          }
          case "W" -> {
            var tmp = time_tmp.plusWeeks(1);
            for (var count = 1; !time.contains(tmp.toString()); count++) {
              tmp = time_tmp.plusDays(count);
            }
            target = time.indexOf(tmp.toString());
          }
          case "M" -> {
            var tmp = time_tmp.plusMonths(1);
            for (var count = 1; !time.contains(tmp.toString()); count++) {
              tmp = time_tmp.plusDays(count);
            }
            target = time.indexOf(tmp.toString());
          }
          case "HY" -> {
            var tmp = time_tmp.plusMonths(6);
            for (var count = 1; !time.contains(tmp.toString()); count++) {
              tmp = time_tmp.plusDays(count);
            }
            target = time.indexOf(tmp.toString());
          }
          case "Y" -> {
            var tmp = time_tmp.plusYears(1);
            for (var count = 1; !time.contains(tmp.toString()); count++) {
              tmp = time_tmp.plusDays(count);
            }
            target = time.indexOf(tmp.toString());
          }
        }
        var tmp = Integer.parseInt(data.get(target + 1)) - Integer.parseInt(data.get(mark_tmp + 1));
        if (tmp < 0) {
          minus_points += tmp;
          minus_odd += 1;
        }
        if (tmp > 0) {
          plus_points += tmp;
          plus_odd += 1;
        }
      } catch (Exception e) {
        System.out.println("compare operator invalid " + e);
      }
    });
    plus_odd = plus_odd / mark.size();
    minus_odd = minus_odd / mark.size();
    out = String.valueOf(plus_odd * plus_points + minus_odd * minus_points);
    return out;
  }
}
