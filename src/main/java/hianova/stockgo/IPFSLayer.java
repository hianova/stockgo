package hianova.stockgo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.ipfs.api.IPFS;
import io.ipfs.api.NamedStreamable.ByteArrayWrapper;
import io.ipfs.cid.Cid;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.Objects;

public class IPFSLayer extends Config {

  private final IPFS ipfs;

  public IPFSLayer() throws Exception {
    ipfs = new IPFS("/ip4/127.0.0.1/tcp/5001");
    ipfs.refs.local();
  }

  public String share(int numIn) throws Exception {
    String out;
    var json = new ObjectMapper().createObjectNode();
    var config = json.putObject("config");
    var file = json.putObject("file");

    config.put("title", label_title.get(numIn));
    config.put("URL", label_URL.get(numIn));
    config.put("folder", label_folder.get(numIn));
    config.put("tag", label_tag.get(numIn));
    config.put("status", label_status.get(numIn));

    Arrays
        .stream(Objects.requireNonNull(new File(downloads_dir + label_folder.get(numIn) + File.separator).listFiles()))
        .forEach(next -> {
          try (var input = new FileInputStream(next)) {
            var tmp = ipfs.add(new ByteArrayWrapper(input.readAllBytes()));
            file.putObject(next.getName().split("\\.")[0])
                .put("/", tmp.get(0).toString().split("-")[0]);
          } catch (Exception e) {
            System.out.println("IPFS can't upload " + e);
          }
        });

    out = ipfs.dag.put(json.toPrettyString().getBytes()).toString().split("-")[0];
    return out;
  }

  public void add(String CIDIn) throws Exception {
    var json = (ObjectNode) new ObjectMapper().readTree(ipfs.dag.get(Cid.decode(CIDIn)));
    var dir = downloads_dir + json.at("/config/title").textValue() + File.separator;

    json.at("/file").fields().forEachRemaining(next -> new Thread(() -> {
      try {
        new File(dir).mkdir();
        var output = new FileOutputStream(dir + next.getKey() + ".txt");
        output.write(ipfs.get(Cid.decode(next.getValue().get("/").textValue())));
        output.close();
      } catch (Exception e) {
        System.out.println("IPFS gone wrong " + e);
      }
    }).start());

    label_title.add(json.at("/config/title").textValue());
    label_URL.add(json.at("/config/URL").textValue());
    label_folder.add(json.at("/config/folder").textValue());
    label_tag.add(json.at("/config/tag").textValue());
    label_status.add(json.at("/config/status").textValue());
    syncConfig();
  }
}
