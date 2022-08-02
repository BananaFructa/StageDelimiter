## Usage

After you have joined a world at least once a there will be directory in your config called StgDel. Inside it there will be a file name StageNames.stg, in this file you want to write in your stage names. The way you do this is by opening the file with any text editor and write the stages in the following format (they do not have to be in the order of the progression).

```
Some stage name#0
Some other stage name#1
Something idk#2
```

There can be as many stages as you want. The numbers after the "#" don't have to be in order they only have to be a positive integer and distinct from each other, those represent the stage id.

After that you have to join any world again. Then in the StgDel directory will be a file for each stage you have in StageDelimiter.stg, each having the name of the stage they represent and the .stg extension. Each one of those files is used for storing the registry names of the results of the banned recipes for each stage.You have to put each separate registry name on a new line and if you are using craft tweaker for getting the registry names it doesn't matter if the names are between angle brackets.

```
minecraft:grass
<minecraft:ender_pearl>
minecraft:arrow
```

If you instead want to ban all the registry names from a certain mod or a set of registry names that share a common start you can use the character "!" at the start of a line to ban all registry names that start with that specific character sequence. The opposite can be done with "@".

This bans everything that has a registry name that starts with "minecraft".

```
!minecraft
```

This allows everything that starts with "minecraft:glass".

```
@minecraft:glass
```
This is especially useful when you want to ban all registries that start with a certain key except for a few.
If both "!" and "@" are going to be used in the same .stg file make sure to declare the "@" registries first, otherwise it will not work.


A more practical example would be this one:

```
@minecraft:stained_glass
!immersiveengineering
!minecraft
<minecraft:stained_glass:5>
<minecraft:stained_glass:6>
<minecraft:stained_glass:7>
<minecraft:stained_glass:8>
<minecraft:stained_glass:9>
<minecraft:stained_glass:10>
<minecraft:stained_glass:11>
<minecraft:stained_glass:12>
<minecraft:stained_glass:13>
<tfctech:pot_ash>
<tfctech:pot_potash>
<tfctech:powder/potash>
<tfctech:latex/vulcanizing_agents>
<tfctech:latex/rubber_mix>
<tfctech:latex/rubber>
<tfctech:wiredraw/leather_belt>
<tfctech:metal/copper_inductor>
<tfctech:metal/wrought_iron_blowpipe>
<tfctech:metal/steel_blowpipe>
<tfctech:metal/black_steel_blowpipe>
<tfctech:metal/blue_steel_blowpipe>
<tfctech:metal/red_steel_blowpipe>
<tfctech:metal/aluminium_blowpipe>
<tfctech:electric_forge>
<tfctech:induction_crucible>
<tfctech:smeltery_cauldron>
<tfctech:smeltery_firebox>
```

Kinda long but I think it makes it clear how things work. The first line blocks everything from immersive engineering since it blocks all registry names that start with "immersiveengineering" and the next lines simply ban various recipes.

## Server hosting

If you are planning to use this mod on a server the people that connect to the server do not need to have their stage files set up, only the server needs them.

## Keys

The way stages are unlocked is trough the usage of Stage Keys. A Stage Key is an item added by the mod with no recipe. A normal key doesn't have much use because it needs an NBT tag that contains what stage it unlocks. The simplest way to attribute such nbt tag is trough the in built ```/setkey``` command, the way you use it is by holding an Stage Key in your main hand and running the command ```/setkey <stage id>``` (it's a creative only command) and then that key will have the ability to unlock the specified stage (the stage id are the numbers which were attributed in the StageData.stg file).

You can then make that key be acquired trough a quest book or from a custom recipe using craft tweaker.

## Tooltips

If a player doesn't have a certain stage unlocked to craft an item, if he hovers over that item, in JEI for example, the tooltip will contain a line of red text saying ```"Requires <name of the stage> !"```. Keys have a similar things, if a key unlocks a stage its tooltip will contain a green line of text saying ```"Unlocks <name of the stage> !"```.

## Teams

If you are playing with some friends or you are doing some other thing and you want the ability for people to share stage progress you can create a team. If one member of the team unlocks a stage that stage will be unlocked for everyone within the team. If a person joins the difference of stage progress between that person and the team will be equalized, so if the person doesn't have stages unlocked that the team has those stages will become unlocked for that person and if the person has stages unlocked that the team doesn't have, the rest of the team will get those stages unlocked.

Commands about team management and interaction are in the commands section.

## Commands

```
/setkey <stage id> - Makes the Stage Key which is held in the main hand to unlock the specified stage

/team create <name> - Creates a team
/team inviteonly - Toggles on or off if the team requires an invitation to join (owner only) (on by default)
/team invite <player-name> - Invites a player into your team (owner only)
/team kick <player-name> - Kicks a player from the team (owner only)
/team ban <player-name> - Bans a player from the team (owner only)
/team unban <player-name> - Unbans a player from the team (owner only)

/team accept - Accepts the current pending invitation
/team decline - Declines the current pending invitation
/team join <team-name> - Joines the team with the specified name, you have to not be banned and the team has to not require an invitation
/team leave - Exit a team (if you are the owner the team will be disbanded)
```
