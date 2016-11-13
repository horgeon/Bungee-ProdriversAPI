package fr.horgeon.prodrivers.bungee.api;

import fr.horgeon.apiserver.HTTPHandler;
import fr.horgeon.apiserver.HTTPServer;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginDescription;
import net.md_5.bungee.api.scheduler.TaskScheduler;

import java.util.concurrent.TimeUnit;

public class ProdriversAPI extends Plugin {
	private Chat chat;
	private PluginConfiguration config;
	private HTTPServer server;

	@Override
	public void onDisable() {
		stopServer();
	}

	@Override
	public void onEnable() {
		PluginDescription plugindescription = this.getDescription();
		this.chat = new Chat( plugindescription.getName() );
		this.config = new PluginConfiguration( this );

		Commands commands = new Commands();
		commands.setPlugin( this );
		getProxy().getPluginManager().registerCommand( this, commands );

		initServer();
	}

	public void registerHandler( String endpoint, HTTPHandler handler ) {
		if( this.server != null )
			this.server.registerHandler( endpoint, handler );
	}

	public void unregisterHandler( String endpoint ) {
		if( this.server != null )
			this.server.unregisterHandler( endpoint );
	}

	private void initServer() {
		if( this.server != null )
			return;

		if( !createServer() )
			return;

		try {
			loadKeys();

		} catch( Exception e ) {
			getLogger().severe( "Couldn't load keys!" );
			e.printStackTrace();
			return;
		}

		TaskScheduler scheduler = getProxy().getScheduler();
		scheduler.schedule( this, new Runnable() {
			@Override
			public void run() {
				startServer();
			}
		}, 1L, TimeUnit.MILLISECONDS );
	}

	private boolean createServer() {
		try {
			if( this.server == null ) {
				this.server = new HTTPServer( this.config.getInt( "port" ) );
			}
		} catch( Exception e ) {
			getLogger().severe( "The HTTP server couldn't be created!" );
			e.printStackTrace();
			return false;
		}

		return true;
	}

	private void startServer() {
		try {
			if( this.server != null ) {
				this.server.start();
				getLogger().info( String.format( "HTTP Server now listening on port %d.", this.server.port ) );
			}
		} catch( Exception e ) {
			getLogger().severe( "The HTTP server couldn't be started!" );
			e.printStackTrace();
		}
	}

	private void stopServer() {
		try {
			if( this.server != null ) {
				this.server.stop();

				getLogger().info( "HTTP Server stopped." );
			}
		} catch( Exception e ) {
			getLogger().severe( "The HTTP server couldn't be stopped! It is highly recommended to restart the server." );
			e.printStackTrace();
		}
	}

	private void restartServer() {
		try {
			if( this.server != null ) {
				if( this.server.isStarted() )
					this.server.restart();
				else
					this.server.start();
				getLogger().info( String.format( "HTTP Server restarted, now listening on port %d.", this.server.port ) );
			}
		} catch( Exception e ) {
			getLogger().severe( "The HTTP server couldn't be restarted! It is highly recommended to restart the server." );
			e.printStackTrace();
		}
	}

	private void loadKeys() {
		this.server.setKeys( this.config.getKeys() );
	}

	void startCommand( CommandSender sender ) {
		if( sender.hasPermission( "prodriversapi.start" ) ) {
			if( this.server != null ) {
				if( this.server.isStarted() ) {
					chat.error( sender, this.config.getMessage( "serveralreadystarted" ) );
				} else {
					try {
						startServer();

						chat.success( sender, this.config.getMessage( "serverstarted" ) );
					} catch( Exception e ) {
						chat.error( sender, this.config.getMessage( "errorocurred" ) );
						chat.error( sender, e.getLocalizedMessage() );
					}
				}
			} else {
				try {
					initServer();

					chat.success( sender, this.config.getMessage( "serverstarted" ) );
				} catch( Exception e ) {
					chat.error( sender, this.config.getMessage( "errorocurred" ) );
					chat.error( sender, e.getLocalizedMessage() );
				}
			}
		} else {
			chat.error( sender, this.config.getMessage( "nopermission" ) );
		}
	}

	void stopCommand( CommandSender sender ) {
		if( sender.hasPermission( "prodriversapi.stop" ) ) {
			if( ( this.server == null ) || ( !this.server.isStarted() ) ) {
				chat.error( sender, this.config.getMessage( "serveralreadystopped" ) );
			} else {
				try {
					stopServer();

					chat.success( sender, this.config.getMessage( "serverstopped" ) );
				} catch( Exception e ) {
					chat.error( sender, this.config.getMessage( "errorocurred" ) );
					chat.error( sender, e.getLocalizedMessage() );
				}

				chat.success( sender, this.config.getMessage( "serverstopped" ) );
			}
		} else {
			chat.error( sender, this.config.getMessage( "nopermission" ) );
		}
	}

	void restartCommand( CommandSender sender ) {
		if( sender.hasPermission( "prodriversapi.restart" ) ) {
			if( this.server != null ) {
				try {
					restartServer();

					chat.success( sender, this.config.getMessage( "serverrestarted" ) );
				} catch( Exception e ) {
					chat.error( sender, this.config.getMessage( "errorocurred" ) );
					chat.error( sender, e.getLocalizedMessage() );
				}
			}
		} else {
			chat.error( sender, this.config.getMessage( "nopermission" ) );
		}
	}

	void reloadCommand( CommandSender sender ) {
		if( sender.hasPermission( "prodriversapi.reload" ) ) {
			this.config.reload();

			chat.success( sender, this.config.getMessage( "configurationreloaded" ) );
		} else {
			chat.error( sender, this.config.getMessage( "nopermission" ) );
		}
	}

	void addKeyCommand( CommandSender sender ) {
		if( sender.hasPermission( "prodriversapi.addkey" ) ) {
			String publicKey = this.config.addKey();

			if( !publicKey.equalsIgnoreCase( "" ) )
				chat.success( sender, String.format( this.config.getMessage( "keyadded" ), publicKey ) );
			else
				chat.error( sender, this.config.getMessage( "keynotadded" ) );
		} else {
			chat.error( sender, this.config.getMessage( "nopermission" ) );
		}
	}

	void sendChat( CommandSender sender, String message ) {
		this.chat.send( sender, message );
	}
}
