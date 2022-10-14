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
        var match = Pattern.compile("-\\w");
        while (!(command = input.next()).matches("exit")) {
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
            }home_layout();
        }
    }

    public static void manager(String in) throws Exception {
        manage_layout();
        var manager = new manager();
        var in_done = false;
        do {
            var cmd = in_done ? command : in;
            var match = in_done ?
                    Pattern.compile("-\\w").matcher(command) : Pattern.compile("-\\w").matcher(in);
            switch (match.find() ? match.group(0) : "") {
                case "-A" -> manager.add((ArrayList<String>) Arrays.asList
                        (cmd.replace("-A ", "").split(",")));
                case "-U" -> manager.update();
                case "-D" -> manager.delete((ArrayList<String>) Arrays.asList
                        (cmd.replace("-A ", "").split(",")));
                case "-R" -> manager.reset_config();
                default -> System.out.println("command not found");
            }
            in_done = true;
        } while (!(command = input.next()).matches("(home|exit)"));

    }

    public static void selecter(String in) throws Exception {
        select_layout();
        var in_done = false;
        do {
            var cmd = in_done ? command : in;
            var match = in_done ?
                    Pattern.compile("-\\w").matcher(command) : Pattern.compile("-\\w").matcher(in);
            switch (match.find() ? match.group(0) : "") {
                case "-S" -> {
                    var tmp = new selecter((ArrayList<String>) Arrays.asList(cmd.split(" ")[1].split(",")));
                    data = tmp.select(true);

                }
                case "-SE" -> {
                    var tmp = new selecter((ArrayList<String>) Arrays.asList(cmd.split(" ")[1].split(",")));
                    data = tmp.select(true);
                    tmp.export(cmd.split(" ")[2], true);

                }
                default -> System.out.println("command not found");
            }
            in_done = true;

        } while (!(command = input.next()).matches("(home|exit)"));
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

    public static void select_layout() {
        System.out.println("Select \"select\" function:");
        System.out.println("                           -S(select data) -SE(select&export data)");
    }

    public ArrayList<String> getData() {
        var out = data;
        return out;
    }

}
