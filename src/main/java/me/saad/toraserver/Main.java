package me.saad.toraserver;

import bt.Bt;
import bt.data.Storage;
import bt.data.file.FileSystemStorage;
import bt.dht.DHTConfig;
import bt.dht.DHTModule;
import bt.runtime.BtClient;
import bt.runtime.Config;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.inject.Module;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.nio.file.Path;

@SpringBootApplication
@RestController
public class Main {

	@RequestMapping(
			value = "/request",
			method = RequestMethod.POST,
			consumes = "application/json")
	public void process(@RequestBody String payload) {

		JsonObject object = (JsonObject) new JsonParser().parse(payload);
		System.out.println(object.toString());
		if (object.has("mega_user") && object.has("mega_pass") && object.has("torrent_link")) {
			String megaUser = object.getAsJsonPrimitive("mega_user").getAsString();
			String megaPass = object.getAsJsonPrimitive("mega_pass").getAsString();
			String torrentLink = object.getAsJsonPrimitive("torrent_link").getAsString();

			// enable multithreaded verification of torrent data
		//	Config config = new Config() {
		//		@Override
		//		public int getNumOfHashingThreads() {
		//			return Runtime.getRuntime().availableProcessors() * 2;
		//		}
		//	};
		//
		//	// enable bootstrapping from public routers
		//	Module dhtModule = new DHTModule(new DHTConfig() {
		//		@Override
		//		public boolean shouldUseRouterBootstrap() {
		//			return true;
		//		}
		//	});
		//
		//	// get download directory
		//	Path targetDirectory = new File("~/Downloads").toPath();
		//
		//	// create file system based backend for torrent data
		//	Storage storage = new FileSystemStorage(targetDirectory);
		//
		//	// create client with a private runtime
		//	BtClient client = Bt.client()
		//			.config(config)
		//			.storage(storage)
		//			.magnet("magnet:?xt=urn:btih:af0d9aa01a9ae123a73802cfa58ccaf355eb19f1")
		//			.autoLoadModules()
		//			.module(dhtModule)
		//			.stopWhenDownloaded()
		//			.build();
		//
		//	// launch
		//	client.startAsync().join();
		}
	}

	@RequestMapping("/")
	public String home() {
		return "sup";
	}

	public static void main(String[] args) {
		SpringApplication.run(Main.class, args);

	}
}
