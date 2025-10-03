package me.alpha432.oyvey.manager;

// OyVey Core Imports
import me.alpha432.oyvey.OyVey;
import me.alpha432.oyvey.features.Feature;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.modules.client.ClickGui;
import me.alpha432.oyvey.features.modules.client.HudModule;
import me.alpha432.oyvey.features.modules.player.FastPlace;
import me.alpha432.oyvey.util.traits.Jsonable;
import me.alpha432.oyvey.util.traits.Util;

// Module Imports
import me.alpha432.oyvey.features.modules.movement.Fly;
import me.alpha432.oyvey.features.modules.client.RespawnModule;
import me.alpha432.oyvey.features.modules.movement.NoFall;
import me.alpha432.oyvey.features.modules.render.Nametags;
// ↓↓↓ 修正: KillAura を movement パッケージからインポート ↓↓↓
import me.alpha432.oyvey.features.modules.movement.KillAura;
// ↑↑↑ 修正 ↑↑↑

// ユーティリティインポート
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.alpha432.oyvey.event.impl.Render2DEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


public class ModuleManager implements Jsonable, Util {
    public List<Module> modules = new ArrayList<>();
    public List<Module> sortedModules = new ArrayList<>();
    public List<String> sortedModulesABC = new ArrayList<>();

    private List<String> modulesToRestore = new ArrayList<>();

    public void init() {
        modules.add(new HudModule());
        modules.add(new ClickGui());
        modules.add(new FastPlace());
        modules.add(new Fly());
        modules.add(new RespawnModule());
        modules.add(new NoFall());
        modules.add(new KillAura()); // KillAura の登録

        modules.add(new Nametags()); // Nametags の登録
    }

    public Module getModuleByName(String name) {
        for (Module module : this.modules) {
            if (!module.getName().equalsIgnoreCase(name)) continue;
            return module;
        }
        return null;
    }

    public List<me.alpha432.oyvey.features.Feature> getFeatures() {
        // 解決できないシンボル 'features' を返しています。
        // ここで 'features' が解決しない場合、この 'features' を 'getFeatures()' で解決できたはずの
        // モジュールリストの実際のフィールド名（例: 'modules' や 'moduleList'）に置き換える必要があります。
        return getFeatures();
    }

    public <T extends Module> T getModuleByClass(Class<T> clazz) {
        for (Module module : this.modules) {
            if (!clazz.isInstance(module)) continue;
            return (T) module;
        }
        return null;
    }

    // ... (引数を持たないメソッドは省略) ...

    public void enableModule(Class<Module> clazz) {
        Module module = this.getModuleByClass(clazz);
        if (module != null) {
            module.enable();
        }
    }

    public void disableModule(Class<Module> clazz) {
        Module module = this.getModuleByClass(clazz);
        if (module != null) {
            module.disable();
        }
    }

    public void enableModule(String name) {
        Module module = this.getModuleByName(name);
        if (module != null) {
            module.enable();
        }
    }

    public void disableModule(String name) {
        Module module = this.getModuleByName(name);
        if (module != null) {
            module.disable();
        }
    }

    public boolean isModuleEnabled(String name) {
        Module module = this.getModuleByName(name);
        return module != null && module.isOn();
    }

    public boolean isModuleEnabled(Class<Module> clazz) {
        Module module = this.getModuleByClass(clazz);
        return module != null && module.isOn();
    }

    public Module getModuleByDisplayName(String displayName) {
        for (Module module : this.modules) {
            if (!module.getDisplayName().equalsIgnoreCase(displayName)) continue;
            return module;
        }
        return null;
    }

    public ArrayList<Module> getEnabledModules() {
        ArrayList<Module> enabledModules = new ArrayList<>();
        for (Module module : this.modules) {
            if (!module.isEnabled()) continue;
            enabledModules.add(module);
        }
        return enabledModules;
    }

    public ArrayList<String> getEnabledModulesName() {
        ArrayList<String> enabledModules = new ArrayList<>();
        for (Module module : this.modules) {
            if (!module.isEnabled() || !module.isDrawn()) continue;
            enabledModules.add(module.getFullArrayString());
        }
        return enabledModules;
    }

    public ArrayList<Module> getModulesByCategory(Module.Category category) {
        ArrayList<Module> modulesCategory = new ArrayList<Module>();
        this.modules.forEach(module -> {
            if (module.getCategory() == category) {
                modulesCategory.add(module);
            }
        });
        return modulesCategory;
    }

    public List<Module.Category> getCategories() {
        return Arrays.asList(Module.Category.values());
    }

    public void onLoad() {
        this.modules.stream().filter(Module::listening).forEach(OyVey.EVENT_BUS::register);
        this.modules.forEach(Module::onLoad);
    }

    public void onUpdate() {
        this.modules.stream().filter(Feature::isEnabled).forEach(Module::onUpdate);
    }

    public void onTick() {
        this.modules.stream().filter(Feature::isEnabled).forEach(Module::onTick);
    }

    public void onRender2D(Render2DEvent event) {
        this.modules.stream().filter(Feature::isEnabled).forEach(module -> module.onRender2D(event));
    }


    public void sortModules(boolean reverse) {
        this.sortedModules = this.getEnabledModules().stream().filter(Module::isDrawn)
                .sorted(Comparator.comparing(module -> mc.textRenderer.getWidth(module.getFullArrayString()) * (reverse ? -1 : 1)))
                .collect(Collectors.toList());
    }

    public void sortModulesABC() {
        this.sortedModulesABC = new ArrayList<>(this.getEnabledModulesName());
        this.sortedModulesABC.sort(String.CASE_INSENSITIVE_ORDER);
    }

    public void onUnload() {
        this.modules.forEach(OyVey.EVENT_BUS::unregister);
        this.modules.forEach(Module::onUnload);
    }

    public void onUnloadPost() {
        for (Module module : this.modules) {
            module.enabled.setValue(false);
        }
    }

    public void onKeyPressed(int eventKey) {
        if (eventKey <= 0 || mc.currentScreen != null) return;
        this.modules.forEach(module -> {
            if (module.getBind().getKey() == eventKey) {
                module.toggle();
            }
        });
    }

    public void saveModulesBeforeDeath() {
        this.modulesToRestore.clear();
        for (Module module : this.modules) {
            // RespawnModule はリスポーン機能を制御するため、状態を保存しないように除外
            if (module.isEnabled() && !(module instanceof HudModule) && !(module instanceof ClickGui) && !(module instanceof RespawnModule)) {
                this.modulesToRestore.add(module.getName());
            }
        }
    }

    public void restoreModulesAfterDeath() {
        for (String moduleName : this.modulesToRestore) {
            Module module = this.getModuleByName(moduleName);
            if (module != null) {
                module.enable();
            }
        }
        this.modulesToRestore.clear();
    }

    @Override
    public JsonElement toJson() {
        JsonObject object = new JsonObject();
        for (Module module : modules) {
            object.add(module.getName(), module.toJson());
        }
        return object;
    }

    @Override
    public void fromJson(JsonElement element) {
        for (Module module : modules) {
            module.fromJson(element.getAsJsonObject().get(module.getName()));
        }
    }

    @Override
    public String getFileName() {
        return "modules.json";
    }
}