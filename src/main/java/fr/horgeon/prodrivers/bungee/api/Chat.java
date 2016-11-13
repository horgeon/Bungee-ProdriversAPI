package fr.horgeon.prodrivers.bungee.api;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;

public class Chat {
	private String prefix;

	public Chat( String name ) {
		this.prefix = ChatColor.BOLD + "[" + ChatColor.GOLD + name + ChatColor.WHITE + "] " + ChatColor.RESET;
	}

	public void send( CommandSender sender, String message, ChatColor color ) {
		sender.sendMessage( new TextComponent( this.prefix + color + message ) );
	}

	public void send( CommandSender sender, String message ) {
		send( sender, message, ChatColor.WHITE );
	}

	public void success( CommandSender sender, String message ) {
		send( sender, message, ChatColor.GREEN );
	}

	public void error( CommandSender sender, String message ) {
		send( sender, message, ChatColor.RED );
	}
}
