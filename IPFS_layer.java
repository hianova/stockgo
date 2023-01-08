package com.mycompany.stockgo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.ipfs.api.IPFS;
import io.ipfs.api.NamedStreamable.ByteArrayWrapper;
import io.ipfs.cid.Cid;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Arrays;

public class IPFS_layer extends config {

  public final IPFS ipfs;

  public IPFS_layer() throws Exception {
    ipfs = new IPFS("/ip4/127.0.0.1/tcp/5001");
    ipfs.refs.local();
  }

  public String share_file(int in) throws Exception {
    var json = new ObjectMapper().createObjectNode();
    var json_config = json.putObject("config");
    var json_file = json.putObject("file");
    String out;

    json_config.put("title", label_title.get(in));
    json_config.put("URL", label_URL.get(in));
    json_config.put("folder", label_folder.get(in));
    json_config.put("tag", label_tag.get(in));
    json_config.put("status", label_status.get(in));
    Arrays.stream(new File(downloads_dir + label_folder.get(in) + File.separator).listFiles())
        .iterator().forEachRemaining((file) -> {
          try {
            var tmp = ipfs.add(new ByteArrayWrapper(new FileInputStream(file).readAllBytes()));
            json_file.putObject(file.getName().split("\\.")[0])
                .put("/", tmp.get(0).toString().split("-")[0]);
          } catch (Exception e) {
            System.out.println("IPFS cant upload " + e);
          }
        });
    out = ipfs.dag.put(json.toPrettyString().getBytes()).toString().split("-")[0];
    return out;
  }
  public void get_file(String in) throws Exception {
    var json = (ObjectNode) new ObjectMapper().readTree(ipfs.dag.get(Cid.decode(in)));
    var tmp = downloads_dir + json.at("/config/title").textValue() + File.separator;

    json.at("/file").fields().forEachRemaining((file_tmp) -> new Thread(() -> {
      try {
        new File(tmp).mkdir();
        new File(tmp + file_tmp.getKey() + ".txt").createNewFile();
        var file_out = new FileOutputStream(tmp + file_tmp.getKey() + ".txt");
        file_out.write(ipfs.get(Cid.decode(file_tmp.getValue().get("/").textValue())));
        file_out.close();
      } catch (Exception e) {
        System.out.println("IPFS gone wrong " + e);
      }
    }).start());
    label_title.add(json.at("/config/title").textValue());
    label_URL.add(json.at("/config/URL").textValue());
    label_folder.add(json.at("/config/folder").textValue());
    label_tag.add(json.at("/config/tag").textValue());
    label_status.add(json.at("/config/status").textValue());
    sync_config();
  }
}
