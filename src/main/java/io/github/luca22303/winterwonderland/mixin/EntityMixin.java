package io.github.luca22303.winterwonderland.mixin;

import io.github.luca22303.winterwonderland.ModSounds;
import io.github.luca22303.winterwonderland.config.WinterConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Layers a soft, crunchy sound on top of players' footsteps in snow. */
@Mixin(Entity.class)
public abstract class EntityMixin {
	@Shadow
	@Final
	protected RandomSource random;

	@Shadow
	public abstract void playSound(net.minecraft.sounds.SoundEvent sound, float volume, float pitch);

	@Inject(method = "walkingStepSound", at = @At("HEAD"))
	private void winterwonderland$snowCrunch(BlockPos pos, BlockState state, CallbackInfo ci) {
		if (!((Object) this instanceof Player) || !WinterConfig.get().snowCrunch) {
			return;
		}
		if (state.is(Blocks.SNOW) || state.is(Blocks.SNOW_BLOCK) || state.is(Blocks.POWDER_SNOW)) {
			this.playSound(ModSounds.SNOW_CRUNCH, 0.18F, 0.85F + this.random.nextFloat() * 0.3F);
		}
	}
}
