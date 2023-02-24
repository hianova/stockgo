package hianova.stockgo;

import static java.lang.System.out;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

public class Stockgo {

  private static Manager man;
  private static Selecter sel;
  private static IPFSLayer ipfs;
  private static ArrayList<String>[] data;
  private static final Pattern cmdPat;

  static {
    try {
      man = new Manager();
    } catch (Exception e) {
      e.printStackTrace();
    }
    try {
      ipfs = new IPFSLayer();
    } catch (Exception e) {
      e.printStackTrace();
    }
    cmdPat = Pattern.compile("-\\w+( )*");
  }

  public static void main(String[] args) throws Exception {
    String cmd;
    var input = new Scanner(System.in);
    var exitPat = Pattern.compile("exit");

    if (args.length > 0) {
      manager("add " + args[0]);
      input.close();
      return;
    }
    manager("U");
    homeLayout();
    while (!exitPat.matcher((cmd = input.nextLine())).find()) {
      var match = cmdPat.matcher(cmd);
      if (!match.find()) {
        out.println("invalid command");
        continue;
      }
      switch (match.group(0).replaceAll(" -", "")) {
        case "M" -> {
          managerLayout();
          while (!exitPat.matcher((cmd = input.nextLine())).find()) {
            manager(cmd);
          }
        }
        case "S" -> {
          selecterLayout();
          while (!exitPat.matcher((cmd = input.nextLine())).find()) {
            selecter(cmd);
          }
        }
        case "IPFS" -> {
          ipfsLayout();
          while (!exitPat.matcher((cmd = input.nextLine())).find()) {
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
    out.println("                           add(add list) update(update list) delete(del list)\n");
    out.println("                           --help(how to use)");
    out.println(man.listConfig());
  }

  private static void selecterLayout() {
    out.println("Select \"select\" function:");
    out.println("                           select(select data) export(export data) test(back test data)");
    out.println("                           view(quick view on data)");
    out.println("                           --help(how to use)");
  }

  private static void ipfsLayout() {
    out.println("Select \"IPFS\" function:");
    out.println("                         import(import list) export(export list)");
    out.println("                         --help(how to use)");
  }

  private static void helpLayout(String layoutIn) {
    switch (layoutIn) {
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
    var match = cmdPat.matcher(cmdIn);

    switch (match.find() ? match.group().trim() : "") {
      case "add" -> man.add(new ArrayList<>(List.of(cmdIn.replaceAll("add( )+", "").split(","))));
      case "update" -> man.update();
      case "delete" -> {
        man.delete(Integer.parseInt(cmdIn.replaceAll("delete( )+", "")));
        managerLayout();
      }
      case "--help" -> helpLayout("manager");
      default -> out.println("command not found");
    }
  }

  public static void IPFSLayer(String cmdIn) throws Exception {
    var match = cmdPat.matcher(cmdIn);

    switch (match.find() ? match.group(0) : "") {
      case "import" -> {
        ipfs.add(cmdIn.replaceAll("import( )+", ""));
        man.update();
        out.println("file imported");
        managerLayout();
      }
      case "export" -> out.println(ipfs.share(Integer.parseInt(cmdIn.replaceAll("export( )+", ""))));
      case "--help" -> helpLayout("ipfsLayer");
      default -> out.println("command not found");
    }
  }

  public static void selecter(String cmdIn) throws Exception {
    var match = cmdPat.matcher(cmdIn);

    switch (match.find() ? match.group(0) : "") {
      case "select" -> {
        var tmp = new ArrayList<>(List.of(cmdIn.replaceAll("select( )+", "").split(",")));
        sel = new Selecter(tmp);
        data = sel.select();
      }
      case "export" -> {
        if (sel == null) {
          out.println("please select data(select) first");
          break;
        }
        sel.export(cmdIn.replaceAll("export( )+", ""), true);
      }
      case "test" -> {
        if (sel == null) {
          out.println("please select data(select) first");
          break;
        }
        // out.println(sel.back_test(cmd_in.replace("-T ", "")));
        out.println("remain develop");
      }
      case "view" -> {
        if (sel == null) {
          out.println("please select data(select) first");
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
      case "--help" -> helpLayout("selecter");
      default -> out.println("command not found");
    }
  }
}
