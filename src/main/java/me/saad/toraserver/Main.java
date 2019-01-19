package me.saad.toraserver;

import bt.Bt;
import bt.StandaloneClientBuilder;
import bt.data.Storage;
import bt.data.file.FileSystemStorage;
import bt.dht.DHTConfig;
import bt.dht.DHTModule;
import bt.runtime.BtClient;
import bt.runtime.Config;
import bt.torrent.TorrentSessionState;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.inject.Module;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

@SpringBootApplication
@RestController
public class Main {

	private List<Request> requests = new ArrayList<>();
	private CompletableFuture lastTorrent = null;
	private File targetDirectory = new File("~/Downloads");

	@RequestMapping(
			value = "/request",
			method = RequestMethod.POST,
			consumes = "application/json")
	public void receiveRequest(@RequestBody String payload) throws MalformedURLException {

		JsonObject object = (JsonObject) new JsonParser().parse(payload);
		if (object.has("mega_user") && object.has("mega_pass") && object.has("torrent_link")) {
			String megaUser = object.getAsJsonPrimitive("mega_user").getAsString();
			String megaPass = object.getAsJsonPrimitive("mega_pass").getAsString();
			String torrentLink = object.getAsJsonPrimitive("torrent_link").getAsString();

			Config config = new Config() {
				@Override
				public int getNumOfHashingThreads() {
					return Runtime.getRuntime().availableProcessors() * 2;
				}
			};

			Module dhtModule = new DHTModule(new DHTConfig() {
				@Override
				public boolean shouldUseRouterBootstrap() {
					return true;
				}
			});

			// get download directory

			// create file system based backend for torrent data
			Storage storage = new FileSystemStorage(targetDirectory.toPath());

			// create client with a private runtime
			AtomicReference<String> torrentName = new AtomicReference<>();
			StandaloneClientBuilder builder = Bt.client()
					.config(config)
					.storage(storage)
					.autoLoadModules()
					.module(dhtModule)
					.afterTorrentFetched(torrent -> {
						torrentName.set(torrent.getName());
					})
					.stopWhenDownloaded();

			if (torrentLink.startsWith("magnet:")) builder.magnet(torrentLink);
			else builder.torrent(new URL(torrentLink));

			// launch
			BtClient client = builder.build();

			if (lastTorrent == null)
				lastTorrent = client.startAsync().thenAcceptAsync(state -> {
					if (((TorrentSessionState) state).getPiecesRemaining() == 0) {
						client.stop();

						zipAndUpload(torrentName.get());
					}
				}).thenRunAsync(() -> {

				});
			else lastTorrent.thenRun(() -> lastTorrent = client.startAsync().thenAcceptAsync(state -> {
				if (((TorrentSessionState) state).getPiecesRemaining() == 0) {
					client.stop();

					zipAndUpload(torrentName.get());
				}
			}));
		}
	}

	private void zipAndUpload(String torrentName) {
		ZipParameters parameters = new ZipParameters();
		parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
		parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_ULTRA);

		//	if(password.length()>0){
		//		parameters.setEncryptFiles(true);
		//		parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_AES);
		//		parameters.setAesKeyStrength(Zip4jConstants.AES_STRENGTH_256);
		//		parameters.setPassword(password);
		//	}

		try {
			ZipFile zipFile = new ZipFile(new File(targetDirectory, torrentName));

			File targetFile = new File(targetDirectory, torrentName);
			if (targetFile.isFile()) {
				zipFile.addFile(targetFile, parameters);
			} else if (targetFile.isDirectory()) {
				zipFile.addFolder(targetFile, parameters);
			}
			File zip = zipFile.getFile();
		} catch (ZipException e) {
			e.printStackTrace();
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
