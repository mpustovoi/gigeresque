package mods.cybercat.gigeresque.common.block.entity;

import mod.azure.azurelib.animatable.GeoBlockEntity;
import mod.azure.azurelib.core.animatable.instance.AnimatableInstanceCache;
import mod.azure.azurelib.core.animation.AnimatableManager.ControllerRegistrar;
import mod.azure.azurelib.core.animation.AnimationController;
import mod.azure.azurelib.core.object.PlayState;
import mod.azure.azurelib.util.AzureLibUtil;
import mods.cybercat.gigeresque.common.block.storage.StorageProperties;
import mods.cybercat.gigeresque.common.block.storage.StorageStates;
import mods.cybercat.gigeresque.common.entity.Entities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public class IdolStorageEntity extends RandomizableContainerBlockEntity implements GeoBlockEntity {

	private NonNullList<ItemStack> items = NonNullList.withSize(9, ItemStack.EMPTY);
	private final AnimatableInstanceCache cache = AzureLibUtil.createInstanceCache(this);
	public static final EnumProperty<StorageStates> CHEST_STATE = StorageProperties.STORAGE_STATE;
	private final ContainerOpenersCounter stateManager = new ContainerOpenersCounter() {

		@Override
		protected void onOpen(Level world, BlockPos pos, BlockState state) {
		}

		@Override
		protected void onClose(Level world, BlockPos pos, BlockState state) {
		}

		@Override
		protected void openerCountChanged(Level world, BlockPos pos, BlockState state, int oldViewerCount,
				int newViewerCount) {
			IdolStorageEntity.this.onInvOpenOrClose(world, pos, state, oldViewerCount, newViewerCount);
		}

		@Override
		protected boolean isOwnContainer(Player player) {
			if (player.containerMenu instanceof ChestMenu) {
				var inventory = ((ChestMenu) player.containerMenu).getContainer();
				return inventory == IdolStorageEntity.this;
			}
			return false;
		}
	};

	public IdolStorageEntity(BlockPos pos, BlockState state) {
		super(Entities.ALIEN_STORAGE_BLOCK_ENTITY_3, pos, state);
	}

	@Override
	public void load(CompoundTag nbt) {
		super.load(nbt);
		this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
		if (!this.tryLoadLootTable(nbt))
			ContainerHelper.loadAllItems(nbt, this.items);
	}

	@Override
	public void saveAdditional(CompoundTag nbt) {
		super.saveAdditional(nbt);
		if (!this.trySaveLootTable(nbt))
			ContainerHelper.saveAllItems(nbt, this.items);
	}

	@Override
	public int getContainerSize() {
		return 9;
	}

	@Override
	public NonNullList<ItemStack> getItems() {
		return this.items;
	}

	@Override
	protected void setItems(NonNullList<ItemStack> list) {
		this.items = list;
	}

	@Override
	protected Component getDefaultName() {
		return Component.translatable("block.gigeresque.alien_storage_block3");
	}

	@Override
	protected AbstractContainerMenu createMenu(int syncId, Inventory inventory) {
		return new ChestMenu(MenuType.GENERIC_3x3, syncId, inventory, this, 1);
	}

	@Override
	public void startOpen(Player player) {
		if (!this.isRemoved() && !player.isSpectator())
			this.stateManager.incrementOpeners(player, this.getLevel(), this.getBlockPos(), this.getBlockState());
	}

	@Override
	public void stopOpen(Player player) {
		if (!this.isRemoved() && !player.isSpectator())
			this.stateManager.decrementOpeners(player, this.getLevel(), this.getBlockPos(), this.getBlockState());
	}

	public void tick() {
		if (!this.isRemoved())
			this.stateManager.recheckOpeners(this.getLevel(), this.getBlockPos(), this.getBlockState());
	}

	protected void onInvOpenOrClose(Level world, BlockPos pos, BlockState state, int oldViewerCount,
			int newViewerCount) {
		var block = state.getBlock();
		world.blockEvent(pos, block, 1, newViewerCount);
		if (oldViewerCount != newViewerCount)
			if (newViewerCount > 0)
				world.setBlockAndUpdate(pos, state.setValue(CHEST_STATE, StorageStates.OPENED));
			else
				world.setBlockAndUpdate(pos, state.setValue(CHEST_STATE, StorageStates.CLOSING));
	}

	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache() {
		return this.cache;
	}

	@Override
	public void registerControllers(ControllerRegistrar controllers) {
		controllers.add(new AnimationController<>(this, event -> {
			return PlayState.CONTINUE;
		}));
	}
}