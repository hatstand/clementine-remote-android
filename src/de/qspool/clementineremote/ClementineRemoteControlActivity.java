/* This file is part of the Android Clementine Remote.
 * Copyright (C) 2013, Andreas Muttscheller <asfa194@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package de.qspool.clementineremote;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.nsd.NsdManager;
import android.os.Build;
import android.os.Bundle;

import de.qspool.clementineremote.backend.Clementine;
import de.qspool.clementineremote.backend.ClementineService;
import de.qspool.clementineremote.ui.ConnectDialog;
import de.qspool.clementineremote.ui.Player;

public class ClementineRemoteControlActivity extends Activity {
	private final int ID_CONNECT_DIALOG = 0;
	private final int ID_PLAYER_DIALOG = 1;
	
	public final static int RESULT_CONNECT = 1;
	public final static int RESULT_DISCONNECT = 2;
	
	Intent mServiceIntent;
	MulticastServiceListener mServiceListener;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Get the application context
        App.mApp = getApplication();
        
        // Create the main background objects
        checkBackend();
        
        // First start the connectDialog with autoconnect
        if (App.mClementine.isConnected()) {
        	startPlayerDialog();
        } else {
        	startConnectDialog(true);
        }
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	checkBackend();
    	
    	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
    	  listenForService();
    	}
    }
    
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void listenForService() {
        mServiceListener = new MulticastServiceListener();
        NsdManager nsdManager = (NsdManager) getSystemService(Context.NSD_SERVICE);
        nsdManager.discoverServices(
            MulticastServiceListener.CLEMENTINE_SERVICE_TYPE,
            NsdManager.PROTOCOL_DNS_SD,
            mServiceListener);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	// Check what activity has finished. Depending on that, another activity 
    	// is called or the app closes
     	if (requestCode == ID_CONNECT_DIALOG) {
    		if (resultCode == Activity.RESULT_CANCELED) {
        		finish();
        	}
        	if (resultCode == RESULT_CONNECT) {
        		startPlayerDialog();
        	}
        	return;
    	}
    	
    	if (requestCode == ID_PLAYER_DIALOG) {
    		if (resultCode == Activity.RESULT_CANCELED) {
    			finish();
    		}
    		if (resultCode == RESULT_DISCONNECT) {
        		startConnectDialog(false);
        	}
        	return;
    	}
    }
    
    private void checkBackend() {
    	if (App.mClementine == null) {
    		App.mClementine = new Clementine();
	    	mServiceIntent = new Intent(this, ClementineService.class);
	        startService(mServiceIntent);
        }
    }
    
    /**
     * Open the connect dialog
     * @param useAutoConnect true if the application should connect directly to clementine, false if not
     */
    private void startConnectDialog(boolean useAutoConnect) {        
    	Intent connectDialog = new Intent(this, ConnectDialog.class);
    	connectDialog.putExtra(App.SP_KEY_AC, useAutoConnect);
        startActivityForResult(connectDialog, ID_CONNECT_DIALOG);
    }
    
    /**
     * Open the Player dialog
     */
    private void startPlayerDialog() {
    	Intent playerDialog = new Intent(this, Player.class);
    	playerDialog.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
    	startActivityForResult(playerDialog, ID_PLAYER_DIALOG);
    }
}