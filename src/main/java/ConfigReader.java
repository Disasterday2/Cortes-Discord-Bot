import com.google.gson.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import processor.models.BotConfig;
import processor.models.statistics.DamageStatistic;
import processor.models.statistics.TeamDamageStatistic;
import processor.utilities.SystemPath;

import java.io.*;
import java.lang.reflect.Type;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A class to read in the config file using Gson.
 */
public class ConfigReader {

    private final Gson gson;
    private final Logger logger = LogManager.getLogger();

    public ConfigReader() {
        this.gson = new GsonBuilder().registerTypeAdapter(Instant.class, new JsonDeserializer<Instant>() {

            @Override
            public Instant deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                return Instant.parse(json.getAsString());
            }
        }).create();
    }

    /**
     * Reads the config file at src/resources.
     *
     * @return an Object of the type BotConfig
     */
    public BotConfig readConfig() {
        BotConfig config = null;
        this.logger.info("Reading config...");
        try (Reader reader = new FileReader(SystemPath.INSTANCE.getPath() + "config.json")) {
            config = this.gson.fromJson(reader, BotConfig.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return config;
    }

    public void writeConfig(BotConfig config) {
        this.logger.info("Writing config...");
        try (Writer writer = new FileWriter(SystemPath.INSTANCE.getPath() + "config.json")) {
            this.gson.toJson(config, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<DamageStatistic> readDamageStatistic() {
        DamageStatistic[] list = new DamageStatistic[50];
        try (Reader reader = new FileReader(SystemPath.INSTANCE.getPath() + "damageStatistic.json")) {
            list = this.gson.fromJson(reader, DamageStatistic[].class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Arrays.stream(list).collect(Collectors.toList());
    }

    public List<TeamDamageStatistic> readTeamDamageStatistic() {
        TeamDamageStatistic[] list = new TeamDamageStatistic[50];
        try (Reader reader = new FileReader(SystemPath.INSTANCE.getPath() + "teamStatistic.json")) {
            list = this.gson.fromJson(reader, TeamDamageStatistic[].class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Arrays.stream(list).collect(Collectors.toList());
    }
}
