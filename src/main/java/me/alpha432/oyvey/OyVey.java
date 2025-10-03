package me.alpha432.oyvey;

import me.alpha432.oyvey.manager.*;
import me.alpha432.oyvey.util.TextUtil;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.client.MinecraftClient; // ★★★ これをインポート ★★★
import com.google.common.eventbus.EventBus; // ★★★ これをインポート ★★★

public class OyVey implements ModInitializer, ClientModInitializer {
    public static final String NAME = "Mio";
    public static final String VERSION = "v0.0.4-nightly :)"; //+ SharedConstants.getGameVersion().getName();

    public static float TIMER = 1f;

    public static final Logger LOGGER = LogManager.getLogger("OyVey");
    public static ServerManager serverManager;
    public static ColorManager colorManager;
    public static RotationManager rotationManager;
    public static PositionManager positionManager;
    public static HoleManager holeManager;
    public static EventManager eventManager;
    public static SpeedManager speedManager;
    public static CommandManager commandManager;
    public static FriendManager friendManager;
    public static ModuleManager moduleManager;
    public static ConfigManager configManager;

    // ★★★ 不足していた mc と EVENT_BUS フィールドを追加 ★★★
    public static MinecraftClient mc;
    public static EventBus EVENT_BUS; // final にせず、onInitializeClient で初期化します

    @Override
    public void onInitialize() {
        eventManager = new EventManager();
        serverManager = new ServerManager();
        rotationManager = new RotationManager();
        positionManager = new PositionManager();
        friendManager = new FriendManager();
        colorManager = new ColorManager();
        commandManager = new CommandManager();
        moduleManager = new ModuleManager();
        speedManager = new SpeedManager();
        holeManager = new HoleManager();

        TextUtil.init();
    }

    @Override
    public void onInitializeClient() {
        // ★★★ mc と EVENT_BUS の初期化を追加 ★★★
        mc = MinecraftClient.getInstance(); // MinecraftClient のインスタンスを取得
        EVENT_BUS = new EventBus(); // EventBus の新しいインスタンスを作成

        eventManager.init();
        moduleManager.init();

        configManager = new ConfigManager();
        configManager.load();
        colorManager.init();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> configManager.save()));
    }
}