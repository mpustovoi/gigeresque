package mods.cybercat.gigeresque.common.entity.ai.tasks;

import mod.azure.azurelib.common.api.common.animatable.GeoEntity;
import mods.cybercat.gigeresque.interfacing.AbstractAlien;
import mods.cybercat.gigeresque.interfacing.AnimationSelector;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public abstract class CustomDelayedRangedBehaviour<E extends PathfinderMob & AbstractAlien & GeoEntity> extends ExtendedBehaviour<E> {
    protected final int delayTime;
    protected long delayFinishedAt = 0;
    protected Consumer<E> delayedCallback = entity -> {
    };
    private final AnimationSelector<? super E> animationSelector;

    public CustomDelayedRangedBehaviour(int delayTicks, AnimationSelector<? super E> animationSelector) {
        this.delayTime = delayTicks;
        this.animationSelector = animationSelector;
        runFor(entity -> Math.max(delayTicks, 120));
    }

    public final CustomDelayedRangedBehaviour<E> whenActivating(Consumer<E> callback) {
        this.delayedCallback = callback;

        return this;
    }

    @Override
    protected final void start(@NotNull ServerLevel level, @NotNull E entity, long gameTime) {
        if (this.delayTime > 0) {
            this.delayFinishedAt = gameTime + this.delayTime;
            super.start(level, entity, gameTime);
        } else {
            super.start(level, entity, gameTime);
            doDelayedAction(entity);
        }
        animationSelector.select(entity);
    }

    @Override
    protected final void stop(@NotNull ServerLevel level, @NotNull E entity, long gameTime) {
        super.stop(level, entity, gameTime);

        this.delayFinishedAt = 0;
    }

    @Override
    protected boolean shouldKeepRunning(E entity) {
        return this.delayFinishedAt >= entity.level().getGameTime();
    }

    @Override
    protected final void tick(@NotNull ServerLevel level, @NotNull E entity, long gameTime) {
        super.tick(level, entity, gameTime);
        if (entity.getTarget() != null)
            BehaviorUtils.lookAtEntity(entity, entity.getTarget());
        if (this.delayFinishedAt <= gameTime) {
            doDelayedAction(entity);
            this.delayedCallback.accept(entity);
        }
    }

    protected void doDelayedAction(E entity) {
    }
}
