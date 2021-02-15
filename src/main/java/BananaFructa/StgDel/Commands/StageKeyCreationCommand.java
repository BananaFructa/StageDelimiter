package BananaFructa.StgDel.Commands;

import BananaFructa.StgDel.StageKey;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumHand;

public class StageKeyCreationCommand extends CommandBase {
    @Override
    public String getName() {
        return "setkey";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "command.key.usage";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        EntityPlayerMP player = ((EntityPlayerMP)sender.getCommandSenderEntity());
        ItemStack heldItem = player.getHeldItem(EnumHand.MAIN_HAND);
        if (heldItem.getItem().getClass() == StageKey.class && (args.length > 0 ? !args[0].chars().anyMatch(o -> !Character.isDigit((char)o)) : false)) {
            NBTTagCompound nbt = new NBTTagCompound();
            int i = Integer.parseInt(args[0]);
            nbt.setInteger("stage",i);
            heldItem.setTagCompound(nbt);
        }
    }
}
