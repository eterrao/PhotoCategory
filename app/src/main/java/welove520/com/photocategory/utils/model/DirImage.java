package welove520.com.photocategory.utils.model;

import java.io.Serializable;
import java.util.List;

/**
 * Created by wanbo on 2017/1/3.
 */

public class DirImage implements Serializable {

    private static final long serialVersionUID = 4883706598136003823L;

    public List<String> dirName;

    public List<String> getDirName() {
        return dirName;
    }

    public void setDirName(List<String> dirName) {
        this.dirName = dirName;
    }

    public String toJson() {
        if (dirName == null || dirName.size() == 0) {
            return "{\"dir_name\":[]}";
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("{\"dir_name\":[");
            for (int i = 0; i < dirName.size(); i++) {
                String s = dirName.get(i);
                sb.append("\"");
                sb.append(s);
                sb.append("\"");
                if (i < dirName.size() - 1) {
                    sb.append(",");
                }
            }
            sb.append("]}");
            return sb.toString();
        }
    }

}
