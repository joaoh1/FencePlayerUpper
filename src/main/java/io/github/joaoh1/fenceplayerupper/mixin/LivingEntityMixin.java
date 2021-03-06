/*
 * Fence Player Upper
 * Copyright (C) 2020-2021 boredomh1
 *
 * This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.joaoh1.fenceplayerupper.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Map;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import io.github.joaoh1.fenceplayerupper.utils.UpperUtils;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
	public LivingEntityMixin(EntityType<?> type, World world) {
		super(type, world);
	}

	@Shadow
	@Final
	private Map<StatusEffect, StatusEffectInstance> activeStatusEffects;

	@Shadow
	public boolean hasStatusEffect(StatusEffect effect) {
		return this.activeStatusEffects.containsKey(effect);
	}

	@Inject(at = @At("RETURN"), method = "getJumpVelocity()F", cancellable = true)
	private float increaseJumpVelocity(CallbackInfoReturnable<Float> cir) {
		float jumpVelocity = cir.getReturnValueF();
		if (this.getType().isIn(UpperUtils.ALLOWED_ENTITIES)) {
			if (!this.hasStatusEffect(StatusEffects.JUMP_BOOST)) {
				BlockPos currentPos = this.getBlockPos();
				BlockPos[] positionsToCheck = UpperUtils.createFencePosArray(
					currentPos,
					Math.round(this.getRotationVector(0, this.getYaw()).getX() * 4.0) / 4.0,
					Math.round(this.getRotationVector(0, this.getYaw()).getZ() * 4.0) / 4.0
				);
				boolean boostJump = false;
				for (BlockPos blockPos : positionsToCheck) {
					//this.world.addParticle(ParticleTypes.ANGRY_VILLAGER, blockPos.getX() + 0.5, blockPos.getY() - 0.5, blockPos.getZ() + 0.5, 0, 0, 0);
					if (UpperUtils.canJumpFence(this.world, blockPos)) {
						boostJump = true;
						break;
					} else if (!this.world.getBlockState(blockPos).getCollisionShape(this.world, blockPos).isEmpty()) {
						break;
					}
				}
				if (boostJump) {
					if (!this.world.isClient) {
						if (this.isPlayer()) {
							jumpVelocity -= 0.03F;
						} else {
							jumpVelocity += 0.06F;
						}
					}
					this.velocityModified = true;
					cir.setReturnValue(jumpVelocity);
				}
			}
		}
		return jumpVelocity;
	}
}
