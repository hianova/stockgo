package com.mycompany.stockgo;

import javax.script.ScriptEngineManager;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class expectval {
    ArrayList<String> data;
    ArrayList<Integer> mark;
    int plus_odd, minus_odd, plus_points, minus_points;

    public expectval(ArrayList<String> in, ArrayList<Integer> mark_in) {
        data = new ArrayList<String>() {{
            addAll(in);
        }};
        mark = new ArrayList<Integer>() {{
            addAll(mark_in);
        }};
    }

    public ArrayList<String> compare(String in) {
        var out = new ArrayList<String>();
        var in_tmp = in.split(":");
        var script = new ScriptEngineManager().getEngineByName("JavaScript");

        mark.forEach((mark_tmp) -> {
            var time_tmp = LocalDate.parse(data.get(mark_tmp), DateTimeFormatter.ofPattern("yyyyMMdd"));
            var data_tmp = data.get(mark_tmp + 1);
            var end = 0;
            try {
                var target_data = script.eval(in_tmp[0].replace("@mark", data_tmp));
                switch (in_tmp[1]) {
                    case "D" -> {
                        LocalDate tmp = null;
                        for (var count_tmp = 0; tmp.isBefore(time_tmp.plusDays(1)); count_tmp += 2) {
                            tmp = LocalDate.parse(data.get(end), DateTimeFormatter.ofPattern("yyyyMMdd"));
                            end = count_tmp;
                        }
                    }
                    case "W" -> {
                        LocalDate tmp = null;
                        for (var count_tmp = 0; tmp.isBefore(time_tmp.plusWeeks(1)); count_tmp += 2) {
                            tmp = LocalDate.parse(data.get(end), DateTimeFormatter.ofPattern("yyyyMMdd"));
                            end = count_tmp;
                        }
                    }
                    case "M" -> {
                        LocalDate tmp = null;
                        for (var count_tmp = 0; tmp.isBefore(time_tmp.plusMonths(1)); count_tmp += 2) {
                            tmp = LocalDate.parse(data.get(end), DateTimeFormatter.ofPattern("yyyyMMdd"));
                            end = count_tmp;
                        }
                    }
                    case "Y" -> {
                        LocalDate tmp = null;
                        for (var count_tmp = 0; tmp.isBefore(time_tmp.plusYears(1)); count_tmp += 2) {
                            tmp = LocalDate.parse(data.get(end), DateTimeFormatter.ofPattern("yyyyMMdd"));
                            end = count_tmp;
                        }
                    }
                }
                var tmp = Integer.parseInt(data.get(end)) - Integer.parseInt(target_data.toString());
                out.add(String.valueOf(tmp));
                if (tmp < 0) {
                    minus_points += tmp;
                    minus_odd += 1;
                }
                if (tmp > 0) {
                    plus_points += tmp;
                    plus_odd += 1;
                }
            } catch (Exception e) {
                System.out.println("compare operator invalid");
            }
        });
        plus_odd = plus_odd / mark.size();
        minus_odd = minus_odd / mark.size();
        return out;
    }

    public String getExpectval() {
        var out = String.valueOf(plus_odd * plus_points + minus_odd * minus_points);
        return out;
    }

    public String getPlus_ref() {
        var out = "odd:" + String.valueOf(plus_odd) + "points:" + plus_points;
        return out;
    }

    public String getMinus_ref() {
        var out = "odd:" + String.valueOf(minus_odd) + "points:" + minus_points;
        return out;
    }
}
