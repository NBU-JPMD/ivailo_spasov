package com.ispasov.nbujpmd.common.protocol;

import java.io.Serializable;
import java.util.HashMap;

public class ISMsg implements Serializable {
	private int resp_code = 0;
	private final HashMap<String, Object> dict = new HashMap<>();
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

    @Override
    public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("resp_code=");
		sb.append(resp_code);
		dict.forEach((k, v) -> {
			sb.append("\n");
			sb.append(k);
			sb.append("=");
			sb.append(v);
		});
		return sb.toString();
    }
}