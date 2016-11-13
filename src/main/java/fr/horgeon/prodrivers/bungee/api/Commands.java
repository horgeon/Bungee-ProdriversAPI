package fr.horgeon.prodrivers.bungee.api;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;

public class Commands extends Command {
	private ProdriversAPI plugin;
	
	Commands() {
		super( "prodriversapi" );
	}
	
	void setPlugin( ProdriversAPI plugin ) {
		this.plugin = plugin;
	}

	@Override
	public void execute( CommandSender sender, String[] args ) {
		if( args.length > 0 ) {
			switch( args[ 0 ] ) {
				case "start":
					this.plugin.startCommand( sender );
					break;

				case "stop":
					this.plugin.stopCommand( sender );
					break;

				case "restart":
					this.plugin.restartCommand( sender );
					break;

				case "reload":
					this.plugin.reloadCommand( sender );
					break;

				case "addkey":
					this.plugin.addKeyCommand( sender );
					break;
			}
		} else {
			this.plugin.sendChat( sender, "Usage:" );
			this.plugin.sendChat( sender, "/prodriversapi <start/stop/restart/reload/addkey>" );
		}
	}
}
