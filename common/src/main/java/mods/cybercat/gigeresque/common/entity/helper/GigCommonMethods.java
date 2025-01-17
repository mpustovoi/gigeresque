package mods.cybercat.gigeresque.common.entity.helper;

import mods.cybercat.gigeresque.common.entity.GigEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;

public record GigCommonMethods() {

    public static void generateAcidPool(LivingEntity entity, BlockPos pos, int xOffset, int zOffset) {
        var acidEntity = GigEntities.ACID.get().create(entity.level());
        assert acidEntity != null;
        acidEntity.moveTo(pos.offset(xOffset, 0, zOffset), entity.getYRot(), entity.getXRot());
        entity.level().addFreshEntity(acidEntity);
    }

    public static void generateGooBlood(LivingEntity entity, BlockPos pos, int xOffset, int zOffset) {
        var acidEntity = GigEntities.GOO.get().create(entity.level());
        assert acidEntity != null;
        acidEntity.moveTo(pos.offset(xOffset, 0, zOffset), entity.getYRot(), entity.getXRot());
        entity.level().addFreshEntity(acidEntity);
    }
}
