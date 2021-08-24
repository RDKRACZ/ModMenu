package com.terraformersmc.modmenu.updates.providers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.terraformersmc.modmenu.updates.AvailableUpdate;
import com.terraformersmc.modmenu.updates.ModUpdateProvider;
import com.terraformersmc.modmenu.util.mod.fabric.FabricMod;
import net.minecraft.util.Util;
import org.apache.commons.io.FilenameUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class CurseForgeUpdateProvider extends ModUpdateProvider {
	private static final Gson gson = new GsonBuilder().create();

	public CurseForgeUpdateProvider(String gameVersion) {
		super(gameVersion);
	}

	@Override
	public void check(String modId, FabricMod.ModUpdateData data, Consumer<AvailableUpdate> callback) {
		beginUpdateCheck();
		Util.getMainWorkerExecutor().execute(() -> {
			String url = String.format("https://addons-ecs.forgesvc.net/api/v2/addon/%s/files", data.getProjectId().get());
			HttpGet request = new HttpGet(url);
			request.addHeader(HttpHeaders.USER_AGENT, "ModMenu (CurseForgeUpdateProvider)");

			try (CloseableHttpResponse response = httpClient.execute(request)) {
				if (response.getStatusLine().getStatusCode() == 200) {
					HttpEntity entity = response.getEntity();
					if (entity != null) {
						CurseForgeResponse[] responses = gson.fromJson(EntityUtils.toString(entity), CurseForgeResponse[].class);
						List<CurseForgeResponse> versions = Arrays.stream(responses)
								.filter(r -> r.gameVersion.contains(this.gameVersion) && !r.gameVersion.contains("Forge"))
								.sorted(Comparator.comparing(r -> r.fileDate))
								.collect(Collectors.toList());

						if (!versions.isEmpty()) {
							//As we sort by date, the last one in the list will be the most recent.
							CurseForgeResponse ver = versions.get(versions.size() - 1);
							String fileName = FilenameUtils.getBaseName(ver.fileName);
							if (!fileName.equals(data.getModFileName())) {

								String downloadUrl;
								//If the project slug is there, we can link straight to the file
								if (data.getProjectSlug().isPresent()) {
									downloadUrl = String.format("https://www.curseforge.com/minecraft/mc-mods/%s/files/%s", data.getProjectSlug().get(), ver.id);
								} else {
									//Otherwise, we can just link to the project homepage.
									downloadUrl = String.format("https://minecraft.curseforge.com/projects/%s", data.getProjectId().get());
								}

								callback.accept(new AvailableUpdate(fileName, downloadUrl, null, "curseforge"));
							}
						}
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			completeUpdateCheck();
		});
	}

	@Override
	public void validateProviderConfig(FabricMod.ModUpdateData data) throws RuntimeException {
		if (data.getProjectId().isEmpty()) {
			throw new RuntimeException("The CurseForge update provider requires a single \"projectId\" field.");
		}
	}

	public static class CurseForgeResponse {
		private String fileName;
		private String fileDate;
		private String id;
		private Set<String> gameVersion;
	}
}