package data.scripts;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Items;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.BaseSalvageSpecial;

import java.util.ArrayList;
import java.util.Random;

public class ABC_ModPlugin extends BaseModPlugin {
    ArrayList<SectorEntityToken> listSurveyShip = new ArrayList<SectorEntityToken>();
    ArrayList<SectorEntityToken> listMothership = new ArrayList<SectorEntityToken>();
    ArrayList<SectorEntityToken> listNexus = new ArrayList<SectorEntityToken>();
    Random rand = new Random();
    String[] derelictBPs = {"defender", "picket", "sentry", "warden", "bastillon", "berserker", "rampart"};
    String[] derelictLargeBP = {"guardian"};
    String[] remnantShipBPs = {"lumen", "glimmer", "fulgent", "scintilla", "brilliant", "apex", "nova", "radiant"};
    String[] remnantFighterBPs = {"spark_wing", "lux_wing", "flash_wing"};

    public void onNewGameAfterProcGen(){
        //Iterates through all fleets and custom entities in the game to find Derelict stations and Remnant Nexuses
        for (LocationAPI system : Global.getSector().getAllLocations()) {
            for (CampaignFleetAPI entity : system.getFleets()) {
                if (entity.getFaction().getId().equals(Factions.REMNANTS)) {
                    for (FleetMemberAPI member : entity.getMembersWithFightersCopy()) {
                        if (member.isStation()) {listNexus.add(entity);}
                        }
                    }
                }
            for (SectorEntityToken entity : system.getCustomEntitiesWithTag(Tags.SALVAGEABLE)) {
                switch(entity.getCustomEntityType()) {
                    case Entities.DERELICT_SURVEY_SHIP:
                        listSurveyShip.add(entity);
                        break;
                    case Entities.DERELICT_MOTHERSHIP:
                        listMothership.add(entity);
                        break;
                }
            }
        }

        //Adds Derelict blueprints to survey ships
        for (String bp : derelictBPs) {
            int pick = rand.nextInt(listSurveyShip.size());
            CargoAPI cargo = Global.getFactory().createCargo(false);
            cargo.addSpecial(new SpecialItemData(Items.SHIP_BP, bp), 1);
            BaseSalvageSpecial.addExtraSalvage(listSurveyShip.get(pick), cargo);
        }

        //Adds Guardian blueprint to one mothership
        for (String bp : derelictLargeBP) {
            int pick = rand.nextInt(listMothership.size());
            CargoAPI cargo = Global.getFactory().createCargo(false);
            cargo.addSpecial(new SpecialItemData(Items.SHIP_BP, bp), 1);
            BaseSalvageSpecial.addExtraSalvage(listMothership.get(pick), cargo);
        }

        //Adds Remnant ship blueprints to Nexuses
        for (String bp : remnantShipBPs) {
            int pick = rand.nextInt(listNexus.size());
            CargoAPI cargo = Global.getFactory().createCargo(false);
            cargo.addSpecial(new SpecialItemData(Items.SHIP_BP, bp), 1);
            BaseSalvageSpecial.addExtraSalvage(listNexus.get(pick), cargo);
        }

        //Adds Remnant fighter blueprints to Nexuses
        for (String bp : remnantFighterBPs) {
            int pick = rand.nextInt(listNexus.size());
            CargoAPI cargo = Global.getFactory().createCargo(false);
            cargo.addSpecial(new SpecialItemData(Items.FIGHTER_BP, bp), 1);
            BaseSalvageSpecial.addExtraSalvage(listNexus.get(pick), cargo);
        }

    }
}
