package net.silentchaos512.gear.config;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.silentchaos512.gear.item.blueprint.BlueprintType;
import net.silentchaos512.gear.util.IAOETool;
import net.silentchaos512.utils.config.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Config {
    private static final ConfigSpecWrapper WRAPPER_CLIENT = ConfigSpecWrapper.create(
            FMLPaths.CONFIGDIR.get().resolve("silentgear-client.toml"));
    private static final ConfigSpecWrapper WRAPPER = ConfigSpecWrapper.create(
            FMLPaths.CONFIGDIR.get().resolve("silentgear-common.toml"));

    public static final Client CLIENT = new Client(WRAPPER_CLIENT);
    public static final General GENERAL = new General(WRAPPER);

    public static class General {
        // Blueprints
        public final EnumValue<BlueprintType> blueprintTypes;
        public final BooleanValue spawnWithStarterBlueprints;
        // Block placers
        final ConfigValue<List<? extends String>> placerTools;
        final ConfigValue<List<? extends String>> placeableItems;
        // Sinew
        public final DoubleValue sinewDropRate;
        final ConfigValue<List<? extends String>> sinewAnimals;
        // Gear
        public final EnumValue<IAOETool.MatchMode> matchModeStandard;
        public final EnumValue<IAOETool.MatchMode> matchModeOres;
        public final BooleanValue gearBreaksPermanently;
        public final DoubleValue repairFactorAnvil;
        public final DoubleValue repairFactorQuick;
        public final BooleanValue upgradesInAnvilOnly;
        // Salvager
        public final DoubleValue salvagerMinLossRate;
        public final DoubleValue salvagerMaxLossRate;

        General(ConfigSpecWrapper wrapper) {
            wrapper.comment("item.blueprint", "Blueprint and template settings");

            blueprintTypes = wrapper
                    .builder("item.blueprint.typesAllowed")
                    .comment("Allowed blueprint types. Valid values are: BOTH, BLUEPRINT, and TEMPLATE")
                    .defineEnum(BlueprintType.BOTH);

            spawnWithStarterBlueprints = wrapper
                    .builder("item.blueprint.spawnWithStarterBlueprints")
                    .comment("When joining a new world, should players be given a blueprint package?",
                            "The blueprint package gives some blueprints when used (right-click).",
                            "To change what is given, override the starter_blueprints loot table.")
                    .define(true);

            wrapper.comment("item.blockPlacers",
                    "Silent Gear allows some items to be used to place blocks.",
                    "You can change which items place blocks and what other items they can activate.");

            placerTools = wrapper
                    .builder("item.blockPlacers.placerTools")
                    .comment("These items are able to place blocks. The player must be sneaking.")
                    .defineList(
                            ImmutableList.of(
                                    "silentgear:axe",
                                    "silentgear:pickaxe",
                                    "silentgear:shovel"
                            ),
                            o -> o instanceof String && ResourceLocation.tryCreate((String) o) != null);
            placeableItems = wrapper
                    .builder("item.blockPlacers.placeableItems")
                    .comment("These items can be used by placer tools. The player must be sneaking.",
                            "Note that some items may not work with this feature.")
                    .defineList(
                            ImmutableList.of(
                                    "danknull:dank_null",
                                    "torchbandolier:torch_bandolier",
                                    "xreliquary:sojourner_staff"
                            ),
                            o -> o instanceof String && ResourceLocation.tryCreate((String) o) != null);

            wrapper.comment("item.sinew", "Settings for sinew drops");

            sinewDropRate = wrapper
                    .builder("item.sinew.dropRate")
                    .comment("Drop rate of sinew (chance out of 1)")
                    .defineInRange(0.2, 0, 1);
            sinewAnimals = wrapper
                    .builder("item.sinew.dropsFrom")
                    .comment("These entities can drop sinew when killed.")
                    .defineList(
                            ImmutableList.of(
                                    "minecraft:cow",
                                    "minecraft:pig",
                                    "minecraft:sheep"
                            ),
                            o -> o instanceof String && ResourceLocation.tryCreate((String) o) != null);

            wrapper.comment("item.gear", "Settings for gear (tools, weapons, and armor)");

            wrapper.comment("item.gear.aoeTools",
                    "Settings for AOE tools (hammer, excavator)",
                    "Match modes determine what blocks are considered similar enough to be mined together.",
                    "LOOSE: Break anything (you probably do not want this)",
                    "MODERATE: Break anything with the same harvest level",
                    "STRICT: Break only the exact same block");

            matchModeStandard = wrapper
                    .builder("item.gear.aoeTools.matchMode.standard")
                    .comment("Match mode for most blocks")
                    .defineEnum(IAOETool.MatchMode.MODERATE);
            matchModeOres = wrapper
                    .builder("item.gear.aoeTools.matchMode.ores")
                    .comment("Match mode for ore blocks (anything in the forge:ores block tag)")
                    .defineEnum(IAOETool.MatchMode.STRICT);

            gearBreaksPermanently = wrapper
                    .builder("item.gear.breaksPermanently")
                    .comment("If true, gear breaks permanently, like vanilla tools and armor")
                    .define(false);

            repairFactorAnvil = wrapper
                    .builder("item.gear.repairs.anvilEffectiveness")
                    .comment("Effectiveness of gear repairs done in an anvil. Set to 0 to disable anvil repairs.")
                    .defineInRange(0.5, 0, 1);
            repairFactorQuick = wrapper
                    .builder("item.gear.repairs.quickEffectiveness")
                    .comment("Effectiveness of quick gear repairs (crafting grid). Set to 0 to disable quick repairs.")
                    .defineInRange(0.35, 0, 1);

            upgradesInAnvilOnly = wrapper
                    .builder("item.gear.upgrades.applyInAnvilOnly")
                    .comment("If true, upgrade parts may only be applied in an anvil.")
                    .define(false);

            wrapper.comment("salvager", "Settings for the salvager");

            salvagerMinLossRate = wrapper
                    .builder("salvager.partLossRate.min")
                    .comment("Minimum rate of part loss when salvaging items. 0 = no loss, 1 = complete loss.",
                            "Rate depends on remaining durability.")
                    .defineInRange(0.0, 0, 1);
            salvagerMaxLossRate = wrapper
                    .builder("salvager.partLossRate.max")
                    .comment("Maximum rate of part loss when salvaging items. 0 = no loss, 1 = complete loss.",
                            "Rate depends on remaining durability.")
                    .defineInRange(0.5, 0, 1);
        }

        public boolean isPlacerTool(ItemStack stack) {
            return isThingInList(stack.getItem(), placerTools);
        }

        public boolean isPlaceableItem(ItemStack stack) {
            return isThingInList(stack.getItem(), placeableItems);
        }

        public boolean isSinewAnimal(EntityLivingBase entity) {
            return isThingInList(entity.getType(), sinewAnimals);
        }

        private static boolean isThingInList(IForgeRegistryEntry<?> thing, ConfigValue<List<? extends String>> list) {
            ResourceLocation name = thing.getRegistryName();
            for (String str : list.get()) {
                ResourceLocation fromList = ResourceLocation.tryCreate(str);
                if (fromList != null && fromList.equals(name)) {
                    return true;
                }
            }
            return false;
        }
    }

    public static class Client {
        public final BooleanValue allowEnchantedEffect;

        Client(ConfigSpecWrapper wrapper) {
            allowEnchantedEffect = wrapper
                    .builder("gear.allowEnchantedEffect")
                    .comment("Allow gear items to have the 'enchanted glow' effect. Set to 'false' to disable the effect.",
                            "The way vanilla handles the effect is bugged, and it is recommended to disable this until custom models are possible again.")
                    .define(false);
        }
    }

    private Config() {}

    public static void init() {
        WRAPPER_CLIENT.validate();
        WRAPPER_CLIENT.validate();
        WRAPPER.validate();
        WRAPPER.validate();
    }

    // TODO: Old stuff below, needs to be fixed

    private static String[] getDefaultNerfedGear() {
        Set<String> toolTypes = ImmutableSet.of("pickaxe", "shovel", "axe", "sword");
        Set<String> toolMaterials = ImmutableSet.of("wooden", "stone", "iron", "golden", "diamond");
        List<String> items = toolTypes.stream()
                .flatMap(type -> toolMaterials.stream()
                        .map(material -> "minecraft:" + material + "_" + type))
                .collect(Collectors.toList());

        Set<String> armorTypes = ImmutableSet.of("helmet", "chestplate", "leggings", "boots");
        Set<String> armorMaterials = ImmutableSet.of("leather", "chainmail", "iron", "diamond", "golden");
        items.addAll(armorTypes.stream()
                .flatMap(type -> armorMaterials.stream()
                        .map(material -> "minecraft:" + material + "_" + type))
                .collect(Collectors.toList()));

        return items.toArray(new String[0]);
    }
}
