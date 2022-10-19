package com.mycompany.stockgo;

import java.util.*;
import java.util.regex.Pattern;

public class stockgo {

    static Scanner input = new Scanner(System.in);
    static String command = "";
    static ArrayList<String> data = new ArrayList<>();

    public static void main(String[] in) throws Exception {
        new manager().update();
        home_layout();
        var match = Pattern.compile("-\\w+");
        while (!(command = input.nextLine()).matches("exit")) {
            var match_tmp = match.matcher(command);
            if (!match_tmp.find()) {
                System.out.println("invalid command");
                continue;
            }
            switch (match_tmp.group(0)) {
                case "-M" -> {
                    var tmp = command.replace("-M ", "");
                    manager(tmp);
                }
                case "-S" -> {
                    var tmp = command.replace("-S ", "");
                    selecter(tmp);
                }
                default -> System.out.println("command not found");
            }
            home_layout();
        }
    }

    public static void manager(String in) throws Exception {
        manage_layout();
        var manager = new manager();
        var in_done = false;
        do {
            var cmd = in_done ? command : in;
            var match = in_done ?
                    Pattern.compile("-\\w+").matcher(command) : Pattern.compile("-\\w+").matcher(in);
            switch (match.find() ? match.group(0) : "") {
                case "-A" -> manager.add(new ArrayList<String>(Arrays.asList(cmd
                        .replace("-A ", "").split(","))));
                case "-U" -> manager.update();
                case "-D" -> {
                    manager.delete(Integer.parseInt(cmd.replace("-D ", "")));
                    manage_layout();
                }
                case "-R" -> manager.reset_config();
                default -> System.out.println("command not found");
            }
            in_done = true;
        } while (!(command = input.nextLine()).matches("(home|exit)"));

    }

    public static void selecter(String in) throws Exception {
        select_layout();
        var in_done = false;
        selecter session = null;
        do {
            var cmd = in_done ? command : in;
            var match = in_done ?
                    Pattern.compile("-\\w+").matcher(command) : Pattern.compile("-\\w+").matcher(in);
            switch (match.find() ? match.group(0) : "") {
                case "-D" -> {
                    var tmp = new selecter(new ArrayList<String>(Arrays.asList(cmd
                            .replace("-D ", "").split(","))));
                    data = tmp.select(true);
                    session = tmp;
                }
                case "-E" -> {
                    if (data.isEmpty()) {
                        System.out.println("please select data(-D) first");
                        break;
                    }
                    session.export(cmd.replace("-E ", ""), true);
                }
                case "-BT" -> {
                    if (data.isEmpty()) {
                        System.out.println("please select data(-D) first");
                        break;
                    }
                    var count = 0;
                    for (var tmp : session.mark_exp_val()){
                        System.out.println(count+".");
                        System.out.println(tmp);
                    }
                }
                case "-detail" -> {
                    if (data.isEmpty()) {
                        System.out.println("please select data(-D) first");
                        break;
                    }
                    System.out.println("request:");
                    System.out.println(session.getRequest());
                    System.out.println("data:");
                    System.out.println(data.subList(0, 10) + " ...");
                }
                default -> System.out.println("command not found");
            }
            in_done = true;
        } while (!(command = input.nextLine()).matches("(home|exit)"));
    }

    public static void home_layout() {
        System.out.println("Select function:");
        System.out.println("                -M(manage config.txt) -S(select data)");
    }

    public static void manage_layout() throws Exception {
        System.out.println("Select \"manage\" function:");
        System.out.println("                           -A(add list) -U(update list) -D(del list) -R(reset list)");
        System.out.println(new config().getConfig());
    }

    public static void select_layout() throws Exception {
        System.out.println("Select \"select\" function:");
        System.out.println("                           -D(select data) -DE(select&export data) -BT(back test data)");
    }

}
