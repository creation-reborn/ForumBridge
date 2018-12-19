/*
 * Copyright 2018 lolnet.co.nz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nz.co.lolnet.forumbridge.velocity;

import com.moandjiezana.toml.Toml;
import com.moandjiezana.toml.TomlWriter;
import nz.co.lolnet.forumbridge.common.ForumBridge;
import nz.co.lolnet.forumbridge.common.configuration.Config;
import nz.co.lolnet.forumbridge.common.configuration.Configuration;
import nz.co.lolnet.forumbridge.common.util.Toolbox;

import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Optional;

public class VelocityConfiguration implements Configuration {
    
    private static final Toml TOML = new Toml();
    private static final TomlWriter TOML_WRITER = new TomlWriter();
    private final Path path;
    private Config config;
    
    public VelocityConfiguration(Path path) {
        this.path = path;
    }
    
    @Override
    public boolean loadConfiguration() {
        Optional<Config> config = loadFile(getPath().resolve("config.json"), Config.class);
        if (config.isPresent()) {
            this.config = config.get();
            return true;
        }
        
        return false;
    }
    
    @Override
    public boolean saveConfiguration() {
        return saveFile(getPath().resolve("config.json"), config);
    }
    
    public static <T> Optional<T> loadFile(Path path, Class<T> typeOfT) {
        if (Files.exists(path)) {
            return deserializeFile(path, typeOfT);
        }
        
        return Toolbox.newInstance(typeOfT).filter(object -> saveFile(path, object));
    }
    
    public static boolean saveFile(Path path, Object object) {
        if (Files.exists(path) || createFile(path)) {
            return serializeFile(path, object);
        }
        
        return false;
    }
    
    public static <T> Optional<T> deserializeFile(Path path, Class<T> typeOfT) {
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            return Optional.ofNullable(getToml().read(reader).to(typeOfT));
        } catch (Exception ex) {
            ForumBridge.getInstance().getLogger().error("Encountered an error while deserializing {}", path, ex);
            return Optional.empty();
        }
    }
    
    public static boolean serializeFile(Path path, Object object) {
        try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
            getTomlWriter().write(object, writer);
            return true;
        } catch (Exception ex) {
            ForumBridge.getInstance().getLogger().error("Encountered an error while serializing {}", path, ex);
            return false;
        }
    }
    
    private static boolean createFile(Path path) {
        try {
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
            
            Files.createFile(path);
            return true;
        } catch (Exception ex) {
            ForumBridge.getInstance().getLogger().error("Encountered an error while creating {}", path, ex);
            return false;
        }
    }
    
    public static Toml getToml() {
        return TOML;
    }
    
    public static TomlWriter getTomlWriter() {
        return TOML_WRITER;
    }
    
    public Path getPath() {
        return path;
    }
    
    @Override
    public Config getConfig() {
        return config;
    }
}