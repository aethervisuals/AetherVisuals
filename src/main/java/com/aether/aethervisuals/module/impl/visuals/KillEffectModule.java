package com.aether.aethervisuals.module.impl.visuals;

import com.aether.aethervisuals.module.Category;
import com.aether.aethervisuals.module.Module;
import com.aether.aethervisuals.module.settings.ModeSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvents;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class KillEffectModule extends Module {

    public final ModeSetting mode = new ModeSetting("Эффект", "Молния", "Молния", "Неоновый взрыв");
    private final Set<Integer> trackedTargets = new HashSet<>();

    public KillEffectModule() {
        super("KillEffect", "Эффектный разряд при победе над целью (ПКМ для выбора)", Category.VISUALS);
        addSettings(mode);
        setEnabled(true);
    }

    public void trackTarget(Entity entity) {
        if (isEnabled() && entity instanceof LivingEntity) {
            trackedTargets.add(entity.getId());
        }
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null || mc.player == null || trackedTargets.isEmpty()) return;

        Iterator<Integer> it = trackedTargets.iterator();
        while (it.hasNext()) {
            int id = it.next();
            Entity e = mc.world.getEntityById(id);
            if (e == null) {
                it.remove();
            } else if (e instanceof LivingEntity living && (!living.isAlive() || living.getHealth() <= 0.0f)) {
                spawnEffect(mc, living);
                it.remove();
            }
        }
    }

    private void spawnEffect(MinecraftClient mc, LivingEntity target) {
        if (mode.getValue().equals("Молния")) {
            LightningEntity bolt = new LightningEntity(EntityType.LIGHTNING_BOLT, mc.world);
            bolt.refreshPositionAfterTeleport(target.getX(), target.getY(), target.getZ());
            bolt.setCosmetic(true);
            mc.world.addEntity(bolt);
            mc.player.playSound(SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, 0.35f, 1.2f);
        } else {
            for (int i = 0; i < 35; i++) {
                double vx = (mc.world.random.nextDouble() - 0.5) * 0.45;
                double vy = mc.world.random.nextDouble() * 0.45;
                double vz = (mc.world.random.nextDouble() - 0.5) * 0.45;
                mc.world.addParticle(ParticleTypes.END_ROD, target.getX(), target.getY() + 1.0, target.getZ(), vx, vy, vz);
            }
            mc.player.playSound(SoundEvents.ENTITY_FIREWORK_ROCKET_BLAST, 0.5f, 1.3f);
        }
    }
}
