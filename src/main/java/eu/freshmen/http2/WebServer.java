/*
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
package eu.freshmen.http2;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http2.server.HTTP2ServerConnectionFactory;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

public class WebServer {

	private final Server server;
	private final ServerConnector connector;

	private WebServer(Map<String, HttpServlet> servlets, String root, int port) {
		final QueuedThreadPool executor = new QueuedThreadPool();
		executor.setName("server");

		server = new Server(executor);
		connector = new ServerConnector(server, 1, 1, new HTTP2ServerConnectionFactory(new HttpConfiguration()));
		connector.setPort(port);
		server.addConnector(connector);

		final ServletContextHandler context = new ServletContextHandler(server, root, true, false);
		servlets.forEach((path, servlet) -> context.addServlet(new ServletHolder(servlet), path));
	}

	@SneakyThrows
	public void start() {
		server.start();
	}

	@SneakyThrows
	public void stop() {
		server.stop();
	}

	public int getLocalport() {
		return connector.getLocalPort();
	}

	public static WebServerBuilder builder(String root, int port) {
		return new WebServerBuilder(root, port);
	}

	@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
	public static class WebServerBuilder {

		private final String root;
		private final int port;

		private final Map<String, HttpServlet> servlets = new HashMap<>();

		public WebServerBuilder handler(String path, BiConsumer<HttpServletRequest, HttpServletResponse> handler) {
			HttpServlet servlet = new HttpServlet() {
				private static final long serialVersionUID = -7741340028518626628L;

				@Override
				protected void service(HttpServletRequest req, HttpServletResponse resp)
						throws ServletException, IOException {
					handler.accept(req, resp);
				}
			};
			servlets.put(path, servlet);
			return this;
		}

		public WebServer build() {
			return new WebServer(servlets, root, port);
		}
	}
}