package fr.horgeon.prodrivers.bungee.api;

import com.amazon.webservices.common.HMACEncoding;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.*;
import java.nio.file.FileSystemException;
import java.security.SecureRandom;
import java.util.*;

public class PluginConfiguration {
	private Plugin plugin;

	private Configuration config;
	private Configuration messages;

	private Map<String, String> keys;

	public PluginConfiguration( Plugin plugin ) {
		this.plugin = plugin;

		loadConfig();
		loadMessages();
		loadKeys();
	}

	private void copy( InputStream in, File file ) {
		try {
			OutputStream out = new FileOutputStream( file );
			byte[] buf = new byte[ 1024 ];
			int len;
			while( ( len = in.read( buf ) ) > 0 ) {
				out.write( buf, 0, len );
			}
			out.close();
			in.close();
		} catch( Exception e ) {
			e.printStackTrace();
		}
	}

	private void loadConfig() {
		try {
			File pluginconfigfile = new File( this.plugin.getDataFolder(), "config.yml" );

			if( !pluginconfigfile.exists() ) {
				if( pluginconfigfile.getParentFile().mkdirs() ) {
					copy( this.plugin.getResourceAsStream( "config.yml" ), pluginconfigfile );
				} else {
					throw new FileSystemException( "Unable to create plugin's configuration directory." );
				}
			}

			this.config = ConfigurationProvider.getProvider( YamlConfiguration.class ).load( pluginconfigfile );
		} catch( Exception e ) {
			e.printStackTrace();
		}
	}

	private void loadMessages() {
		try {
			File pluginmessagesfile = new File( this.plugin.getDataFolder(), "messages.yml" );

			if( !pluginmessagesfile.exists() ) {
				if( pluginmessagesfile.getParentFile().mkdirs() ) {
					copy( this.plugin.getResourceAsStream( "messages.yml" ), pluginmessagesfile );
				} else {
					throw new FileSystemException( "Unable to create plugin's configuration directory." );
				}
			}

			this.messages = ConfigurationProvider.getProvider( YamlConfiguration.class ).load( pluginmessagesfile );
		} catch( Exception e ) {
			e.printStackTrace();
		}
	}

	private void loadKeys() {
		if( this.keys == null ) {
			this.keys = new HashMap<>();
		} else {
			this.keys.clear();
		}

		unserializeKeys( this.config.getStringList( "keys" ) );
	}

	private void unserializeKeys( List<String> serializedKeys ) {
		if( serializedKeys == null )
			return;

		for( String key : serializedKeys ) {
			String pair[] = key.split( ":" );

			if( pair.length > 1 ) {
				this.keys.put( pair[ 0 ], pair[ 1 ] );
			}
		}
	}

	private String generateKey() throws Exception {
		SecureRandom rand = new SecureRandom();
		KeyGenerator keyGen = KeyGenerator.getInstance( "HmacSHA256" );
		keyGen.init( rand );
		keyGen.init( 512 );
		SecretKey key = keyGen.generateKey();
		String keystr = HMACEncoding.EncodeBase64( key.getEncoded() );
		keystr = keystr.replaceAll( ":", "=" );
		return keystr;
	}

	public String addKey() {
		try {
			String publicKey = generateKey();
			this.keys.put( publicKey, generateKey() );
			this.config.set( "keys", serializeKeys() );
			ConfigurationProvider.getProvider( YamlConfiguration.class ).save( this.config, new File( this.plugin.getDataFolder(), "config.yml" ) );

			return publicKey;
		} catch( Exception e ) {
			System.err.println( "Could not generate key!" );
			e.printStackTrace();
		}

		return "";
	}

	public List<String> serializeKeys() {
		List<String> serializedKeys = new ArrayList<>();

		for( Map.Entry<String, String> entry : this.keys.entrySet() ) {
			serializedKeys.add( String.format( "%s:%s", entry.getKey(), entry.getValue() ) );
		}

		return serializedKeys;
	}

	public void reload() {
		loadConfig();
		loadMessages();
		loadKeys();
	}

	public String getString( String key ) {
		return this.config.getString( key );
	}

	public Integer getInt( String key ) {
		return this.config.getInt( key );
	}

	public Map<String, String> getKeys() {
		return this.keys;
	}

	public String getMessage( String key ) {
		return this.messages.getString( key );
	}
}
