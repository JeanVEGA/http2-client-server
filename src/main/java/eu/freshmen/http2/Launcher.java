package eu.freshmen.http2;

import java.util.List;
import java.util.Properties;

import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.StdErrLog;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

import lombok.Getter;
import lombok.ToString;

public class Launcher {

	@Getter
	@ToString
	static class Args {
		@Parameter(description = "server|client")
		private List<String> params;
		@Parameter(names = "-host")
		private String host = "localhost";
		@Parameter(names = "-port")
		private Integer port = 8443;
	}

	public static void main(String[] args) throws Exception {
		final Args params = new Args();
		final JCommander jc = new JCommander(params, args);
		jc.usage();
		print(params);

		if (params.getParams() == null || params.getParams().isEmpty()) {
			System.err.println("client or server?");
			System.exit(1);
		}
		final Properties p = new Properties();
		p.setProperty("jetty.LEVEL", "WARN");
		Log.setLog(new StdErrLog("jetty", p));
		switch (params.getParams().get(0)) {
		case "client":
			Client.run(params);
			break;
		case "server":
			Server.run(params);
			break;
		}

	}

	private static void print(Object msg) {
		print(String.valueOf(msg));
	}

	private static void print(String msg) {
		JCommander.getConsole().println(msg);
	}
}
