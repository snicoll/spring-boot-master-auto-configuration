package hornetq.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("hornetq")
public class HornetQProperties {

	/**
	 * HornetQ deployment mode, auto-detected by default. Can be explicitly set to
	 * "native" or "embedded".
	 */
	private HornetQMode mode;

	/**
	 * HornetQ broker host.
	 */
	private String host = "localhost";

	/**
	 * HornetQ broker port.
	 */
	private int port = 5445;

	private final Embedded embedded = new Embedded();

	public HornetQMode getMode() {
		return mode;
	}

	public void setMode(HornetQMode mode) {
		this.mode = mode;
	}

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

	public Embedded getEmbedded() {
		return embedded;
	}

	/**
	 * Configuration for an embedded HornetQ server.
	 */
	public static class Embedded {

		/**
		 * Enable embedded mode if the HornetQ server APIs are available.
		 */
		private boolean enabled = true;

		/**
		 * Enable persistent store.
		 */
		private boolean persistent;

		/**
		 * Journal file directory. Not necessary if persistence is turned off.
		 */
		private String dataDirectory;

		/**
		 * Comma-separated list of queues to create on startup.
		 */
		private String[] queues = new String[0];

		/**
		 * Comma-separated list of topics to create on startup.
		 */
		private String[] topics = new String[0];

		public boolean isEnabled() {
			return this.enabled;
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}

		public boolean isPersistent() {
			return this.persistent;
		}

		public void setPersistent(boolean persistent) {
			this.persistent = persistent;
		}

		public String getDataDirectory() {
			return this.dataDirectory;
		}

		public void setDataDirectory(String dataDirectory) {
			this.dataDirectory = dataDirectory;
		}

		public String[] getQueues() {
			return this.queues;
		}

		public void setQueues(String[] queues) {
			this.queues = queues;
		}

		public String[] getTopics() {
			return this.topics;
		}

		public void setTopics(String[] topics) {
			this.topics = topics;
		}

	}

}