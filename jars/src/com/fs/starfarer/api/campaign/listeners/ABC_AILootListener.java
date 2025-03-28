package com.fs.starfarer.api.campaign.listeners;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class ABC_AILootListener implements ShowLootListener {

    public static void register(){
        ListenerManagerAPI manager = Global.getSector().getListenerManager();
        if (manager.hasListenerOfClass(ABC_AILootListener.class)) return;

        manager.addListener(new ABC_AILootListener(), true);
    }

    String[] derelictSmall = {"defender", "picket", "sentry", "warden"};
    String[] derelictMedium = {"bastillon", "berserker", "rampart"};
    String[] derelictLarge = {"guardian"};
    String[] remnantSmall = {"lumen", "glimmer", "fulgent", "scintilla"};
    String[] remnantMedium = {"brilliant", "apex"};
    String[] remnantLarge = {"nova", "radiant"};
    String[] remnantFighters = {"spark", "lux", "flash"};

    @Override
    public void reportAboutToShowLootToPlayer(CargoAPI loot, InteractionDialogAPI dialog) {

        List<String> blueprintId = new ArrayList<>();
        SectorEntityToken interactionTarget = dialog.getInteractionTarget();

        //Checks if the loot is coming from a derelict entity
        if (interactionTarget.getCustomEntitySpec() != null) {
            if (interactionTarget.getCustomEntitySpec().getId().equals(Entities.DERELICT_MOTHERSHIP) ||
                    interactionTarget.getCustomEntitySpec().getId().equals(Entities.DERELICT_SURVEY_SHIP) ||
                    interactionTarget.getCustomEntitySpec().getId().equals(Entities.DERELICT_SURVEY_PROBE)) {
                blueprintId = blueprintPicker(interactionTarget.getCustomEntitySpec().getId());
            }
        }

        //Checks if the loot is coming from a fleet, and if so a remnant fleet, and if so containing a nexus
        if (interactionTarget instanceof CampaignFleetAPI) {
            CampaignFleetAPI fleet = (CampaignFleetAPI) interactionTarget;
            if (!fleet.getFaction().getId().equals(Factions.REMNANTS)) return;
            boolean containsNexus = false;

            for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
                if (member.isStation()) {
                    containsNexus = true;
                    break;
                }
            }

            if (!containsNexus) for (FleetMemberAPI member : fleet.getFleetData().getSnapshot()) {
                if (member.isStation()) {
                    containsNexus = true;
                    break;
                }
            }

            if (containsNexus) {
                blueprintId = blueprintPicker("remnant_nexus");
            }
        }

        if (!blueprintId.isEmpty()) {
            for (String blueprint : blueprintId) {
                loot.addSpecial(new SpecialItemData(Items.SHIP_BP, blueprint), 1);
            }
        }
    }

    List<String> blueprintPicker(String entity) {
        List<String> output = new ArrayList<>();
        List<String> availableList = new ArrayList<>();
        FactionAPI playerFaction = Global.getSector().getPlayerFaction();
        Random rand = new Random();
        int chance = rand.nextInt(4);
        int picker;

        /*
        If the loot comes from a derelict entity, it picks from unknown blueprints first to avoid handing out
        duplicates. If all blueprints are known, it picks one at random from the full list.
        The chance to get a blueprint is 25% for probes, 50% for ships, 100% for motherships.
         */
        switch (entity) {
            case Entities.DERELICT_SURVEY_PROBE:
                if (chance <= 2) break;

                for (String s : derelictSmall) {
                    if (!playerFaction.getKnownShips().contains(s)) {
                        availableList.add(s);
                    }
                }

                if (availableList.isEmpty()) availableList.addAll(Arrays.asList(derelictSmall));

            case Entities.DERELICT_SURVEY_SHIP:
                if (chance <= 1) break;

                for (String s : derelictMedium) {
                    if (!playerFaction.getKnownShips().contains(s)) {
                        availableList.add(s);
                    }
                }

                if (availableList.isEmpty()) availableList.addAll(Arrays.asList(derelictMedium));

            case Entities.DERELICT_MOTHERSHIP:
                for (String s : derelictLarge) {
                    if (!playerFaction.getKnownShips().contains(s)) {
                        availableList.add(s);
                    }
                }

                if (availableList.isEmpty()) availableList.addAll(Arrays.asList(derelictLarge));
        }

        if (!availableList.isEmpty()) {
            picker = rand.nextInt(availableList.size());
            output.add(availableList.get(picker));
        }

        /*
        For remnant nexuses, it will first try to have 3 drops from unknown blueprints: 1 small, 1 medium/large,
        1 fighter. Medium/large drops are weighted so medium ships have twice the drop chance as large ships.
        If all blueprints are known, a single blueprint will be picked at random out of all remnant blueprints.
         */
        if (entity.equals("remnant_nexus")) {
            for (String s : remnantSmall) {
                if (!playerFaction.getKnownShips().contains(s)) {
                    availableList.add(s);
                }
            }
            if (!availableList.isEmpty()) {
                picker = rand.nextInt(availableList.size());
                output.add(availableList.get(picker));
                availableList.clear();
            }

            for (String s : remnantMedium) {
                if (!playerFaction.getKnownShips().contains(s)) {
                    availableList.add(s);
                    availableList.add(s);
                }
            }
            for (String s : remnantLarge) {
                if (!playerFaction.getKnownShips().contains(s)) {
                    availableList.add(s);
                }
            }
            if (!availableList.isEmpty()) {
                picker = rand.nextInt(availableList.size());
                output.add(availableList.get(picker));
                availableList.clear();
            }

            for (String s : remnantFighters) {
                if (!playerFaction.getKnownShips().contains(s)) {
                    availableList.add(s);
                }
            }
            if (!availableList.isEmpty()) {
                picker = rand.nextInt(availableList.size());
                output.add(availableList.get(picker));
            }

            if (availableList.isEmpty()) {
                availableList.addAll(Arrays.asList(remnantSmall));
                availableList.addAll(Arrays.asList(remnantMedium));
                availableList.addAll(Arrays.asList(remnantLarge));
                availableList.addAll(Arrays.asList(remnantFighters));
                picker = rand.nextInt(availableList.size());
                output.add(availableList.get(picker));
            }
        }

        return output;
    }
}
