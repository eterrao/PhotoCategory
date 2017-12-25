package welove520.com.photocategory.utils.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by wanbo on 2017/1/3.
 */

public class GroupImage implements Serializable {

    public HashMap<String, List<String>> groupMap;

    public HashMap<String, List<String>> getGroupMap() {
        return groupMap;
    }

    public void setGroupMap(HashMap<String, List<String>> groupMap) {
        this.groupMap = groupMap;
    }

    public String toJson(){
        if(groupMap == null){
            return "{\"group_map\":{}}";
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("{\"group_map\":{");
            Iterator<HashMap.Entry<String, List<String>>> entries = groupMap.entrySet().iterator();
            while (entries.hasNext()) {
                Map.Entry<String, List<String>> entry = entries.next();
                sb.append("\"");
                sb.append(entry.getKey());
                sb.append("\":[");
                for(int i = 0 ; i < entry.getValue().size() ; i++){
                    String s = entry.getValue().get(i);
                    sb.append("\"");
                    sb.append(s);
                    sb.append("\"");
                    if(i < entry.getValue().size()-1) {
                        sb.append(",");
                    }
                }
                sb.append("]");
                if(entries.hasNext()){
                    sb.append(",");
                }
            }
            sb.append("}}");
            return sb.toString();
        }
    }

}
