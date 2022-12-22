package com.mycompany.stockgo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Scanner;
import java.util.regex.Pattern;

public class stockgo {

  static manager man;
  static selecter sel;
  static ArrayList<String> data;

  public static void main(String[] in) throws Exception {
    Scanner input = new Scanner(System.in);
    String command;

    if (in.length > 0 && Objects.equals(in[0], "-A")) {
      new manager().add(new ArrayList<>(Arrays.asList(in[1].split(","))));
      return;
    }
    new manager().update();
    home_layout();
    while (!(command = input.nextLine()).matches("exit")) {
      var match = Pattern.compile("-\\w+").matcher(command);
      if (!match.find()) {
        System.out.println("invalid command");
        continue;
      }
      switch (match.group(0)) {
        case "-M" -> {
          manage_layout();
          var first_done = false;
          do {
            var tmp = first_done ? command : command.replace("-M ", "");
            manager(tmp);
            first_done = true;
          } while (!(command = input.nextLine()).matches("(home|exit)"));
        }
        case "-S" -> {
          select_layout();
          var first_done = false;
          do {
            var tmp = first_done ? command : command.replace("-S ", "");
            selecter(tmp);
            first_done = true;
          } while (!(command = input.nextLine()).matches("(home|exit)"));
        }
        default -> System.out.println("command not found");
      }
      home_layout();
    }
  }

  public static void manager(String in) throws Exception {
    var match = Pattern.compile("-\\w+").matcher(in);
    man = new manager();

    switch (match.find() ? match.group(0) : "") {
      case "-A" -> man.add(new ArrayList<>(Arrays.asList(in
          .replace("-A ", "").split(","))));
      case "-U" -> man.update();
      case "-D" -> {
        man.delete(Integer.parseInt(in.replace("-D ", "")));
        manage_layout();
      }
      case "-R" -> {
        comfirm_layout();
        if (new Scanner(System.in).nextLine().matches("-Y")) {
          man.reset_config();
        }
      }
      case "-syntax" -> syntax_layout(1);
      default -> System.out.println("command not found");
    }
  }

  public static void selecter(String in) throws Exception {
    var match = Pattern.compile("-\\w+").matcher(in);

    switch (match.find() ? match.group(0) : "") {
      case "-D" -> {
        var tmp = new selecter(new ArrayList<>(Arrays.asList(in
            .replace("-D ", "").split(","))));
        data = tmp.select(true);
        sel = tmp;
      }
      case "-E" -> {
        if (sel == null) {
          System.out.println("please select data(-D) first");
          break;
        }
        var in_tmp = in.replace("-E ", "");
        sel.export(in_tmp, true);
      }
      case "-BT" -> {
        if (sel == null) {
          System.out.println("please select data(-D) first");
          break;
        }
        sel.setMark(in.replace("-BT ", ""));
        System.out.println(sel.mark_exp_val());
      }
      case "-detail" -> {
        if (sel == null) {
          System.out.println("please select data(-D) first");
          break;
        }
        System.out.println("request:");
        System.out.println(sel.getRequest());
        System.out.println("data:");
        System.out.println(data.subList(0, 10) + " ...");
      }
      case "-syntax" -> syntax_layout(2);
      default -> System.out.println("command not found");
    }
  }

  public static void home_layout() {
    System.out.println("Select function:");
    System.out.println("                -M(manage config.txt) -S(select data)");
  }

  public static void manage_layout() throws Exception {
    System.out.println("Select \"manage\" function:");
    System.out.println("                           -A(add list) -U(update list) -D(del list)\n");
    System.out.println("                           -R(reset list) -syntax(how to use)");
    System.out.println(new config().getConfig());
  }

  public static void select_layout() {
    System.out.println("Select \"select\" function:");
    System.out.println(
        "                           -D(select data) -E(export data) -BT(back test data)");
    System.out.println(
        "                           -detail(quick check on data) -syntax(how to use)");
  }

  public static void syntax_layout(int in) {
    switch (in) {
      case 1 -> {
        System.out.println("\n-M(manage config.txt) page command:");
        System.out.println(
            "    -A(add list): type in -A [URL,custom title,custom folder_name,tags,date]");
        System.out.println("    -U(update list): update to today");
        System.out.println("    -D(del list): type in -D [number of line on list]");
        System.out.println("    -R(reset list): reset to clean config");
      }
      case 2 -> {
        System.out.println("\n-S(select data) page command:");
        System.out.println(
            "    -D(select data): type in -D [URL/title -request req,req... option:(-date 8digit~8digit)(-numbers num.num...)],[]...");
        System.out.println("    -E(export data): type in -E [path](default:downloads/exports.csv)");
        System.out.println("    -BT(back test data): type in -BT [strategy]");
        System.out.println("    -detail(quick check on data): brief detail of data");
      }
    }
  }

  public static void comfirm_layout() {
    System.out.println("Are you sure?");
    System.out.println("             -Y(yes) -N(no)");
  }

  public ArrayList<String> getData() {
    var out = data;
    return out;
  }

}
