package data.scripts;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.listeners.ABC_AILootListener;
import com.fs.starfarer.api.campaign.listeners.ListenerManagerAPI;

public class ABC_ModPlugin extends BaseModPlugin {

    public void onGameLoad(boolean newGame) {
        ABC_AILootListener.register();
    }
}
