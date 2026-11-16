package dev.waystonescustomizations.test;

import dev.waystonescustomizations.ThemeConfig;
import dev.waystonescustomizations.custom.CustomImages;
import dev.waystonescustomizations.icon.GroupIcons;
import dev.waystonescustomizations.icon.ItemIcons;
import dev.waystonescustomizations.style.EntryStyles;
import net.blay09.mods.waystones.api.MutableWaystone;
import net.blay09.mods.waystones.api.Waystone;
import net.blay09.mods.waystones.api.WaystoneGroup;
import net.blay09.mods.waystones.api.WaystoneGroups;
import net.blay09.mods.waystones.api.WaystoneTypes;
import net.blay09.mods.waystones.api.WaystonesAPI;
import net.blay09.mods.waystones.client.gui.screen.ManageWaystoneGroupsScreen;
import net.blay09.mods.waystones.client.gui.screen.WaystoneGroupEditScreen;
import net.blay09.mods.waystones.client.gui.screen.WaystoneSelectionScreenBase;
import net.blay09.mods.waystones.core.PlayerWaystoneManager;
import net.blay09.mods.waystones.core.WaystoneGroupImpl;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.Set;

/** Creates a world with grouped waystones, opens the menus and takes screenshots of every theme. */
public class WaystonesCustomizationsClientTest implements FabricClientGameTest {
    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath("waystonescustomizations_test", path);
    }

    private static Identifier item(String name) {
        return ItemIcons.toIcon(Identifier.withDefaultNamespace(name));
    }

    private static final Map<String, UUID> WAYSTONES = new ConcurrentHashMap<>();

    /** Puts two of our own sprites into the custom folders, like a player would. */
    private static void copySprite(String sprite, CustomImages.Kind kind, String fileName) {
        try (InputStream in = WaystonesCustomizationsClientTest.class.getResourceAsStream("/assets/waystonescustomizations/textures/gui/sprites/icons/" + sprite + ".png")) {
            Path folder = CustomImages.folder(kind);
            Files.createDirectories(folder);
            Files.copy(in, folder.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            context.waitTicks(60);
            singleplayer.getServer().runOnServer(server -> {
                ServerPlayer player = server.getPlayerList().getPlayers().getFirst();
                ServerLevel level = player.level();
                List<WaystoneGroup> groups = List.of(
                        new WaystoneGroupImpl(id("bases"), Component.literal("Basen"), item("red_bed"), 0xFFFF5555, false, false, 1),
                        new WaystoneGroupImpl(id("farms"), Component.literal("Farmen"), GroupIcons.sprite("barn"), 0xFF55FF55, false, false, 2),
                        new WaystoneGroupImpl(id("dungeons"), Component.literal("Dungeons"), GroupIcons.sprite("spawner"), 0xFFAA00AA, false, false, 3),
                        new WaystoneGroupImpl(id("nether"), Component.literal("Nether"), GroupIcons.sprite("nether_portal"), 0xFFFFAA00, false, false, 4),
                        new WaystoneGroupImpl(id("mines"), Component.literal("Minen"), GroupIcons.sprite("pickaxe"), 0xFF55FFFF, false, false, 5)
                );
                PlayerWaystoneManager.ensureWaystoneGroups(player, groups);

                String[] names = {"Spawn", "Hauptbasis", "Weizenfarm", "Eisenfarm", "Wüstentempel", "Netherportal", "Diamantmine",
                        "Dorf Nord", "Hafen", "Ozeanmonument", "Endportal", "Kürbisfarm"};
                List<Set<Identifier>> assignments = List.of(
                        Set.of(id("bases")), Set.of(id("bases")), Set.of(id("farms")), Set.of(id("farms"), id("bases")),
                        Set.of(id("dungeons")), Set.of(id("nether")), Set.of(id("mines")), Set.of(),
                        Set.of(id("bases")), Set.of(id("dungeons")), Set.of(id("dungeons"), id("nether")), Set.of(id("farms")));
                List<Waystone> created = new ArrayList<>();
                BlockPos origin = player.blockPosition();
                for (int i = 0; i < names.length; i++) {
                    BlockPos pos = origin.offset(4 + (i % 4) * 3, 0, 4 + (i / 4) * 3);
                    int index = i;
                    WaystonesAPI.placeWaystone(level, pos, WaystoneTypes.ANDESITE).ifPresent(waystone -> {
                        if (waystone instanceof MutableWaystone mutable) {
                            mutable.setName(Component.literal(names[index]));
                        }
                        WaystonesAPI.activateWaystone(player, waystone);
                        PlayerWaystoneManager.setConfiguredWaystoneGroups(player, waystone.getWaystoneUid(), assignments.get(index));
                        created.add(waystone);
                        WAYSTONES.put(names[index], waystone.getWaystoneUid());
                    });
                }
                System.out.println("[waystonescustomizations-test] created " + created.size() + " waystones");
            });
            context.waitTicks(20);

            // own pictures and per-entry backgrounds
            context.runOnClient(client -> {
                copySprite("ore_block", CustomImages.Kind.BACKGROUNDS, "Kristall.png");
                copySprite("castle", CustomImages.Kind.ICONS, "Meine Burg.png");
                CustomImages.reload();
                EntryStyles styles = EntryStyles.get();
                styles.setGroup(id("bases"), "block:cherry_planks");
                styles.setWaystone(WAYSTONES.get("Hafen"), "color:3AB3DA");
                styles.setWaystone(WAYSTONES.get("Wüstentempel"), "block:sandstone_top");
                styles.setWaystone(WAYSTONES.get("Diamantmine"), "custom:kristall");
                styles.setWaystoneText(WAYSTONES.get("Hafen"), "color:FED83D");
            });

            singleplayer.getServer().runCommand("execute as @p run waystones gui @s");
            context.waitForScreen(WaystoneSelectionScreenBase.class);
            context.waitTicks(10);

            for (String theme : List.of("vanilla", "oak", "deepslate", "nether", "cherry", "prismarine", "end", "inventory", "custom:kristall")) {
                context.runOnClient(client -> ThemeConfig.get().theme = theme);
                context.waitTicks(3);
                context.takeScreenshot("theme_" + theme.replace(':', '_'));
            }

            // hover an entry: brush appears
            context.runOnClient(client -> ThemeConfig.get().theme = "oak");
            context.getInput().setCursorPos(420, 237);
            context.waitTicks(3);
            context.takeScreenshot("hover_oak");

            // right click the entry: background picker
            context.getInput().pressMouse(1);
            context.waitTicks(3);
            context.takeScreenshot("background_popup");
            // click outside closes the popup (and must not click anything below it)
            context.getInput().setCursorPos(20, 20);
            context.waitTicks(2);
            context.getInput().pressMouse(0);
            context.waitTicks(3);
            context.waitForScreen(WaystoneSelectionScreenBase.class);

            // theme list with live preview
            context.getInput().setCursorPos(758, 86);
            context.waitTicks(2);
            context.getInput().pressMouse(0);
            context.waitTicks(3);
            context.takeScreenshot("theme_list");
            context.getInput().setCursorPos(600, 300);
            context.waitTicks(3);
            context.takeScreenshot("theme_list_preview");
            context.getInput().scroll(-5);
            context.waitTicks(3);
            context.takeScreenshot("theme_list_scrolled");
            // click outside closes the popup (and must not click anything below it)
            context.getInput().setCursorPos(20, 20);
            context.waitTicks(2);
            context.getInput().pressMouse(0);
            context.waitTicks(3);
            context.waitForScreen(WaystoneSelectionScreenBase.class);

            // group editor with the icon picker
            context.setScreen(() -> {
                var client = net.minecraft.client.Minecraft.getInstance();
                var menu = client.player.containerMenu;
                var manage = new ManageWaystoneGroupsScreen(menu, client.player.getInventory(), null);
                return new WaystoneGroupEditScreen(menu, client.player.getInventory(), manage, id("bases"), "Basen");
            });
            context.waitTicks(5);
            context.takeScreenshot("group_editor");
            context.getInput().setCursorPos(690, 250);
            context.waitTicks(2);
            context.getInput().scroll(-4);
            context.waitTicks(3);
            context.takeScreenshot("group_editor_scrolled");
            for (String theme : List.of("deepslate", "cherry", "vanilla")) {
                context.runOnClient(client -> ThemeConfig.get().theme = theme);
                context.waitTicks(3);
                context.takeScreenshot("group_editor_" + theme);
            }
            context.runOnClient(client -> ThemeConfig.get().theme = "oak");
            context.waitTicks(2);

            // "Entry style" button in the group editor opens the background / text colour picker for the group
            int[] styleButton = context.computeOnClient(client -> {
                for (var child : currentScreen(client).children()) {
                    if (child instanceof net.minecraft.client.gui.components.Button b
                            && b.getMessage().getContents() instanceof net.minecraft.network.chat.contents.TranslatableContents t
                            && t.getKey().equals("waystonescustomizations.group.entry_style")) {
                        double scale = client.getWindow().getGuiScale();
                        return new int[]{(int) ((b.getX() + b.getWidth() / 2) * scale), (int) ((b.getY() + b.getHeight() / 2) * scale)};
                    }
                }
                return null;
            });
            if (styleButton == null) {
                throw new AssertionError("entry style button missing in group editor");
            }
            context.getInput().setCursorPos(styleButton[0], styleButton[1]);
            context.waitTicks(2);
            context.getInput().pressMouse(0);
            context.waitTicks(3);
            context.takeScreenshot("group_editor_style_popup");
            context.getInput().setCursorPos(20, 20);
            context.waitTicks(2);
            context.getInput().pressMouse(0);
            context.waitTicks(2);

            // the normal menu you get by right-clicking a waystone
            context.setScreen(() -> null);
            context.waitTicks(5);
            singleplayer.getServer().runOnServer(server -> {
                ServerPlayer player = server.getPlayerList().getPlayers().getFirst();
                BlockPos pos = player.blockPosition().offset(4, 0, 4);
                player.level().getBlockState(pos).useWithoutItem(player.level(), player,
                        new BlockHitResult(Vec3.atCenterOf(pos), Direction.NORTH, pos, false));
            });
            context.waitForScreen(WaystoneSelectionScreenBase.class);
            context.waitTicks(10);
            for (String theme : List.of("oak", "deepslate", "vanilla")) {
                context.runOnClient(client -> ThemeConfig.get().theme = theme);
                context.waitTicks(3);
                context.takeScreenshot("waystone_" + theme);
            }
        }
    }

    /** The open screen: a field up to 26.1, gui.screen() from 26.2 on. */
    private static net.minecraft.client.gui.screens.Screen currentScreen(net.minecraft.client.Minecraft client) {
        try {
            try {
                return (net.minecraft.client.gui.screens.Screen) net.minecraft.client.Minecraft.class.getField("screen").get(client);
            } catch (NoSuchFieldException e) {
                Object gui = net.minecraft.client.Minecraft.class.getField("gui").get(client);
                return (net.minecraft.client.gui.screens.Screen) gui.getClass().getMethod("screen").invoke(gui);
            }
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }
}
