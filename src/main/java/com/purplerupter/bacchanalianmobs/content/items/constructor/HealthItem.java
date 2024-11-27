package com.purplerupter.bacchanalianmobs.content.items.constructor;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

import static com.purplerupter.bacchanalianmobs.content.items.utils.HealthBoostUtils.healPlayer;
import static com.purplerupter.bacchanalianmobs.content.items.utils.HealthBoostUtils.healthBooster;

public class HealthItem extends Item {

    public static final short HEALTH_ITEM_STACK_SIZE = 16;

    public static final String TAG_ADDITIONAL_HEALTH_AMOUNT = "AdditionalHealthAmount_sum";
    public static final String TAG_NUMBERS_OF_HP_BOOST_USE = "NumbersOfHpBoostUse";
    public static final String TAG_DEATH_TEMP_HP_BOOSTS = "DeathTempHpBoosts";

    public static final byte HP_BOOST = 2;

    private final boolean isLoseOnDeath;

    public HealthItem(boolean isLoseOnDeath) {
        this.isLoseOnDeath = isLoseOnDeath;
        init();
    }

    public HealthItem() {
        this.isLoseOnDeath = false;
        init();
    }

    private void init() {
        // TODO tooltip
        setMaxStackSize(HEALTH_ITEM_STACK_SIZE);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (!world.isRemote) {

            // взять стоимость из конфига и затребовать её
            short cost = 3; // TODO config
            if (player.experienceLevel < cost) {
                player.sendMessage(new TextComponentString("Not enough XP to use this item!"));
                return pass(stack);
            }

            byte increase = HP_BOOST;
            byte loseOnDeath = 0;
            if (this.isLoseOnDeath) {
                increase = HP_BOOST + HP_BOOST;
                loseOnDeath = HP_BOOST;
            }

            // получить количество использований в прошлом из nbt
            NBTTagCompound data = player.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
            if (data == null) {
                System.out.println("Error! Cannot find Persisted tag!!!");
                return pass(stack);
            }
            // TODO лимит на количество использований???
            // TODO записать данные в тег

            data.setShort(TAG_NUMBERS_OF_HP_BOOST_USE, (short)(data.getShort(TAG_NUMBERS_OF_HP_BOOST_USE) + 1));
            if (this.isLoseOnDeath) {
                data.setShort(TAG_DEATH_TEMP_HP_BOOSTS, (short)(data.getShort(TAG_DEATH_TEMP_HP_BOOSTS) + 1));
            }

            // увеличить ХП игрока
            healthBooster(world, player, stack, increase);
            data.setFloat(TAG_ADDITIONAL_HEALTH_AMOUNT, data.getFloat(TAG_ADDITIONAL_HEALTH_AMOUNT) + increase);

            // забрать уровни опыта
            player.addExperienceLevel(-cost);

            // восстановить часть здоровья и дать эффект регенерации, либо просто дать эффект
            healPlayer(player, player.getHealth(), player.getMaxHealth());

        }
        return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
    }

    private static ActionResult<ItemStack> pass(ItemStack stack) {
        return ActionResult.newResult(EnumActionResult.PASS, stack);
    }
}
