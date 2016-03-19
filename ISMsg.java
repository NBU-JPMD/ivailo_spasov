import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.lang.StringBuilder;

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
		StringBuilder sb = new StringBuilder();
		sb.append("resp_code=");
		sb.append(resp_code);
        for (Map.Entry<String, Object> entry : dict.entrySet()) {
			sb.append("\n");
			sb.append(entry.getKey());
			sb.append("=");
			sb.append(entry.getValue().toString());
        }
        return sb.toString();
    }
}