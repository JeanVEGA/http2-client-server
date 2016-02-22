package eu.freshmen.http2;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import eu.freshmen.http2.Launcher.Args;

/**
 * Inspired by <a href="https://github.com/grro/http2">github.com/grro/http2</a>
 * 
 * Copyright 2016 Jan Ondrusek
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
public class Server {

	public static void run(Args params) {
		// start the test server
		WebServer server = WebServer.builder("/", params.getPort())
				.handler("/head", (rq, rs) -> write("<head></head>", rs))
				.handler("/body", (rq, rs) -> write("<body></body>", rs))
				.build();
		server.start();
	}

	private static void write(String text, HttpServletResponse rs) {
		try {
			rs.getWriter().write(text);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
