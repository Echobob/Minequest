/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.martin.bukkit.npclib;

import java.util.logging.Level;
import java.util.logging.Logger;

import net.minecraft.server.EntityPlayer;
import net.minecraft.server.InventoryLargeChest;
import net.minecraft.server.TileEntityChest;

import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

/**
 *
 * @author martin
 */
public class BPlayer {

    private CraftPlayer cPlayer;
    private EntityPlayer ePlayer;
    public BPlayer(Player player) {
        try {
            cPlayer = (CraftPlayer) player;
            ePlayer = cPlayer.getHandle();
            BServer.getInstance(player.getServer()).getWorld(player.getWorld().getName()).getPlayerManager();
        } catch (Exception ex) {
            Logger.getLogger("Minecraft").log(Level.SEVERE, null, ex);
        }
    }

    public void openVirtualChest(TileEntityChest chest) {
        ePlayer.a(chest);
    }

    public void openVirtualChest(InventoryLargeChest lChest) {
        ePlayer.a(lChest);
    }

    
}
