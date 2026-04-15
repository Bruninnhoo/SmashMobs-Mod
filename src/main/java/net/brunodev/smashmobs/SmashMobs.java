package net.brunodev.smashmobs;

import net.brunodev.smashmobs.client.ClientEvents;
import net.brunodev.smashmobs.client.ClientModEvents;
import net.brunodev.smashmobs.entity.GolemTrainEntity;
import net.brunodev.smashmobs.entity.IronGolemMorph;
import net.brunodev.smashmobs.entity.SmashTntEntity;
import net.brunodev.smashmobs.item.*;
import net.brunodev.smashmobs.registration.ModAttachments;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

@Mod(SmashMobs.MODID)
public class SmashMobs {
        public static final String MODID = "smashmobs";
        public static final Logger LOGGER = LogUtils.getLogger();

        public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister
                        .create(net.minecraft.core.registries.Registries.ENTITY_TYPE, MODID);

        public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
        public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);

        public static final DeferredRegister<SoundEvent> SOUNDS = net.neoforged.neoforge.registries.DeferredRegister
                        .create(net.minecraft.core.registries.Registries.SOUND_EVENT, MODID);

        public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister
                        .create(Registries.CREATIVE_MODE_TAB, MODID);

        public static final java.util.function.Supplier<SoundEvent> PICK_MOB_SOUND = SOUNDS.register("pick_mob",
                        SoundEvent::createVariableRangeEvent);

        /*
         * ==================================================================
         * MOBS E ITEMS
         * ==================================================================
         */

        // =-=-=-==-= CREEPER =-=-=-==-=
        public static final DeferredItem<CreeperExplosionItem> CREEPER_EXPLOSION = ITEMS.registerItem(
                        "creeper_explosion", p -> new net.brunodev.smashmobs.item.CreeperExplosionItem(p.stacksTo(1)));

        public static final DeferredItem<CreeperThrowTntItem> CREEPER_THROW_TNT = ITEMS.registerItem(
                        "throw_tnt", p -> new CreeperThrowTntItem(p.stacksTo(1)));

        public static final net.neoforged.neoforge.registries.DeferredItem<CreeperSupremeItem> CREEPER_SUPREME = ITEMS
                        .registerItem(
                                        "creeper_supreme",
                                        p -> new net.brunodev.smashmobs.item.CreeperSupremeItem(p.stacksTo(1)));

        public static final DeferredHolder<EntityType<?>, EntityType<SmashTntEntity>> SMASH_TNT = ENTITIES.register(
                        "smash_tnt",
                        (identifier) -> EntityType.Builder.<SmashTntEntity>of(SmashTntEntity::new, MobCategory.MISC)
                                        .sized(0.98F, 0.98F)
                                        .fireImmune()
                                        .build(net.minecraft.resources.ResourceKey.create(Registries.ENTITY_TYPE,
                                                        identifier)));

        public static final Supplier<SoundEvent> CREEPER_LAUNCH_SOUND = SOUNDS.register("creeper_launch",
                        SoundEvent::createVariableRangeEvent);

        // =-=-=-==-= IRON GOLEM =-=-=-==-=
        public static final DeferredItem<GolemAnvilItem> GOLEM_THROW_ANVIL = ITEMS.registerItem(
                        "golem_throw_anvil", p -> new GolemAnvilItem(p.stacksTo(1)));

        public static final DeferredItem<GolemGrabItem> GOLEM_GRAB = ITEMS.registerItem(
                        "golem_grab", p -> new net.brunodev.smashmobs.item.GolemGrabItem(p.stacksTo(1)));

        public static final DeferredItem<GolemUltItem> GOLEM_SUPREME = ITEMS.registerItem(
                        "golem_supreme", p -> new net.brunodev.smashmobs.item.GolemUltItem(p.stacksTo(1)));

        // Registra a Entidade do Trem
        public static final Supplier<EntityType<GolemTrainEntity>> GOLEM_TRAIN = ENTITIES.register("golem_train",
                        (net.minecraft.resources.Identifier identifier) -> EntityType.Builder
                                        .of(net.brunodev.smashmobs.entity.GolemTrainEntity::new,
                                                        net.minecraft.world.entity.MobCategory.MISC)
                                        .sized(3.0F, 3.0F) // O trem é gigante (3x3 blocos)
                                        .clientTrackingRange(10)
                                        .updateInterval(1)
                                        .build(net.minecraft.resources.ResourceKey.create(
                                                        net.minecraft.core.registries.Registries.ENTITY_TYPE,
                                                        identifier)));

        public static final DeferredHolder<EntityType<?>, EntityType<IronGolemMorph>> IRON_GOLEM_MORPH = ENTITIES
                        .register("iron_golem_morph", (
                                        identifier) -> EntityType.Builder.<net.brunodev.smashmobs.entity.IronGolemMorph>of(
                                                        net.brunodev.smashmobs.entity.IronGolemMorph::new,
                                                        MobCategory.MISC)
                                                        .sized(1.4F, 2.7F)
                                                        .build(net.minecraft.resources.ResourceKey
                                                                        .create(Registries.ENTITY_TYPE, identifier)));

        public static final Supplier<SoundEvent> GOLEM_THROW_ANVIL_SOUND = SOUNDS.register("golem_throw_anvil",
                        SoundEvent::createVariableRangeEvent);

        public static final Supplier<SoundEvent> GOLEM_SUPREME_SOUND = SOUNDS.register("golem_supreme",
                        SoundEvent::createVariableRangeEvent);

        // =-=-=-==-= GOAT =-=-=-==-=
        public static final DeferredItem<GoatDashItem> GOAT_DASH = ITEMS.registerItem(
                        "goat_dash", p -> new GoatDashItem(p.stacksTo(1)));

        public static final DeferredItem<GoatTongueItem> GOAT_TONGUE = ITEMS.registerItem(
                        "goat_tongue", p -> new GoatTongueItem(p.stacksTo(1)));

        /*
         * ==================================================================
         * TAB CRIATIVA
         * ==================================================================
         */
        public static final DeferredHolder<CreativeModeTab, CreativeModeTab> SMASH_MOBS_TAB = CREATIVE_MODE_TABS
                        .register("example_tab", () -> CreativeModeTab.builder()
                                        .title(Component.translatable("itemGroup.smashmobs"))
                                        .withTabsBefore(CreativeModeTabs.COMBAT)
                                        .icon(() -> CREEPER_EXPLOSION.get().getDefaultInstance())
                                        .displayItems((parameters, output) -> {
                                                output.accept(CREEPER_EXPLOSION.get());
                                        }).build());

        // ==================================================================
        // CONSTRUTOR PRINCIPAL DO MOD
        // ==================================================================
        public SmashMobs(IEventBus modEventBus, ModContainer modContainer) {
                modEventBus.addListener(this::commonSetup);

                BLOCKS.register(modEventBus);
                ITEMS.register(modEventBus);
                SOUNDS.register(modEventBus);
                ENTITIES.register(modEventBus);
                CREATIVE_MODE_TABS.register(modEventBus);
                ModAttachments.ATTACHMENT_TYPES.register(modEventBus);

                NeoForge.EVENT_BUS.register(this);
                NeoForge.EVENT_BUS.register(ClientEvents.class);

                modEventBus.addListener(this::addCreative);
                modEventBus.addListener(this::registerAttributes);
                modEventBus.addListener(ClientModEvents::registerRenderers);

                modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        }

        private void commonSetup(FMLCommonSetupEvent event) {
                LOGGER.info("HELLO FROM SMASH MOBS SETUP!");
        }

        private void addCreative(BuildCreativeModeTabContentsEvent event) {
                if (event.getTabKey() == CreativeModeTabs.COMBAT) {
                        event.accept(CREEPER_EXPLOSION);
                        event.accept(CREEPER_THROW_TNT);
                        event.accept(CREEPER_SUPREME);
                }
        }

        private void registerAttributes(net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent event) {
                event.put(IRON_GOLEM_MORPH.get(), net.minecraft.world.entity.Mob.createMobAttributes().build());
        }

        @SubscribeEvent
        public void onServerStarting(ServerStartingEvent event) {
                LOGGER.info("SMASH MOBS SERVER IS STARTING!");
        }
}