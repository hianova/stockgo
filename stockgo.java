package com.mycompany.stockgo;

import java.util.*;
import java.util.regex.Pattern;

public class stockgo {

    public static void main(String[] in) throws Exception {
        var manage = new manager();
        var main = new stockgo();
        var input = new Scanner(System.in);
        var pat = Pattern.compile("(-\\w|-\\w \\w*)");
        var command = "";
        manage.update();
        home_layout();

        while (!(command = input.next()).matches("exit")) {
            var mat = pat.matcher(command);
            var cmd_tmp = command.split(" ");
            if (!mat.find()) {
                System.out.println("invalid command");
                continue;
            }
            switch (cmd_tmp[0]) {
                case "-M": {
                    main.manage_list(cmd_tmp.length == 1 ? input.next() : cmd_tmp[1]);
                    break;
                }
                case "-S": {
                    main.select_data(cmd_tmp.length == 1 ? input.next() : cmd_tmp[1]);
                    break;
                }
                default:
                    System.out.println("invalid command");
            }
            home_layout();
        }
    }


    public void manage_list(String in) {
        manage_layout();

    }

    public void select_data(String in) {
        select_layout();
    }

    public static void home_layout() {
        System.out.println("Select function:");
        System.out.println("                -M(manage config.txt) -S(select data)");
    }

    public static void manage_layout() {
        System.out.println("Select \"manage\" function:");
        System.out.println("                           -A(add list) -U(update list) -D(del list) -R(reset list)");
    }

    public static void select_layout() {
        System.out.println("Select \"select\" function:");
        System.out.println("                           -S(select data) -E(export data)");
    }

}
