package hianova.stockgo;

import java.util.ArrayList;
import java.util.HashMap;

import javax.script.ScriptEngineManager;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;

public class BackTest {

  private final ArrayList<String>[] data;
  private ArrayList<Integer> mark;
  private double pos_odd, neg_odd;
  private BigInteger pos_point, neg_point;

  public BackTest(ArrayList<String>[] data_in, String file_in) throws Exception {
    var script = new ScriptEngineManager().getEngineByName("javascript");
    data = data_in;

    script.put("data_in", data);
    mark = (ArrayList<Integer>) script.eval(new String(Files.readAllBytes(Paths.get(file_in))));
  }

  public String expecVal(String time_in) {
    String out = "";

    return out;
  }

  public HashMap<String, String> getStatistics() {
    var out = new HashMap<String, String>();

    out.put("pos_odd", String.valueOf(pos_odd));
    out.put("neg_odd", String.valueOf(neg_odd));
    out.put("pos_point", pos_point.toString());
    out.put("neg_point", neg_point.toString());
    return out;
  }
}