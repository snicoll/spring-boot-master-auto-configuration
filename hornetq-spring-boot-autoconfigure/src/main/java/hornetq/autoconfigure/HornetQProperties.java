package hornetq.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("hornetq")
public class HornetQProperties {

	/**
	 * HornetQ broker host.
	 */
	private String host = "localhost";

	/**
	 * HornetQ broker port.
	 */
	private int port = 5445;

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

}
