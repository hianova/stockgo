package com.mycompany.stockgo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.ipfs.api.IPFS;
import io.ipfs.api.NamedStreamable.ByteArrayWrapper;
import io.ipfs.cid.Cid;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Arrays;

public class IPFS_layer extends config {

  public final IPFS ipfs;

  public IPFS_layer() throws Exception {
    ipfs = new IPFS("/ip4/127.0.0.1/tcp/5001");
    ipfs.refs.local();
  }

  public String share_file(int in) throws Exception {
    var out = "";
    var json = new ObjectMapper().createObjectNode();
    json.put("config",
        label_URL.get(in) + "," + label_title.get(in) + "," + label_folder.get(in) + ","
            + label_tag.get(in) + "," + label_status.get(in));
    var json_file = json.putObject("file");
    Arrays.stream(new File(downloads_dir + label_folder.get(in) + File.separator).listFiles())
        .iterator().forEachRemaining((tmp) -> {
          try {
            var cid = ipfs.add(new ByteArrayWrapper(new FileInputStream(tmp).readAllBytes()));
            json_file.put(tmp.getName().replace(".txt", ""), cid.get(0).toString().split("-")[0]);
          } catch (Exception e) {
            System.out.println("IPFS cant upload");
          }
        });
    out = ipfs.dag.put(json.toPrettyString().getBytes()).toString().replace("-", "");
    return out;
  }

  public void get_file(String in) throws Exception {
    var json = (ObjectNode) new ObjectMapper().readTree(ipfs.dag.get(Cid.decode(in)));
    var file = json.get("file").fields();
    var config = json.get("config").textValue().split(",");

    label_URL.add(config[0]);
    label_title.add(config[1]);
    label_folder.add(config[2]);
    label_tag.add(config[3]);
    label_status.add(config[4]);
    sync_config();
    file.forEachRemaining((tmp)-> new Thread(() -> {
      new File(downloads_dir + config[2] + File.separator).mkdir();
      var file_tmp = new File(downloads_dir + config[2] + File.separator + tmp.getKey() + ".txt");
      try {
        var file_out = new OutputStreamWriter(new FileOutputStream(file_tmp));
        file_tmp.createNewFile();
        file_out.write(new String(ipfs.get(Cid.decode(tmp.getValue().textValue()))));
        file_out.close();
      } catch (IOException e) {
        System.out.println("IPFS gone wrong");
      }
    }).start());
  }

}
