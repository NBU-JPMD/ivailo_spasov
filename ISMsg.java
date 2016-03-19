import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ISMsg implements Serializable {
    private int resp_code = 0;
    private HashMap<String, Object> dict = new HashMap<>();
    static final long serialVersionUID = 69;

    public void setRespCode(int resp_code) {
        this.resp_code = resp_code;
    }

    public int getRespCode() {
        return resp_code;
    }

    public void addKey(String string, Object object) {
        dict.put(string, object);
    }

    public Object getData(String string) {
        return dict.get(string);
    }

    public String toString() {
        String string = "resp_code=" + resp_code + "\n";
        for (Map.Entry<String, Object> entry : dict.entrySet()) {
            string = string + entry.getKey() + "=" + entry.getValue().toString();
        }
        return string;
    }
}