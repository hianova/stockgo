package hianova.stockgo;

import static java.lang.System.out;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

public class Stockgo {

  static Manager man;
  static Selecter sel;
  static IPFSLayer ipfs;
  static ArrayList<String>[] data;

  public static void main(String[] args) throws Exception {
    String cmd;
    var input = new Scanner(System.in);

    if (args.length > 0) {
      for (var tmp : args) {
        manager("-A " + tmp);
      }
      input.close();
      return;
    }
    manager("-U");
    homeLayout();
    while (!(cmd = input.nextLine()).matches("exit")) {
      var match = Pattern.compile("-\\w+").matcher(cmd);
      if (!match.find()) {
        out.println("invalid command");
        continue;
      }
      switch (match.group(0)) {
        case "-M" -> {
          managerLayout();
          while (!(cmd = input.nextLine()).matches("(exit)")) {
            manager(cmd);
          }
        }
        case "-S" -> {
          selecterLayout();
          while (!(cmd = input.nextLine()).matches("(exit)")) {
            selecter(cmd);
          }
        }
        case "-IPFS" -> {
          ipfsLayout();
          while (!(cmd = input.nextLine()).matches("(exit)")) {
            IPFSLayer(cmd);
          }
        }
        default -> out.println("command not exist");
      }
      homeLayout();
    }
    input.close();
  }

  private static void homeLayout() {
    out.println("Select function:");
    out.println("                -M(manage config) -S(select data) -IPFS(IPFS dedicate)");
  }

  private static void managerLayout() {
    out.println("Select \"manage\" function:");
    out.println("                           -A(add list) -U(update list) -D(del list)\n");
    out.println("                           -help(how to use)");
    out.println(man.listConfig());
  }

  private static void selecterLayout() {
    out.println("Select \"select\" function:");
    out.println("                           -D(select data) -E(export data) -T(back test data)");
    out.println("                           -view(quick view on data)");
    out.println("                           -help(how to use)");
  }

  private static void ipfsLayout() {
    out.println("Select \"IPFS\" function:");
    out.println("                         -I(import list) -O(export list)");
    out.println("                         -help(how to use)");
  }

  private static void helpLayout(String cmdIn) {
    switch (cmdIn) {
      case "manager" -> {
        out.println("\n-M(manage config.txt) page command:");
        out.println("    -A(add list): type in -A [custom title,URL,custom folder_name,tags,date]");
        out.println("    -U(update list): update to now");
        out.println("    -D(del list): type in -D [number of list]");
      }
      case "selecter" -> {
        out.println("\n-S(select data) page command:");
        out.println(
            "    -D(select data): type in -D [title -request req.req... option:(-date 8digit~8digit)(-numbers num.num...)],[]...");
        out.println("    -E(export data): type in -E [path](default:downloads/exports.csv)");
        out.println("    -T(back test data): type in -T [strategy]");
        out.println("    -detail(quick check on data): preview ten elements per column");
      }
      case "ipfsLayer" -> {
        out.println("\n-IPFS(IPFS dedicate) page command:");
        out.println("    -I(import list): type in -I [CID]");
        out.println("    -O(export list): type in -O [number of list]");
      }
    }
  }

  public static void manager(String cmdIn) throws Exception {
    var match = Pattern.compile("-\\w+").matcher(cmdIn);

    if (man == null) {
      man = new Manager();
    }
    switch (match.find() ? match.group(0) : "") {
      case "-A" -> man.add(new ArrayList<>(List.of(cmdIn.replace("-A ", "").split(","))));
      case "-U" -> man.update();
      case "-D" -> {
        man.delete(Integer.parseInt(cmdIn.replace("-D ", "")));
        managerLayout();
      }
      case "-help" -> helpLayout("manager");
      default -> out.println("command not found");
    }
  }

  public static void IPFSLayer(String cmdIn) throws Exception {
    var match = Pattern.compile("-\\w+").matcher(cmdIn);

    if (ipfs == null) {
      ipfs = new IPFSLayer();
    }
    switch (match.find() ? match.group(0) : "") {
      case "-I" -> {
        ipfs.add(cmdIn.replace("-I ", ""));
        man.update();
        out.println("file imported");
        managerLayout();
      }
      case "-O" -> out.println(ipfs.share(Integer.parseInt(cmdIn.replace("-O ", ""))));
      case "-help" -> helpLayout("ipfsLayer");
      default -> out.println("command not found");
    }
  }

  public static void selecter(String cmdIn) throws Exception {
    var match = Pattern.compile("-\\w+").matcher(cmdIn);

    switch (match.find() ? match.group(0) : "") {
      case "-D" -> {
        var tmp = new ArrayList<>(List.of(cmdIn.replace("-D ", "").split(",")));
        sel = new Selecter(tmp, true);
        data = sel.select();
      }
      case "-E" -> {
        if (sel == null) {
          out.println("please select data(-D) first");
          break;
        }
        sel.export(cmdIn.replaceAll("-E( )*", ""), true);
      }
      case "-T" -> {
        if (sel == null) {
          out.println("please select data(-D) first");
          break;
        }
        // out.println(sel.back_test(cmd_in.replace("-T ", "")));
        out.println("remain develop");
      }
      case "-view" -> {
        if (sel == null) {
          out.println("please select data(-D) first");
          break;
        }
        var req = sel.getRequests();
        out.println("\nrequest:");
        IntStream.range(0, req.length).forEach(
            next -> out.println(req[next].subList(0, Math.min(req[next].size(), 10))));
        out.println("\ndata:");
        IntStream.range(0, data.length).forEach(
            next -> out.println(data[next].subList(0, Math.min(data[next].size(), 10))));
      }
      case "-syntax" -> helpLayout("selecter");
      default -> out.println("command not found");
    }
  }
}
